package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.TrendIndicatorDocument;
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
public class TrendIndicatorMongoRepository {

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

    public Optional<TrendIndicatorDocument> findBySymbolAndOpenTime(String symbol, Instant openTime, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").is(openTime));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, TrendIndicatorDocument.class, collectionName(interval)));
    }

    public List<TrendIndicatorDocument> findByRangeExclEnd(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        return mongoTemplate.find(query, TrendIndicatorDocument.class, collectionName(interval));
    }

    public List<Instant> findOpenTimesByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        query.fields().include("openTime").exclude("_id");

        return mongoTemplate.find(query, TrendIndicatorDocument.class, collectionName(interval))
                .stream()
                .map(TrendIndicatorDocument::getOpenTime)
                .toList();
    }

    public void save(TrendIndicatorDocument doc, CandleIntervals interval) {
        String collection = collectionName(interval);
        Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                .and("openTime").is(doc.getOpenTime()));
        mongoTemplate.upsert(query, buildUpdate(doc), collection);
    }

    public void saveBatch(List<TrendIndicatorDocument> documents, CandleIntervals interval) {
        if (documents.isEmpty()) return;
        String collection = collectionName(interval);
        var bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        int count = 0;
        for (TrendIndicatorDocument doc : documents) {
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

    private Update buildUpdate(TrendIndicatorDocument doc) {
        Update update = new Update();

        // 1) EMA - RAW
        if (doc.getTrdEma8() != null) update.set("trdEma8", doc.getTrdEma8());
        if (doc.getTrdEma12() != null) update.set("trdEma12", doc.getTrdEma12());
        if (doc.getTrdEma16() != null) update.set("trdEma16", doc.getTrdEma16());
        if (doc.getTrdEma20() != null) update.set("trdEma20", doc.getTrdEma20());
        if (doc.getTrdEma21() != null) update.set("trdEma21", doc.getTrdEma21());
        if (doc.getTrdEma32() != null) update.set("trdEma32", doc.getTrdEma32());
        if (doc.getTrdEma34() != null) update.set("trdEma34", doc.getTrdEma34());
        if (doc.getTrdEma50() != null) update.set("trdEma50", doc.getTrdEma50());
        if (doc.getTrdEma55() != null) update.set("trdEma55", doc.getTrdEma55());
        if (doc.getTrdEma100() != null) update.set("trdEma100", doc.getTrdEma100());
        if (doc.getTrdEma144() != null) update.set("trdEma144", doc.getTrdEma144());
        if (doc.getTrdEma200() != null) update.set("trdEma200", doc.getTrdEma200());
        if (doc.getTrdEma233() != null) update.set("trdEma233", doc.getTrdEma233());

        // 2) EMA - SLOPE
        if (doc.getTrdEma8Slp() != null) update.set("trdEma8Slp", doc.getTrdEma8Slp());
        if (doc.getTrdEma20Slp() != null) update.set("trdEma20Slp", doc.getTrdEma20Slp());
        if (doc.getTrdEma50Slp() != null) update.set("trdEma50Slp", doc.getTrdEma50Slp());
        if (doc.getTrdEma16Slp() != null) update.set("trdEma16Slp", doc.getTrdEma16Slp());
        if (doc.getTrdEma32Slp() != null) update.set("trdEma32Slp", doc.getTrdEma32Slp());

        // 3) EMA - SLOPE (ATR-N)
        if (doc.getTrdEma8SlpAtrn() != null) update.set("trdEma8SlpAtrn", doc.getTrdEma8SlpAtrn());
        if (doc.getTrdEma20SlpAtrn() != null) update.set("trdEma20SlpAtrn", doc.getTrdEma20SlpAtrn());
        if (doc.getTrdEma50SlpAtrn() != null) update.set("trdEma50SlpAtrn", doc.getTrdEma50SlpAtrn());
        if (doc.getTrdEma16SlpAtrn() != null) update.set("trdEma16SlpAtrn", doc.getTrdEma16SlpAtrn());
        if (doc.getTrdEma32SlpAtrn() != null) update.set("trdEma32SlpAtrn", doc.getTrdEma32SlpAtrn());

        // 4) EMA - SLOPE ACC
        if (doc.getTrdEma8SlpAcc() != null) update.set("trdEma8SlpAcc", doc.getTrdEma8SlpAcc());
        if (doc.getTrdEma20SlpAcc() != null) update.set("trdEma20SlpAcc", doc.getTrdEma20SlpAcc());
        if (doc.getTrdEma50SlpAcc() != null) update.set("trdEma50SlpAcc", doc.getTrdEma50SlpAcc());

        // 5) EMA - SLOPE ACC (ATR-N)
        if (doc.getTrdEma8SlpAccAtrn() != null) update.set("trdEma8SlpAccAtrn", doc.getTrdEma8SlpAccAtrn());
        if (doc.getTrdEma20SlpAccAtrn() != null) update.set("trdEma20SlpAccAtrn", doc.getTrdEma20SlpAccAtrn());
        if (doc.getTrdEma50SlpAccAtrn() != null) update.set("trdEma50SlpAccAtrn", doc.getTrdEma50SlpAccAtrn());

        // 6) EMA - TDS / TVR
        if (doc.getTrdEma8SlpTds() != null) update.set("trdEma8SlpTds", doc.getTrdEma8SlpTds());
        if (doc.getTrdEma20SlpTds() != null) update.set("trdEma20SlpTds", doc.getTrdEma20SlpTds());
        if (doc.getTrdEma50SlpTds() != null) update.set("trdEma50SlpTds", doc.getTrdEma50SlpTds());
        if (doc.getTrdEma8SlpTvr() != null) update.set("trdEma8SlpTvr", doc.getTrdEma8SlpTvr());
        if (doc.getTrdEma20SlpTvr() != null) update.set("trdEma20SlpTvr", doc.getTrdEma20SlpTvr());
        if (doc.getTrdEma50SlpTvr() != null) update.set("trdEma50SlpTvr", doc.getTrdEma50SlpTvr());

        // 7) DISTANCE - ATR-N ONLY
        if (doc.getTrdDistCloseEma8Atrn() != null) update.set("trdDistCloseEma8Atrn", doc.getTrdDistCloseEma8Atrn());
        if (doc.getTrdDistCloseEma20Atrn() != null) update.set("trdDistCloseEma20Atrn", doc.getTrdDistCloseEma20Atrn());
        if (doc.getTrdDistCloseEma50Atrn() != null) update.set("trdDistCloseEma50Atrn", doc.getTrdDistCloseEma50Atrn());
        if (doc.getTrdDistCloseEma16Atrn() != null) update.set("trdDistCloseEma16Atrn", doc.getTrdDistCloseEma16Atrn());
        if (doc.getTrdDistCloseEma32Atrn() != null) update.set("trdDistCloseEma32Atrn", doc.getTrdDistCloseEma32Atrn());
        if (doc.getTrdDistEma820Atrn() != null) update.set("trdDistEma820Atrn", doc.getTrdDistEma820Atrn());
        if (doc.getTrdDistEma2050Atrn() != null) update.set("trdDistEma2050Atrn", doc.getTrdDistEma2050Atrn());
        if (doc.getTrdDistEma850Atrn() != null) update.set("trdDistEma850Atrn", doc.getTrdDistEma850Atrn());

        // 8) RATIOS - RAW ONLY
        if (doc.getTrdRatioEma820() != null) update.set("trdRatioEma820", doc.getTrdRatioEma820());
        if (doc.getTrdRatioEma2050() != null) update.set("trdRatioEma2050", doc.getTrdRatioEma2050());
        if (doc.getTrdRatioEma850() != null) update.set("trdRatioEma850", doc.getTrdRatioEma850());

        // 9) ALIGNMENT / CROSS / DURATION
        if (doc.getTrdAlignmentEma82050Score() != null) update.set("trdAlignmentEma82050Score", doc.getTrdAlignmentEma82050Score());
        if (doc.getTrdAlignmentEma82050Normalized() != null) update.set("trdAlignmentEma82050Normalized", doc.getTrdAlignmentEma82050Normalized());
        if (doc.getTrdAligmentEma82050Delta() != null) update.set("trdAligmentEma82050Delta", doc.getTrdAligmentEma82050Delta());
        if (doc.getTrdAligmentEma82050Binary() != null) update.set("trdAligmentEma82050Binary", doc.getTrdAligmentEma82050Binary());
        if (doc.getTrdCrossEma820Binary() != null) update.set("trdCrossEma820Binary", doc.getTrdCrossEma820Binary());
        if (doc.getTrdCrossEma820Delta() != null) update.set("trdCrossEma820Delta", doc.getTrdCrossEma820Delta());
        if (doc.getTrdCrossEma820DeltaAtrn() != null) update.set("trdCrossEma820DeltaAtrn", doc.getTrdCrossEma820DeltaAtrn());
        if (doc.getTrdCrossEma2050Binary() != null) update.set("trdCrossEma2050Binary", doc.getTrdCrossEma2050Binary());
        if (doc.getTrdCrossEma2050Delta() != null) update.set("trdCrossEma2050Delta", doc.getTrdCrossEma2050Delta());
        if (doc.getTrdCrossEma2050DeltaAtrn() != null) update.set("trdCrossEma2050DeltaAtrn", doc.getTrdCrossEma2050DeltaAtrn());
        if (doc.getTrdDurationEma820() != null) update.set("trdDurationEma820", doc.getTrdDurationEma820());
        if (doc.getTrdDurationEma2050() != null) update.set("trdDurationEma2050", doc.getTrdDurationEma2050());
        if (doc.getTrdDurationEma850() != null) update.set("trdDurationEma850", doc.getTrdDurationEma850());

        // 10) PUSH / GAP
        if (doc.getTrdDeltaCloseEma8() != null) update.set("trdDeltaCloseEma8", doc.getTrdDeltaCloseEma8());
        if (doc.getTrdDeltaCloseEma20() != null) update.set("trdDeltaCloseEma20", doc.getTrdDeltaCloseEma20());
        if (doc.getTrdDeltaCloseEma50() != null) update.set("trdDeltaCloseEma50", doc.getTrdDeltaCloseEma50());
        if (doc.getTrdDeltaCloseEma8Atrn() != null) update.set("trdDeltaCloseEma8Atrn", doc.getTrdDeltaCloseEma8Atrn());
        if (doc.getTrdDeltaCloseEma20Atrn() != null) update.set("trdDeltaCloseEma20Atrn", doc.getTrdDeltaCloseEma20Atrn());
        if (doc.getTrdDeltaCloseEma50Atrn() != null) update.set("trdDeltaCloseEma50Atrn", doc.getTrdDeltaCloseEma50Atrn());
        if (doc.getTrdDeltaEma820() != null) update.set("trdDeltaEma820", doc.getTrdDeltaEma820());
        if (doc.getTrdDeltaEma2050() != null) update.set("trdDeltaEma2050", doc.getTrdDeltaEma2050());
        if (doc.getTrdDeltaEma820Atrn() != null) update.set("trdDeltaEma820Atrn", doc.getTrdDeltaEma820Atrn());
        if (doc.getTrdDeltaEma2050Atrn() != null) update.set("trdDeltaEma2050Atrn", doc.getTrdDeltaEma2050Atrn());

        // 11) QUALITY / REGIME COMPOSITES
        if (doc.getTrdCtsCloseEma20W50() != null) update.set("trdCtsCloseEma20W50", doc.getTrdCtsCloseEma20W50());
        if (doc.getTrdCtsCloseEma20W10() != null) update.set("trdCtsCloseEma20W10", doc.getTrdCtsCloseEma20W10());
        if (doc.getTrdCtsEma82050() != null) update.set("trdCtsEma82050", doc.getTrdCtsEma82050());
        if (doc.getTrdEma82050SlpTcs() != null) update.set("trdEma82050SlpTcs", doc.getTrdEma82050SlpTcs());
        if (doc.getTrdTmEma820W10() != null) update.set("trdTmEma820W10", doc.getTrdTmEma820W10());
        if (doc.getTrdEma20SlpSnrW10() != null) update.set("trdEma20SlpSnrW10", doc.getTrdEma20SlpSnrW10());
        if (doc.getTrdCloseTcpW50() != null) update.set("trdCloseTcpW50", doc.getTrdCloseTcpW50());
        if (doc.getTrdEma8Zsc() != null) update.set("trdEma8Zsc", doc.getTrdEma8Zsc());
        if (doc.getTrdEma20Zsc() != null) update.set("trdEma20Zsc", doc.getTrdEma20Zsc());
        if (doc.getTrdEma50Zsc() != null) update.set("trdEma50Zsc", doc.getTrdEma50Zsc());

        // 12) ADX / DI - Trend Strength
        if (doc.getTrdAdx14() != null) update.set("trdAdx14", doc.getTrdAdx14());
        if (doc.getTrdPdi14() != null) update.set("trdPdi14", doc.getTrdPdi14());
        if (doc.getTrdMdi14() != null) update.set("trdMdi14", doc.getTrdMdi14());
        if (doc.getTrdDiDiff14() != null) update.set("trdDiDiff14", doc.getTrdDiDiff14());

        return update;
    }

    public static String collectionName(CandleIntervals interval) {
        return switch (interval) {
            case I1_MN -> "trd_1";
            case I5_MN -> "trd_5";
            case I15_MN -> "trd_15";
            case I30_MN -> "trd_30";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
    }
}
