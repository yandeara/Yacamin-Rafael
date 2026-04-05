package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.MicrostructureIndicatorDocument;
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
public class MicrostructureIndicatorMongoRepository {

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

    public Optional<MicrostructureIndicatorDocument> findBySymbolAndOpenTime(String symbol, Instant openTime, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").is(openTime));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, MicrostructureIndicatorDocument.class, collectionName(interval)));
    }

    public List<MicrostructureIndicatorDocument> findByRangeExclEnd(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        return mongoTemplate.find(query, MicrostructureIndicatorDocument.class, collectionName(interval));
    }

    public List<Instant> findOpenTimesByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        query.fields().include("openTime").exclude("_id");

        return mongoTemplate.find(query, MicrostructureIndicatorDocument.class, collectionName(interval))
                .stream()
                .map(MicrostructureIndicatorDocument::getOpenTime)
                .toList();
    }

    public void save(MicrostructureIndicatorDocument doc, CandleIntervals interval) {
        String collection = collectionName(interval);
        Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                .and("openTime").is(doc.getOpenTime()));
        mongoTemplate.upsert(query, buildUpdate(doc), collection);
    }

    public void saveBatch(List<MicrostructureIndicatorDocument> documents, CandleIntervals interval) {
        if (documents.isEmpty()) return;
        String collection = collectionName(interval);
        var bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        int count = 0;
        for (MicrostructureIndicatorDocument doc : documents) {
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

    private Update buildUpdate(MicrostructureIndicatorDocument doc) {
        Update update = new Update();
        if (doc.getMicAmihud() != null)             update.set("micAmihud", doc.getMicAmihud());
        if (doc.getMicAmihudZscore20() != null)     update.set("micAmihudZscore20", doc.getMicAmihudZscore20());
        if (doc.getMicAmihudZscore80() != null)     update.set("micAmihudZscore80", doc.getMicAmihudZscore80());
        if (doc.getMicAmihudRel10() != null)        update.set("micAmihudRel10", doc.getMicAmihudRel10());
        if (doc.getMicAmihudRel40() != null)        update.set("micAmihudRel40", doc.getMicAmihudRel40());
        if (doc.getMicAmihudSlpW4() != null)        update.set("micAmihudSlpW4", doc.getMicAmihudSlpW4());
        if (doc.getMicAmihudSlpW20() != null)       update.set("micAmihudSlpW20", doc.getMicAmihudSlpW20());
        if (doc.getMicAmihudSlpW50() != null)       update.set("micAmihudSlpW50", doc.getMicAmihudSlpW50());
        if (doc.getMicAmihudAccW4() != null)        update.set("micAmihudAccW4", doc.getMicAmihudAccW4());
        if (doc.getMicAmihudAccW5() != null)        update.set("micAmihudAccW5", doc.getMicAmihudAccW5());
        if (doc.getMicAmihudAccW10() != null)       update.set("micAmihudAccW10", doc.getMicAmihudAccW10());
        if (doc.getMicAmihudAccW16() != null)       update.set("micAmihudAccW16", doc.getMicAmihudAccW16());
        if (doc.getMicAmihudMa10() != null)         update.set("micAmihudMa10", doc.getMicAmihudMa10());
        if (doc.getMicAmihudMa20() != null)         update.set("micAmihudMa20", doc.getMicAmihudMa20());
        if (doc.getMicAmihudMa30() != null)         update.set("micAmihudMa30", doc.getMicAmihudMa30());
        if (doc.getMicAmihudVol10() != null)        update.set("micAmihudVol10", doc.getMicAmihudVol10());
        if (doc.getMicAmihudVol20() != null)        update.set("micAmihudVol20", doc.getMicAmihudVol20());
        if (doc.getMicAmihudVol40() != null)        update.set("micAmihudVol40", doc.getMicAmihudVol40());
        if (doc.getMicAmihudTurnover() != null)     update.set("micAmihudTurnover", doc.getMicAmihudTurnover());
        if (doc.getMicAmihudPctileW20() != null)    update.set("micAmihudPctileW20", doc.getMicAmihudPctileW20());
        if (doc.getMicAmihudSigned() != null)       update.set("micAmihudSigned", doc.getMicAmihudSigned());
        if (doc.getMicAmihudLrmr1040() != null)     update.set("micAmihudLrmr1040", doc.getMicAmihudLrmr1040());
        if (doc.getMicAmihudStability40() != null)   update.set("micAmihudStability40", doc.getMicAmihudStability40());
        if (doc.getMicAmihudVolRel40() != null)     update.set("micAmihudVolRel40", doc.getMicAmihudVolRel40());
        if (doc.getMicAmihudAtrn() != null)         update.set("micAmihudAtrn", doc.getMicAmihudAtrn());
        if (doc.getMicAmihudPrstW10() != null)      update.set("micAmihudPrstW10", doc.getMicAmihudPrstW10());
        if (doc.getMicAmihudPrstW20() != null)      update.set("micAmihudPrstW20", doc.getMicAmihudPrstW20());
        if (doc.getMicAmihudPrstW40() != null)      update.set("micAmihudPrstW40", doc.getMicAmihudPrstW40());
        if (doc.getMicAmihudDvgc() != null)         update.set("micAmihudDvgc", doc.getMicAmihudDvgc());
        if (doc.getMicAmihudRegimeState() != null)  update.set("micAmihudRegimeState", doc.getMicAmihudRegimeState());
        if (doc.getMicAmihudTrendAlignment() != null) update.set("micAmihudTrendAlignment", doc.getMicAmihudTrendAlignment());
        if (doc.getMicAmihudBreakdownRisk() != null) update.set("micAmihudBreakdownRisk", doc.getMicAmihudBreakdownRisk());
        if (doc.getMicAmihudRegimeConf() != null)   update.set("micAmihudRegimeConf", doc.getMicAmihudRegimeConf());

        // Body
        if (doc.getMicCandleBody() != null)              update.set("micCandleBody", doc.getMicCandleBody());
        if (doc.getMicCandleBodyAbs() != null)            update.set("micCandleBodyAbs", doc.getMicCandleBodyAbs());
        if (doc.getMicBodyRatio() != null)                update.set("micBodyRatio", doc.getMicBodyRatio());
        if (doc.getMicCandleEnergyRaw() != null)          update.set("micCandleEnergyRaw", doc.getMicCandleEnergyRaw());
        if (doc.getMicCandleBodySlpW10() != null)         update.set("micCandleBodySlpW10", doc.getMicCandleBodySlpW10());
        if (doc.getMicCandleBodySlpW20() != null)         update.set("micCandleBodySlpW20", doc.getMicCandleBodySlpW20());
        if (doc.getMicCandleBodyMa10() != null)           update.set("micCandleBodyMa10", doc.getMicCandleBodyMa10());
        if (doc.getMicCandleBodyMa20() != null)           update.set("micCandleBodyMa20", doc.getMicCandleBodyMa20());
        if (doc.getMicCandleBodyVol10() != null)          update.set("micCandleBodyVol10", doc.getMicCandleBodyVol10());
        if (doc.getMicCandleBodyVol20() != null)          update.set("micCandleBodyVol20", doc.getMicCandleBodyVol20());
        if (doc.getMicBodyRatioSlpW10() != null)          update.set("micBodyRatioSlpW10", doc.getMicBodyRatioSlpW10());
        if (doc.getMicBodyRatioVolW10() != null)          update.set("micBodyRatioVolW10", doc.getMicBodyRatioVolW10());
        if (doc.getMicBodyAtrRatio() != null)             update.set("micBodyAtrRatio", doc.getMicBodyAtrRatio());
        if (doc.getMicCandleEnergyAtrn() != null)         update.set("micCandleEnergyAtrn", doc.getMicCandleEnergyAtrn());
        if (doc.getMicCandleBodyCenterPosition() != null) update.set("micCandleBodyCenterPosition", doc.getMicCandleBodyCenterPosition());
        if (doc.getMicCandlePressureRaw() != null)        update.set("micCandlePressureRaw", doc.getMicCandlePressureRaw());
        if (doc.getMicCandleStrength() != null)           update.set("micCandleStrength", doc.getMicCandleStrength());
        if (doc.getMicCandleBodyStrengthScore() != null)  update.set("micCandleBodyStrengthScore", doc.getMicCandleBodyStrengthScore());
        if (doc.getMicCandleBodyPct() != null)            update.set("micCandleBodyPct", doc.getMicCandleBodyPct());
        if (doc.getMicBodyPerc() != null)                 update.set("micBodyPerc", doc.getMicBodyPerc());
        if (doc.getMicCandleBodyRatio() != null)          update.set("micCandleBodyRatio", doc.getMicCandleBodyRatio());
        if (doc.getMicBodyReturn() != null)               update.set("micBodyReturn", doc.getMicBodyReturn());
        if (doc.getMicBodyShockAtrn() != null)            update.set("micBodyShockAtrn", doc.getMicBodyShockAtrn());
        if (doc.getMicBodySignPrstW20() != null)          update.set("micBodySignPrstW20", doc.getMicBodySignPrstW20());
        if (doc.getMicBodyRunLen() != null)               update.set("micBodyRunLen", doc.getMicBodyRunLen());

        // Hasbrouck W16
        if (doc.getMicHasbLambdaW16() != null)           update.set("micHasbLambdaW16", doc.getMicHasbLambdaW16());
        if (doc.getMicHasbLambdaW16Zsc40() != null)      update.set("micHasbLambdaW16Zsc40", doc.getMicHasbLambdaW16Zsc40());
        if (doc.getMicHasbLambdaW16Ma20() != null)        update.set("micHasbLambdaW16Ma20", doc.getMicHasbLambdaW16Ma20());
        if (doc.getMicHasbLambdaW16Dvgc() != null)        update.set("micHasbLambdaW16Dvgc", doc.getMicHasbLambdaW16Dvgc());
        if (doc.getMicHasbLambdaW16SlpW20() != null)      update.set("micHasbLambdaW16SlpW20", doc.getMicHasbLambdaW16SlpW20());
        if (doc.getMicHasbLambdaW16Vol40() != null)        update.set("micHasbLambdaW16Vol40", doc.getMicHasbLambdaW16Vol40());
        if (doc.getMicHasbLambdaW16Stability40() != null)  update.set("micHasbLambdaW16Stability40", doc.getMicHasbLambdaW16Stability40());
        if (doc.getMicHasbLambdaW16PctileW40() != null)    update.set("micHasbLambdaW16PctileW40", doc.getMicHasbLambdaW16PctileW40());
        if (doc.getMicHasbLambdaW16Atrn() != null)        update.set("micHasbLambdaW16Atrn", doc.getMicHasbLambdaW16Atrn());
        // Hasbrouck W48
        if (doc.getMicHasbLambdaW48() != null)           update.set("micHasbLambdaW48", doc.getMicHasbLambdaW48());
        if (doc.getMicHasbLambdaW48Zsc40() != null)      update.set("micHasbLambdaW48Zsc40", doc.getMicHasbLambdaW48Zsc40());
        if (doc.getMicHasbLambdaW48Ma20() != null)        update.set("micHasbLambdaW48Ma20", doc.getMicHasbLambdaW48Ma20());
        if (doc.getMicHasbLambdaW48Dvgc() != null)        update.set("micHasbLambdaW48Dvgc", doc.getMicHasbLambdaW48Dvgc());
        if (doc.getMicHasbLambdaW48SlpW20() != null)      update.set("micHasbLambdaW48SlpW20", doc.getMicHasbLambdaW48SlpW20());
        if (doc.getMicHasbLambdaW48Vol40() != null)        update.set("micHasbLambdaW48Vol40", doc.getMicHasbLambdaW48Vol40());
        if (doc.getMicHasbLambdaW48Stability40() != null)  update.set("micHasbLambdaW48Stability40", doc.getMicHasbLambdaW48Stability40());
        if (doc.getMicHasbLambdaW48PctileW40() != null)    update.set("micHasbLambdaW48PctileW40", doc.getMicHasbLambdaW48PctileW40());
        if (doc.getMicHasbLambdaW48Atrn() != null)        update.set("micHasbLambdaW48Atrn", doc.getMicHasbLambdaW48Atrn());
        // Hasbrouck W64
        if (doc.getMicHasbLambdaW64() != null)           update.set("micHasbLambdaW64", doc.getMicHasbLambdaW64());
        if (doc.getMicHasbLambdaW64Zsc40() != null)      update.set("micHasbLambdaW64Zsc40", doc.getMicHasbLambdaW64Zsc40());
        if (doc.getMicHasbLambdaW64Ma20() != null)        update.set("micHasbLambdaW64Ma20", doc.getMicHasbLambdaW64Ma20());
        if (doc.getMicHasbLambdaW64Dvgc() != null)        update.set("micHasbLambdaW64Dvgc", doc.getMicHasbLambdaW64Dvgc());
        if (doc.getMicHasbLambdaW64SlpW20() != null)      update.set("micHasbLambdaW64SlpW20", doc.getMicHasbLambdaW64SlpW20());
        if (doc.getMicHasbLambdaW64Vol40() != null)        update.set("micHasbLambdaW64Vol40", doc.getMicHasbLambdaW64Vol40());
        if (doc.getMicHasbLambdaW64Stability40() != null)  update.set("micHasbLambdaW64Stability40", doc.getMicHasbLambdaW64Stability40());
        if (doc.getMicHasbLambdaW64PctileW40() != null)    update.set("micHasbLambdaW64PctileW40", doc.getMicHasbLambdaW64PctileW40());
        if (doc.getMicHasbLambdaW64Atrn() != null)        update.set("micHasbLambdaW64Atrn", doc.getMicHasbLambdaW64Atrn());
        // Hasbrouck W288
        if (doc.getMicHasbLambdaW288() != null)           update.set("micHasbLambdaW288", doc.getMicHasbLambdaW288());
        if (doc.getMicHasbLambdaW288Zsc40() != null)      update.set("micHasbLambdaW288Zsc40", doc.getMicHasbLambdaW288Zsc40());
        if (doc.getMicHasbLambdaW288Ma20() != null)        update.set("micHasbLambdaW288Ma20", doc.getMicHasbLambdaW288Ma20());
        if (doc.getMicHasbLambdaW288Dvgc() != null)        update.set("micHasbLambdaW288Dvgc", doc.getMicHasbLambdaW288Dvgc());
        if (doc.getMicHasbLambdaW288SlpW20() != null)      update.set("micHasbLambdaW288SlpW20", doc.getMicHasbLambdaW288SlpW20());
        if (doc.getMicHasbLambdaW288Vol40() != null)        update.set("micHasbLambdaW288Vol40", doc.getMicHasbLambdaW288Vol40());
        if (doc.getMicHasbLambdaW288Stability40() != null)  update.set("micHasbLambdaW288Stability40", doc.getMicHasbLambdaW288Stability40());
        if (doc.getMicHasbLambdaW288PctileW40() != null)    update.set("micHasbLambdaW288PctileW40", doc.getMicHasbLambdaW288PctileW40());
        if (doc.getMicHasbLambdaW288Atrn() != null)        update.set("micHasbLambdaW288Atrn", doc.getMicHasbLambdaW288Atrn());
        // Hasbrouck-to-Kyle ratios
        if (doc.getMicHasbToKyleRatioW16() != null)  update.set("micHasbToKyleRatioW16", doc.getMicHasbToKyleRatioW16());
        if (doc.getMicHasbToKyleRatioW48() != null)  update.set("micHasbToKyleRatioW48", doc.getMicHasbToKyleRatioW48());
        if (doc.getMicHasbToKyleRatioW64() != null)  update.set("micHasbToKyleRatioW64", doc.getMicHasbToKyleRatioW64());
        if (doc.getMicHasbToKyleRatioW288() != null) update.set("micHasbToKyleRatioW288", doc.getMicHasbToKyleRatioW288());

        // Kyle W4
        if (doc.getMicKyleLambdaW4() != null)           update.set("micKyleLambdaW4", doc.getMicKyleLambdaW4());
        if (doc.getMicKyleLambdaW4Atrn() != null)       update.set("micKyleLambdaW4Atrn", doc.getMicKyleLambdaW4Atrn());
        if (doc.getMicKyleLambdaW4Zsc20() != null)      update.set("micKyleLambdaW4Zsc20", doc.getMicKyleLambdaW4Zsc20());
        if (doc.getMicKyleLambdaW4Rel10() != null)      update.set("micKyleLambdaW4Rel10", doc.getMicKyleLambdaW4Rel10());
        if (doc.getMicKyleLambdaW4Rel40() != null)      update.set("micKyleLambdaW4Rel40", doc.getMicKyleLambdaW4Rel40());
        if (doc.getMicKyleLambdaW4SlpW4() != null)      update.set("micKyleLambdaW4SlpW4", doc.getMicKyleLambdaW4SlpW4());
        if (doc.getMicKyleLambdaW4SlpW20() != null)     update.set("micKyleLambdaW4SlpW20", doc.getMicKyleLambdaW4SlpW20());
        if (doc.getMicKyleLambdaW4SlpW50() != null)     update.set("micKyleLambdaW4SlpW50", doc.getMicKyleLambdaW4SlpW50());
        if (doc.getMicKyleLambdaW4AccW4() != null)      update.set("micKyleLambdaW4AccW4", doc.getMicKyleLambdaW4AccW4());
        if (doc.getMicKyleLambdaW4AccW5() != null)      update.set("micKyleLambdaW4AccW5", doc.getMicKyleLambdaW4AccW5());
        if (doc.getMicKyleLambdaW4AccW10() != null)     update.set("micKyleLambdaW4AccW10", doc.getMicKyleLambdaW4AccW10());
        if (doc.getMicKyleLambdaW4AccW16() != null)     update.set("micKyleLambdaW4AccW16", doc.getMicKyleLambdaW4AccW16());
        if (doc.getMicKyleLambdaW4Ma10() != null)       update.set("micKyleLambdaW4Ma10", doc.getMicKyleLambdaW4Ma10());
        if (doc.getMicKyleLambdaW4Ma20() != null)       update.set("micKyleLambdaW4Ma20", doc.getMicKyleLambdaW4Ma20());
        if (doc.getMicKyleLambdaW4Ma30() != null)       update.set("micKyleLambdaW4Ma30", doc.getMicKyleLambdaW4Ma30());
        if (doc.getMicKyleLambdaW4Vol10() != null)      update.set("micKyleLambdaW4Vol10", doc.getMicKyleLambdaW4Vol10());
        if (doc.getMicKyleLambdaW4Vol20() != null)      update.set("micKyleLambdaW4Vol20", doc.getMicKyleLambdaW4Vol20());
        if (doc.getMicKyleLambdaW4Vol40() != null)      update.set("micKyleLambdaW4Vol40", doc.getMicKyleLambdaW4Vol40());
        if (doc.getMicKyleLambdaW4VolRel40() != null)   update.set("micKyleLambdaW4VolRel40", doc.getMicKyleLambdaW4VolRel40());
        if (doc.getMicKyleLambdaW4PrstW10() != null)    update.set("micKyleLambdaW4PrstW10", doc.getMicKyleLambdaW4PrstW10());
        if (doc.getMicKyleLambdaW4PrstW20() != null)    update.set("micKyleLambdaW4PrstW20", doc.getMicKyleLambdaW4PrstW20());
        if (doc.getMicKyleLambdaW4PrstW40() != null)    update.set("micKyleLambdaW4PrstW40", doc.getMicKyleLambdaW4PrstW40());
        if (doc.getMicKyleLambdaW4Dvgc() != null)       update.set("micKyleLambdaW4Dvgc", doc.getMicKyleLambdaW4Dvgc());
        if (doc.getMicKyleLambdaW4PctileW20() != null)  update.set("micKyleLambdaW4PctileW20", doc.getMicKyleLambdaW4PctileW20());
        if (doc.getMicKyleLambdaW4Lrmr1040() != null)   update.set("micKyleLambdaW4Lrmr1040", doc.getMicKyleLambdaW4Lrmr1040());
        if (doc.getMicKyleLambdaW4Stability40() != null) update.set("micKyleLambdaW4Stability40", doc.getMicKyleLambdaW4Stability40());
        // Kyle W16
        if (doc.getMicKyleLambdaW16() != null)           update.set("micKyleLambdaW16", doc.getMicKyleLambdaW16());
        if (doc.getMicKyleLambdaW16Atrn() != null)       update.set("micKyleLambdaW16Atrn", doc.getMicKyleLambdaW16Atrn());
        if (doc.getMicKyleLambdaW16Zsc20() != null)      update.set("micKyleLambdaW16Zsc20", doc.getMicKyleLambdaW16Zsc20());
        if (doc.getMicKyleLambdaW16Rel10() != null)      update.set("micKyleLambdaW16Rel10", doc.getMicKyleLambdaW16Rel10());
        if (doc.getMicKyleLambdaW16Rel40() != null)      update.set("micKyleLambdaW16Rel40", doc.getMicKyleLambdaW16Rel40());
        if (doc.getMicKyleLambdaW16SlpW4() != null)      update.set("micKyleLambdaW16SlpW4", doc.getMicKyleLambdaW16SlpW4());
        if (doc.getMicKyleLambdaW16SlpW20() != null)     update.set("micKyleLambdaW16SlpW20", doc.getMicKyleLambdaW16SlpW20());
        if (doc.getMicKyleLambdaW16SlpW50() != null)     update.set("micKyleLambdaW16SlpW50", doc.getMicKyleLambdaW16SlpW50());
        if (doc.getMicKyleLambdaW16AccW4() != null)      update.set("micKyleLambdaW16AccW4", doc.getMicKyleLambdaW16AccW4());
        if (doc.getMicKyleLambdaW16AccW5() != null)      update.set("micKyleLambdaW16AccW5", doc.getMicKyleLambdaW16AccW5());
        if (doc.getMicKyleLambdaW16AccW10() != null)     update.set("micKyleLambdaW16AccW10", doc.getMicKyleLambdaW16AccW10());
        if (doc.getMicKyleLambdaW16AccW16() != null)     update.set("micKyleLambdaW16AccW16", doc.getMicKyleLambdaW16AccW16());
        if (doc.getMicKyleLambdaW16Ma10() != null)       update.set("micKyleLambdaW16Ma10", doc.getMicKyleLambdaW16Ma10());
        if (doc.getMicKyleLambdaW16Ma20() != null)       update.set("micKyleLambdaW16Ma20", doc.getMicKyleLambdaW16Ma20());
        if (doc.getMicKyleLambdaW16Ma30() != null)       update.set("micKyleLambdaW16Ma30", doc.getMicKyleLambdaW16Ma30());
        if (doc.getMicKyleLambdaW16Vol10() != null)      update.set("micKyleLambdaW16Vol10", doc.getMicKyleLambdaW16Vol10());
        if (doc.getMicKyleLambdaW16Vol20() != null)      update.set("micKyleLambdaW16Vol20", doc.getMicKyleLambdaW16Vol20());
        if (doc.getMicKyleLambdaW16Vol40() != null)      update.set("micKyleLambdaW16Vol40", doc.getMicKyleLambdaW16Vol40());
        if (doc.getMicKyleLambdaW16VolRel40() != null)   update.set("micKyleLambdaW16VolRel40", doc.getMicKyleLambdaW16VolRel40());
        if (doc.getMicKyleLambdaW16PrstW10() != null)    update.set("micKyleLambdaW16PrstW10", doc.getMicKyleLambdaW16PrstW10());
        if (doc.getMicKyleLambdaW16PrstW20() != null)    update.set("micKyleLambdaW16PrstW20", doc.getMicKyleLambdaW16PrstW20());
        if (doc.getMicKyleLambdaW16PrstW40() != null)    update.set("micKyleLambdaW16PrstW40", doc.getMicKyleLambdaW16PrstW40());
        if (doc.getMicKyleLambdaW16Dvgc() != null)       update.set("micKyleLambdaW16Dvgc", doc.getMicKyleLambdaW16Dvgc());
        if (doc.getMicKyleLambdaW16PctileW20() != null)  update.set("micKyleLambdaW16PctileW20", doc.getMicKyleLambdaW16PctileW20());
        if (doc.getMicKyleLambdaW16Lrmr1040() != null)   update.set("micKyleLambdaW16Lrmr1040", doc.getMicKyleLambdaW16Lrmr1040());
        if (doc.getMicKyleLambdaW16Stability40() != null) update.set("micKyleLambdaW16Stability40", doc.getMicKyleLambdaW16Stability40());
        // Kyle W48
        if (doc.getMicKyleLambdaW48() != null)           update.set("micKyleLambdaW48", doc.getMicKyleLambdaW48());
        if (doc.getMicKyleLambdaW48Atrn() != null)       update.set("micKyleLambdaW48Atrn", doc.getMicKyleLambdaW48Atrn());
        if (doc.getMicKyleLambdaW48Zsc20() != null)      update.set("micKyleLambdaW48Zsc20", doc.getMicKyleLambdaW48Zsc20());
        if (doc.getMicKyleLambdaW48Rel10() != null)      update.set("micKyleLambdaW48Rel10", doc.getMicKyleLambdaW48Rel10());
        if (doc.getMicKyleLambdaW48Rel40() != null)      update.set("micKyleLambdaW48Rel40", doc.getMicKyleLambdaW48Rel40());
        if (doc.getMicKyleLambdaW48SlpW4() != null)      update.set("micKyleLambdaW48SlpW4", doc.getMicKyleLambdaW48SlpW4());
        if (doc.getMicKyleLambdaW48SlpW20() != null)     update.set("micKyleLambdaW48SlpW20", doc.getMicKyleLambdaW48SlpW20());
        if (doc.getMicKyleLambdaW48SlpW50() != null)     update.set("micKyleLambdaW48SlpW50", doc.getMicKyleLambdaW48SlpW50());
        if (doc.getMicKyleLambdaW48AccW4() != null)      update.set("micKyleLambdaW48AccW4", doc.getMicKyleLambdaW48AccW4());
        if (doc.getMicKyleLambdaW48AccW5() != null)      update.set("micKyleLambdaW48AccW5", doc.getMicKyleLambdaW48AccW5());
        if (doc.getMicKyleLambdaW48AccW10() != null)     update.set("micKyleLambdaW48AccW10", doc.getMicKyleLambdaW48AccW10());
        if (doc.getMicKyleLambdaW48AccW16() != null)     update.set("micKyleLambdaW48AccW16", doc.getMicKyleLambdaW48AccW16());
        if (doc.getMicKyleLambdaW48Ma10() != null)       update.set("micKyleLambdaW48Ma10", doc.getMicKyleLambdaW48Ma10());
        if (doc.getMicKyleLambdaW48Ma20() != null)       update.set("micKyleLambdaW48Ma20", doc.getMicKyleLambdaW48Ma20());
        if (doc.getMicKyleLambdaW48Ma30() != null)       update.set("micKyleLambdaW48Ma30", doc.getMicKyleLambdaW48Ma30());
        if (doc.getMicKyleLambdaW48Vol10() != null)      update.set("micKyleLambdaW48Vol10", doc.getMicKyleLambdaW48Vol10());
        if (doc.getMicKyleLambdaW48Vol20() != null)      update.set("micKyleLambdaW48Vol20", doc.getMicKyleLambdaW48Vol20());
        if (doc.getMicKyleLambdaW48Vol40() != null)      update.set("micKyleLambdaW48Vol40", doc.getMicKyleLambdaW48Vol40());
        if (doc.getMicKyleLambdaW48VolRel40() != null)   update.set("micKyleLambdaW48VolRel40", doc.getMicKyleLambdaW48VolRel40());
        if (doc.getMicKyleLambdaW48PrstW10() != null)    update.set("micKyleLambdaW48PrstW10", doc.getMicKyleLambdaW48PrstW10());
        if (doc.getMicKyleLambdaW48PrstW20() != null)    update.set("micKyleLambdaW48PrstW20", doc.getMicKyleLambdaW48PrstW20());
        if (doc.getMicKyleLambdaW48PrstW40() != null)    update.set("micKyleLambdaW48PrstW40", doc.getMicKyleLambdaW48PrstW40());
        if (doc.getMicKyleLambdaW48Dvgc() != null)       update.set("micKyleLambdaW48Dvgc", doc.getMicKyleLambdaW48Dvgc());
        if (doc.getMicKyleLambdaW48PctileW20() != null)  update.set("micKyleLambdaW48PctileW20", doc.getMicKyleLambdaW48PctileW20());
        if (doc.getMicKyleLambdaW48Lrmr1040() != null)   update.set("micKyleLambdaW48Lrmr1040", doc.getMicKyleLambdaW48Lrmr1040());
        if (doc.getMicKyleLambdaW48Stability40() != null) update.set("micKyleLambdaW48Stability40", doc.getMicKyleLambdaW48Stability40());
        // Kyle W96
        if (doc.getMicKyleLambdaW96() != null)           update.set("micKyleLambdaW96", doc.getMicKyleLambdaW96());
        if (doc.getMicKyleLambdaW96Atrn() != null)       update.set("micKyleLambdaW96Atrn", doc.getMicKyleLambdaW96Atrn());
        if (doc.getMicKyleLambdaW96Zsc20() != null)      update.set("micKyleLambdaW96Zsc20", doc.getMicKyleLambdaW96Zsc20());
        if (doc.getMicKyleLambdaW96Rel10() != null)      update.set("micKyleLambdaW96Rel10", doc.getMicKyleLambdaW96Rel10());
        if (doc.getMicKyleLambdaW96Rel40() != null)      update.set("micKyleLambdaW96Rel40", doc.getMicKyleLambdaW96Rel40());
        if (doc.getMicKyleLambdaW96SlpW4() != null)      update.set("micKyleLambdaW96SlpW4", doc.getMicKyleLambdaW96SlpW4());
        if (doc.getMicKyleLambdaW96SlpW20() != null)     update.set("micKyleLambdaW96SlpW20", doc.getMicKyleLambdaW96SlpW20());
        if (doc.getMicKyleLambdaW96SlpW50() != null)     update.set("micKyleLambdaW96SlpW50", doc.getMicKyleLambdaW96SlpW50());
        if (doc.getMicKyleLambdaW96AccW4() != null)      update.set("micKyleLambdaW96AccW4", doc.getMicKyleLambdaW96AccW4());
        if (doc.getMicKyleLambdaW96AccW5() != null)      update.set("micKyleLambdaW96AccW5", doc.getMicKyleLambdaW96AccW5());
        if (doc.getMicKyleLambdaW96AccW10() != null)     update.set("micKyleLambdaW96AccW10", doc.getMicKyleLambdaW96AccW10());
        if (doc.getMicKyleLambdaW96AccW16() != null)     update.set("micKyleLambdaW96AccW16", doc.getMicKyleLambdaW96AccW16());
        if (doc.getMicKyleLambdaW96Ma10() != null)       update.set("micKyleLambdaW96Ma10", doc.getMicKyleLambdaW96Ma10());
        if (doc.getMicKyleLambdaW96Ma20() != null)       update.set("micKyleLambdaW96Ma20", doc.getMicKyleLambdaW96Ma20());
        if (doc.getMicKyleLambdaW96Ma30() != null)       update.set("micKyleLambdaW96Ma30", doc.getMicKyleLambdaW96Ma30());
        if (doc.getMicKyleLambdaW96Vol10() != null)      update.set("micKyleLambdaW96Vol10", doc.getMicKyleLambdaW96Vol10());
        if (doc.getMicKyleLambdaW96Vol20() != null)      update.set("micKyleLambdaW96Vol20", doc.getMicKyleLambdaW96Vol20());
        if (doc.getMicKyleLambdaW96Vol40() != null)      update.set("micKyleLambdaW96Vol40", doc.getMicKyleLambdaW96Vol40());
        if (doc.getMicKyleLambdaW96VolRel40() != null)   update.set("micKyleLambdaW96VolRel40", doc.getMicKyleLambdaW96VolRel40());
        if (doc.getMicKyleLambdaW96PrstW10() != null)    update.set("micKyleLambdaW96PrstW10", doc.getMicKyleLambdaW96PrstW10());
        if (doc.getMicKyleLambdaW96PrstW20() != null)    update.set("micKyleLambdaW96PrstW20", doc.getMicKyleLambdaW96PrstW20());
        if (doc.getMicKyleLambdaW96PrstW40() != null)    update.set("micKyleLambdaW96PrstW40", doc.getMicKyleLambdaW96PrstW40());
        if (doc.getMicKyleLambdaW96Dvgc() != null)       update.set("micKyleLambdaW96Dvgc", doc.getMicKyleLambdaW96Dvgc());
        if (doc.getMicKyleLambdaW96PctileW20() != null)  update.set("micKyleLambdaW96PctileW20", doc.getMicKyleLambdaW96PctileW20());
        if (doc.getMicKyleLambdaW96Lrmr1040() != null)   update.set("micKyleLambdaW96Lrmr1040", doc.getMicKyleLambdaW96Lrmr1040());
        if (doc.getMicKyleLambdaW96Stability40() != null) update.set("micKyleLambdaW96Stability40", doc.getMicKyleLambdaW96Stability40());
        // Kyle W200
        if (doc.getMicKyleLambdaW200() != null)           update.set("micKyleLambdaW200", doc.getMicKyleLambdaW200());
        if (doc.getMicKyleLambdaW200Atrn() != null)       update.set("micKyleLambdaW200Atrn", doc.getMicKyleLambdaW200Atrn());
        if (doc.getMicKyleLambdaW200Zsc20() != null)      update.set("micKyleLambdaW200Zsc20", doc.getMicKyleLambdaW200Zsc20());
        if (doc.getMicKyleLambdaW200Rel10() != null)      update.set("micKyleLambdaW200Rel10", doc.getMicKyleLambdaW200Rel10());
        if (doc.getMicKyleLambdaW200Rel40() != null)      update.set("micKyleLambdaW200Rel40", doc.getMicKyleLambdaW200Rel40());
        if (doc.getMicKyleLambdaW200SlpW4() != null)      update.set("micKyleLambdaW200SlpW4", doc.getMicKyleLambdaW200SlpW4());
        if (doc.getMicKyleLambdaW200SlpW20() != null)     update.set("micKyleLambdaW200SlpW20", doc.getMicKyleLambdaW200SlpW20());
        if (doc.getMicKyleLambdaW200SlpW50() != null)     update.set("micKyleLambdaW200SlpW50", doc.getMicKyleLambdaW200SlpW50());
        if (doc.getMicKyleLambdaW200AccW4() != null)      update.set("micKyleLambdaW200AccW4", doc.getMicKyleLambdaW200AccW4());
        if (doc.getMicKyleLambdaW200AccW5() != null)      update.set("micKyleLambdaW200AccW5", doc.getMicKyleLambdaW200AccW5());
        if (doc.getMicKyleLambdaW200AccW10() != null)     update.set("micKyleLambdaW200AccW10", doc.getMicKyleLambdaW200AccW10());
        if (doc.getMicKyleLambdaW200AccW16() != null)     update.set("micKyleLambdaW200AccW16", doc.getMicKyleLambdaW200AccW16());
        if (doc.getMicKyleLambdaW200Ma10() != null)       update.set("micKyleLambdaW200Ma10", doc.getMicKyleLambdaW200Ma10());
        if (doc.getMicKyleLambdaW200Ma20() != null)       update.set("micKyleLambdaW200Ma20", doc.getMicKyleLambdaW200Ma20());
        if (doc.getMicKyleLambdaW200Ma30() != null)       update.set("micKyleLambdaW200Ma30", doc.getMicKyleLambdaW200Ma30());
        if (doc.getMicKyleLambdaW200Vol10() != null)      update.set("micKyleLambdaW200Vol10", doc.getMicKyleLambdaW200Vol10());
        if (doc.getMicKyleLambdaW200Vol20() != null)      update.set("micKyleLambdaW200Vol20", doc.getMicKyleLambdaW200Vol20());
        if (doc.getMicKyleLambdaW200Vol40() != null)      update.set("micKyleLambdaW200Vol40", doc.getMicKyleLambdaW200Vol40());
        if (doc.getMicKyleLambdaW200VolRel40() != null)   update.set("micKyleLambdaW200VolRel40", doc.getMicKyleLambdaW200VolRel40());
        if (doc.getMicKyleLambdaW200PrstW10() != null)    update.set("micKyleLambdaW200PrstW10", doc.getMicKyleLambdaW200PrstW10());
        if (doc.getMicKyleLambdaW200PrstW20() != null)    update.set("micKyleLambdaW200PrstW20", doc.getMicKyleLambdaW200PrstW20());
        if (doc.getMicKyleLambdaW200PrstW40() != null)    update.set("micKyleLambdaW200PrstW40", doc.getMicKyleLambdaW200PrstW40());
        if (doc.getMicKyleLambdaW200Dvgc() != null)       update.set("micKyleLambdaW200Dvgc", doc.getMicKyleLambdaW200Dvgc());
        if (doc.getMicKyleLambdaW200PctileW20() != null)  update.set("micKyleLambdaW200PctileW20", doc.getMicKyleLambdaW200PctileW20());
        if (doc.getMicKyleLambdaW200Lrmr1040() != null)   update.set("micKyleLambdaW200Lrmr1040", doc.getMicKyleLambdaW200Lrmr1040());
        if (doc.getMicKyleLambdaW200Stability40() != null) update.set("micKyleLambdaW200Stability40", doc.getMicKyleLambdaW200Stability40());
        // Kyle W288
        if (doc.getMicKyleLambdaW288() != null)           update.set("micKyleLambdaW288", doc.getMicKyleLambdaW288());
        if (doc.getMicKyleLambdaW288Atrn() != null)       update.set("micKyleLambdaW288Atrn", doc.getMicKyleLambdaW288Atrn());
        if (doc.getMicKyleLambdaW288Zsc20() != null)      update.set("micKyleLambdaW288Zsc20", doc.getMicKyleLambdaW288Zsc20());
        if (doc.getMicKyleLambdaW288Rel10() != null)      update.set("micKyleLambdaW288Rel10", doc.getMicKyleLambdaW288Rel10());
        if (doc.getMicKyleLambdaW288Rel40() != null)      update.set("micKyleLambdaW288Rel40", doc.getMicKyleLambdaW288Rel40());
        if (doc.getMicKyleLambdaW288SlpW4() != null)      update.set("micKyleLambdaW288SlpW4", doc.getMicKyleLambdaW288SlpW4());
        if (doc.getMicKyleLambdaW288SlpW20() != null)     update.set("micKyleLambdaW288SlpW20", doc.getMicKyleLambdaW288SlpW20());
        if (doc.getMicKyleLambdaW288SlpW50() != null)     update.set("micKyleLambdaW288SlpW50", doc.getMicKyleLambdaW288SlpW50());
        if (doc.getMicKyleLambdaW288AccW4() != null)      update.set("micKyleLambdaW288AccW4", doc.getMicKyleLambdaW288AccW4());
        if (doc.getMicKyleLambdaW288AccW5() != null)      update.set("micKyleLambdaW288AccW5", doc.getMicKyleLambdaW288AccW5());
        if (doc.getMicKyleLambdaW288AccW10() != null)     update.set("micKyleLambdaW288AccW10", doc.getMicKyleLambdaW288AccW10());
        if (doc.getMicKyleLambdaW288AccW16() != null)     update.set("micKyleLambdaW288AccW16", doc.getMicKyleLambdaW288AccW16());
        if (doc.getMicKyleLambdaW288Ma10() != null)       update.set("micKyleLambdaW288Ma10", doc.getMicKyleLambdaW288Ma10());
        if (doc.getMicKyleLambdaW288Ma20() != null)       update.set("micKyleLambdaW288Ma20", doc.getMicKyleLambdaW288Ma20());
        if (doc.getMicKyleLambdaW288Ma30() != null)       update.set("micKyleLambdaW288Ma30", doc.getMicKyleLambdaW288Ma30());
        if (doc.getMicKyleLambdaW288Vol10() != null)      update.set("micKyleLambdaW288Vol10", doc.getMicKyleLambdaW288Vol10());
        if (doc.getMicKyleLambdaW288Vol20() != null)      update.set("micKyleLambdaW288Vol20", doc.getMicKyleLambdaW288Vol20());
        if (doc.getMicKyleLambdaW288Vol40() != null)      update.set("micKyleLambdaW288Vol40", doc.getMicKyleLambdaW288Vol40());
        if (doc.getMicKyleLambdaW288VolRel40() != null)   update.set("micKyleLambdaW288VolRel40", doc.getMicKyleLambdaW288VolRel40());
        if (doc.getMicKyleLambdaW288PrstW10() != null)    update.set("micKyleLambdaW288PrstW10", doc.getMicKyleLambdaW288PrstW10());
        if (doc.getMicKyleLambdaW288PrstW20() != null)    update.set("micKyleLambdaW288PrstW20", doc.getMicKyleLambdaW288PrstW20());
        if (doc.getMicKyleLambdaW288PrstW40() != null)    update.set("micKyleLambdaW288PrstW40", doc.getMicKyleLambdaW288PrstW40());
        if (doc.getMicKyleLambdaW288Dvgc() != null)       update.set("micKyleLambdaW288Dvgc", doc.getMicKyleLambdaW288Dvgc());
        if (doc.getMicKyleLambdaW288PctileW20() != null)  update.set("micKyleLambdaW288PctileW20", doc.getMicKyleLambdaW288PctileW20());
        if (doc.getMicKyleLambdaW288Lrmr1040() != null)   update.set("micKyleLambdaW288Lrmr1040", doc.getMicKyleLambdaW288Lrmr1040());
        if (doc.getMicKyleLambdaW288Stability40() != null) update.set("micKyleLambdaW288Stability40", doc.getMicKyleLambdaW288Stability40());
        // Kyle signed
        if (doc.getMicKyleLambdaSigned() != null) update.set("micKyleLambdaSigned", doc.getMicKyleLambdaSigned());

        // PositionBalance
        if (doc.getMicCloseOpenRatio() != null)          update.set("micCloseOpenRatio", doc.getMicCloseOpenRatio());
        if (doc.getMicCloseOpenNorm() != null)           update.set("micCloseOpenNorm", doc.getMicCloseOpenNorm());
        if (doc.getMicCloseToHighNorm() != null)         update.set("micCloseToHighNorm", doc.getMicCloseToHighNorm());
        if (doc.getMicCloseToLowNorm() != null)          update.set("micCloseToLowNorm", doc.getMicCloseToLowNorm());
        if (doc.getMicClosePosNorm() != null)            update.set("micClosePosNorm", doc.getMicClosePosNorm());
        if (doc.getMicCandleClosePosNorm() != null)      update.set("micCandleClosePosNorm", doc.getMicCandleClosePosNorm());
        if (doc.getMicCloseHlc3Delta() != null)          update.set("micCloseHlc3Delta", doc.getMicCloseHlc3Delta());
        if (doc.getMicCloseVwapDelta() != null)          update.set("micCloseVwapDelta", doc.getMicCloseVwapDelta());
        if (doc.getMicCloseHlc3Atrn() != null)           update.set("micCloseHlc3Atrn", doc.getMicCloseHlc3Atrn());
        if (doc.getMicCloseVwapAtrn() != null)           update.set("micCloseVwapAtrn", doc.getMicCloseVwapAtrn());
        if (doc.getMicCandleBalanceScore() != null)      update.set("micCandleBalanceScore", doc.getMicCandleBalanceScore());
        if (doc.getMicClosePosZscore20() != null)        update.set("micClosePosZscore20", doc.getMicClosePosZscore20());
        if (doc.getMicCandleBalanceZscore20() != null)   update.set("micCandleBalanceZscore20", doc.getMicCandleBalanceZscore20());
        if (doc.getMicClosePosMa10() != null)            update.set("micClosePosMa10", doc.getMicClosePosMa10());
        if (doc.getMicClosePosVol10() != null)           update.set("micClosePosVol10", doc.getMicClosePosVol10());
        if (doc.getMicCloseOpenRatioMa10() != null)      update.set("micCloseOpenRatioMa10", doc.getMicCloseOpenRatioMa10());
        if (doc.getMicCloseOpenRatioVol10() != null)     update.set("micCloseOpenRatioVol10", doc.getMicCloseOpenRatioVol10());
        if (doc.getMicCandleBalanceMa10() != null)       update.set("micCandleBalanceMa10", doc.getMicCandleBalanceMa10());
        if (doc.getMicCandleBalanceVol10() != null)      update.set("micCandleBalanceVol10", doc.getMicCandleBalanceVol10());
        if (doc.getMicBalanceState() != null)            update.set("micBalanceState", doc.getMicBalanceState());
        if (doc.getMicCloseTriangleScoreAtrn() != null)  update.set("micCloseTriangleScoreAtrn", doc.getMicCloseTriangleScoreAtrn());
        if (doc.getMicCloseTriangleScore() != null)      update.set("micCloseTriangleScore", doc.getMicCloseTriangleScore());

        // Range
        if (doc.getMicRange() != null)                   update.set("micRange", doc.getMicRange());
        if (doc.getMicTrueRange() != null)               update.set("micTrueRange", doc.getMicTrueRange());
        if (doc.getMicHlc3() != null)                    update.set("micHlc3", doc.getMicHlc3());
        if (doc.getMicLogRange() != null)                update.set("micLogRange", doc.getMicLogRange());
        if (doc.getMicRangeSlpW10() != null)             update.set("micRangeSlpW10", doc.getMicRangeSlpW10());
        if (doc.getMicRangeSlpW20() != null)             update.set("micRangeSlpW20", doc.getMicRangeSlpW20());
        if (doc.getMicRangeAccW5() != null)              update.set("micRangeAccW5", doc.getMicRangeAccW5());
        if (doc.getMicRangeAccW10() != null)             update.set("micRangeAccW10", doc.getMicRangeAccW10());
        if (doc.getMicRangeMa10() != null)               update.set("micRangeMa10", doc.getMicRangeMa10());
        if (doc.getMicRangeMa20() != null)               update.set("micRangeMa20", doc.getMicRangeMa20());
        if (doc.getMicRangeMa30() != null)               update.set("micRangeMa30", doc.getMicRangeMa30());
        if (doc.getMicRangeVol10() != null)              update.set("micRangeVol10", doc.getMicRangeVol10());
        if (doc.getMicRangeVol20() != null)              update.set("micRangeVol20", doc.getMicRangeVol20());
        if (doc.getMicRangeCompressionW20() != null)     update.set("micRangeCompressionW20", doc.getMicRangeCompressionW20());
        if (doc.getMicCandleBrr() != null)               update.set("micCandleBrr", doc.getMicCandleBrr());
        if (doc.getMicCandleRange() != null)             update.set("micCandleRange", doc.getMicCandleRange());
        if (doc.getMicCandleVolatilityInside() != null)  update.set("micCandleVolatilityInside", doc.getMicCandleVolatilityInside());
        if (doc.getMicCandleSpreadRatio() != null)       update.set("micCandleSpreadRatio", doc.getMicCandleSpreadRatio());
        if (doc.getMicCandleLmr() != null)               update.set("micCandleLmr", doc.getMicCandleLmr());
        if (doc.getMicRangeReturn() != null)             update.set("micRangeReturn", doc.getMicRangeReturn());
        if (doc.getMicHighReturn() != null)              update.set("micHighReturn", doc.getMicHighReturn());
        if (doc.getMicLowReturn() != null)               update.set("micLowReturn", doc.getMicLowReturn());
        if (doc.getMicExtremeRangeReturn() != null)      update.set("micExtremeRangeReturn", doc.getMicExtremeRangeReturn());
        if (doc.getMicRangeAtrn() != null)               update.set("micRangeAtrn", doc.getMicRangeAtrn());
        if (doc.getMicTrAtrn() != null)                  update.set("micTrAtrn", doc.getMicTrAtrn());
        if (doc.getMicRangeStdn() != null)               update.set("micRangeStdn", doc.getMicRangeStdn());
        if (doc.getMicRangeAtrRatio() != null)           update.set("micRangeAtrRatio", doc.getMicRangeAtrRatio());
        if (doc.getMicRangeAsymmetry() != null)          update.set("micRangeAsymmetry", doc.getMicRangeAsymmetry());
        if (doc.getMicRangeHeadroomAtr() != null)        update.set("micRangeHeadroomAtr", doc.getMicRangeHeadroomAtr());
        if (doc.getMicHlc3Ma10() != null)                update.set("micHlc3Ma10", doc.getMicHlc3Ma10());
        if (doc.getMicHlc3Ma20() != null)                update.set("micHlc3Ma20", doc.getMicHlc3Ma20());
        if (doc.getMicHlc3SlpW20() != null)              update.set("micHlc3SlpW20", doc.getMicHlc3SlpW20());
        if (doc.getMicHlc3Vol10() != null)               update.set("micHlc3Vol10", doc.getMicHlc3Vol10());
        if (doc.getMicLogRangeMa10() != null)            update.set("micLogRangeMa10", doc.getMicLogRangeMa10());
        if (doc.getMicLogRangeSlpW20() != null)          update.set("micLogRangeSlpW20", doc.getMicLogRangeSlpW20());
        if (doc.getMicLogRangeVol10() != null)           update.set("micLogRangeVol10", doc.getMicLogRangeVol10());
        if (doc.getMicRangeSqueezeW20() != null)         update.set("micRangeSqueezeW20", doc.getMicRangeSqueezeW20());
        if (doc.getMicGapRatio() != null)                update.set("micGapRatio", doc.getMicGapRatio());
        if (doc.getMicTrRangeRatio() != null)            update.set("micTrRangeRatio", doc.getMicTrRangeRatio());
        if (doc.getMicLogRangePctileW48() != null)       update.set("micLogRangePctileW48", doc.getMicLogRangePctileW48());
        if (doc.getMicRangeRegimeState() != null)        update.set("micRangeRegimeState", doc.getMicRangeRegimeState());

        // Return1C
        if (doc.getMicReturn() != null)                  update.set("micReturn", doc.getMicReturn());
        if (doc.getMicReturnLog() != null)               update.set("micReturnLog", doc.getMicReturnLog());
        if (doc.getMicReturnDirection() != null)          update.set("micReturnDirection", doc.getMicReturnDirection());
        if (doc.getMicReturnAbsoluteStrength() != null)   update.set("micReturnAbsoluteStrength", doc.getMicReturnAbsoluteStrength());
        if (doc.getMicReturnAcceleration() != null)       update.set("micReturnAcceleration", doc.getMicReturnAcceleration());
        if (doc.getMicReturnReversalForce() != null)      update.set("micReturnReversalForce", doc.getMicReturnReversalForce());
        if (doc.getMicReturnDominanceRatio() != null)     update.set("micReturnDominanceRatio", doc.getMicReturnDominanceRatio());
        if (doc.getMicRvr() != null)                     update.set("micRvr", doc.getMicRvr());
        if (doc.getMicLogReturnDominance() != null)       update.set("micLogReturnDominance", doc.getMicLogReturnDominance());
        if (doc.getMicReturnTrDominance() != null)        update.set("micReturnTrDominance", doc.getMicReturnTrDominance());
        if (doc.getMicReturnGapPressure() != null)        update.set("micReturnGapPressure", doc.getMicReturnGapPressure());
        if (doc.getMicReturnSignPrstW20() != null)        update.set("micReturnSignPrstW20", doc.getMicReturnSignPrstW20());
        if (doc.getMicReturnRunLen() != null)             update.set("micReturnRunLen", doc.getMicReturnRunLen());
        if (doc.getMicReturnAtrn() != null)              update.set("micReturnAtrn", doc.getMicReturnAtrn());
        if (doc.getMicReturnStdn() != null)              update.set("micReturnStdn", doc.getMicReturnStdn());

        // ReturnWindow
        if (doc.getMicReturnZscore5() != null)           update.set("micReturnZscore5", doc.getMicReturnZscore5());
        if (doc.getMicReturnZscore14() != null)          update.set("micReturnZscore14", doc.getMicReturnZscore14());
        if (doc.getMicReturnStdnW96() != null)           update.set("micReturnStdnW96", doc.getMicReturnStdnW96());
        if (doc.getMicReturnStdnW48() != null)           update.set("micReturnStdnW48", doc.getMicReturnStdnW48());
        if (doc.getMicReturnStdnW288() != null)          update.set("micReturnStdnW288", doc.getMicReturnStdnW288());
        if (doc.getMicReturnPctl20() != null)            update.set("micReturnPctl20", doc.getMicReturnPctl20());
        if (doc.getMicReturnPctl50() != null)            update.set("micReturnPctl50", doc.getMicReturnPctl50());
        if (doc.getMicReturnSkew() != null)              update.set("micReturnSkew", doc.getMicReturnSkew());
        if (doc.getMicReturnKurtosis() != null)          update.set("micReturnKurtosis", doc.getMicReturnKurtosis());
        if (doc.getMicReturnStdRolling() != null)        update.set("micReturnStdRolling", doc.getMicReturnStdRolling());
        if (doc.getMicReturnSmoothness() != null)        update.set("micReturnSmoothness", doc.getMicReturnSmoothness());
        if (doc.getMicReturnRsp() != null)               update.set("micReturnRsp", doc.getMicReturnRsp());
        if (doc.getMicReturnRds() != null)               update.set("micReturnRds", doc.getMicReturnRds());
        if (doc.getMicReturnRnr() != null)               update.set("micReturnRnr", doc.getMicReturnRnr());
        if (doc.getMicReturnFlipRateW20() != null)       update.set("micReturnFlipRateW20", doc.getMicReturnFlipRateW20());
        if (doc.getMicReturnAutocorr1W20() != null)      update.set("micReturnAutocorr1W20", doc.getMicReturnAutocorr1W20());

        // ── Roll ──────────────────────────────────────────────────────────────
        // Roll W16
        if (doc.getMicRollCovW16() != null)              update.set("micRollCovW16", doc.getMicRollCovW16());
        if (doc.getMicRollCovPctW16() != null)           update.set("micRollCovPctW16", doc.getMicRollCovPctW16());
        if (doc.getMicRollCovZscW16() != null)           update.set("micRollCovZscW16", doc.getMicRollCovZscW16());
        if (doc.getMicRollCovPctZscW16() != null)        update.set("micRollCovPctZscW16", doc.getMicRollCovPctZscW16());
        if (doc.getMicRollSpreadW16() != null)           update.set("micRollSpreadW16", doc.getMicRollSpreadW16());
        if (doc.getMicRollSpreadPctW16() != null)        update.set("micRollSpreadPctW16", doc.getMicRollSpreadPctW16());
        if (doc.getMicRollSpreadZscW16() != null)        update.set("micRollSpreadZscW16", doc.getMicRollSpreadZscW16());
        if (doc.getMicRollSpreadPctZscW16() != null)     update.set("micRollSpreadPctZscW16", doc.getMicRollSpreadPctZscW16());
        if (doc.getMicRollSpreadMaW16() != null)         update.set("micRollSpreadMaW16", doc.getMicRollSpreadMaW16());
        if (doc.getMicRollSpreadPrstW16() != null)       update.set("micRollSpreadPrstW16", doc.getMicRollSpreadPrstW16());
        if (doc.getMicRollSpreadPctileW16() != null)     update.set("micRollSpreadPctileW16", doc.getMicRollSpreadPctileW16());
        if (doc.getMicRollSpreadAccW16() != null)        update.set("micRollSpreadAccW16", doc.getMicRollSpreadAccW16());
        if (doc.getMicRollSpreadSlpW16() != null)        update.set("micRollSpreadSlpW16", doc.getMicRollSpreadSlpW16());
        if (doc.getMicRollSpreadVolW16() != null)        update.set("micRollSpreadVolW16", doc.getMicRollSpreadVolW16());
        if (doc.getMicRollSpreadDvgcW16() != null)       update.set("micRollSpreadDvgcW16", doc.getMicRollSpreadDvgcW16());
        if (doc.getMicRollSpreadAtrn14W16() != null)     update.set("micRollSpreadAtrn14W16", doc.getMicRollSpreadAtrn14W16());
        if (doc.getMicRollSpreadPctAtrn14W16() != null)  update.set("micRollSpreadPctAtrn14W16", doc.getMicRollSpreadPctAtrn14W16());
        // Roll W32
        if (doc.getMicRollCovW32() != null)              update.set("micRollCovW32", doc.getMicRollCovW32());
        if (doc.getMicRollCovPctW32() != null)           update.set("micRollCovPctW32", doc.getMicRollCovPctW32());
        if (doc.getMicRollCovZscW32() != null)           update.set("micRollCovZscW32", doc.getMicRollCovZscW32());
        if (doc.getMicRollCovPctZscW32() != null)        update.set("micRollCovPctZscW32", doc.getMicRollCovPctZscW32());
        if (doc.getMicRollSpreadW32() != null)           update.set("micRollSpreadW32", doc.getMicRollSpreadW32());
        if (doc.getMicRollSpreadPctW32() != null)        update.set("micRollSpreadPctW32", doc.getMicRollSpreadPctW32());
        if (doc.getMicRollSpreadZscW32() != null)        update.set("micRollSpreadZscW32", doc.getMicRollSpreadZscW32());
        if (doc.getMicRollSpreadPctZscW32() != null)     update.set("micRollSpreadPctZscW32", doc.getMicRollSpreadPctZscW32());
        if (doc.getMicRollSpreadMaW32() != null)         update.set("micRollSpreadMaW32", doc.getMicRollSpreadMaW32());
        if (doc.getMicRollSpreadPrstW32() != null)       update.set("micRollSpreadPrstW32", doc.getMicRollSpreadPrstW32());
        if (doc.getMicRollSpreadPctileW32() != null)     update.set("micRollSpreadPctileW32", doc.getMicRollSpreadPctileW32());
        if (doc.getMicRollSpreadAccW32() != null)        update.set("micRollSpreadAccW32", doc.getMicRollSpreadAccW32());
        if (doc.getMicRollSpreadSlpW32() != null)        update.set("micRollSpreadSlpW32", doc.getMicRollSpreadSlpW32());
        if (doc.getMicRollSpreadVolW32() != null)        update.set("micRollSpreadVolW32", doc.getMicRollSpreadVolW32());
        if (doc.getMicRollSpreadDvgcW32() != null)       update.set("micRollSpreadDvgcW32", doc.getMicRollSpreadDvgcW32());
        if (doc.getMicRollSpreadAtrn14W32() != null)     update.set("micRollSpreadAtrn14W32", doc.getMicRollSpreadAtrn14W32());
        if (doc.getMicRollSpreadPctAtrn14W32() != null)  update.set("micRollSpreadPctAtrn14W32", doc.getMicRollSpreadPctAtrn14W32());
        // Roll W48
        if (doc.getMicRollCovW48() != null)              update.set("micRollCovW48", doc.getMicRollCovW48());
        if (doc.getMicRollCovPctW48() != null)           update.set("micRollCovPctW48", doc.getMicRollCovPctW48());
        if (doc.getMicRollCovZscW48() != null)           update.set("micRollCovZscW48", doc.getMicRollCovZscW48());
        if (doc.getMicRollCovPctZscW48() != null)        update.set("micRollCovPctZscW48", doc.getMicRollCovPctZscW48());
        if (doc.getMicRollSpreadW48() != null)           update.set("micRollSpreadW48", doc.getMicRollSpreadW48());
        if (doc.getMicRollSpreadPctW48() != null)        update.set("micRollSpreadPctW48", doc.getMicRollSpreadPctW48());
        if (doc.getMicRollSpreadZscW48() != null)        update.set("micRollSpreadZscW48", doc.getMicRollSpreadZscW48());
        if (doc.getMicRollSpreadPctZscW48() != null)     update.set("micRollSpreadPctZscW48", doc.getMicRollSpreadPctZscW48());
        if (doc.getMicRollSpreadMaW48() != null)         update.set("micRollSpreadMaW48", doc.getMicRollSpreadMaW48());
        if (doc.getMicRollSpreadPrstW48() != null)       update.set("micRollSpreadPrstW48", doc.getMicRollSpreadPrstW48());
        if (doc.getMicRollSpreadPctileW48() != null)     update.set("micRollSpreadPctileW48", doc.getMicRollSpreadPctileW48());
        if (doc.getMicRollSpreadAccW48() != null)        update.set("micRollSpreadAccW48", doc.getMicRollSpreadAccW48());
        if (doc.getMicRollSpreadSlpW48() != null)        update.set("micRollSpreadSlpW48", doc.getMicRollSpreadSlpW48());
        if (doc.getMicRollSpreadVolW48() != null)        update.set("micRollSpreadVolW48", doc.getMicRollSpreadVolW48());
        if (doc.getMicRollSpreadDvgcW48() != null)       update.set("micRollSpreadDvgcW48", doc.getMicRollSpreadDvgcW48());
        if (doc.getMicRollSpreadAtrn14W48() != null)     update.set("micRollSpreadAtrn14W48", doc.getMicRollSpreadAtrn14W48());
        if (doc.getMicRollSpreadPctAtrn14W48() != null)  update.set("micRollSpreadPctAtrn14W48", doc.getMicRollSpreadPctAtrn14W48());
        // Roll W96
        if (doc.getMicRollCovW96() != null)              update.set("micRollCovW96", doc.getMicRollCovW96());
        if (doc.getMicRollCovPctW96() != null)           update.set("micRollCovPctW96", doc.getMicRollCovPctW96());
        if (doc.getMicRollCovZscW96() != null)           update.set("micRollCovZscW96", doc.getMicRollCovZscW96());
        if (doc.getMicRollCovPctZscW96() != null)        update.set("micRollCovPctZscW96", doc.getMicRollCovPctZscW96());
        if (doc.getMicRollSpreadW96() != null)           update.set("micRollSpreadW96", doc.getMicRollSpreadW96());
        if (doc.getMicRollSpreadPctW96() != null)        update.set("micRollSpreadPctW96", doc.getMicRollSpreadPctW96());
        if (doc.getMicRollSpreadZscW96() != null)        update.set("micRollSpreadZscW96", doc.getMicRollSpreadZscW96());
        if (doc.getMicRollSpreadPctZscW96() != null)     update.set("micRollSpreadPctZscW96", doc.getMicRollSpreadPctZscW96());
        if (doc.getMicRollSpreadMaW96() != null)         update.set("micRollSpreadMaW96", doc.getMicRollSpreadMaW96());
        if (doc.getMicRollSpreadPrstW96() != null)       update.set("micRollSpreadPrstW96", doc.getMicRollSpreadPrstW96());
        if (doc.getMicRollSpreadPctileW96() != null)     update.set("micRollSpreadPctileW96", doc.getMicRollSpreadPctileW96());
        if (doc.getMicRollSpreadAccW96() != null)        update.set("micRollSpreadAccW96", doc.getMicRollSpreadAccW96());
        if (doc.getMicRollSpreadSlpW96() != null)        update.set("micRollSpreadSlpW96", doc.getMicRollSpreadSlpW96());
        if (doc.getMicRollSpreadVolW96() != null)        update.set("micRollSpreadVolW96", doc.getMicRollSpreadVolW96());
        if (doc.getMicRollSpreadDvgcW96() != null)       update.set("micRollSpreadDvgcW96", doc.getMicRollSpreadDvgcW96());
        if (doc.getMicRollSpreadAtrn14W96() != null)     update.set("micRollSpreadAtrn14W96", doc.getMicRollSpreadAtrn14W96());
        if (doc.getMicRollSpreadPctAtrn14W96() != null)  update.set("micRollSpreadPctAtrn14W96", doc.getMicRollSpreadPctAtrn14W96());
        // Roll W336
        if (doc.getMicRollCovW336() != null)              update.set("micRollCovW336", doc.getMicRollCovW336());
        if (doc.getMicRollCovPctW336() != null)           update.set("micRollCovPctW336", doc.getMicRollCovPctW336());
        if (doc.getMicRollCovZscW336() != null)           update.set("micRollCovZscW336", doc.getMicRollCovZscW336());
        if (doc.getMicRollCovPctZscW336() != null)        update.set("micRollCovPctZscW336", doc.getMicRollCovPctZscW336());
        if (doc.getMicRollSpreadW336() != null)           update.set("micRollSpreadW336", doc.getMicRollSpreadW336());
        if (doc.getMicRollSpreadPctW336() != null)        update.set("micRollSpreadPctW336", doc.getMicRollSpreadPctW336());
        if (doc.getMicRollSpreadZscW336() != null)        update.set("micRollSpreadZscW336", doc.getMicRollSpreadZscW336());
        if (doc.getMicRollSpreadPctZscW336() != null)     update.set("micRollSpreadPctZscW336", doc.getMicRollSpreadPctZscW336());
        if (doc.getMicRollSpreadMaW336() != null)         update.set("micRollSpreadMaW336", doc.getMicRollSpreadMaW336());
        if (doc.getMicRollSpreadPrstW336() != null)       update.set("micRollSpreadPrstW336", doc.getMicRollSpreadPrstW336());
        if (doc.getMicRollSpreadPctileW336() != null)     update.set("micRollSpreadPctileW336", doc.getMicRollSpreadPctileW336());
        if (doc.getMicRollSpreadAccW336() != null)        update.set("micRollSpreadAccW336", doc.getMicRollSpreadAccW336());
        if (doc.getMicRollSpreadSlpW336() != null)        update.set("micRollSpreadSlpW336", doc.getMicRollSpreadSlpW336());
        if (doc.getMicRollSpreadVolW336() != null)        update.set("micRollSpreadVolW336", doc.getMicRollSpreadVolW336());
        if (doc.getMicRollSpreadDvgcW336() != null)       update.set("micRollSpreadDvgcW336", doc.getMicRollSpreadDvgcW336());
        if (doc.getMicRollSpreadAtrn14W336() != null)     update.set("micRollSpreadAtrn14W336", doc.getMicRollSpreadAtrn14W336());
        if (doc.getMicRollSpreadPctAtrn14W336() != null)  update.set("micRollSpreadPctAtrn14W336", doc.getMicRollSpreadPctAtrn14W336());
        // Roll W512
        if (doc.getMicRollCovW512() != null)              update.set("micRollCovW512", doc.getMicRollCovW512());
        if (doc.getMicRollCovPctW512() != null)           update.set("micRollCovPctW512", doc.getMicRollCovPctW512());
        if (doc.getMicRollCovZscW512() != null)           update.set("micRollCovZscW512", doc.getMicRollCovZscW512());
        if (doc.getMicRollCovPctZscW512() != null)        update.set("micRollCovPctZscW512", doc.getMicRollCovPctZscW512());
        if (doc.getMicRollSpreadW512() != null)           update.set("micRollSpreadW512", doc.getMicRollSpreadW512());
        if (doc.getMicRollSpreadPctW512() != null)        update.set("micRollSpreadPctW512", doc.getMicRollSpreadPctW512());
        if (doc.getMicRollSpreadZscW512() != null)        update.set("micRollSpreadZscW512", doc.getMicRollSpreadZscW512());
        if (doc.getMicRollSpreadPctZscW512() != null)     update.set("micRollSpreadPctZscW512", doc.getMicRollSpreadPctZscW512());
        if (doc.getMicRollSpreadMaW512() != null)         update.set("micRollSpreadMaW512", doc.getMicRollSpreadMaW512());
        if (doc.getMicRollSpreadPrstW512() != null)       update.set("micRollSpreadPrstW512", doc.getMicRollSpreadPrstW512());
        if (doc.getMicRollSpreadPctileW512() != null)     update.set("micRollSpreadPctileW512", doc.getMicRollSpreadPctileW512());
        if (doc.getMicRollSpreadAccW512() != null)        update.set("micRollSpreadAccW512", doc.getMicRollSpreadAccW512());
        if (doc.getMicRollSpreadSlpW512() != null)        update.set("micRollSpreadSlpW512", doc.getMicRollSpreadSlpW512());
        if (doc.getMicRollSpreadVolW512() != null)        update.set("micRollSpreadVolW512", doc.getMicRollSpreadVolW512());
        if (doc.getMicRollSpreadDvgcW512() != null)       update.set("micRollSpreadDvgcW512", doc.getMicRollSpreadDvgcW512());
        if (doc.getMicRollSpreadAtrn14W512() != null)     update.set("micRollSpreadAtrn14W512", doc.getMicRollSpreadAtrn14W512());
        if (doc.getMicRollSpreadPctAtrn14W512() != null)  update.set("micRollSpreadPctAtrn14W512", doc.getMicRollSpreadPctAtrn14W512());
        // Roll W672
        if (doc.getMicRollCovW672() != null)              update.set("micRollCovW672", doc.getMicRollCovW672());
        if (doc.getMicRollCovPctW672() != null)           update.set("micRollCovPctW672", doc.getMicRollCovPctW672());
        if (doc.getMicRollCovZscW672() != null)           update.set("micRollCovZscW672", doc.getMicRollCovZscW672());
        if (doc.getMicRollCovPctZscW672() != null)        update.set("micRollCovPctZscW672", doc.getMicRollCovPctZscW672());
        if (doc.getMicRollSpreadW672() != null)           update.set("micRollSpreadW672", doc.getMicRollSpreadW672());
        if (doc.getMicRollSpreadPctW672() != null)        update.set("micRollSpreadPctW672", doc.getMicRollSpreadPctW672());
        if (doc.getMicRollSpreadZscW672() != null)        update.set("micRollSpreadZscW672", doc.getMicRollSpreadZscW672());
        if (doc.getMicRollSpreadPctZscW672() != null)     update.set("micRollSpreadPctZscW672", doc.getMicRollSpreadPctZscW672());
        if (doc.getMicRollSpreadMaW672() != null)         update.set("micRollSpreadMaW672", doc.getMicRollSpreadMaW672());
        if (doc.getMicRollSpreadPrstW672() != null)       update.set("micRollSpreadPrstW672", doc.getMicRollSpreadPrstW672());
        if (doc.getMicRollSpreadPctileW672() != null)     update.set("micRollSpreadPctileW672", doc.getMicRollSpreadPctileW672());
        if (doc.getMicRollSpreadAccW672() != null)        update.set("micRollSpreadAccW672", doc.getMicRollSpreadAccW672());
        if (doc.getMicRollSpreadSlpW672() != null)        update.set("micRollSpreadSlpW672", doc.getMicRollSpreadSlpW672());
        if (doc.getMicRollSpreadVolW672() != null)        update.set("micRollSpreadVolW672", doc.getMicRollSpreadVolW672());
        if (doc.getMicRollSpreadDvgcW672() != null)       update.set("micRollSpreadDvgcW672", doc.getMicRollSpreadDvgcW672());
        if (doc.getMicRollSpreadAtrn14W672() != null)     update.set("micRollSpreadAtrn14W672", doc.getMicRollSpreadAtrn14W672());
        if (doc.getMicRollSpreadPctAtrn14W672() != null)  update.set("micRollSpreadPctAtrn14W672", doc.getMicRollSpreadPctAtrn14W672());

        // ── Wick ──────────────────────────────────────────────────────────────
        if (doc.getMicCandleUpperWick() != null)              update.set("micCandleUpperWick", doc.getMicCandleUpperWick());
        if (doc.getMicCandleLowerWick() != null)              update.set("micCandleLowerWick", doc.getMicCandleLowerWick());
        if (doc.getMicCandleUpperWickPct() != null)           update.set("micCandleUpperWickPct", doc.getMicCandleUpperWickPct());
        if (doc.getMicCandleLowerWickPct() != null)           update.set("micCandleLowerWickPct", doc.getMicCandleLowerWickPct());
        if (doc.getMicWickPercUp() != null)                   update.set("micWickPercUp", doc.getMicWickPercUp());
        if (doc.getMicWickPercDown() != null)                 update.set("micWickPercDown", doc.getMicWickPercDown());
        if (doc.getMicCandleWickImbalance() != null)          update.set("micCandleWickImbalance", doc.getMicCandleWickImbalance());
        if (doc.getMicWickImbalance() != null)                update.set("micWickImbalance", doc.getMicWickImbalance());
        if (doc.getMicCandleWickPressureScore() != null)      update.set("micCandleWickPressureScore", doc.getMicCandleWickPressureScore());
        if (doc.getMicCandleShadowRatio() != null)            update.set("micCandleShadowRatio", doc.getMicCandleShadowRatio());
        if (doc.getMicCandleWickBodyAlignment() != null)      update.set("micCandleWickBodyAlignment", doc.getMicCandleWickBodyAlignment());
        if (doc.getMicShadowImbalanceScore() != null)         update.set("micShadowImbalanceScore", doc.getMicShadowImbalanceScore());
        if (doc.getMicCandleWickDominance() != null)          update.set("micCandleWickDominance", doc.getMicCandleWickDominance());
        if (doc.getMicCandleWickExhaustion() != null)         update.set("micCandleWickExhaustion", doc.getMicCandleWickExhaustion());
        if (doc.getMicCandleTotalWick() != null)              update.set("micCandleTotalWick", doc.getMicCandleTotalWick());
        if (doc.getMicCandleTotalWickPct() != null)           update.set("micCandleTotalWickPct", doc.getMicCandleTotalWickPct());
        if (doc.getMicCandleTotalWickAtrn() != null)          update.set("micCandleTotalWickAtrn", doc.getMicCandleTotalWickAtrn());
        if (doc.getMicCandleWickImbalanceNorm() != null)      update.set("micCandleWickImbalanceNorm", doc.getMicCandleWickImbalanceNorm());
        if (doc.getMicCandleWickImbalanceSlpW10() != null)    update.set("micCandleWickImbalanceSlpW10", doc.getMicCandleWickImbalanceSlpW10());
        if (doc.getMicCandleWickImbalanceVol10() != null)     update.set("micCandleWickImbalanceVol10", doc.getMicCandleWickImbalanceVol10());
        if (doc.getMicClosePosSlpW20() != null)               update.set("micClosePosSlpW20", doc.getMicClosePosSlpW20());
        if (doc.getMicCandleUpperWickMa10() != null)          update.set("micCandleUpperWickMa10", doc.getMicCandleUpperWickMa10());
        if (doc.getMicCandleLowerWickMa10() != null)          update.set("micCandleLowerWickMa10", doc.getMicCandleLowerWickMa10());
        if (doc.getMicUpperWickReturn() != null)              update.set("micUpperWickReturn", doc.getMicUpperWickReturn());
        if (doc.getMicLowerWickReturn() != null)              update.set("micLowerWickReturn", doc.getMicLowerWickReturn());

        // ── ShapePattern ──────────────────────────────────────────────────────
        if (doc.getMicCandleDirection() != null)              update.set("micCandleDirection", doc.getMicCandleDirection());
        if (doc.getMicCandleType() != null)                   update.set("micCandleType", doc.getMicCandleType());
        if (doc.getMicCandleShapeIndex() != null)             update.set("micCandleShapeIndex", doc.getMicCandleShapeIndex());
        if (doc.getMicCandleSymmetryScore() != null)          update.set("micCandleSymmetryScore", doc.getMicCandleSymmetryScore());
        if (doc.getMicCandleTriangleScore() != null)          update.set("micCandleTriangleScore", doc.getMicCandleTriangleScore());
        if (doc.getMicCandleGeometryScore() != null)          update.set("micCandleGeometryScore", doc.getMicCandleGeometryScore());
        if (doc.getMicCandleEntropy() != null)                update.set("micCandleEntropy", doc.getMicCandleEntropy());
        if (doc.getMicCandleDojiScore() != null)              update.set("micCandleDojiScore", doc.getMicCandleDojiScore());
        if (doc.getMicCandleImpulseScore() != null)           update.set("micCandleImpulseScore", doc.getMicCandleImpulseScore());
        if (doc.getMicCandleCompressionIndex() != null)       update.set("micCandleCompressionIndex", doc.getMicCandleCompressionIndex());
        if (doc.getMicCandleDirectionPrstW10() != null)       update.set("micCandleDirectionPrstW10", doc.getMicCandleDirectionPrstW10());
        if (doc.getMicCandleDirectionPrstW20() != null)       update.set("micCandleDirectionPrstW20", doc.getMicCandleDirectionPrstW20());
        if (doc.getMicCandleGeometryMa10() != null)           update.set("micCandleGeometryMa10", doc.getMicCandleGeometryMa10());
        if (doc.getMicCandleGeometrySlpW20() != null)         update.set("micCandleGeometrySlpW20", doc.getMicCandleGeometrySlpW20());
        if (doc.getMicCandleGeometryVol10() != null)          update.set("micCandleGeometryVol10", doc.getMicCandleGeometryVol10());
        if (doc.getMicCandleShapeIndexMa20() != null)         update.set("micCandleShapeIndexMa20", doc.getMicCandleShapeIndexMa20());
        if (doc.getMicCandleShapeIndexVol20() != null)        update.set("micCandleShapeIndexVol20", doc.getMicCandleShapeIndexVol20());
        if (doc.getMicCandleCompressionMa20() != null)        update.set("micCandleCompressionMa20", doc.getMicCandleCompressionMa20());
        if (doc.getMicCandleCompressionMa48() != null)        update.set("micCandleCompressionMa48", doc.getMicCandleCompressionMa48());
        if (doc.getMicCandleCompressionVol48() != null)       update.set("micCandleCompressionVol48", doc.getMicCandleCompressionVol48());
        if (doc.getMicCandleCompressionZscore20() != null)    update.set("micCandleCompressionZscore20", doc.getMicCandleCompressionZscore20());
        if (doc.getMicCandleShapeRegimeState() != null)       update.set("micCandleShapeRegimeState", doc.getMicCandleShapeRegimeState());
        if (doc.getMicCandleTypeFlipRateW20() != null)        update.set("micCandleTypeFlipRateW20", doc.getMicCandleTypeFlipRateW20());
        if (doc.getMicCandleDirectionFlipRateW20() != null)   update.set("micCandleDirectionFlipRateW20", doc.getMicCandleDirectionFlipRateW20());
        if (doc.getMicCandleGeometryMa48() != null)           update.set("micCandleGeometryMa48", doc.getMicCandleGeometryMa48());

        return update;
    }

    public static String collectionName(CandleIntervals interval) {
        return switch (interval) {
            case I1_MN -> "mic_1";
            case I5_MN -> "mic_5";
            case I15_MN -> "mic_15";
            case I30_MN -> "mic_30";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
    }
}
