package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.TimeIndicatorDocument;
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
public class TimeIndicatorMongoRepository {

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

    public Optional<TimeIndicatorDocument> findBySymbolAndOpenTime(String symbol, Instant openTime, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").is(openTime));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, TimeIndicatorDocument.class, collectionName(interval)));
    }

    public List<TimeIndicatorDocument> findByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lte(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        return mongoTemplate.find(query, TimeIndicatorDocument.class, collectionName(interval));
    }

    public List<TimeIndicatorDocument> findByRangeExclEnd(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        return mongoTemplate.find(query, TimeIndicatorDocument.class, collectionName(interval));
    }

    public void save(TimeIndicatorDocument doc, CandleIntervals interval) {
        String collection = collectionName(interval);
        Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                .and("openTime").is(doc.getOpenTime()));
        mongoTemplate.upsert(query, buildUpdate(doc), collection);
    }

    public void saveBatch(List<TimeIndicatorDocument> documents, CandleIntervals interval) {
        if (documents.isEmpty()) return;
        String collection = collectionName(interval);
        var bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        int count = 0;
        for (TimeIndicatorDocument doc : documents) {
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

    private Update buildUpdate(TimeIndicatorDocument doc) {
        Update update = new Update();
        if (doc.getMinuteOfDay() != null)   update.set("minuteOfDay", doc.getMinuteOfDay());
        if (doc.getDayOfWeek() != null)     update.set("dayOfWeek", doc.getDayOfWeek());
        if (doc.getSessionAsia() != null)   update.set("sessionAsia", doc.getSessionAsia());
        if (doc.getSessionEurope() != null) update.set("sessionEurope", doc.getSessionEurope());
        if (doc.getSessionNy() != null)     update.set("sessionNy", doc.getSessionNy());
        if (doc.getSinTime() != null)       update.set("sinTime", doc.getSinTime());
        if (doc.getCosTime() != null)       update.set("cosTime", doc.getCosTime());
        if (doc.getDayOfMonth() != null)    update.set("dayOfMonth", doc.getDayOfMonth());
        if (doc.getSinDayOfWeek() != null)  update.set("sinDayOfWeek", doc.getSinDayOfWeek());
        if (doc.getCosDayOfWeek() != null)  update.set("cosDayOfWeek", doc.getCosDayOfWeek());
        if (doc.getOverlapAsiaEur() != null) update.set("overlapAsiaEur", doc.getOverlapAsiaEur());
        if (doc.getOverlapEurNy() != null)  update.set("overlapEurNy", doc.getOverlapEurNy());
        if (doc.getCandleInH1() != null)    update.set("candleInH1", doc.getCandleInH1());
        return update;
    }

    public List<Instant> findOpenTimesByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        query.fields().include("openTime").exclude("_id");

        return mongoTemplate.find(query, TimeIndicatorDocument.class, collectionName(interval))
                .stream()
                .map(TimeIndicatorDocument::getOpenTime)
                .toList();
    }

    public static String collectionName(CandleIntervals interval) {
        return switch (interval) {
            case I1_MN -> "tim_1";
            case I5_MN -> "tim_5";
            case I15_MN -> "tim_15";
            case I30_MN -> "tim_30";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
    }
}
