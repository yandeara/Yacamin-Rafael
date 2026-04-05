package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.TpSlIndicatorDocument;
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
public class TpSlIndicatorMongoRepository {

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

    public Optional<TpSlIndicatorDocument> findBySymbolAndOpenTime(String symbol, Instant openTime, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").is(openTime));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, TpSlIndicatorDocument.class, collectionName(interval)));
    }

    public List<TpSlIndicatorDocument> findByRangeExclEnd(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        return mongoTemplate.find(query, TpSlIndicatorDocument.class, collectionName(interval));
    }

    public List<Instant> findOpenTimesByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        query.fields().include("openTime").exclude("_id");

        return mongoTemplate.find(query, TpSlIndicatorDocument.class, collectionName(interval))
                .stream()
                .map(TpSlIndicatorDocument::getOpenTime)
                .toList();
    }

    public void save(TpSlIndicatorDocument doc, CandleIntervals interval) {
        String collection = collectionName(interval);
        Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                .and("openTime").is(doc.getOpenTime()));
        mongoTemplate.upsert(query, buildUpdate(doc), collection);
    }

    public void saveBatch(List<TpSlIndicatorDocument> documents, CandleIntervals interval) {
        if (documents.isEmpty()) return;
        String collection = collectionName(interval);
        var bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        int count = 0;
        for (TpSlIndicatorDocument doc : documents) {
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

    private Update buildUpdate(TpSlIndicatorDocument doc) {
        Update update = new Update();

        // Setup prices
        if (doc.getTpslTpPriceLong() != null)           update.set("tpslTpPriceLong", doc.getTpslTpPriceLong());
        if (doc.getTpslSlPriceLong() != null)           update.set("tpslSlPriceLong", doc.getTpslSlPriceLong());
        if (doc.getTpslTpPriceShort() != null)          update.set("tpslTpPriceShort", doc.getTpslTpPriceShort());
        if (doc.getTpslSlPriceShort() != null)          update.set("tpslSlPriceShort", doc.getTpslSlPriceShort());

        // Distances
        if (doc.getTpslTpDistLong() != null)            update.set("tpslTpDistLong", doc.getTpslTpDistLong());
        if (doc.getTpslTpDistShort() != null)           update.set("tpslTpDistShort", doc.getTpslTpDistShort());
        if (doc.getTpslSlDistLong() != null)            update.set("tpslSlDistLong", doc.getTpslSlDistLong());
        if (doc.getTpslSlDistShort() != null)           update.set("tpslSlDistShort", doc.getTpslSlDistShort());

        // ATR14 normalized distances
        if (doc.getTpslTpDistAtr14Long() != null)       update.set("tpslTpDistAtr14Long", doc.getTpslTpDistAtr14Long());
        if (doc.getTpslTpDistAtr14Short() != null)      update.set("tpslTpDistAtr14Short", doc.getTpslTpDistAtr14Short());
        if (doc.getTpslSlDistAtr14Long() != null)       update.set("tpslSlDistAtr14Long", doc.getTpslSlDistAtr14Long());
        if (doc.getTpslSlDistAtr14Short() != null)      update.set("tpslSlDistAtr14Short", doc.getTpslSlDistAtr14Short());

        // RV48 normalized distances
        if (doc.getTpslTpDistRv48Long() != null)        update.set("tpslTpDistRv48Long", doc.getTpslTpDistRv48Long());
        if (doc.getTpslTpDistRv48Short() != null)       update.set("tpslTpDistRv48Short", doc.getTpslTpDistRv48Short());
        if (doc.getTpslSlDistRv48Long() != null)        update.set("tpslSlDistRv48Long", doc.getTpslSlDistRv48Long());
        if (doc.getTpslSlDistRv48Short() != null)       update.set("tpslSlDistRv48Short", doc.getTpslSlDistRv48Short());

        // EWMA48 normalized distances
        if (doc.getTpslTpDistEwma48Long() != null)      update.set("tpslTpDistEwma48Long", doc.getTpslTpDistEwma48Long());
        if (doc.getTpslTpDistEwma48Short() != null)     update.set("tpslTpDistEwma48Short", doc.getTpslTpDistEwma48Short());
        if (doc.getTpslSlDistEwma48Long() != null)      update.set("tpslSlDistEwma48Long", doc.getTpslSlDistEwma48Long());
        if (doc.getTpslSlDistEwma48Short() != null)     update.set("tpslSlDistEwma48Short", doc.getTpslSlDistEwma48Short());

        // Risk/Reward
        if (doc.getTpslRrRatioLong() != null)           update.set("tpslRrRatioLong", doc.getTpslRrRatioLong());
        if (doc.getTpslRrRatioShort() != null)          update.set("tpslRrRatioShort", doc.getTpslRrRatioShort());

        // Headroom/Room ATR14
        if (doc.getTpslHeadroomToTpAtr14Long() != null)  update.set("tpslHeadroomToTpAtr14Long", doc.getTpslHeadroomToTpAtr14Long());
        if (doc.getTpslHeadroomToTpAtr14Short() != null) update.set("tpslHeadroomToTpAtr14Short", doc.getTpslHeadroomToTpAtr14Short());
        if (doc.getTpslRoomToSlAtr14Long() != null)      update.set("tpslRoomToSlAtr14Long", doc.getTpslRoomToSlAtr14Long());
        if (doc.getTpslRoomToSlAtr14Short() != null)     update.set("tpslRoomToSlAtr14Short", doc.getTpslRoomToSlAtr14Short());

        // Headroom/Room candle extremes
        if (doc.getTpslHeadroomHighToTpAtr14Long() != null)  update.set("tpslHeadroomHighToTpAtr14Long", doc.getTpslHeadroomHighToTpAtr14Long());
        if (doc.getTpslHeadroomLowToTpAtr14Short() != null)  update.set("tpslHeadroomLowToTpAtr14Short", doc.getTpslHeadroomLowToTpAtr14Short());
        if (doc.getTpslRoomLowToSlAtr14Long() != null)       update.set("tpslRoomLowToSlAtr14Long", doc.getTpslRoomLowToSlAtr14Long());
        if (doc.getTpslRoomHighToSlAtr14Short() != null)     update.set("tpslRoomHighToSlAtr14Short", doc.getTpslRoomHighToSlAtr14Short());

        // Donchian w48
        if (doc.getTpslDonchHighW48() != null)              update.set("tpslDonchHighW48", doc.getTpslDonchHighW48());
        if (doc.getTpslDonchLowW48() != null)               update.set("tpslDonchLowW48", doc.getTpslDonchLowW48());
        if (doc.getTpslDonchPosW48() != null)               update.set("tpslDonchPosW48", doc.getTpslDonchPosW48());
        if (doc.getTpslDistToDonchHighW48Atrn() != null)    update.set("tpslDistToDonchHighW48Atrn", doc.getTpslDistToDonchHighW48Atrn());
        if (doc.getTpslDistToDonchLowW48Atrn() != null)     update.set("tpslDistToDonchLowW48Atrn", doc.getTpslDistToDonchLowW48Atrn());

        // Donchian w96
        if (doc.getTpslDonchHighW96() != null)              update.set("tpslDonchHighW96", doc.getTpslDonchHighW96());
        if (doc.getTpslDonchLowW96() != null)               update.set("tpslDonchLowW96", doc.getTpslDonchLowW96());
        if (doc.getTpslDonchPosW96() != null)               update.set("tpslDonchPosW96", doc.getTpslDonchPosW96());
        if (doc.getTpslDistToDonchHighW96Atrn() != null)    update.set("tpslDistToDonchHighW96Atrn", doc.getTpslDistToDonchHighW96Atrn());
        if (doc.getTpslDistToDonchLowW96Atrn() != null)     update.set("tpslDistToDonchLowW96Atrn", doc.getTpslDistToDonchLowW96Atrn());

        // Donchian w288
        if (doc.getTpslDonchHighW288() != null)             update.set("tpslDonchHighW288", doc.getTpslDonchHighW288());
        if (doc.getTpslDonchLowW288() != null)              update.set("tpslDonchLowW288", doc.getTpslDonchLowW288());
        if (doc.getTpslDonchPosW288() != null)              update.set("tpslDonchPosW288", doc.getTpslDonchPosW288());
        if (doc.getTpslDistToDonchHighW288Atrn() != null)   update.set("tpslDistToDonchHighW288Atrn", doc.getTpslDistToDonchHighW288Atrn());
        if (doc.getTpslDistToDonchLowW288Atrn() != null)    update.set("tpslDistToDonchLowW288Atrn", doc.getTpslDistToDonchLowW288Atrn());

        // Bollinger
        if (doc.getTpslBbPercentb20() != null)              update.set("tpslBbPercentb20", doc.getTpslBbPercentb20());
        if (doc.getTpslBbPercentb48() != null)              update.set("tpslBbPercentb48", doc.getTpslBbPercentb48());
        if (doc.getTpslBbPercentb96() != null)              update.set("tpslBbPercentb96", doc.getTpslBbPercentb96());
        if (doc.getTpslBbZ20() != null)                     update.set("tpslBbZ20", doc.getTpslBbZ20());
        if (doc.getTpslBbZ48() != null)                     update.set("tpslBbZ48", doc.getTpslBbZ48());
        if (doc.getTpslBbZ96() != null)                     update.set("tpslBbZ96", doc.getTpslBbZ96());

        // Keltner
        if (doc.getTpslKeltPos20() != null)                 update.set("tpslKeltPos20", doc.getTpslKeltPos20());
        if (doc.getTpslKeltPos48() != null)                 update.set("tpslKeltPos48", doc.getTpslKeltPos48());
        if (doc.getTpslKeltPos96() != null)                 update.set("tpslKeltPos96", doc.getTpslKeltPos96());

        // Efficiency
        if (doc.getTpslEffRatioW48() != null)               update.set("tpslEffRatioW48", doc.getTpslEffRatioW48());
        if (doc.getTpslChopEffW48() != null)                update.set("tpslChopEffW48", doc.getTpslChopEffW48());

        return update;
    }

    public static String collectionName(CandleIntervals interval) {
        return switch (interval) {
            case I1_MN -> "tpsl_1";
            case I5_MN -> "tpsl_5";
            case I15_MN -> "tpsl_15";
            case I30_MN -> "tpsl_30";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
    }
}
