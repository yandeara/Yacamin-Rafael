package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.VolatilityIndicatorDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VolatilityIndicatorMongoRepository {

    @Qualifier("mikhaelMongoTemplate")
    private final MongoTemplate mongoTemplate;

    private static final List<CandleIntervals> SUPPORTED = List.of(
            CandleIntervals.I1_MN, CandleIntervals.I5_MN,
            CandleIntervals.I15_MN, CandleIntervals.I30_MN);

    @PostConstruct
    public void ensureIndexes() {
        for (CandleIntervals interval : SUPPORTED) {
            String collection = collectionName(interval);
            mongoTemplate.indexOps(collection).ensureIndex(
                    new CompoundIndexDefinition(new Document("symbol", 1).append("openTime", 1))
                            .named("symbol_openTime_unique")
                            .unique());
            log.info("Indice symbol_openTime_unique garantido em {}", collection);
        }
    }

    public Optional<VolatilityIndicatorDocument> findBySymbolAndOpenTime(String symbol, Instant openTime, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").is(openTime));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, VolatilityIndicatorDocument.class, collectionName(interval)));
    }

    public List<VolatilityIndicatorDocument> findByRangeExclEnd(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        return mongoTemplate.find(query, VolatilityIndicatorDocument.class, collectionName(interval));
    }

    public List<Instant> findOpenTimesByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        query.fields().include("openTime").exclude("_id");

        return mongoTemplate.find(query, VolatilityIndicatorDocument.class, collectionName(interval))
                .stream()
                .map(VolatilityIndicatorDocument::getOpenTime)
                .toList();
    }

    public void save(VolatilityIndicatorDocument doc, CandleIntervals interval) {
        String collection = collectionName(interval);
        Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                .and("openTime").is(doc.getOpenTime()));
        mongoTemplate.upsert(query, buildUpdate(doc), collection);
    }

    public void saveBatch(List<VolatilityIndicatorDocument> documents, CandleIntervals interval) {
        if (documents.isEmpty()) return;
        String collection = collectionName(interval);
        var bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        int count = 0;
        for (VolatilityIndicatorDocument doc : documents) {
            Update update = buildUpdate(doc);
            if (update.getUpdateObject().isEmpty()) continue;
            Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                    .and("openTime").is(doc.getOpenTime()));
            bulkOps.upsert(query, update);
            count++;
        }
        if (count == 0) return;
        bulkOps.execute();
    }

    private Update buildUpdate(VolatilityIndicatorDocument doc) {
        Update update = new Update();

        // 1) ATR — LEVEL + CHANGE
        if (doc.getVltAtr7() != null) update.set("vltAtr7", doc.getVltAtr7());
        if (doc.getVltAtr14() != null) update.set("vltAtr14", doc.getVltAtr14());
        if (doc.getVltAtr21() != null) update.set("vltAtr21", doc.getVltAtr21());

        if (doc.getVltAtr7Chg() != null) update.set("vltAtr7Chg", doc.getVltAtr7Chg());
        if (doc.getVltAtr14Chg() != null) update.set("vltAtr14Chg", doc.getVltAtr14Chg());
        if (doc.getVltAtr21Chg() != null) update.set("vltAtr21Chg", doc.getVltAtr21Chg());

        // V3.1 — ATR 24H / MULTI-SCALE
        if (doc.getVltAtr48() != null) update.set("vltAtr48", doc.getVltAtr48());
        if (doc.getVltAtr96() != null) update.set("vltAtr96", doc.getVltAtr96());
        if (doc.getVltAtr288() != null) update.set("vltAtr288", doc.getVltAtr288());

        if (doc.getVltAtr48Chg() != null) update.set("vltAtr48Chg", doc.getVltAtr48Chg());
        if (doc.getVltAtr96Chg() != null) update.set("vltAtr96Chg", doc.getVltAtr96Chg());
        if (doc.getVltAtr288Chg() != null) update.set("vltAtr288Chg", doc.getVltAtr288Chg());

        // 2) ATR — LOCAL / RANGE-LOCAL
        if (doc.getVltRangeAtr14Loc() != null) update.set("vltRangeAtr14Loc", doc.getVltRangeAtr14Loc());
        if (doc.getVltRangeAtr14LocChg() != null) update.set("vltRangeAtr14LocChg", doc.getVltRangeAtr14LocChg());

        // V3.1 — ATR LOCAL 24H / MULTI-SCALE
        if (doc.getVltRangeAtr48Loc() != null) update.set("vltRangeAtr48Loc", doc.getVltRangeAtr48Loc());
        if (doc.getVltRangeAtr48LocChg() != null) update.set("vltRangeAtr48LocChg", doc.getVltRangeAtr48LocChg());

        if (doc.getVltRangeAtr96Loc() != null) update.set("vltRangeAtr96Loc", doc.getVltRangeAtr96Loc());
        if (doc.getVltRangeAtr96LocChg() != null) update.set("vltRangeAtr96LocChg", doc.getVltRangeAtr96LocChg());

        if (doc.getVltRangeAtr288Loc() != null) update.set("vltRangeAtr288Loc", doc.getVltRangeAtr288Loc());
        if (doc.getVltRangeAtr288LocChg() != null) update.set("vltRangeAtr288LocChg", doc.getVltRangeAtr288LocChg());

        // 3) ATR — REGIME (7/21) + V3.1 REGIME MULTI-PAIRS
        if (doc.getVltAtr721Ratio() != null) update.set("vltAtr721Ratio", doc.getVltAtr721Ratio());
        if (doc.getVltAtr721Expr() != null) update.set("vltAtr721Expr", doc.getVltAtr721Expr());
        if (doc.getVltAtr721Expn() != null) update.set("vltAtr721Expn", doc.getVltAtr721Expn());
        if (doc.getVltAtr721Cmpr() != null) update.set("vltAtr721Cmpr", doc.getVltAtr721Cmpr());

        if (doc.getVltAtr748Ratio() != null) update.set("vltAtr748Ratio", doc.getVltAtr748Ratio());
        if (doc.getVltAtr748Expn() != null) update.set("vltAtr748Expn", doc.getVltAtr748Expn());
        if (doc.getVltAtr748Cmpr() != null) update.set("vltAtr748Cmpr", doc.getVltAtr748Cmpr());

        if (doc.getVltAtr1448Ratio() != null) update.set("vltAtr1448Ratio", doc.getVltAtr1448Ratio());
        if (doc.getVltAtr1448Expn() != null) update.set("vltAtr1448Expn", doc.getVltAtr1448Expn());
        if (doc.getVltAtr1448Cmpr() != null) update.set("vltAtr1448Cmpr", doc.getVltAtr1448Cmpr());

        if (doc.getVltAtr1496Ratio() != null) update.set("vltAtr1496Ratio", doc.getVltAtr1496Ratio());
        if (doc.getVltAtr1496Expn() != null) update.set("vltAtr1496Expn", doc.getVltAtr1496Expn());
        if (doc.getVltAtr1496Cmpr() != null) update.set("vltAtr1496Cmpr", doc.getVltAtr1496Cmpr());

        if (doc.getVltAtr14288Ratio() != null) update.set("vltAtr14288Ratio", doc.getVltAtr14288Ratio());
        if (doc.getVltAtr14288Expn() != null) update.set("vltAtr14288Expn", doc.getVltAtr14288Expn());
        if (doc.getVltAtr14288Cmpr() != null) update.set("vltAtr14288Cmpr", doc.getVltAtr14288Cmpr());

        if (doc.getVltAtr48288Ratio() != null) update.set("vltAtr48288Ratio", doc.getVltAtr48288Ratio());
        if (doc.getVltAtr48288Expn() != null) update.set("vltAtr48288Expn", doc.getVltAtr48288Expn());
        if (doc.getVltAtr48288Cmpr() != null) update.set("vltAtr48288Cmpr", doc.getVltAtr48288Cmpr());

        if (doc.getVltAtr96288Ratio() != null) update.set("vltAtr96288Ratio", doc.getVltAtr96288Ratio());
        if (doc.getVltAtr96288Expn() != null) update.set("vltAtr96288Expn", doc.getVltAtr96288Expn());
        if (doc.getVltAtr96288Cmpr() != null) update.set("vltAtr96288Cmpr", doc.getVltAtr96288Cmpr());

        // 4) ATR — ZSCORE / SLOPE
        if (doc.getVltAtr14Zsc() != null) update.set("vltAtr14Zsc", doc.getVltAtr14Zsc());
        if (doc.getVltAtr14Slp() != null) update.set("vltAtr14Slp", doc.getVltAtr14Slp());

        if (doc.getVltAtr7Zsc() != null) update.set("vltAtr7Zsc", doc.getVltAtr7Zsc());
        if (doc.getVltAtr21Zsc() != null) update.set("vltAtr21Zsc", doc.getVltAtr21Zsc());
        if (doc.getVltAtr48Zsc() != null) update.set("vltAtr48Zsc", doc.getVltAtr48Zsc());
        if (doc.getVltAtr96Zsc() != null) update.set("vltAtr96Zsc", doc.getVltAtr96Zsc());
        if (doc.getVltAtr288Zsc() != null) update.set("vltAtr288Zsc", doc.getVltAtr288Zsc());

        if (doc.getVltAtr7Slp() != null) update.set("vltAtr7Slp", doc.getVltAtr7Slp());
        if (doc.getVltAtr21Slp() != null) update.set("vltAtr21Slp", doc.getVltAtr21Slp());
        if (doc.getVltAtr48Slp() != null) update.set("vltAtr48Slp", doc.getVltAtr48Slp());
        if (doc.getVltAtr96Slp() != null) update.set("vltAtr96Slp", doc.getVltAtr96Slp());
        if (doc.getVltAtr288Slp() != null) update.set("vltAtr288Slp", doc.getVltAtr288Slp());

        // 5) ATR — VOL-OF-VOL
        if (doc.getVltAtr14VvW16() != null) update.set("vltAtr14VvW16", doc.getVltAtr14VvW16());
        if (doc.getVltAtr14VvW32() != null) update.set("vltAtr14VvW32", doc.getVltAtr14VvW32());

        if (doc.getVltAtr14VvW48() != null) update.set("vltAtr14VvW48", doc.getVltAtr14VvW48());
        if (doc.getVltAtr14VvW96() != null) update.set("vltAtr14VvW96", doc.getVltAtr14VvW96());
        if (doc.getVltAtr14VvW288() != null) update.set("vltAtr14VvW288", doc.getVltAtr14VvW288());

        if (doc.getVltAtr48VvW16() != null) update.set("vltAtr48VvW16", doc.getVltAtr48VvW16());
        if (doc.getVltAtr48VvW32() != null) update.set("vltAtr48VvW32", doc.getVltAtr48VvW32());
        if (doc.getVltAtr48VvW48() != null) update.set("vltAtr48VvW48", doc.getVltAtr48VvW48());

        if (doc.getVltAtr96VvW32() != null) update.set("vltAtr96VvW32", doc.getVltAtr96VvW32());
        if (doc.getVltAtr96VvW48() != null) update.set("vltAtr96VvW48", doc.getVltAtr96VvW48());
        if (doc.getVltAtr96VvW96() != null) update.set("vltAtr96VvW96", doc.getVltAtr96VvW96());

        if (doc.getVltAtr288VvW48() != null) update.set("vltAtr288VvW48", doc.getVltAtr288VvW48());
        if (doc.getVltAtr288VvW96() != null) update.set("vltAtr288VvW96", doc.getVltAtr288VvW96());
        if (doc.getVltAtr288VvW288() != null) update.set("vltAtr288VvW288", doc.getVltAtr288VvW288());

        // 6) SEASONALITY — ATR / RANGE (+ additions)
        if (doc.getVltAtr14Season() != null) update.set("vltAtr14Season", doc.getVltAtr14Season());
        if (doc.getVltRangeSeason() != null) update.set("vltRangeSeason", doc.getVltRangeSeason());

        if (doc.getVltAtr48Season() != null) update.set("vltAtr48Season", doc.getVltAtr48Season());
        if (doc.getVltAtr96Season() != null) update.set("vltAtr96Season", doc.getVltAtr96Season());
        if (doc.getVltAtr288Season() != null) update.set("vltAtr288Season", doc.getVltAtr288Season());

        if (doc.getVltStd20Season() != null) update.set("vltStd20Season", doc.getVltStd20Season());
        if (doc.getVltStd48Season() != null) update.set("vltStd48Season", doc.getVltStd48Season());
        if (doc.getVltStd96Season() != null) update.set("vltStd96Season", doc.getVltStd96Season());
        if (doc.getVltStd288Season() != null) update.set("vltStd288Season", doc.getVltStd288Season());

        if (doc.getVltBoll20WidthSeason() != null) update.set("vltBoll20WidthSeason", doc.getVltBoll20WidthSeason());
        if (doc.getVltBoll48WidthSeason() != null) update.set("vltBoll48WidthSeason", doc.getVltBoll48WidthSeason());
        if (doc.getVltBoll96WidthSeason() != null) update.set("vltBoll96WidthSeason", doc.getVltBoll96WidthSeason());
        if (doc.getVltBoll288WidthSeason() != null) update.set("vltBoll288WidthSeason", doc.getVltBoll288WidthSeason());

        if (doc.getVltVolRv30Season() != null) update.set("vltVolRv30Season", doc.getVltVolRv30Season());
        if (doc.getVltVolRv48Season() != null) update.set("vltVolRv48Season", doc.getVltVolRv48Season());
        if (doc.getVltVolRv96Season() != null) update.set("vltVolRv96Season", doc.getVltVolRv96Season());
        if (doc.getVltVolRv288Season() != null) update.set("vltVolRv288Season", doc.getVltVolRv288Season());

        // 7) STD — LEVEL + CHANGE (+ 24h additions)
        if (doc.getVltStd14() != null) update.set("vltStd14", doc.getVltStd14());
        if (doc.getVltStd20() != null) update.set("vltStd20", doc.getVltStd20());
        if (doc.getVltStd50() != null) update.set("vltStd50", doc.getVltStd50());

        if (doc.getVltStd14Chg() != null) update.set("vltStd14Chg", doc.getVltStd14Chg());
        if (doc.getVltStd20Chg() != null) update.set("vltStd20Chg", doc.getVltStd20Chg());
        if (doc.getVltStd50Chg() != null) update.set("vltStd50Chg", doc.getVltStd50Chg());

        if (doc.getVltStd48() != null) update.set("vltStd48", doc.getVltStd48());
        if (doc.getVltStd96() != null) update.set("vltStd96", doc.getVltStd96());
        if (doc.getVltStd288() != null) update.set("vltStd288", doc.getVltStd288());

        if (doc.getVltStd48Chg() != null) update.set("vltStd48Chg", doc.getVltStd48Chg());
        if (doc.getVltStd96Chg() != null) update.set("vltStd96Chg", doc.getVltStd96Chg());
        if (doc.getVltStd288Chg() != null) update.set("vltStd288Chg", doc.getVltStd288Chg());

        // 8) STD — REGIME (14/50) + multi-pairs
        if (doc.getVltStd1450Ratio() != null) update.set("vltStd1450Ratio", doc.getVltStd1450Ratio());
        if (doc.getVltStd1450Expn() != null) update.set("vltStd1450Expn", doc.getVltStd1450Expn());
        if (doc.getVltStd1450Cmpr() != null) update.set("vltStd1450Cmpr", doc.getVltStd1450Cmpr());

        if (doc.getVltStd1448Ratio() != null) update.set("vltStd1448Ratio", doc.getVltStd1448Ratio());
        if (doc.getVltStd1448Expn() != null) update.set("vltStd1448Expn", doc.getVltStd1448Expn());
        if (doc.getVltStd1448Cmpr() != null) update.set("vltStd1448Cmpr", doc.getVltStd1448Cmpr());

        if (doc.getVltStd1496Ratio() != null) update.set("vltStd1496Ratio", doc.getVltStd1496Ratio());
        if (doc.getVltStd1496Expn() != null) update.set("vltStd1496Expn", doc.getVltStd1496Expn());
        if (doc.getVltStd1496Cmpr() != null) update.set("vltStd1496Cmpr", doc.getVltStd1496Cmpr());

        if (doc.getVltStd14288Ratio() != null) update.set("vltStd14288Ratio", doc.getVltStd14288Ratio());
        if (doc.getVltStd14288Expn() != null) update.set("vltStd14288Expn", doc.getVltStd14288Expn());
        if (doc.getVltStd14288Cmpr() != null) update.set("vltStd14288Cmpr", doc.getVltStd14288Cmpr());

        if (doc.getVltStd2048Ratio() != null) update.set("vltStd2048Ratio", doc.getVltStd2048Ratio());
        if (doc.getVltStd2048Expn() != null) update.set("vltStd2048Expn", doc.getVltStd2048Expn());
        if (doc.getVltStd2048Cmpr() != null) update.set("vltStd2048Cmpr", doc.getVltStd2048Cmpr());

        if (doc.getVltStd48288Ratio() != null) update.set("vltStd48288Ratio", doc.getVltStd48288Ratio());
        if (doc.getVltStd48288Expn() != null) update.set("vltStd48288Expn", doc.getVltStd48288Expn());
        if (doc.getVltStd48288Cmpr() != null) update.set("vltStd48288Cmpr", doc.getVltStd48288Cmpr());

        if (doc.getVltStd96288Ratio() != null) update.set("vltStd96288Ratio", doc.getVltStd96288Ratio());
        if (doc.getVltStd96288Expn() != null) update.set("vltStd96288Expn", doc.getVltStd96288Expn());
        if (doc.getVltStd96288Cmpr() != null) update.set("vltStd96288Cmpr", doc.getVltStd96288Cmpr());

        // 9) STD — ZSCORE / SLOPE / VOL-OF-VOL (+ additions)
        if (doc.getVltStd20Zsc() != null) update.set("vltStd20Zsc", doc.getVltStd20Zsc());
        if (doc.getVltStd20Slp() != null) update.set("vltStd20Slp", doc.getVltStd20Slp());
        if (doc.getVltStd20VvW20() != null) update.set("vltStd20VvW20", doc.getVltStd20VvW20());

        if (doc.getVltStd14Zsc() != null) update.set("vltStd14Zsc", doc.getVltStd14Zsc());
        if (doc.getVltStd50Zsc() != null) update.set("vltStd50Zsc", doc.getVltStd50Zsc());
        if (doc.getVltStd48Zsc() != null) update.set("vltStd48Zsc", doc.getVltStd48Zsc());
        if (doc.getVltStd96Zsc() != null) update.set("vltStd96Zsc", doc.getVltStd96Zsc());
        if (doc.getVltStd288Zsc() != null) update.set("vltStd288Zsc", doc.getVltStd288Zsc());

        if (doc.getVltStd14Slp() != null) update.set("vltStd14Slp", doc.getVltStd14Slp());
        if (doc.getVltStd50Slp() != null) update.set("vltStd50Slp", doc.getVltStd50Slp());
        if (doc.getVltStd48Slp() != null) update.set("vltStd48Slp", doc.getVltStd48Slp());
        if (doc.getVltStd96Slp() != null) update.set("vltStd96Slp", doc.getVltStd96Slp());
        if (doc.getVltStd288Slp() != null) update.set("vltStd288Slp", doc.getVltStd288Slp());

        if (doc.getVltStd14VvW20() != null) update.set("vltStd14VvW20", doc.getVltStd14VvW20());
        if (doc.getVltStd50VvW20() != null) update.set("vltStd50VvW20", doc.getVltStd50VvW20());
        if (doc.getVltStd48VvW20() != null) update.set("vltStd48VvW20", doc.getVltStd48VvW20());
        if (doc.getVltStd96VvW20() != null) update.set("vltStd96VvW20", doc.getVltStd96VvW20());
        if (doc.getVltStd288VvW20() != null) update.set("vltStd288VvW20", doc.getVltStd288VvW20());

        if (doc.getVltStd20VvW48() != null) update.set("vltStd20VvW48", doc.getVltStd20VvW48());
        if (doc.getVltStd48VvW48() != null) update.set("vltStd48VvW48", doc.getVltStd48VvW48());
        if (doc.getVltStd96VvW48() != null) update.set("vltStd96VvW48", doc.getVltStd96VvW48());
        if (doc.getVltStd288VvW48() != null) update.set("vltStd288VvW48", doc.getVltStd288VvW48());

        // 10) BOLLINGER WIDTH (BB) (+ 24h additions)
        if (doc.getVltBoll20Width() != null) update.set("vltBoll20Width", doc.getVltBoll20Width());
        if (doc.getVltBoll20WidthChg() != null) update.set("vltBoll20WidthChg", doc.getVltBoll20WidthChg());
        if (doc.getVltBoll20WidthZsc() != null) update.set("vltBoll20WidthZsc", doc.getVltBoll20WidthZsc());
        if (doc.getVltBoll20WidthAtrn() != null) update.set("vltBoll20WidthAtrn", doc.getVltBoll20WidthAtrn());

        if (doc.getVltBoll48Width() != null) update.set("vltBoll48Width", doc.getVltBoll48Width());
        if (doc.getVltBoll48WidthChg() != null) update.set("vltBoll48WidthChg", doc.getVltBoll48WidthChg());
        if (doc.getVltBoll48WidthZsc() != null) update.set("vltBoll48WidthZsc", doc.getVltBoll48WidthZsc());
        if (doc.getVltBoll48WidthAtrn() != null) update.set("vltBoll48WidthAtrn", doc.getVltBoll48WidthAtrn());

        if (doc.getVltBoll96Width() != null) update.set("vltBoll96Width", doc.getVltBoll96Width());
        if (doc.getVltBoll96WidthChg() != null) update.set("vltBoll96WidthChg", doc.getVltBoll96WidthChg());
        if (doc.getVltBoll96WidthZsc() != null) update.set("vltBoll96WidthZsc", doc.getVltBoll96WidthZsc());
        if (doc.getVltBoll96WidthAtrn() != null) update.set("vltBoll96WidthAtrn", doc.getVltBoll96WidthAtrn());

        if (doc.getVltBoll288Width() != null) update.set("vltBoll288Width", doc.getVltBoll288Width());
        if (doc.getVltBoll288WidthChg() != null) update.set("vltBoll288WidthChg", doc.getVltBoll288WidthChg());
        if (doc.getVltBoll288WidthZsc() != null) update.set("vltBoll288WidthZsc", doc.getVltBoll288WidthZsc());
        if (doc.getVltBoll288WidthAtrn() != null) update.set("vltBoll288WidthAtrn", doc.getVltBoll288WidthAtrn());

        // 11) KELTNER + SQUEEZE (+ 24h additions)
        if (doc.getVltKelt20Width() != null) update.set("vltKelt20Width", doc.getVltKelt20Width());
        if (doc.getVltVolSqzBbKelt() != null) update.set("vltVolSqzBbKelt", doc.getVltVolSqzBbKelt());
        if (doc.getVltVolSqzBbKeltChg() != null) update.set("vltVolSqzBbKeltChg", doc.getVltVolSqzBbKeltChg());

        if (doc.getVltKelt48Width() != null) update.set("vltKelt48Width", doc.getVltKelt48Width());
        if (doc.getVltKelt96Width() != null) update.set("vltKelt96Width", doc.getVltKelt96Width());
        if (doc.getVltKelt288Width() != null) update.set("vltKelt288Width", doc.getVltKelt288Width());

        if (doc.getVltVolSqzBbKelt20() != null) update.set("vltVolSqzBbKelt20", doc.getVltVolSqzBbKelt20());
        if (doc.getVltVolSqzBbKelt20Chg() != null) update.set("vltVolSqzBbKelt20Chg", doc.getVltVolSqzBbKelt20Chg());
        if (doc.getVltVolSqzBbKelt20Zsc() != null) update.set("vltVolSqzBbKelt20Zsc", doc.getVltVolSqzBbKelt20Zsc());

        if (doc.getVltVolSqzBbKelt48() != null) update.set("vltVolSqzBbKelt48", doc.getVltVolSqzBbKelt48());
        if (doc.getVltVolSqzBbKelt48Chg() != null) update.set("vltVolSqzBbKelt48Chg", doc.getVltVolSqzBbKelt48Chg());
        if (doc.getVltVolSqzBbKelt48Zsc() != null) update.set("vltVolSqzBbKelt48Zsc", doc.getVltVolSqzBbKelt48Zsc());

        if (doc.getVltVolSqzBbKelt96() != null) update.set("vltVolSqzBbKelt96", doc.getVltVolSqzBbKelt96());
        if (doc.getVltVolSqzBbKelt96Chg() != null) update.set("vltVolSqzBbKelt96Chg", doc.getVltVolSqzBbKelt96Chg());
        if (doc.getVltVolSqzBbKelt96Zsc() != null) update.set("vltVolSqzBbKelt96Zsc", doc.getVltVolSqzBbKelt96Zsc());

        if (doc.getVltVolSqzBbKelt288() != null) update.set("vltVolSqzBbKelt288", doc.getVltVolSqzBbKelt288());
        if (doc.getVltVolSqzBbKelt288Chg() != null) update.set("vltVolSqzBbKelt288Chg", doc.getVltVolSqzBbKelt288Chg());
        if (doc.getVltVolSqzBbKelt288Zsc() != null) update.set("vltVolSqzBbKelt288Zsc", doc.getVltVolSqzBbKelt288Zsc());

        // 12) RANGE-BASED VOLATILITY (GK / PARK / RS) (+ 24h additions)
        if (doc.getVltVolGk16() != null) update.set("vltVolGk16", doc.getVltVolGk16());
        if (doc.getVltVolGk32() != null) update.set("vltVolGk32", doc.getVltVolGk32());

        if (doc.getVltVolPark16() != null) update.set("vltVolPark16", doc.getVltVolPark16());
        if (doc.getVltVolPark32() != null) update.set("vltVolPark32", doc.getVltVolPark32());

        if (doc.getVltVolRs16() != null) update.set("vltVolRs16", doc.getVltVolRs16());
        if (doc.getVltVolRs32() != null) update.set("vltVolRs32", doc.getVltVolRs32());

        if (doc.getVltVolGk48() != null) update.set("vltVolGk48", doc.getVltVolGk48());
        if (doc.getVltVolGk96() != null) update.set("vltVolGk96", doc.getVltVolGk96());
        if (doc.getVltVolGk288() != null) update.set("vltVolGk288", doc.getVltVolGk288());

        if (doc.getVltVolPark48() != null) update.set("vltVolPark48", doc.getVltVolPark48());
        if (doc.getVltVolPark96() != null) update.set("vltVolPark96", doc.getVltVolPark96());
        if (doc.getVltVolPark288() != null) update.set("vltVolPark288", doc.getVltVolPark288());

        if (doc.getVltVolRs48() != null) update.set("vltVolRs48", doc.getVltVolRs48());
        if (doc.getVltVolRs96() != null) update.set("vltVolRs96", doc.getVltVolRs96());
        if (doc.getVltVolRs288() != null) update.set("vltVolRs288", doc.getVltVolRs288());

        if (doc.getVltVolGk16Zsc() != null) update.set("vltVolGk16Zsc", doc.getVltVolGk16Zsc());
        if (doc.getVltVolGk32Zsc() != null) update.set("vltVolGk32Zsc", doc.getVltVolGk32Zsc());
        if (doc.getVltVolGk48Zsc() != null) update.set("vltVolGk48Zsc", doc.getVltVolGk48Zsc());
        if (doc.getVltVolGk96Zsc() != null) update.set("vltVolGk96Zsc", doc.getVltVolGk96Zsc());
        if (doc.getVltVolGk288Zsc() != null) update.set("vltVolGk288Zsc", doc.getVltVolGk288Zsc());

        if (doc.getVltVolPark16Zsc() != null) update.set("vltVolPark16Zsc", doc.getVltVolPark16Zsc());
        if (doc.getVltVolPark32Zsc() != null) update.set("vltVolPark32Zsc", doc.getVltVolPark32Zsc());
        if (doc.getVltVolPark48Zsc() != null) update.set("vltVolPark48Zsc", doc.getVltVolPark48Zsc());
        if (doc.getVltVolPark96Zsc() != null) update.set("vltVolPark96Zsc", doc.getVltVolPark96Zsc());
        if (doc.getVltVolPark288Zsc() != null) update.set("vltVolPark288Zsc", doc.getVltVolPark288Zsc());

        if (doc.getVltVolRs16Zsc() != null) update.set("vltVolRs16Zsc", doc.getVltVolRs16Zsc());
        if (doc.getVltVolRs32Zsc() != null) update.set("vltVolRs32Zsc", doc.getVltVolRs32Zsc());
        if (doc.getVltVolRs48Zsc() != null) update.set("vltVolRs48Zsc", doc.getVltVolRs48Zsc());
        if (doc.getVltVolRs96Zsc() != null) update.set("vltVolRs96Zsc", doc.getVltVolRs96Zsc());
        if (doc.getVltVolRs288Zsc() != null) update.set("vltVolRs288Zsc", doc.getVltVolRs288Zsc());

        if (doc.getVltVolGk16Slp() != null) update.set("vltVolGk16Slp", doc.getVltVolGk16Slp());
        if (doc.getVltVolGk32Slp() != null) update.set("vltVolGk32Slp", doc.getVltVolGk32Slp());
        if (doc.getVltVolGk48Slp() != null) update.set("vltVolGk48Slp", doc.getVltVolGk48Slp());
        if (doc.getVltVolGk96Slp() != null) update.set("vltVolGk96Slp", doc.getVltVolGk96Slp());
        if (doc.getVltVolGk288Slp() != null) update.set("vltVolGk288Slp", doc.getVltVolGk288Slp());

        if (doc.getVltVolPark16Slp() != null) update.set("vltVolPark16Slp", doc.getVltVolPark16Slp());
        if (doc.getVltVolPark32Slp() != null) update.set("vltVolPark32Slp", doc.getVltVolPark32Slp());
        if (doc.getVltVolPark48Slp() != null) update.set("vltVolPark48Slp", doc.getVltVolPark48Slp());
        if (doc.getVltVolPark96Slp() != null) update.set("vltVolPark96Slp", doc.getVltVolPark96Slp());
        if (doc.getVltVolPark288Slp() != null) update.set("vltVolPark288Slp", doc.getVltVolPark288Slp());

        if (doc.getVltVolRs16Slp() != null) update.set("vltVolRs16Slp", doc.getVltVolRs16Slp());
        if (doc.getVltVolRs32Slp() != null) update.set("vltVolRs32Slp", doc.getVltVolRs32Slp());
        if (doc.getVltVolRs48Slp() != null) update.set("vltVolRs48Slp", doc.getVltVolRs48Slp());
        if (doc.getVltVolRs96Slp() != null) update.set("vltVolRs96Slp", doc.getVltVolRs96Slp());
        if (doc.getVltVolRs288Slp() != null) update.set("vltVolRs288Slp", doc.getVltVolRs288Slp());

        if (doc.getVltVolGk1648Ratio() != null) update.set("vltVolGk1648Ratio", doc.getVltVolGk1648Ratio());
        if (doc.getVltVolGk3248Ratio() != null) update.set("vltVolGk3248Ratio", doc.getVltVolGk3248Ratio());
        if (doc.getVltVolGk48288Ratio() != null) update.set("vltVolGk48288Ratio", doc.getVltVolGk48288Ratio());

        if (doc.getVltVolPark1648Ratio() != null) update.set("vltVolPark1648Ratio", doc.getVltVolPark1648Ratio());
        if (doc.getVltVolPark3248Ratio() != null) update.set("vltVolPark3248Ratio", doc.getVltVolPark3248Ratio());
        if (doc.getVltVolPark48288Ratio() != null) update.set("vltVolPark48288Ratio", doc.getVltVolPark48288Ratio());

        if (doc.getVltVolRs1648Ratio() != null) update.set("vltVolRs1648Ratio", doc.getVltVolRs1648Ratio());
        if (doc.getVltVolRs3248Ratio() != null) update.set("vltVolRs3248Ratio", doc.getVltVolRs3248Ratio());
        if (doc.getVltVolRs48288Ratio() != null) update.set("vltVolRs48288Ratio", doc.getVltVolRs48288Ratio());

        // 13) REALIZED VOLATILITY (RV) (+ 24h additions)
        if (doc.getVltVolRv10() != null) update.set("vltVolRv10", doc.getVltVolRv10());
        if (doc.getVltVolRv30() != null) update.set("vltVolRv30", doc.getVltVolRv30());
        if (doc.getVltVolRv50() != null) update.set("vltVolRv50", doc.getVltVolRv50());

        if (doc.getVltVolRv10Zsc() != null) update.set("vltVolRv10Zsc", doc.getVltVolRv10Zsc());
        if (doc.getVltVolRv1050Ratio() != null) update.set("vltVolRv1050Ratio", doc.getVltVolRv1050Ratio());
        if (doc.getVltVolRv30Slp() != null) update.set("vltVolRv30Slp", doc.getVltVolRv30Slp());

        if (doc.getVltVolRv48() != null) update.set("vltVolRv48", doc.getVltVolRv48());
        if (doc.getVltVolRv96() != null) update.set("vltVolRv96", doc.getVltVolRv96());
        if (doc.getVltVolRv288() != null) update.set("vltVolRv288", doc.getVltVolRv288());

        if (doc.getVltVolRv30Zsc() != null) update.set("vltVolRv30Zsc", doc.getVltVolRv30Zsc());
        if (doc.getVltVolRv50Zsc() != null) update.set("vltVolRv50Zsc", doc.getVltVolRv50Zsc());
        if (doc.getVltVolRv48Zsc() != null) update.set("vltVolRv48Zsc", doc.getVltVolRv48Zsc());
        if (doc.getVltVolRv96Zsc() != null) update.set("vltVolRv96Zsc", doc.getVltVolRv96Zsc());
        if (doc.getVltVolRv288Zsc() != null) update.set("vltVolRv288Zsc", doc.getVltVolRv288Zsc());

        if (doc.getVltVolRv10PctileW80() != null) update.set("vltVolRv10PctileW80", doc.getVltVolRv10PctileW80());
        if (doc.getVltVolRv30PctileW80() != null) update.set("vltVolRv30PctileW80", doc.getVltVolRv30PctileW80());
        if (doc.getVltVolRv50PctileW80() != null) update.set("vltVolRv50PctileW80", doc.getVltVolRv50PctileW80());
        if (doc.getVltVolRv48PctileW80() != null) update.set("vltVolRv48PctileW80", doc.getVltVolRv48PctileW80());
        if (doc.getVltVolRv96PctileW80() != null) update.set("vltVolRv96PctileW80", doc.getVltVolRv96PctileW80());
        if (doc.getVltVolRv288PctileW80() != null) update.set("vltVolRv288PctileW80", doc.getVltVolRv288PctileW80());

        if (doc.getVltVolRv1030Ratio() != null) update.set("vltVolRv1030Ratio", doc.getVltVolRv1030Ratio());
        if (doc.getVltVolRv1048Ratio() != null) update.set("vltVolRv1048Ratio", doc.getVltVolRv1048Ratio());
        if (doc.getVltVolRv1096Ratio() != null) update.set("vltVolRv1096Ratio", doc.getVltVolRv1096Ratio());
        if (doc.getVltVolRv10288Ratio() != null) update.set("vltVolRv10288Ratio", doc.getVltVolRv10288Ratio());

        if (doc.getVltVolRv3048Ratio() != null) update.set("vltVolRv3048Ratio", doc.getVltVolRv3048Ratio());
        if (doc.getVltVolRv3096Ratio() != null) update.set("vltVolRv3096Ratio", doc.getVltVolRv3096Ratio());
        if (doc.getVltVolRv30288Ratio() != null) update.set("vltVolRv30288Ratio", doc.getVltVolRv30288Ratio());

        if (doc.getVltVolRv48288Ratio() != null) update.set("vltVolRv48288Ratio", doc.getVltVolRv48288Ratio());
        if (doc.getVltVolRv96288Ratio() != null) update.set("vltVolRv96288Ratio", doc.getVltVolRv96288Ratio());

        if (doc.getVltVolRv10Slp() != null) update.set("vltVolRv10Slp", doc.getVltVolRv10Slp());
        if (doc.getVltVolRv50Slp() != null) update.set("vltVolRv50Slp", doc.getVltVolRv50Slp());
        if (doc.getVltVolRv48Slp() != null) update.set("vltVolRv48Slp", doc.getVltVolRv48Slp());
        if (doc.getVltVolRv96Slp() != null) update.set("vltVolRv96Slp", doc.getVltVolRv96Slp());
        if (doc.getVltVolRv288Slp() != null) update.set("vltVolRv288Slp", doc.getVltVolRv288Slp());

        if (doc.getVltVolRv30VvW20() != null) update.set("vltVolRv30VvW20", doc.getVltVolRv30VvW20());
        if (doc.getVltVolRv48VvW20() != null) update.set("vltVolRv48VvW20", doc.getVltVolRv48VvW20());
        if (doc.getVltVolRv96VvW20() != null) update.set("vltVolRv96VvW20", doc.getVltVolRv96VvW20());
        if (doc.getVltVolRv288VvW20() != null) update.set("vltVolRv288VvW20", doc.getVltVolRv288VvW20());

        // 14) EWMA VOLATILITY
        if (doc.getVltEwmaVol20() != null) update.set("vltEwmaVol20", doc.getVltEwmaVol20());
        if (doc.getVltEwmaVol32() != null) update.set("vltEwmaVol32", doc.getVltEwmaVol32());

        if (doc.getVltEwmaVol48() != null) update.set("vltEwmaVol48", doc.getVltEwmaVol48());
        if (doc.getVltEwmaVol96() != null) update.set("vltEwmaVol96", doc.getVltEwmaVol96());
        if (doc.getVltEwmaVol288() != null) update.set("vltEwmaVol288", doc.getVltEwmaVol288());

        if (doc.getVltEwmaVol20Zsc() != null) update.set("vltEwmaVol20Zsc", doc.getVltEwmaVol20Zsc());
        if (doc.getVltEwmaVol32Zsc() != null) update.set("vltEwmaVol32Zsc", doc.getVltEwmaVol32Zsc());
        if (doc.getVltEwmaVol48Zsc() != null) update.set("vltEwmaVol48Zsc", doc.getVltEwmaVol48Zsc());
        if (doc.getVltEwmaVol96Zsc() != null) update.set("vltEwmaVol96Zsc", doc.getVltEwmaVol96Zsc());
        if (doc.getVltEwmaVol288Zsc() != null) update.set("vltEwmaVol288Zsc", doc.getVltEwmaVol288Zsc());

        if (doc.getVltEwmaVol20Slp() != null) update.set("vltEwmaVol20Slp", doc.getVltEwmaVol20Slp());
        if (doc.getVltEwmaVol32Slp() != null) update.set("vltEwmaVol32Slp", doc.getVltEwmaVol32Slp());
        if (doc.getVltEwmaVol48Slp() != null) update.set("vltEwmaVol48Slp", doc.getVltEwmaVol48Slp());
        if (doc.getVltEwmaVol96Slp() != null) update.set("vltEwmaVol96Slp", doc.getVltEwmaVol96Slp());
        if (doc.getVltEwmaVol288Slp() != null) update.set("vltEwmaVol288Slp", doc.getVltEwmaVol288Slp());

        if (doc.getVltEwmaVol2048Ratio() != null) update.set("vltEwmaVol2048Ratio", doc.getVltEwmaVol2048Ratio());
        if (doc.getVltEwmaVol3248Ratio() != null) update.set("vltEwmaVol3248Ratio", doc.getVltEwmaVol3248Ratio());
        if (doc.getVltEwmaVol48288Ratio() != null) update.set("vltEwmaVol48288Ratio", doc.getVltEwmaVol48288Ratio());
        if (doc.getVltEwmaVol96288Ratio() != null) update.set("vltEwmaVol96288Ratio", doc.getVltEwmaVol96288Ratio());

        // 15) MEAN ABS RETURN
        if (doc.getVltRetAbsMean16() != null) update.set("vltRetAbsMean16", doc.getVltRetAbsMean16());
        if (doc.getVltRetAbsMean32() != null) update.set("vltRetAbsMean32", doc.getVltRetAbsMean32());
        if (doc.getVltRetAbsMean1632Ratio() != null) update.set("vltRetAbsMean1632Ratio", doc.getVltRetAbsMean1632Ratio());

        if (doc.getVltRetAbsMean48() != null) update.set("vltRetAbsMean48", doc.getVltRetAbsMean48());
        if (doc.getVltRetAbsMean96() != null) update.set("vltRetAbsMean96", doc.getVltRetAbsMean96());
        if (doc.getVltRetAbsMean288() != null) update.set("vltRetAbsMean288", doc.getVltRetAbsMean288());

        if (doc.getVltRetAbsMean16Zsc() != null) update.set("vltRetAbsMean16Zsc", doc.getVltRetAbsMean16Zsc());
        if (doc.getVltRetAbsMean32Zsc() != null) update.set("vltRetAbsMean32Zsc", doc.getVltRetAbsMean32Zsc());
        if (doc.getVltRetAbsMean48Zsc() != null) update.set("vltRetAbsMean48Zsc", doc.getVltRetAbsMean48Zsc());
        if (doc.getVltRetAbsMean96Zsc() != null) update.set("vltRetAbsMean96Zsc", doc.getVltRetAbsMean96Zsc());
        if (doc.getVltRetAbsMean288Zsc() != null) update.set("vltRetAbsMean288Zsc", doc.getVltRetAbsMean288Zsc());

        if (doc.getVltRetAbsMean16Slp() != null) update.set("vltRetAbsMean16Slp", doc.getVltRetAbsMean16Slp());
        if (doc.getVltRetAbsMean32Slp() != null) update.set("vltRetAbsMean32Slp", doc.getVltRetAbsMean32Slp());
        if (doc.getVltRetAbsMean48Slp() != null) update.set("vltRetAbsMean48Slp", doc.getVltRetAbsMean48Slp());
        if (doc.getVltRetAbsMean96Slp() != null) update.set("vltRetAbsMean96Slp", doc.getVltRetAbsMean96Slp());
        if (doc.getVltRetAbsMean288Slp() != null) update.set("vltRetAbsMean288Slp", doc.getVltRetAbsMean288Slp());

        if (doc.getVltRetAbsMean1648Ratio() != null) update.set("vltRetAbsMean1648Ratio", doc.getVltRetAbsMean1648Ratio());
        if (doc.getVltRetAbsMean3248Ratio() != null) update.set("vltRetAbsMean3248Ratio", doc.getVltRetAbsMean3248Ratio());
        if (doc.getVltRetAbsMean48288Ratio() != null) update.set("vltRetAbsMean48288Ratio", doc.getVltRetAbsMean48288Ratio());
        if (doc.getVltRetAbsMean96288Ratio() != null) update.set("vltRetAbsMean96288Ratio", doc.getVltRetAbsMean96288Ratio());

        // 16) BIG MOVE VOLATILITY (events) + explicit 1%/2%
        if (doc.getVltVolBigmoveFreq20() != null) update.set("vltVolBigmoveFreq20", doc.getVltVolBigmoveFreq20());
        if (doc.getVltVolBigmoveFreq50() != null) update.set("vltVolBigmoveFreq50", doc.getVltVolBigmoveFreq50());
        if (doc.getVltVolBigmoveAge() != null) update.set("vltVolBigmoveAge", doc.getVltVolBigmoveAge());
        if (doc.getVltVolBigmoveClusterLen() != null) update.set("vltVolBigmoveClusterLen", doc.getVltVolBigmoveClusterLen());

        if (doc.getVltBigmove1pctFreq48() != null) update.set("vltBigmove1pctFreq48", doc.getVltBigmove1pctFreq48());
        if (doc.getVltBigmove1pctAge48() != null) update.set("vltBigmove1pctAge48", doc.getVltBigmove1pctAge48());
        if (doc.getVltBigmove1pctClusterLen48() != null) update.set("vltBigmove1pctClusterLen48", doc.getVltBigmove1pctClusterLen48());

        if (doc.getVltBigmove1pctFreq96() != null) update.set("vltBigmove1pctFreq96", doc.getVltBigmove1pctFreq96());
        if (doc.getVltBigmove1pctAge96() != null) update.set("vltBigmove1pctAge96", doc.getVltBigmove1pctAge96());
        if (doc.getVltBigmove1pctClusterLen96() != null) update.set("vltBigmove1pctClusterLen96", doc.getVltBigmove1pctClusterLen96());

        if (doc.getVltBigmove1pctFreq288() != null) update.set("vltBigmove1pctFreq288", doc.getVltBigmove1pctFreq288());
        if (doc.getVltBigmove1pctAge288() != null) update.set("vltBigmove1pctAge288", doc.getVltBigmove1pctAge288());
        if (doc.getVltBigmove1pctClusterLen288() != null) update.set("vltBigmove1pctClusterLen288", doc.getVltBigmove1pctClusterLen288());

        if (doc.getVltBigmove2pctFreq48() != null) update.set("vltBigmove2pctFreq48", doc.getVltBigmove2pctFreq48());
        if (doc.getVltBigmove2pctAge48() != null) update.set("vltBigmove2pctAge48", doc.getVltBigmove2pctAge48());
        if (doc.getVltBigmove2pctClusterLen48() != null) update.set("vltBigmove2pctClusterLen48", doc.getVltBigmove2pctClusterLen48());

        if (doc.getVltBigmove2pctFreq96() != null) update.set("vltBigmove2pctFreq96", doc.getVltBigmove2pctFreq96());
        if (doc.getVltBigmove2pctAge96() != null) update.set("vltBigmove2pctAge96", doc.getVltBigmove2pctAge96());
        if (doc.getVltBigmove2pctClusterLen96() != null) update.set("vltBigmove2pctClusterLen96", doc.getVltBigmove2pctClusterLen96());

        if (doc.getVltBigmove2pctFreq288() != null) update.set("vltBigmove2pctFreq288", doc.getVltBigmove2pctFreq288());
        if (doc.getVltBigmove2pctAge288() != null) update.set("vltBigmove2pctAge288", doc.getVltBigmove2pctAge288());
        if (doc.getVltBigmove2pctClusterLen288() != null) update.set("vltBigmove2pctClusterLen288", doc.getVltBigmove2pctClusterLen288());

        // 17) RETURN DISTRIBUTION (+ longer windows)
        if (doc.getVltRet10Skew() != null) update.set("vltRet10Skew", doc.getVltRet10Skew());
        if (doc.getVltRet10Kurt() != null) update.set("vltRet10Kurt", doc.getVltRet10Kurt());

        if (doc.getVltRet30Skew() != null) update.set("vltRet30Skew", doc.getVltRet30Skew());
        if (doc.getVltRet30Kurt() != null) update.set("vltRet30Kurt", doc.getVltRet30Kurt());

        if (doc.getVltRet50Skew() != null) update.set("vltRet50Skew", doc.getVltRet50Skew());
        if (doc.getVltRet50Kurt() != null) update.set("vltRet50Kurt", doc.getVltRet50Kurt());
        if (doc.getVltRet96Skew() != null) update.set("vltRet96Skew", doc.getVltRet96Skew());
        if (doc.getVltRet96Kurt() != null) update.set("vltRet96Kurt", doc.getVltRet96Kurt());
        if (doc.getVltRet288Skew() != null) update.set("vltRet288Skew", doc.getVltRet288Skew());
        if (doc.getVltRet288Kurt() != null) update.set("vltRet288Kurt", doc.getVltRet288Kurt());

        // 18) STRUCTURAL VOL (long memory) + extensions
        if (doc.getVltHurst100() != null) update.set("vltHurst100", doc.getVltHurst100());
        if (doc.getVltHurst200() != null) update.set("vltHurst200", doc.getVltHurst200());
        if (doc.getVltEntropyRet50() != null) update.set("vltEntropyRet50", doc.getVltEntropyRet50());

        if (doc.getVltHurst50() != null) update.set("vltHurst50", doc.getVltHurst50());
        if (doc.getVltHurst400() != null) update.set("vltHurst400", doc.getVltHurst400());
        if (doc.getVltHurst800() != null) update.set("vltHurst800", doc.getVltHurst800());

        if (doc.getVltEntropyRet100() != null) update.set("vltEntropyRet100", doc.getVltEntropyRet100());
        if (doc.getVltEntropyRet200() != null) update.set("vltEntropyRet200", doc.getVltEntropyRet200());
        if (doc.getVltEntropyRet400() != null) update.set("vltEntropyRet400", doc.getVltEntropyRet400());

        if (doc.getVltEntropyAbsret50() != null) update.set("vltEntropyAbsret50", doc.getVltEntropyAbsret50());
        if (doc.getVltEntropyAbsret100() != null) update.set("vltEntropyAbsret100", doc.getVltEntropyAbsret100());
        if (doc.getVltEntropyAbsret200() != null) update.set("vltEntropyAbsret200", doc.getVltEntropyAbsret200());

        // 19) VOLATILITY REGIME — COMPOSITES (new group)
        if (doc.getVltRegimeState() != null) update.set("vltRegimeState", doc.getVltRegimeState());
        if (doc.getVltRegimeConf() != null) update.set("vltRegimeConf", doc.getVltRegimeConf());
        if (doc.getVltRegimePrstW20() != null) update.set("vltRegimePrstW20", doc.getVltRegimePrstW20());
        if (doc.getVltRegimeFlipRateW50() != null) update.set("vltRegimeFlipRateW50", doc.getVltRegimeFlipRateW50());

        // 20) SQUEEZE DURATION / AGE (new group)
        if (doc.getVltBoll20SqueezeLen() != null) update.set("vltBoll20SqueezeLen", doc.getVltBoll20SqueezeLen());
        if (doc.getVltBoll20SqueezeAge() != null) update.set("vltBoll20SqueezeAge", doc.getVltBoll20SqueezeAge());
        if (doc.getVltBoll20SqueezePrstW20() != null) update.set("vltBoll20SqueezePrstW20", doc.getVltBoll20SqueezePrstW20());

        if (doc.getVltBoll48SqueezeLen() != null) update.set("vltBoll48SqueezeLen", doc.getVltBoll48SqueezeLen());
        if (doc.getVltBoll48SqueezeAge() != null) update.set("vltBoll48SqueezeAge", doc.getVltBoll48SqueezeAge());
        if (doc.getVltBoll48SqueezePrstW20() != null) update.set("vltBoll48SqueezePrstW20", doc.getVltBoll48SqueezePrstW20());

        if (doc.getVltBoll96SqueezeLen() != null) update.set("vltBoll96SqueezeLen", doc.getVltBoll96SqueezeLen());
        if (doc.getVltBoll96SqueezeAge() != null) update.set("vltBoll96SqueezeAge", doc.getVltBoll96SqueezeAge());
        if (doc.getVltBoll96SqueezePrstW20() != null) update.set("vltBoll96SqueezePrstW20", doc.getVltBoll96SqueezePrstW20());

        if (doc.getVltBoll288SqueezeLen() != null) update.set("vltBoll288SqueezeLen", doc.getVltBoll288SqueezeLen());
        if (doc.getVltBoll288SqueezeAge() != null) update.set("vltBoll288SqueezeAge", doc.getVltBoll288SqueezeAge());
        if (doc.getVltBoll288SqueezePrstW20() != null) update.set("vltBoll288SqueezePrstW20", doc.getVltBoll288SqueezePrstW20());

        if (doc.getVltVolSqzBbKelt20PrstW20() != null) update.set("vltVolSqzBbKelt20PrstW20", doc.getVltVolSqzBbKelt20PrstW20());
        if (doc.getVltVolSqzBbKelt48PrstW20() != null) update.set("vltVolSqzBbKelt48PrstW20", doc.getVltVolSqzBbKelt48PrstW20());
        if (doc.getVltVolSqzBbKelt96PrstW20() != null) update.set("vltVolSqzBbKelt96PrstW20", doc.getVltVolSqzBbKelt96PrstW20());
        if (doc.getVltVolSqzBbKelt288PrstW20() != null) update.set("vltVolSqzBbKelt288PrstW20", doc.getVltVolSqzBbKelt288PrstW20());

        if (doc.getVltVolSqzBbKelt20State() != null) update.set("vltVolSqzBbKelt20State", doc.getVltVolSqzBbKelt20State());
        if (doc.getVltVolSqzBbKelt48State() != null) update.set("vltVolSqzBbKelt48State", doc.getVltVolSqzBbKelt48State());
        if (doc.getVltVolSqzBbKelt96State() != null) update.set("vltVolSqzBbKelt96State", doc.getVltVolSqzBbKelt96State());
        if (doc.getVltVolSqzBbKelt288State() != null) update.set("vltVolSqzBbKelt288State", doc.getVltVolSqzBbKelt288State());

        // 21) TARGET VIABILITY — DIRECT FEATURES FOR 1% / 2% IN 24H (new group)
        if (doc.getVltAtr14Pct() != null) update.set("vltAtr14Pct", doc.getVltAtr14Pct());
        if (doc.getVltAtr48Pct() != null) update.set("vltAtr48Pct", doc.getVltAtr48Pct());
        if (doc.getVltAtr96Pct() != null) update.set("vltAtr96Pct", doc.getVltAtr96Pct());
        if (doc.getVltAtr288Pct() != null) update.set("vltAtr288Pct", doc.getVltAtr288Pct());

        if (doc.getVltStd20Pct() != null) update.set("vltStd20Pct", doc.getVltStd20Pct());
        if (doc.getVltStd48Pct() != null) update.set("vltStd48Pct", doc.getVltStd48Pct());
        if (doc.getVltStd96Pct() != null) update.set("vltStd96Pct", doc.getVltStd96Pct());
        if (doc.getVltStd288Pct() != null) update.set("vltStd288Pct", doc.getVltStd288Pct());

        if (doc.getVltVolRv30Pct() != null) update.set("vltVolRv30Pct", doc.getVltVolRv30Pct());
        if (doc.getVltVolRv48Pct() != null) update.set("vltVolRv48Pct", doc.getVltVolRv48Pct());
        if (doc.getVltVolRv96Pct() != null) update.set("vltVolRv96Pct", doc.getVltVolRv96Pct());
        if (doc.getVltVolRv288Pct() != null) update.set("vltVolRv288Pct", doc.getVltVolRv288Pct());

        if (doc.getVltEwmaVol32Pct() != null) update.set("vltEwmaVol32Pct", doc.getVltEwmaVol32Pct());
        if (doc.getVltEwmaVol48Pct() != null) update.set("vltEwmaVol48Pct", doc.getVltEwmaVol48Pct());
        if (doc.getVltEwmaVol96Pct() != null) update.set("vltEwmaVol96Pct", doc.getVltEwmaVol96Pct());
        if (doc.getVltEwmaVol288Pct() != null) update.set("vltEwmaVol288Pct", doc.getVltEwmaVol288Pct());

        if (doc.getVltTarget1pctInAtr14() != null) update.set("vltTarget1pctInAtr14", doc.getVltTarget1pctInAtr14());
        if (doc.getVltTarget2pctInAtr14() != null) update.set("vltTarget2pctInAtr14", doc.getVltTarget2pctInAtr14());

        if (doc.getVltTarget1pctInRv48() != null) update.set("vltTarget1pctInRv48", doc.getVltTarget1pctInRv48());
        if (doc.getVltTarget2pctInRv48() != null) update.set("vltTarget2pctInRv48", doc.getVltTarget2pctInRv48());

        if (doc.getVltTarget1pctInEwma48() != null) update.set("vltTarget1pctInEwma48", doc.getVltTarget1pctInEwma48());
        if (doc.getVltTarget2pctInEwma48() != null) update.set("vltTarget2pctInEwma48", doc.getVltTarget2pctInEwma48());

        if (doc.getVltTarget1pctInRv96() != null) update.set("vltTarget1pctInRv96", doc.getVltTarget1pctInRv96());
        if (doc.getVltTarget2pctInRv96() != null) update.set("vltTarget2pctInRv96", doc.getVltTarget2pctInRv96());

        if (doc.getVltTarget1pctInRv288() != null) update.set("vltTarget1pctInRv288", doc.getVltTarget1pctInRv288());
        if (doc.getVltTarget2pctInRv288() != null) update.set("vltTarget2pctInRv288", doc.getVltTarget2pctInRv288());

        if (doc.getVltTarget1pctInEwma96() != null) update.set("vltTarget1pctInEwma96", doc.getVltTarget1pctInEwma96());
        if (doc.getVltTarget2pctInEwma96() != null) update.set("vltTarget2pctInEwma96", doc.getVltTarget2pctInEwma96());

        if (doc.getVltTarget1pctInEwma288() != null) update.set("vltTarget1pctInEwma288", doc.getVltTarget1pctInEwma288());
        if (doc.getVltTarget2pctInEwma288() != null) update.set("vltTarget2pctInEwma288", doc.getVltTarget2pctInEwma288());

        // 22) EXTRA RATIOS (added at end of entity)
        if (doc.getVltVolGk1696Ratio() != null) update.set("vltVolGk1696Ratio", doc.getVltVolGk1696Ratio());
        if (doc.getVltVolPark1696Ratio() != null) update.set("vltVolPark1696Ratio", doc.getVltVolPark1696Ratio());
        if (doc.getVltVolRs1696Ratio() != null) update.set("vltVolRs1696Ratio", doc.getVltVolRs1696Ratio());

        return update;
    }

    public static String collectionName(CandleIntervals interval) {
        return switch (interval) {
            case I1_MN -> "vlt_1";
            case I5_MN -> "vlt_5";
            case I15_MN -> "vlt_15";
            case I30_MN -> "vlt_30";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
    }
}
