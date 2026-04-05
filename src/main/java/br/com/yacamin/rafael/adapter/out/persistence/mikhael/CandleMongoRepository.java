package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.CandleDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.bson.Document;

import java.time.Instant;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CandleMongoRepository {

    @Qualifier("mikhaelMongoTemplate")
    private final MongoTemplate mongoTemplate;

    private static final List<CandleIntervals> SUPPORTED = List.of(
            CandleIntervals.I1_MN, CandleIntervals.I5_MN,
            CandleIntervals.I15_MN, CandleIntervals.I30_MN);

    @PostConstruct
    public void ensureIndexes() {
        for (CandleIntervals interval : SUPPORTED) {
            String collection = collectionName(interval);
            IndexOperations ops = mongoTemplate.indexOps(collection);

            // Indice composto unique: garante 1 candle por symbol+openTime
            ops.ensureIndex(new CompoundIndexDefinition(
                    new Document("symbol", 1).append("openTime", 1))
                    .named("symbol_openTime_unique")
                    .unique());

            log.info("Indice symbol_openTime_unique garantido em {}", collection);
        }
    }

    public List<CandleDocument> findByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lte(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));

        return mongoTemplate.find(query, CandleDocument.class, collectionName(interval));
    }

    public List<CandleDocument> findByRangeExclEnd(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));

        return mongoTemplate.find(query, CandleDocument.class, collectionName(interval));
    }

    public CandleDocument findLatest(String symbol, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol))
                .with(Sort.by(Sort.Direction.DESC, "openTime"))
                .limit(1);

        return mongoTemplate.findOne(query, CandleDocument.class, collectionName(interval));
    }

    /**
     * Upsert por symbol+openTime: se ja existe, atualiza. Se nao, insere.
     * Garante unicidade sem depender do _id.
     */
    public void saveAll(List<CandleDocument> documents, CandleIntervals interval) {
        String collection = collectionName(interval);
        for (CandleDocument doc : documents) {
            Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                    .and("openTime").is(doc.getOpenTime()));

            Update update = new Update()
                    .set("open", doc.getOpen())
                    .set("high", doc.getHigh())
                    .set("low", doc.getLow())
                    .set("close", doc.getClose())
                    .set("volume", doc.getVolume())
                    .set("quoteVolume", doc.getQuoteVolume())
                    .set("numberOfTrades", doc.getNumberOfTrades())
                    .set("takerBuyBaseVolume", doc.getTakerBuyBaseVolume())
                    .set("takerBuyQuoteVolume", doc.getTakerBuyQuoteVolume())
                    .set("takerSellBaseVolume", doc.getTakerSellBaseVolume())
                    .set("takerSellQuoteVolume", doc.getTakerSellQuoteVolume());

            mongoTemplate.upsert(query, update, collection);
        }
    }

    public List<Instant> findOpenTimesByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lte(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        query.fields().include("openTime").exclude("_id");

        return mongoTemplate.find(query, CandleDocument.class, collectionName(interval))
                .stream()
                .map(CandleDocument::getOpenTime)
                .toList();
    }

    public long countByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end));
        return mongoTemplate.count(query, collectionName(interval));
    }

    public CandleDocument findLastBefore(String symbol, Instant before, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").lt(before))
                .with(Sort.by(Sort.Direction.DESC, "openTime"))
                .limit(1);
        return mongoTemplate.findOne(query, CandleDocument.class, collectionName(interval));
    }

    public static String collectionName(CandleIntervals interval) {
        return switch (interval) {
            case I1_MN -> "candle_1";
            case I5_MN -> "candle_5";
            case I15_MN -> "candle_15";
            case I30_MN -> "candle_30";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
    }
}
