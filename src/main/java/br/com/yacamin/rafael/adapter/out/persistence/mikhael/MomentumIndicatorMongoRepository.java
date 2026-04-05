package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.MomentumIndicatorDocument;
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
public class MomentumIndicatorMongoRepository {

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

    public Optional<MomentumIndicatorDocument> findBySymbolAndOpenTime(String symbol, Instant openTime, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").is(openTime));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, MomentumIndicatorDocument.class, collectionName(interval)));
    }

    public List<MomentumIndicatorDocument> findByRangeExclEnd(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        return mongoTemplate.find(query, MomentumIndicatorDocument.class, collectionName(interval));
    }

    public List<Instant> findOpenTimesByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        query.fields().include("openTime").exclude("_id");

        return mongoTemplate.find(query, MomentumIndicatorDocument.class, collectionName(interval))
                .stream()
                .map(MomentumIndicatorDocument::getOpenTime)
                .toList();
    }

    public void save(MomentumIndicatorDocument doc, CandleIntervals interval) {
        String collection = collectionName(interval);
        Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                .and("openTime").is(doc.getOpenTime()));
        mongoTemplate.upsert(query, buildUpdate(doc), collection);
    }

    public void saveBatch(List<MomentumIndicatorDocument> documents, CandleIntervals interval) {
        if (documents.isEmpty()) return;
        String collection = collectionName(interval);
        var bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        int count = 0;
        for (MomentumIndicatorDocument doc : documents) {
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

    private Update buildUpdate(MomentumIndicatorDocument doc) {
        Update update = new Update();

        // close_ret
        if (doc.getMomCloseRet1() != null) update.set("momCloseRet1", doc.getMomCloseRet1());
        if (doc.getMomCloseRet2() != null) update.set("momCloseRet2", doc.getMomCloseRet2());
        if (doc.getMomCloseRet3() != null) update.set("momCloseRet3", doc.getMomCloseRet3());
        if (doc.getMomCloseRet4() != null) update.set("momCloseRet4", doc.getMomCloseRet4());
        if (doc.getMomCloseRet5() != null) update.set("momCloseRet5", doc.getMomCloseRet5());
        if (doc.getMomCloseRet6() != null) update.set("momCloseRet6", doc.getMomCloseRet6());
        if (doc.getMomCloseRet8() != null) update.set("momCloseRet8", doc.getMomCloseRet8());
        if (doc.getMomCloseRet10() != null) update.set("momCloseRet10", doc.getMomCloseRet10());
        if (doc.getMomCloseRet12() != null) update.set("momCloseRet12", doc.getMomCloseRet12());
        if (doc.getMomCloseRet16() != null) update.set("momCloseRet16", doc.getMomCloseRet16());
        if (doc.getMomCloseRet24() != null) update.set("momCloseRet24", doc.getMomCloseRet24());
        if (doc.getMomCloseRet32() != null) update.set("momCloseRet32", doc.getMomCloseRet32());
        if (doc.getMomCloseRet48() != null) update.set("momCloseRet48", doc.getMomCloseRet48());
        if (doc.getMomCloseRet288() != null) update.set("momCloseRet288", doc.getMomCloseRet288());

        // close_ret atrn
        if (doc.getMomCloseRet1Atrn() != null) update.set("momCloseRet1Atrn", doc.getMomCloseRet1Atrn());
        if (doc.getMomCloseRet2Atrn() != null) update.set("momCloseRet2Atrn", doc.getMomCloseRet2Atrn());
        if (doc.getMomCloseRet3Atrn() != null) update.set("momCloseRet3Atrn", doc.getMomCloseRet3Atrn());
        if (doc.getMomCloseRet4Atrn() != null) update.set("momCloseRet4Atrn", doc.getMomCloseRet4Atrn());
        if (doc.getMomCloseRet5Atrn() != null) update.set("momCloseRet5Atrn", doc.getMomCloseRet5Atrn());
        if (doc.getMomCloseRet6Atrn() != null) update.set("momCloseRet6Atrn", doc.getMomCloseRet6Atrn());
        if (doc.getMomCloseRet8Atrn() != null) update.set("momCloseRet8Atrn", doc.getMomCloseRet8Atrn());
        if (doc.getMomCloseRet10Atrn() != null) update.set("momCloseRet10Atrn", doc.getMomCloseRet10Atrn());
        if (doc.getMomCloseRet12Atrn() != null) update.set("momCloseRet12Atrn", doc.getMomCloseRet12Atrn());
        if (doc.getMomCloseRet16Atrn() != null) update.set("momCloseRet16Atrn", doc.getMomCloseRet16Atrn());
        if (doc.getMomCloseRet24Atrn() != null) update.set("momCloseRet24Atrn", doc.getMomCloseRet24Atrn());
        if (doc.getMomCloseRet32Atrn() != null) update.set("momCloseRet32Atrn", doc.getMomCloseRet32Atrn());
        if (doc.getMomCloseRet48Atrn() != null) update.set("momCloseRet48Atrn", doc.getMomCloseRet48Atrn());
        if (doc.getMomCloseRet288Atrn() != null) update.set("momCloseRet288Atrn", doc.getMomCloseRet288Atrn());

        // close_ret stdn
        if (doc.getMomCloseRet1Stdn() != null) update.set("momCloseRet1Stdn", doc.getMomCloseRet1Stdn());
        if (doc.getMomCloseRet2Stdn() != null) update.set("momCloseRet2Stdn", doc.getMomCloseRet2Stdn());
        if (doc.getMomCloseRet3Stdn() != null) update.set("momCloseRet3Stdn", doc.getMomCloseRet3Stdn());
        if (doc.getMomCloseRet4Stdn() != null) update.set("momCloseRet4Stdn", doc.getMomCloseRet4Stdn());
        if (doc.getMomCloseRet5Stdn() != null) update.set("momCloseRet5Stdn", doc.getMomCloseRet5Stdn());
        if (doc.getMomCloseRet6Stdn() != null) update.set("momCloseRet6Stdn", doc.getMomCloseRet6Stdn());
        if (doc.getMomCloseRet8Stdn() != null) update.set("momCloseRet8Stdn", doc.getMomCloseRet8Stdn());
        if (doc.getMomCloseRet10Stdn() != null) update.set("momCloseRet10Stdn", doc.getMomCloseRet10Stdn());
        if (doc.getMomCloseRet12Stdn() != null) update.set("momCloseRet12Stdn", doc.getMomCloseRet12Stdn());
        if (doc.getMomCloseRet16Stdn() != null) update.set("momCloseRet16Stdn", doc.getMomCloseRet16Stdn());
        if (doc.getMomCloseRet24Stdn() != null) update.set("momCloseRet24Stdn", doc.getMomCloseRet24Stdn());
        if (doc.getMomCloseRet32Stdn() != null) update.set("momCloseRet32Stdn", doc.getMomCloseRet32Stdn());
        if (doc.getMomCloseRet48Stdn() != null) update.set("momCloseRet48Stdn", doc.getMomCloseRet48Stdn());
        if (doc.getMomCloseRet288Stdn() != null) update.set("momCloseRet288Stdn", doc.getMomCloseRet288Stdn());

        // close_ret abs
        if (doc.getMomCloseRet1Abs() != null) update.set("momCloseRet1Abs", doc.getMomCloseRet1Abs());
        if (doc.getMomCloseRet2Abs() != null) update.set("momCloseRet2Abs", doc.getMomCloseRet2Abs());
        if (doc.getMomCloseRet3Abs() != null) update.set("momCloseRet3Abs", doc.getMomCloseRet3Abs());
        if (doc.getMomCloseRet4Abs() != null) update.set("momCloseRet4Abs", doc.getMomCloseRet4Abs());
        if (doc.getMomCloseRet5Abs() != null) update.set("momCloseRet5Abs", doc.getMomCloseRet5Abs());
        if (doc.getMomCloseRet6Abs() != null) update.set("momCloseRet6Abs", doc.getMomCloseRet6Abs());
        if (doc.getMomCloseRet8Abs() != null) update.set("momCloseRet8Abs", doc.getMomCloseRet8Abs());
        if (doc.getMomCloseRet10Abs() != null) update.set("momCloseRet10Abs", doc.getMomCloseRet10Abs());
        if (doc.getMomCloseRet12Abs() != null) update.set("momCloseRet12Abs", doc.getMomCloseRet12Abs());
        if (doc.getMomCloseRet16Abs() != null) update.set("momCloseRet16Abs", doc.getMomCloseRet16Abs());
        if (doc.getMomCloseRet24Abs() != null) update.set("momCloseRet24Abs", doc.getMomCloseRet24Abs());
        if (doc.getMomCloseRet32Abs() != null) update.set("momCloseRet32Abs", doc.getMomCloseRet32Abs());
        if (doc.getMomCloseRet48Abs() != null) update.set("momCloseRet48Abs", doc.getMomCloseRet48Abs());
        if (doc.getMomCloseRet288Abs() != null) update.set("momCloseRet288Abs", doc.getMomCloseRet288Abs());

        // close_ret abs_atrn
        if (doc.getMomCloseRet1AbsAtrn() != null) update.set("momCloseRet1AbsAtrn", doc.getMomCloseRet1AbsAtrn());
        if (doc.getMomCloseRet2AbsAtrn() != null) update.set("momCloseRet2AbsAtrn", doc.getMomCloseRet2AbsAtrn());
        if (doc.getMomCloseRet3AbsAtrn() != null) update.set("momCloseRet3AbsAtrn", doc.getMomCloseRet3AbsAtrn());
        if (doc.getMomCloseRet4AbsAtrn() != null) update.set("momCloseRet4AbsAtrn", doc.getMomCloseRet4AbsAtrn());
        if (doc.getMomCloseRet5AbsAtrn() != null) update.set("momCloseRet5AbsAtrn", doc.getMomCloseRet5AbsAtrn());
        if (doc.getMomCloseRet6AbsAtrn() != null) update.set("momCloseRet6AbsAtrn", doc.getMomCloseRet6AbsAtrn());
        if (doc.getMomCloseRet8AbsAtrn() != null) update.set("momCloseRet8AbsAtrn", doc.getMomCloseRet8AbsAtrn());
        if (doc.getMomCloseRet10AbsAtrn() != null) update.set("momCloseRet10AbsAtrn", doc.getMomCloseRet10AbsAtrn());
        if (doc.getMomCloseRet12AbsAtrn() != null) update.set("momCloseRet12AbsAtrn", doc.getMomCloseRet12AbsAtrn());
        if (doc.getMomCloseRet16AbsAtrn() != null) update.set("momCloseRet16AbsAtrn", doc.getMomCloseRet16AbsAtrn());
        if (doc.getMomCloseRet24AbsAtrn() != null) update.set("momCloseRet24AbsAtrn", doc.getMomCloseRet24AbsAtrn());
        if (doc.getMomCloseRet32AbsAtrn() != null) update.set("momCloseRet32AbsAtrn", doc.getMomCloseRet32AbsAtrn());
        if (doc.getMomCloseRet48AbsAtrn() != null) update.set("momCloseRet48AbsAtrn", doc.getMomCloseRet48AbsAtrn());
        if (doc.getMomCloseRet288AbsAtrn() != null) update.set("momCloseRet288AbsAtrn", doc.getMomCloseRet288AbsAtrn());

        // close_ret abs_stdn
        if (doc.getMomCloseRet1AbsStdn() != null) update.set("momCloseRet1AbsStdn", doc.getMomCloseRet1AbsStdn());
        if (doc.getMomCloseRet2AbsStdn() != null) update.set("momCloseRet2AbsStdn", doc.getMomCloseRet2AbsStdn());
        if (doc.getMomCloseRet3AbsStdn() != null) update.set("momCloseRet3AbsStdn", doc.getMomCloseRet3AbsStdn());
        if (doc.getMomCloseRet4AbsStdn() != null) update.set("momCloseRet4AbsStdn", doc.getMomCloseRet4AbsStdn());
        if (doc.getMomCloseRet5AbsStdn() != null) update.set("momCloseRet5AbsStdn", doc.getMomCloseRet5AbsStdn());
        if (doc.getMomCloseRet6AbsStdn() != null) update.set("momCloseRet6AbsStdn", doc.getMomCloseRet6AbsStdn());
        if (doc.getMomCloseRet8AbsStdn() != null) update.set("momCloseRet8AbsStdn", doc.getMomCloseRet8AbsStdn());
        if (doc.getMomCloseRet10AbsStdn() != null) update.set("momCloseRet10AbsStdn", doc.getMomCloseRet10AbsStdn());
        if (doc.getMomCloseRet12AbsStdn() != null) update.set("momCloseRet12AbsStdn", doc.getMomCloseRet12AbsStdn());
        if (doc.getMomCloseRet16AbsStdn() != null) update.set("momCloseRet16AbsStdn", doc.getMomCloseRet16AbsStdn());
        if (doc.getMomCloseRet24AbsStdn() != null) update.set("momCloseRet24AbsStdn", doc.getMomCloseRet24AbsStdn());
        if (doc.getMomCloseRet32AbsStdn() != null) update.set("momCloseRet32AbsStdn", doc.getMomCloseRet32AbsStdn());
        if (doc.getMomCloseRet48AbsStdn() != null) update.set("momCloseRet48AbsStdn", doc.getMomCloseRet48AbsStdn());
        if (doc.getMomCloseRet288AbsStdn() != null) update.set("momCloseRet288AbsStdn", doc.getMomCloseRet288AbsStdn());

        // burst
        if (doc.getMomBurst10() != null) update.set("momBurst10", doc.getMomBurst10());
        if (doc.getMomBurst16() != null) update.set("momBurst16", doc.getMomBurst16());
        if (doc.getMomBurst32() != null) update.set("momBurst32", doc.getMomBurst32());
        if (doc.getMomBurst48() != null) update.set("momBurst48", doc.getMomBurst48());
        if (doc.getMomBurst288() != null) update.set("momBurst288", doc.getMomBurst288());

        // cntrate
        if (doc.getMomCntrate10() != null) update.set("momCntrate10", doc.getMomCntrate10());
        if (doc.getMomCntrate16() != null) update.set("momCntrate16", doc.getMomCntrate16());
        if (doc.getMomCntrate32() != null) update.set("momCntrate32", doc.getMomCntrate32());
        if (doc.getMomCntrate48() != null) update.set("momCntrate48", doc.getMomCntrate48());
        if (doc.getMomCntrate288() != null) update.set("momCntrate288", doc.getMomCntrate288());

        // decay
        if (doc.getMomDecay10() != null) update.set("momDecay10", doc.getMomDecay10());
        if (doc.getMomDecay16() != null) update.set("momDecay16", doc.getMomDecay16());
        if (doc.getMomDecay32() != null) update.set("momDecay32", doc.getMomDecay32());
        if (doc.getMomDecay48() != null) update.set("momDecay48", doc.getMomDecay48());
        if (doc.getMomDecay288() != null) update.set("momDecay288", doc.getMomDecay288());

        // impls
        if (doc.getMomImpls10() != null) update.set("momImpls10", doc.getMomImpls10());
        if (doc.getMomImpls16() != null) update.set("momImpls16", doc.getMomImpls16());
        if (doc.getMomImpls32() != null) update.set("momImpls32", doc.getMomImpls32());
        if (doc.getMomImpls48() != null) update.set("momImpls48", doc.getMomImpls48());
        if (doc.getMomImpls288() != null) update.set("momImpls288", doc.getMomImpls288());

        // chprt
        if (doc.getMomChprt10() != null) update.set("momChprt10", doc.getMomChprt10());
        if (doc.getMomChprt16() != null) update.set("momChprt16", doc.getMomChprt16());
        if (doc.getMomChprt32() != null) update.set("momChprt32", doc.getMomChprt32());
        if (doc.getMomChprt48() != null) update.set("momChprt48", doc.getMomChprt48());
        if (doc.getMomChprt288() != null) update.set("momChprt288", doc.getMomChprt288());

        // RSI raw
        if (doc.getMomRsi2() != null) update.set("momRsi2", doc.getMomRsi2());
        if (doc.getMomRsi3() != null) update.set("momRsi3", doc.getMomRsi3());
        if (doc.getMomRsi4() != null) update.set("momRsi4", doc.getMomRsi4());
        if (doc.getMomRsi5() != null) update.set("momRsi5", doc.getMomRsi5());
        if (doc.getMomRsi6() != null) update.set("momRsi6", doc.getMomRsi6());
        if (doc.getMomRsi7() != null) update.set("momRsi7", doc.getMomRsi7());
        if (doc.getMomRsi8() != null) update.set("momRsi8", doc.getMomRsi8());
        if (doc.getMomRsi9() != null) update.set("momRsi9", doc.getMomRsi9());
        if (doc.getMomRsi10() != null) update.set("momRsi10", doc.getMomRsi10());
        if (doc.getMomRsi12() != null) update.set("momRsi12", doc.getMomRsi12());
        if (doc.getMomRsi14() != null) update.set("momRsi14", doc.getMomRsi14());
        if (doc.getMomRsi16() != null) update.set("momRsi16", doc.getMomRsi16());
        if (doc.getMomRsi21() != null) update.set("momRsi21", doc.getMomRsi21());
        if (doc.getMomRsi24() != null) update.set("momRsi24", doc.getMomRsi24());
        if (doc.getMomRsi28() != null) update.set("momRsi28", doc.getMomRsi28());
        if (doc.getMomRsi32() != null) update.set("momRsi32", doc.getMomRsi32());
        if (doc.getMomRsi48() != null) update.set("momRsi48", doc.getMomRsi48());
        if (doc.getMomRsi288() != null) update.set("momRsi288", doc.getMomRsi288());

        // RSI dlt
        if (doc.getMomRsi2Dlt() != null) update.set("momRsi2Dlt", doc.getMomRsi2Dlt());
        if (doc.getMomRsi3Dlt() != null) update.set("momRsi3Dlt", doc.getMomRsi3Dlt());
        if (doc.getMomRsi5Dlt() != null) update.set("momRsi5Dlt", doc.getMomRsi5Dlt());
        if (doc.getMomRsi7Dlt() != null) update.set("momRsi7Dlt", doc.getMomRsi7Dlt());
        if (doc.getMomRsi14Dlt() != null) update.set("momRsi14Dlt", doc.getMomRsi14Dlt());

        // RSI roc
        if (doc.getMomRsi2Roc() != null) update.set("momRsi2Roc", doc.getMomRsi2Roc());
        if (doc.getMomRsi3Roc() != null) update.set("momRsi3Roc", doc.getMomRsi3Roc());
        if (doc.getMomRsi5Roc() != null) update.set("momRsi5Roc", doc.getMomRsi5Roc());
        if (doc.getMomRsi7Roc() != null) update.set("momRsi7Roc", doc.getMomRsi7Roc());
        if (doc.getMomRsi14Roc() != null) update.set("momRsi14Roc", doc.getMomRsi14Roc());

        // RSI slp
        if (doc.getMomRsi7Slp() != null) update.set("momRsi7Slp", doc.getMomRsi7Slp());
        if (doc.getMomRsi14Slp() != null) update.set("momRsi14Slp", doc.getMomRsi14Slp());
        if (doc.getMomRsi28Slp() != null) update.set("momRsi28Slp", doc.getMomRsi28Slp());

        // RSI atrn
        if (doc.getMomRsi14Atrn() != null) update.set("momRsi14Atrn", doc.getMomRsi14Atrn());

        // RSI vlt
        if (doc.getMomRsi7Vlt() != null) update.set("momRsi7Vlt", doc.getMomRsi7Vlt());
        if (doc.getMomRsi14Vlt() != null) update.set("momRsi14Vlt", doc.getMomRsi14Vlt());

        // RSI acc
        if (doc.getMomRsi14Acc() != null) update.set("momRsi14Acc", doc.getMomRsi14Acc());

        // RSI dst_mid
        if (doc.getMomRsi7DstMid() != null) update.set("momRsi7DstMid", doc.getMomRsi7DstMid());
        if (doc.getMomRsi14DstMid() != null) update.set("momRsi14DstMid", doc.getMomRsi14DstMid());

        // RSI tail
        if (doc.getMomRsi7TailUp() != null) update.set("momRsi7TailUp", doc.getMomRsi7TailUp());
        if (doc.getMomRsi7TailDw() != null) update.set("momRsi7TailDw", doc.getMomRsi7TailDw());
        if (doc.getMomRsi14TailUp() != null) update.set("momRsi14TailUp", doc.getMomRsi14TailUp());
        if (doc.getMomRsi14TailDw() != null) update.set("momRsi14TailDw", doc.getMomRsi14TailDw());

        // RSI 48/288 dlt/roc/slp/vlt/dst_mid/tail
        if (doc.getMomRsi48Dlt() != null) update.set("momRsi48Dlt", doc.getMomRsi48Dlt());
        if (doc.getMomRsi288Dlt() != null) update.set("momRsi288Dlt", doc.getMomRsi288Dlt());
        if (doc.getMomRsi48Roc() != null) update.set("momRsi48Roc", doc.getMomRsi48Roc());
        if (doc.getMomRsi288Roc() != null) update.set("momRsi288Roc", doc.getMomRsi288Roc());
        if (doc.getMomRsi48Slp() != null) update.set("momRsi48Slp", doc.getMomRsi48Slp());
        if (doc.getMomRsi288Slp() != null) update.set("momRsi288Slp", doc.getMomRsi288Slp());
        if (doc.getMomRsi48Vlt() != null) update.set("momRsi48Vlt", doc.getMomRsi48Vlt());
        if (doc.getMomRsi288Vlt() != null) update.set("momRsi288Vlt", doc.getMomRsi288Vlt());
        if (doc.getMomRsi48DstMid() != null) update.set("momRsi48DstMid", doc.getMomRsi48DstMid());
        if (doc.getMomRsi288DstMid() != null) update.set("momRsi288DstMid", doc.getMomRsi288DstMid());
        if (doc.getMomRsi48TailUp() != null) update.set("momRsi48TailUp", doc.getMomRsi48TailUp());
        if (doc.getMomRsi48TailDw() != null) update.set("momRsi48TailDw", doc.getMomRsi48TailDw());
        if (doc.getMomRsi288TailUp() != null) update.set("momRsi288TailUp", doc.getMomRsi288TailUp());
        if (doc.getMomRsi288TailDw() != null) update.set("momRsi288TailDw", doc.getMomRsi288TailDw());

        // RSI regime
        if (doc.getMomRsi14RegimeState() != null) update.set("momRsi14RegimeState", doc.getMomRsi14RegimeState());
        if (doc.getMomRsi14RegimePrstW20() != null) update.set("momRsi14RegimePrstW20", doc.getMomRsi14RegimePrstW20());

        // RSI zsc/pctile
        if (doc.getMomRsi14Zsc80() != null) update.set("momRsi14Zsc80", doc.getMomRsi14Zsc80());
        if (doc.getMomRsi14PctileW80() != null) update.set("momRsi14PctileW80", doc.getMomRsi14PctileW80());

        // RSI shock
        if (doc.getMomRsi14Shock1() != null) update.set("momRsi14Shock1", doc.getMomRsi14Shock1());
        if (doc.getMomRsi14Shock1Stdn80() != null) update.set("momRsi14Shock1Stdn80", doc.getMomRsi14Shock1Stdn80());

        // RSI 48/288 zsc/pctile/shock
        if (doc.getMomRsi48Zsc80() != null) update.set("momRsi48Zsc80", doc.getMomRsi48Zsc80());
        if (doc.getMomRsi48PctileW80() != null) update.set("momRsi48PctileW80", doc.getMomRsi48PctileW80());
        if (doc.getMomRsi288Zsc80() != null) update.set("momRsi288Zsc80", doc.getMomRsi288Zsc80());
        if (doc.getMomRsi288PctileW80() != null) update.set("momRsi288PctileW80", doc.getMomRsi288PctileW80());
        if (doc.getMomRsi48Shock1() != null) update.set("momRsi48Shock1", doc.getMomRsi48Shock1());
        if (doc.getMomRsi288Shock1() != null) update.set("momRsi288Shock1", doc.getMomRsi288Shock1());
        if (doc.getMomRsi48Shock1Stdn80() != null) update.set("momRsi48Shock1Stdn80", doc.getMomRsi48Shock1Stdn80());
        if (doc.getMomRsi288Shock1Stdn80() != null) update.set("momRsi288Shock1Stdn80", doc.getMomRsi288Shock1Stdn80());

        // RSI 48/288 regime
        if (doc.getMomRsi48RegimeState() != null) update.set("momRsi48RegimeState", doc.getMomRsi48RegimeState());
        if (doc.getMomRsi48RegimePrstW20() != null) update.set("momRsi48RegimePrstW20", doc.getMomRsi48RegimePrstW20());
        if (doc.getMomRsi288RegimeState() != null) update.set("momRsi288RegimeState", doc.getMomRsi288RegimeState());
        if (doc.getMomRsi288RegimePrstW20() != null) update.set("momRsi288RegimePrstW20", doc.getMomRsi288RegimePrstW20());

        // CMO
        if (doc.getMomCmo14() != null) update.set("momCmo14", doc.getMomCmo14());
        if (doc.getMomCmo20() != null) update.set("momCmo20", doc.getMomCmo20());
        if (doc.getMomCmo14Dlt() != null) update.set("momCmo14Dlt", doc.getMomCmo14Dlt());
        if (doc.getMomCmo20Dlt() != null) update.set("momCmo20Dlt", doc.getMomCmo20Dlt());
        if (doc.getMomCmo14DstMid() != null) update.set("momCmo14DstMid", doc.getMomCmo14DstMid());
        if (doc.getMomCmo48() != null) update.set("momCmo48", doc.getMomCmo48());
        if (doc.getMomCmo288() != null) update.set("momCmo288", doc.getMomCmo288());
        if (doc.getMomCmo48Dlt() != null) update.set("momCmo48Dlt", doc.getMomCmo48Dlt());
        if (doc.getMomCmo288Dlt() != null) update.set("momCmo288Dlt", doc.getMomCmo288Dlt());
        if (doc.getMomCmo20DstMid() != null) update.set("momCmo20DstMid", doc.getMomCmo20DstMid());
        if (doc.getMomCmo48DstMid() != null) update.set("momCmo48DstMid", doc.getMomCmo48DstMid());
        if (doc.getMomCmo288DstMid() != null) update.set("momCmo288DstMid", doc.getMomCmo288DstMid());
        if (doc.getMomCmo20Zsc80() != null) update.set("momCmo20Zsc80", doc.getMomCmo20Zsc80());
        if (doc.getMomCmo20PctileW80() != null) update.set("momCmo20PctileW80", doc.getMomCmo20PctileW80());
        if (doc.getMomCmo48Zsc80() != null) update.set("momCmo48Zsc80", doc.getMomCmo48Zsc80());
        if (doc.getMomCmo48PctileW80() != null) update.set("momCmo48PctileW80", doc.getMomCmo48PctileW80());
        if (doc.getMomCmo288Zsc80() != null) update.set("momCmo288Zsc80", doc.getMomCmo288Zsc80());
        if (doc.getMomCmo288PctileW80() != null) update.set("momCmo288PctileW80", doc.getMomCmo288PctileW80());
        if (doc.getMomCmo20Shock1() != null) update.set("momCmo20Shock1", doc.getMomCmo20Shock1());
        if (doc.getMomCmo20Shock1Stdn80() != null) update.set("momCmo20Shock1Stdn80", doc.getMomCmo20Shock1Stdn80());
        if (doc.getMomCmo48Shock1() != null) update.set("momCmo48Shock1", doc.getMomCmo48Shock1());
        if (doc.getMomCmo48Shock1Stdn80() != null) update.set("momCmo48Shock1Stdn80", doc.getMomCmo48Shock1Stdn80());
        if (doc.getMomCmo288Shock1() != null) update.set("momCmo288Shock1", doc.getMomCmo288Shock1());
        if (doc.getMomCmo288Shock1Stdn80() != null) update.set("momCmo288Shock1Stdn80", doc.getMomCmo288Shock1Stdn80());
        if (doc.getMomCmo20RegimeState() != null) update.set("momCmo20RegimeState", doc.getMomCmo20RegimeState());
        if (doc.getMomCmo20RegimePrstW20() != null) update.set("momCmo20RegimePrstW20", doc.getMomCmo20RegimePrstW20());
        if (doc.getMomCmo48RegimeState() != null) update.set("momCmo48RegimeState", doc.getMomCmo48RegimeState());
        if (doc.getMomCmo48RegimePrstW20() != null) update.set("momCmo48RegimePrstW20", doc.getMomCmo48RegimePrstW20());
        if (doc.getMomCmo288RegimeState() != null) update.set("momCmo288RegimeState", doc.getMomCmo288RegimeState());
        if (doc.getMomCmo288RegimePrstW20() != null) update.set("momCmo288RegimePrstW20", doc.getMomCmo288RegimePrstW20());

        // WPR
        if (doc.getMomWpr14() != null) update.set("momWpr14", doc.getMomWpr14());
        if (doc.getMomWpr14Dlt() != null) update.set("momWpr14Dlt", doc.getMomWpr14Dlt());
        if (doc.getMomWpr28() != null) update.set("momWpr28", doc.getMomWpr28());
        if (doc.getMomWpr28Dlt() != null) update.set("momWpr28Dlt", doc.getMomWpr28Dlt());
        if (doc.getMomWpr42() != null) update.set("momWpr42", doc.getMomWpr42());
        if (doc.getMomWpr42Dlt() != null) update.set("momWpr42Dlt", doc.getMomWpr42Dlt());
        if (doc.getMomWpr14DstMid() != null) update.set("momWpr14DstMid", doc.getMomWpr14DstMid());
        if (doc.getMomWpr48() != null) update.set("momWpr48", doc.getMomWpr48());
        if (doc.getMomWpr48Dlt() != null) update.set("momWpr48Dlt", doc.getMomWpr48Dlt());
        if (doc.getMomWpr288() != null) update.set("momWpr288", doc.getMomWpr288());
        if (doc.getMomWpr288Dlt() != null) update.set("momWpr288Dlt", doc.getMomWpr288Dlt());
        if (doc.getMomWpr28DstMid() != null) update.set("momWpr28DstMid", doc.getMomWpr28DstMid());
        if (doc.getMomWpr42DstMid() != null) update.set("momWpr42DstMid", doc.getMomWpr42DstMid());
        if (doc.getMomWpr48DstMid() != null) update.set("momWpr48DstMid", doc.getMomWpr48DstMid());
        if (doc.getMomWpr288DstMid() != null) update.set("momWpr288DstMid", doc.getMomWpr288DstMid());
        if (doc.getMomWpr28Zsc80() != null) update.set("momWpr28Zsc80", doc.getMomWpr28Zsc80());
        if (doc.getMomWpr28PctileW80() != null) update.set("momWpr28PctileW80", doc.getMomWpr28PctileW80());
        if (doc.getMomWpr48Zsc80() != null) update.set("momWpr48Zsc80", doc.getMomWpr48Zsc80());
        if (doc.getMomWpr48PctileW80() != null) update.set("momWpr48PctileW80", doc.getMomWpr48PctileW80());
        if (doc.getMomWpr288Zsc80() != null) update.set("momWpr288Zsc80", doc.getMomWpr288Zsc80());
        if (doc.getMomWpr288PctileW80() != null) update.set("momWpr288PctileW80", doc.getMomWpr288PctileW80());
        if (doc.getMomWpr28Shock1() != null) update.set("momWpr28Shock1", doc.getMomWpr28Shock1());
        if (doc.getMomWpr28Shock1Stdn80() != null) update.set("momWpr28Shock1Stdn80", doc.getMomWpr28Shock1Stdn80());
        if (doc.getMomWpr48Shock1() != null) update.set("momWpr48Shock1", doc.getMomWpr48Shock1());
        if (doc.getMomWpr48Shock1Stdn80() != null) update.set("momWpr48Shock1Stdn80", doc.getMomWpr48Shock1Stdn80());
        if (doc.getMomWpr288Shock1() != null) update.set("momWpr288Shock1", doc.getMomWpr288Shock1());
        if (doc.getMomWpr288Shock1Stdn80() != null) update.set("momWpr288Shock1Stdn80", doc.getMomWpr288Shock1Stdn80());
        if (doc.getMomWpr28RegimeState() != null) update.set("momWpr28RegimeState", doc.getMomWpr28RegimeState());
        if (doc.getMomWpr28RegimePrstW20() != null) update.set("momWpr28RegimePrstW20", doc.getMomWpr28RegimePrstW20());
        if (doc.getMomWpr48RegimeState() != null) update.set("momWpr48RegimeState", doc.getMomWpr48RegimeState());
        if (doc.getMomWpr48RegimePrstW20() != null) update.set("momWpr48RegimePrstW20", doc.getMomWpr48RegimePrstW20());
        if (doc.getMomWpr288RegimeState() != null) update.set("momWpr288RegimeState", doc.getMomWpr288RegimeState());
        if (doc.getMomWpr288RegimePrstW20() != null) update.set("momWpr288RegimePrstW20", doc.getMomWpr288RegimePrstW20());

        // Stochastic
        if (doc.getMomStoch14K() != null) update.set("momStoch14K", doc.getMomStoch14K());
        if (doc.getMomStoch14D() != null) update.set("momStoch14D", doc.getMomStoch14D());
        if (doc.getMomStoch14KDlt() != null) update.set("momStoch14KDlt", doc.getMomStoch14KDlt());
        if (doc.getMomStoch14DDlt() != null) update.set("momStoch14DDlt", doc.getMomStoch14DDlt());
        if (doc.getMomStoch48K() != null) update.set("momStoch48K", doc.getMomStoch48K());
        if (doc.getMomStoch48D() != null) update.set("momStoch48D", doc.getMomStoch48D());
        if (doc.getMomStoch48KDlt() != null) update.set("momStoch48KDlt", doc.getMomStoch48KDlt());
        if (doc.getMomStoch48DDlt() != null) update.set("momStoch48DDlt", doc.getMomStoch48DDlt());
        if (doc.getMomStoch288K() != null) update.set("momStoch288K", doc.getMomStoch288K());
        if (doc.getMomStoch288D() != null) update.set("momStoch288D", doc.getMomStoch288D());
        if (doc.getMomStoch288KDlt() != null) update.set("momStoch288KDlt", doc.getMomStoch288KDlt());
        if (doc.getMomStoch288DDlt() != null) update.set("momStoch288DDlt", doc.getMomStoch288DDlt());
        if (doc.getMomStoch14Spread() != null) update.set("momStoch14Spread", doc.getMomStoch14Spread());
        if (doc.getMomStoch48Spread() != null) update.set("momStoch48Spread", doc.getMomStoch48Spread());
        if (doc.getMomStoch288Spread() != null) update.set("momStoch288Spread", doc.getMomStoch288Spread());
        if (doc.getMomStoch14CrossState() != null) update.set("momStoch14CrossState", doc.getMomStoch14CrossState());
        if (doc.getMomStoch48CrossState() != null) update.set("momStoch48CrossState", doc.getMomStoch48CrossState());
        if (doc.getMomStoch288CrossState() != null) update.set("momStoch288CrossState", doc.getMomStoch288CrossState());
        if (doc.getMomStoch14KDstMid() != null) update.set("momStoch14KDstMid", doc.getMomStoch14KDstMid());
        if (doc.getMomStoch48KDstMid() != null) update.set("momStoch48KDstMid", doc.getMomStoch48KDstMid());
        if (doc.getMomStoch288KDstMid() != null) update.set("momStoch288KDstMid", doc.getMomStoch288KDstMid());
        if (doc.getMomStoch14KZsc80() != null) update.set("momStoch14KZsc80", doc.getMomStoch14KZsc80());
        if (doc.getMomStoch14KPctileW80() != null) update.set("momStoch14KPctileW80", doc.getMomStoch14KPctileW80());
        if (doc.getMomStoch48KZsc80() != null) update.set("momStoch48KZsc80", doc.getMomStoch48KZsc80());
        if (doc.getMomStoch48KPctileW80() != null) update.set("momStoch48KPctileW80", doc.getMomStoch48KPctileW80());
        if (doc.getMomStoch288KZsc80() != null) update.set("momStoch288KZsc80", doc.getMomStoch288KZsc80());
        if (doc.getMomStoch288KPctileW80() != null) update.set("momStoch288KPctileW80", doc.getMomStoch288KPctileW80());
        if (doc.getMomStoch14KShock1() != null) update.set("momStoch14KShock1", doc.getMomStoch14KShock1());
        if (doc.getMomStoch14KShock1Stdn80() != null) update.set("momStoch14KShock1Stdn80", doc.getMomStoch14KShock1Stdn80());
        if (doc.getMomStoch48KShock1() != null) update.set("momStoch48KShock1", doc.getMomStoch48KShock1());
        if (doc.getMomStoch48KShock1Stdn80() != null) update.set("momStoch48KShock1Stdn80", doc.getMomStoch48KShock1Stdn80());
        if (doc.getMomStoch288KShock1() != null) update.set("momStoch288KShock1", doc.getMomStoch288KShock1());
        if (doc.getMomStoch288KShock1Stdn80() != null) update.set("momStoch288KShock1Stdn80", doc.getMomStoch288KShock1Stdn80());
        if (doc.getMomStoch14KRegimeState() != null) update.set("momStoch14KRegimeState", doc.getMomStoch14KRegimeState());
        if (doc.getMomStoch14KRegimePrstW20() != null) update.set("momStoch14KRegimePrstW20", doc.getMomStoch14KRegimePrstW20());
        if (doc.getMomStoch48KRegimeState() != null) update.set("momStoch48KRegimeState", doc.getMomStoch48KRegimeState());
        if (doc.getMomStoch48KRegimePrstW20() != null) update.set("momStoch48KRegimePrstW20", doc.getMomStoch48KRegimePrstW20());
        if (doc.getMomStoch288KRegimeState() != null) update.set("momStoch288KRegimeState", doc.getMomStoch288KRegimeState());
        if (doc.getMomStoch288KRegimePrstW20() != null) update.set("momStoch288KRegimePrstW20", doc.getMomStoch288KRegimePrstW20());

        // TRIX
        if (doc.getMomTrix9() != null) update.set("momTrix9", doc.getMomTrix9());
        if (doc.getMomTrix48() != null) update.set("momTrix48", doc.getMomTrix48());
        if (doc.getMomTrix288() != null) update.set("momTrix288", doc.getMomTrix288());
        if (doc.getMomTrix9Dlt() != null) update.set("momTrix9Dlt", doc.getMomTrix9Dlt());
        if (doc.getMomTrix48Dlt() != null) update.set("momTrix48Dlt", doc.getMomTrix48Dlt());
        if (doc.getMomTrix288Dlt() != null) update.set("momTrix288Dlt", doc.getMomTrix288Dlt());
        if (doc.getMomTrix9Sig9() != null) update.set("momTrix9Sig9", doc.getMomTrix9Sig9());
        if (doc.getMomTrix9Hist() != null) update.set("momTrix9Hist", doc.getMomTrix9Hist());
        if (doc.getMomTrix9CrossState() != null) update.set("momTrix9CrossState", doc.getMomTrix9CrossState());
        if (doc.getMomTrix48Sig9() != null) update.set("momTrix48Sig9", doc.getMomTrix48Sig9());
        if (doc.getMomTrix48Hist() != null) update.set("momTrix48Hist", doc.getMomTrix48Hist());
        if (doc.getMomTrix48CrossState() != null) update.set("momTrix48CrossState", doc.getMomTrix48CrossState());
        if (doc.getMomTrix288Sig9() != null) update.set("momTrix288Sig9", doc.getMomTrix288Sig9());
        if (doc.getMomTrix288Hist() != null) update.set("momTrix288Hist", doc.getMomTrix288Hist());
        if (doc.getMomTrix288CrossState() != null) update.set("momTrix288CrossState", doc.getMomTrix288CrossState());
        if (doc.getMomTrix9Zsc80() != null) update.set("momTrix9Zsc80", doc.getMomTrix9Zsc80());
        if (doc.getMomTrix9PctileW80() != null) update.set("momTrix9PctileW80", doc.getMomTrix9PctileW80());
        if (doc.getMomTrix48Zsc80() != null) update.set("momTrix48Zsc80", doc.getMomTrix48Zsc80());
        if (doc.getMomTrix48PctileW80() != null) update.set("momTrix48PctileW80", doc.getMomTrix48PctileW80());
        if (doc.getMomTrix288Zsc80() != null) update.set("momTrix288Zsc80", doc.getMomTrix288Zsc80());
        if (doc.getMomTrix288PctileW80() != null) update.set("momTrix288PctileW80", doc.getMomTrix288PctileW80());
        if (doc.getMomTrix9Shock1() != null) update.set("momTrix9Shock1", doc.getMomTrix9Shock1());
        if (doc.getMomTrix9Shock1Stdn80() != null) update.set("momTrix9Shock1Stdn80", doc.getMomTrix9Shock1Stdn80());
        if (doc.getMomTrix48Shock1() != null) update.set("momTrix48Shock1", doc.getMomTrix48Shock1());
        if (doc.getMomTrix48Shock1Stdn80() != null) update.set("momTrix48Shock1Stdn80", doc.getMomTrix48Shock1Stdn80());
        if (doc.getMomTrix288Shock1() != null) update.set("momTrix288Shock1", doc.getMomTrix288Shock1());
        if (doc.getMomTrix288Shock1Stdn80() != null) update.set("momTrix288Shock1Stdn80", doc.getMomTrix288Shock1Stdn80());
        if (doc.getMomTrix9RegimeState() != null) update.set("momTrix9RegimeState", doc.getMomTrix9RegimeState());
        if (doc.getMomTrix9RegimePrstW20() != null) update.set("momTrix9RegimePrstW20", doc.getMomTrix9RegimePrstW20());
        if (doc.getMomTrix48RegimeState() != null) update.set("momTrix48RegimeState", doc.getMomTrix48RegimeState());
        if (doc.getMomTrix48RegimePrstW20() != null) update.set("momTrix48RegimePrstW20", doc.getMomTrix48RegimePrstW20());
        if (doc.getMomTrix288RegimeState() != null) update.set("momTrix288RegimeState", doc.getMomTrix288RegimeState());
        if (doc.getMomTrix288RegimePrstW20() != null) update.set("momTrix288RegimePrstW20", doc.getMomTrix288RegimePrstW20());

        // TSI
        if (doc.getMomTsi2513() != null) update.set("momTsi2513", doc.getMomTsi2513());
        if (doc.getMomTsi2513Dlt() != null) update.set("momTsi2513Dlt", doc.getMomTsi2513Dlt());
        if (doc.getMomTsi4825() != null) update.set("momTsi4825", doc.getMomTsi4825());
        if (doc.getMomTsi4825Dlt() != null) update.set("momTsi4825Dlt", doc.getMomTsi4825Dlt());
        if (doc.getMomTsi288144() != null) update.set("momTsi288144", doc.getMomTsi288144());
        if (doc.getMomTsi288144Dlt() != null) update.set("momTsi288144Dlt", doc.getMomTsi288144Dlt());
        if (doc.getMomTsi2513Sig7() != null) update.set("momTsi2513Sig7", doc.getMomTsi2513Sig7());
        if (doc.getMomTsi2513Hist() != null) update.set("momTsi2513Hist", doc.getMomTsi2513Hist());
        if (doc.getMomTsi2513CrossState() != null) update.set("momTsi2513CrossState", doc.getMomTsi2513CrossState());
        if (doc.getMomTsi4825Sig7() != null) update.set("momTsi4825Sig7", doc.getMomTsi4825Sig7());
        if (doc.getMomTsi4825Hist() != null) update.set("momTsi4825Hist", doc.getMomTsi4825Hist());
        if (doc.getMomTsi4825CrossState() != null) update.set("momTsi4825CrossState", doc.getMomTsi4825CrossState());
        if (doc.getMomTsi288144Sig7() != null) update.set("momTsi288144Sig7", doc.getMomTsi288144Sig7());
        if (doc.getMomTsi288144Hist() != null) update.set("momTsi288144Hist", doc.getMomTsi288144Hist());
        if (doc.getMomTsi288144CrossState() != null) update.set("momTsi288144CrossState", doc.getMomTsi288144CrossState());
        if (doc.getMomTsi2513DstMid() != null) update.set("momTsi2513DstMid", doc.getMomTsi2513DstMid());
        if (doc.getMomTsi4825DstMid() != null) update.set("momTsi4825DstMid", doc.getMomTsi4825DstMid());
        if (doc.getMomTsi288144DstMid() != null) update.set("momTsi288144DstMid", doc.getMomTsi288144DstMid());
        if (doc.getMomTsi2513Zsc80() != null) update.set("momTsi2513Zsc80", doc.getMomTsi2513Zsc80());
        if (doc.getMomTsi2513PctileW80() != null) update.set("momTsi2513PctileW80", doc.getMomTsi2513PctileW80());
        if (doc.getMomTsi4825Zsc80() != null) update.set("momTsi4825Zsc80", doc.getMomTsi4825Zsc80());
        if (doc.getMomTsi4825PctileW80() != null) update.set("momTsi4825PctileW80", doc.getMomTsi4825PctileW80());
        if (doc.getMomTsi288144Zsc80() != null) update.set("momTsi288144Zsc80", doc.getMomTsi288144Zsc80());
        if (doc.getMomTsi288144PctileW80() != null) update.set("momTsi288144PctileW80", doc.getMomTsi288144PctileW80());
        if (doc.getMomTsi2513Shock1() != null) update.set("momTsi2513Shock1", doc.getMomTsi2513Shock1());
        if (doc.getMomTsi2513Shock1Stdn80() != null) update.set("momTsi2513Shock1Stdn80", doc.getMomTsi2513Shock1Stdn80());
        if (doc.getMomTsi4825Shock1() != null) update.set("momTsi4825Shock1", doc.getMomTsi4825Shock1());
        if (doc.getMomTsi4825Shock1Stdn80() != null) update.set("momTsi4825Shock1Stdn80", doc.getMomTsi4825Shock1Stdn80());
        if (doc.getMomTsi288144Shock1() != null) update.set("momTsi288144Shock1", doc.getMomTsi288144Shock1());
        if (doc.getMomTsi288144Shock1Stdn80() != null) update.set("momTsi288144Shock1Stdn80", doc.getMomTsi288144Shock1Stdn80());
        if (doc.getMomTsi2513RegimeState() != null) update.set("momTsi2513RegimeState", doc.getMomTsi2513RegimeState());
        if (doc.getMomTsi2513RegimePrstW20() != null) update.set("momTsi2513RegimePrstW20", doc.getMomTsi2513RegimePrstW20());
        if (doc.getMomTsi4825RegimeState() != null) update.set("momTsi4825RegimeState", doc.getMomTsi4825RegimeState());
        if (doc.getMomTsi4825RegimePrstW20() != null) update.set("momTsi4825RegimePrstW20", doc.getMomTsi4825RegimePrstW20());
        if (doc.getMomTsi288144RegimeState() != null) update.set("momTsi288144RegimeState", doc.getMomTsi288144RegimeState());
        if (doc.getMomTsi288144RegimePrstW20() != null) update.set("momTsi288144RegimePrstW20", doc.getMomTsi288144RegimePrstW20());

        // PPO 12/26
        if (doc.getMomPpo1226() != null) update.set("momPpo1226", doc.getMomPpo1226());
        if (doc.getMomPpoSig12269() != null) update.set("momPpoSig12269", doc.getMomPpoSig12269());
        if (doc.getMomPpoHist12269() != null) update.set("momPpoHist12269", doc.getMomPpoHist12269());
        if (doc.getMomPpo1226Dlt() != null) update.set("momPpo1226Dlt", doc.getMomPpo1226Dlt());
        if (doc.getMomPpoHist12269Dlt() != null) update.set("momPpoHist12269Dlt", doc.getMomPpoHist12269Dlt());
        if (doc.getMomPpo1226Zsc80() != null) update.set("momPpo1226Zsc80", doc.getMomPpo1226Zsc80());
        if (doc.getMomPpo1226PctileW80() != null) update.set("momPpo1226PctileW80", doc.getMomPpo1226PctileW80());
        if (doc.getMomPpoHist12269Zsc80() != null) update.set("momPpoHist12269Zsc80", doc.getMomPpoHist12269Zsc80());
        if (doc.getMomPpoHist12269PctileW80() != null) update.set("momPpoHist12269PctileW80", doc.getMomPpoHist12269PctileW80());
        if (doc.getMomPpoHist12269Shock1() != null) update.set("momPpoHist12269Shock1", doc.getMomPpoHist12269Shock1());
        if (doc.getMomPpoHist12269Shock1Stdn80() != null) update.set("momPpoHist12269Shock1Stdn80", doc.getMomPpoHist12269Shock1Stdn80());
        if (doc.getMomPpoRegimeState() != null) update.set("momPpoRegimeState", doc.getMomPpoRegimeState());
        if (doc.getMomPpoRegimePrstW20() != null) update.set("momPpoRegimePrstW20", doc.getMomPpoRegimePrstW20());

        // PPO 48/104
        if (doc.getMomPpo48104() != null) update.set("momPpo48104", doc.getMomPpo48104());
        if (doc.getMomPpoSig481049() != null) update.set("momPpoSig481049", doc.getMomPpoSig481049());
        if (doc.getMomPpoHist481049() != null) update.set("momPpoHist481049", doc.getMomPpoHist481049());
        if (doc.getMomPpo48104Dlt() != null) update.set("momPpo48104Dlt", doc.getMomPpo48104Dlt());
        if (doc.getMomPpoHist481049Dlt() != null) update.set("momPpoHist481049Dlt", doc.getMomPpoHist481049Dlt());
        if (doc.getMomPpo48104Zsc80() != null) update.set("momPpo48104Zsc80", doc.getMomPpo48104Zsc80());
        if (doc.getMomPpo48104PctileW80() != null) update.set("momPpo48104PctileW80", doc.getMomPpo48104PctileW80());
        if (doc.getMomPpoHist481049Zsc80() != null) update.set("momPpoHist481049Zsc80", doc.getMomPpoHist481049Zsc80());
        if (doc.getMomPpoHist481049PctileW80() != null) update.set("momPpoHist481049PctileW80", doc.getMomPpoHist481049PctileW80());
        if (doc.getMomPpoHist481049Shock1() != null) update.set("momPpoHist481049Shock1", doc.getMomPpoHist481049Shock1());
        if (doc.getMomPpoHist481049Shock1Stdn80() != null) update.set("momPpoHist481049Shock1Stdn80", doc.getMomPpoHist481049Shock1Stdn80());
        if (doc.getMomPpo48104RegimeState() != null) update.set("momPpo48104RegimeState", doc.getMomPpo48104RegimeState());
        if (doc.getMomPpo48104RegimePrstW20() != null) update.set("momPpo48104RegimePrstW20", doc.getMomPpo48104RegimePrstW20());

        // PPO 288/576
        if (doc.getMomPpo288576() != null) update.set("momPpo288576", doc.getMomPpo288576());
        if (doc.getMomPpoSig2885769() != null) update.set("momPpoSig2885769", doc.getMomPpoSig2885769());
        if (doc.getMomPpoHist2885769() != null) update.set("momPpoHist2885769", doc.getMomPpoHist2885769());
        if (doc.getMomPpo288576Dlt() != null) update.set("momPpo288576Dlt", doc.getMomPpo288576Dlt());
        if (doc.getMomPpoHist2885769Dlt() != null) update.set("momPpoHist2885769Dlt", doc.getMomPpoHist2885769Dlt());
        if (doc.getMomPpo288576Zsc80() != null) update.set("momPpo288576Zsc80", doc.getMomPpo288576Zsc80());
        if (doc.getMomPpo288576PctileW80() != null) update.set("momPpo288576PctileW80", doc.getMomPpo288576PctileW80());
        if (doc.getMomPpoHist2885769Zsc80() != null) update.set("momPpoHist2885769Zsc80", doc.getMomPpoHist2885769Zsc80());
        if (doc.getMomPpoHist2885769PctileW80() != null) update.set("momPpoHist2885769PctileW80", doc.getMomPpoHist2885769PctileW80());
        if (doc.getMomPpoHist2885769Shock1() != null) update.set("momPpoHist2885769Shock1", doc.getMomPpoHist2885769Shock1());
        if (doc.getMomPpoHist2885769Shock1Stdn80() != null) update.set("momPpoHist2885769Shock1Stdn80", doc.getMomPpoHist2885769Shock1Stdn80());
        if (doc.getMomPpo288576RegimeState() != null) update.set("momPpo288576RegimeState", doc.getMomPpo288576RegimeState());
        if (doc.getMomPpo288576RegimePrstW20() != null) update.set("momPpo288576RegimePrstW20", doc.getMomPpo288576RegimePrstW20());

        // close slp
        if (doc.getMomClose3Slp() != null) update.set("momClose3Slp", doc.getMomClose3Slp());
        if (doc.getMomClose8Slp() != null) update.set("momClose8Slp", doc.getMomClose8Slp());
        if (doc.getMomClose14Slp() != null) update.set("momClose14Slp", doc.getMomClose14Slp());
        if (doc.getMomClose50Slp() != null) update.set("momClose50Slp", doc.getMomClose50Slp());
        if (doc.getMomClose3SlpAtrn() != null) update.set("momClose3SlpAtrn", doc.getMomClose3SlpAtrn());
        if (doc.getMomClose8SlpAtrn() != null) update.set("momClose8SlpAtrn", doc.getMomClose8SlpAtrn());
        if (doc.getMomClose14SlpAtrn() != null) update.set("momClose14SlpAtrn", doc.getMomClose14SlpAtrn());
        if (doc.getMomClose50SlpAtrn() != null) update.set("momClose50SlpAtrn", doc.getMomClose50SlpAtrn());
        if (doc.getMomClose3SlpAcc() != null) update.set("momClose3SlpAcc", doc.getMomClose3SlpAcc());
        if (doc.getMomClose8SlpAcc() != null) update.set("momClose8SlpAcc", doc.getMomClose8SlpAcc());
        if (doc.getMomClose14SlpAcc() != null) update.set("momClose14SlpAcc", doc.getMomClose14SlpAcc());
        if (doc.getMomClose3SlpAccAtrn() != null) update.set("momClose3SlpAccAtrn", doc.getMomClose3SlpAccAtrn());
        if (doc.getMomClose8SlpAccAtrn() != null) update.set("momClose8SlpAccAtrn", doc.getMomClose8SlpAccAtrn());
        if (doc.getMomClose14SlpAccAtrn() != null) update.set("momClose14SlpAccAtrn", doc.getMomClose14SlpAccAtrn());
        if (doc.getMomClose3Zsc() != null) update.set("momClose3Zsc", doc.getMomClose3Zsc());
        if (doc.getMomClose8Zsc() != null) update.set("momClose8Zsc", doc.getMomClose8Zsc());
        if (doc.getMomClose14Zsc() != null) update.set("momClose14Zsc", doc.getMomClose14Zsc());
        if (doc.getMomClose50Zsc() != null) update.set("momClose50Zsc", doc.getMomClose50Zsc());

        // close slp 48/288
        if (doc.getMomClose48Slp() != null) update.set("momClose48Slp", doc.getMomClose48Slp());
        if (doc.getMomClose288Slp() != null) update.set("momClose288Slp", doc.getMomClose288Slp());
        if (doc.getMomClose48SlpAtrn() != null) update.set("momClose48SlpAtrn", doc.getMomClose48SlpAtrn());
        if (doc.getMomClose288SlpAtrn() != null) update.set("momClose288SlpAtrn", doc.getMomClose288SlpAtrn());
        if (doc.getMomClose48SlpAcc() != null) update.set("momClose48SlpAcc", doc.getMomClose48SlpAcc());
        if (doc.getMomClose288SlpAcc() != null) update.set("momClose288SlpAcc", doc.getMomClose288SlpAcc());
        if (doc.getMomClose48SlpAccAtrn() != null) update.set("momClose48SlpAccAtrn", doc.getMomClose48SlpAccAtrn());
        if (doc.getMomClose288SlpAccAtrn() != null) update.set("momClose288SlpAccAtrn", doc.getMomClose288SlpAccAtrn());
        if (doc.getMomClose48Zsc() != null) update.set("momClose48Zsc", doc.getMomClose48Zsc());
        if (doc.getMomClose288Zsc() != null) update.set("momClose288Zsc", doc.getMomClose288Zsc());

        // CCI
        if (doc.getMomCci14() != null) update.set("momCci14", doc.getMomCci14());
        if (doc.getMomCci20() != null) update.set("momCci20", doc.getMomCci20());
        if (doc.getMomCci48() != null) update.set("momCci48", doc.getMomCci48());
        if (doc.getMomCci288() != null) update.set("momCci288", doc.getMomCci288());
        if (doc.getMomCci14Dlt() != null) update.set("momCci14Dlt", doc.getMomCci14Dlt());
        if (doc.getMomCci20Dlt() != null) update.set("momCci20Dlt", doc.getMomCci20Dlt());
        if (doc.getMomCci48Dlt() != null) update.set("momCci48Dlt", doc.getMomCci48Dlt());
        if (doc.getMomCci288Dlt() != null) update.set("momCci288Dlt", doc.getMomCci288Dlt());
        if (doc.getMomCci14DstMid() != null) update.set("momCci14DstMid", doc.getMomCci14DstMid());
        if (doc.getMomCci20DstMid() != null) update.set("momCci20DstMid", doc.getMomCci20DstMid());
        if (doc.getMomCci48DstMid() != null) update.set("momCci48DstMid", doc.getMomCci48DstMid());
        if (doc.getMomCci288DstMid() != null) update.set("momCci288DstMid", doc.getMomCci288DstMid());
        if (doc.getMomCci20Zsc80() != null) update.set("momCci20Zsc80", doc.getMomCci20Zsc80());
        if (doc.getMomCci20PctileW80() != null) update.set("momCci20PctileW80", doc.getMomCci20PctileW80());
        if (doc.getMomCci48Zsc80() != null) update.set("momCci48Zsc80", doc.getMomCci48Zsc80());
        if (doc.getMomCci48PctileW80() != null) update.set("momCci48PctileW80", doc.getMomCci48PctileW80());
        if (doc.getMomCci288Zsc80() != null) update.set("momCci288Zsc80", doc.getMomCci288Zsc80());
        if (doc.getMomCci288PctileW80() != null) update.set("momCci288PctileW80", doc.getMomCci288PctileW80());
        if (doc.getMomCci20Shock1() != null) update.set("momCci20Shock1", doc.getMomCci20Shock1());
        if (doc.getMomCci20Shock1Stdn80() != null) update.set("momCci20Shock1Stdn80", doc.getMomCci20Shock1Stdn80());
        if (doc.getMomCci48Shock1() != null) update.set("momCci48Shock1", doc.getMomCci48Shock1());
        if (doc.getMomCci48Shock1Stdn80() != null) update.set("momCci48Shock1Stdn80", doc.getMomCci48Shock1Stdn80());
        if (doc.getMomCci288Shock1() != null) update.set("momCci288Shock1", doc.getMomCci288Shock1());
        if (doc.getMomCci288Shock1Stdn80() != null) update.set("momCci288Shock1Stdn80", doc.getMomCci288Shock1Stdn80());
        if (doc.getMomCci20RegimeState() != null) update.set("momCci20RegimeState", doc.getMomCci20RegimeState());
        if (doc.getMomCci20RegimePrstW20() != null) update.set("momCci20RegimePrstW20", doc.getMomCci20RegimePrstW20());
        if (doc.getMomCci48RegimeState() != null) update.set("momCci48RegimeState", doc.getMomCci48RegimeState());
        if (doc.getMomCci48RegimePrstW20() != null) update.set("momCci48RegimePrstW20", doc.getMomCci48RegimePrstW20());
        if (doc.getMomCci288RegimeState() != null) update.set("momCci288RegimeState", doc.getMomCci288RegimeState());
        if (doc.getMomCci288RegimePrstW20() != null) update.set("momCci288RegimePrstW20", doc.getMomCci288RegimePrstW20());

        // ROC
        if (doc.getMomRoc1() != null) update.set("momRoc1", doc.getMomRoc1());
        if (doc.getMomRoc2() != null) update.set("momRoc2", doc.getMomRoc2());
        if (doc.getMomRoc3() != null) update.set("momRoc3", doc.getMomRoc3());
        if (doc.getMomRoc5() != null) update.set("momRoc5", doc.getMomRoc5());
        if (doc.getMomRoc48() != null) update.set("momRoc48", doc.getMomRoc48());
        if (doc.getMomRoc288() != null) update.set("momRoc288", doc.getMomRoc288());
        if (doc.getMomRoc1Abs() != null) update.set("momRoc1Abs", doc.getMomRoc1Abs());
        if (doc.getMomRoc2Abs() != null) update.set("momRoc2Abs", doc.getMomRoc2Abs());
        if (doc.getMomRoc3Abs() != null) update.set("momRoc3Abs", doc.getMomRoc3Abs());
        if (doc.getMomRoc5Abs() != null) update.set("momRoc5Abs", doc.getMomRoc5Abs());
        if (doc.getMomRoc48Abs() != null) update.set("momRoc48Abs", doc.getMomRoc48Abs());
        if (doc.getMomRoc288Abs() != null) update.set("momRoc288Abs", doc.getMomRoc288Abs());
        if (doc.getMomRoc5Zsc80() != null) update.set("momRoc5Zsc80", doc.getMomRoc5Zsc80());
        if (doc.getMomRoc5PctileW80() != null) update.set("momRoc5PctileW80", doc.getMomRoc5PctileW80());
        if (doc.getMomRoc48Zsc80() != null) update.set("momRoc48Zsc80", doc.getMomRoc48Zsc80());
        if (doc.getMomRoc48PctileW80() != null) update.set("momRoc48PctileW80", doc.getMomRoc48PctileW80());
        if (doc.getMomRoc288Zsc80() != null) update.set("momRoc288Zsc80", doc.getMomRoc288Zsc80());
        if (doc.getMomRoc288PctileW80() != null) update.set("momRoc288PctileW80", doc.getMomRoc288PctileW80());
        if (doc.getMomRoc5Shock1() != null) update.set("momRoc5Shock1", doc.getMomRoc5Shock1());
        if (doc.getMomRoc5Shock1Stdn80() != null) update.set("momRoc5Shock1Stdn80", doc.getMomRoc5Shock1Stdn80());
        if (doc.getMomRoc48Shock1() != null) update.set("momRoc48Shock1", doc.getMomRoc48Shock1());
        if (doc.getMomRoc48Shock1Stdn80() != null) update.set("momRoc48Shock1Stdn80", doc.getMomRoc48Shock1Stdn80());
        if (doc.getMomRoc288Shock1() != null) update.set("momRoc288Shock1", doc.getMomRoc288Shock1());
        if (doc.getMomRoc288Shock1Stdn80() != null) update.set("momRoc288Shock1Stdn80", doc.getMomRoc288Shock1Stdn80());
        if (doc.getMomRoc5RegimeState() != null) update.set("momRoc5RegimeState", doc.getMomRoc5RegimeState());
        if (doc.getMomRoc5RegimePrstW20() != null) update.set("momRoc5RegimePrstW20", doc.getMomRoc5RegimePrstW20());
        if (doc.getMomRoc48RegimeState() != null) update.set("momRoc48RegimeState", doc.getMomRoc48RegimeState());
        if (doc.getMomRoc48RegimePrstW20() != null) update.set("momRoc48RegimePrstW20", doc.getMomRoc48RegimePrstW20());
        if (doc.getMomRoc288RegimeState() != null) update.set("momRoc288RegimeState", doc.getMomRoc288RegimeState());
        if (doc.getMomRoc288RegimePrstW20() != null) update.set("momRoc288RegimePrstW20", doc.getMomRoc288RegimePrstW20());

        // Alignment
        if (doc.getMomAlignRsi14PpoHist12269() != null) update.set("momAlignRsi14PpoHist12269", doc.getMomAlignRsi14PpoHist12269());
        if (doc.getMomAlignRsi48PpoHist481049() != null) update.set("momAlignRsi48PpoHist481049", doc.getMomAlignRsi48PpoHist481049());
        if (doc.getMomAlignRsi288PpoHist2885769() != null) update.set("momAlignRsi288PpoHist2885769", doc.getMomAlignRsi288PpoHist2885769());
        if (doc.getMomAlignTrixHist9TsiHist2513() != null) update.set("momAlignTrixHist9TsiHist2513", doc.getMomAlignTrixHist9TsiHist2513());
        if (doc.getMomAlignTrixHist48TsiHist4825() != null) update.set("momAlignTrixHist48TsiHist4825", doc.getMomAlignTrixHist48TsiHist4825());
        if (doc.getMomAlignTrixHist288TsiHist288144() != null) update.set("momAlignTrixHist288TsiHist288144", doc.getMomAlignTrixHist288TsiHist288144());

        // Consensus / Chop
        if (doc.getMomMomentumConsensusScore() != null) update.set("momMomentumConsensusScore", doc.getMomMomentumConsensusScore());
        if (doc.getMomChopScore() != null) update.set("momChopScore", doc.getMomChopScore());

        // Consensus dynamics
        if (doc.getMomMomentumConflictScore() != null) update.set("momMomentumConflictScore", doc.getMomMomentumConflictScore());
        if (doc.getMomMomentumConsensusDlt() != null) update.set("momMomentumConsensusDlt", doc.getMomMomentumConsensusDlt());
        if (doc.getMomMomentumConsensusPrstW20() != null) update.set("momMomentumConsensusPrstW20", doc.getMomMomentumConsensusPrstW20());

        // PPO hist flip rate
        if (doc.getMomPpoHist12269FlipRateW20() != null) update.set("momPpoHist12269FlipRateW20", doc.getMomPpoHist12269FlipRateW20());
        if (doc.getMomPpoHist481049FlipRateW20() != null) update.set("momPpoHist481049FlipRateW20", doc.getMomPpoHist481049FlipRateW20());
        if (doc.getMomPpoHist2885769FlipRateW20() != null) update.set("momPpoHist2885769FlipRateW20", doc.getMomPpoHist2885769FlipRateW20());

        // TRIX hist flip rate
        if (doc.getMomTrixHist9FlipRateW20() != null) update.set("momTrixHist9FlipRateW20", doc.getMomTrixHist9FlipRateW20());
        if (doc.getMomTrixHist48FlipRateW20() != null) update.set("momTrixHist48FlipRateW20", doc.getMomTrixHist48FlipRateW20());
        if (doc.getMomTrixHist288FlipRateW20() != null) update.set("momTrixHist288FlipRateW20", doc.getMomTrixHist288FlipRateW20());

        // TSI hist flip rate
        if (doc.getMomTsiHist2513FlipRateW20() != null) update.set("momTsiHist2513FlipRateW20", doc.getMomTsiHist2513FlipRateW20());
        if (doc.getMomTsiHist4825FlipRateW20() != null) update.set("momTsiHist4825FlipRateW20", doc.getMomTsiHist4825FlipRateW20());
        if (doc.getMomTsiHist288144FlipRateW20() != null) update.set("momTsiHist288144FlipRateW20", doc.getMomTsiHist288144FlipRateW20());

        // PPO hist coherence
        if (doc.getMomPpoHistCoh1226Vs48104() != null) update.set("momPpoHistCoh1226Vs48104", doc.getMomPpoHistCoh1226Vs48104());
        if (doc.getMomPpoHistCoh1226Vs288576() != null) update.set("momPpoHistCoh1226Vs288576", doc.getMomPpoHistCoh1226Vs288576());

        // TRIX hist coherence
        if (doc.getMomTrixHistCoh9Vs48() != null) update.set("momTrixHistCoh9Vs48", doc.getMomTrixHistCoh9Vs48());

        // TSI hist coherence
        if (doc.getMomTsiHistCoh2513Vs4825() != null) update.set("momTsiHistCoh2513Vs4825", doc.getMomTsiHistCoh2513Vs4825());

        // Consensus slp/flip
        if (doc.getMomMomentumConsensusSlpW20() != null) update.set("momMomentumConsensusSlpW20", doc.getMomMomentumConsensusSlpW20());
        if (doc.getMomMomentumConsensusFlipRateW20() != null) update.set("momMomentumConsensusFlipRateW20", doc.getMomMomentumConsensusFlipRateW20());

        // Divergence
        if (doc.getMomDivCloseSlp48VsPpoHist48() != null) update.set("momDivCloseSlp48VsPpoHist48", doc.getMomDivCloseSlp48VsPpoHist48());
        if (doc.getMomDivCloseSlp48VsRsi48() != null) update.set("momDivCloseSlp48VsRsi48", doc.getMomDivCloseSlp48VsRsi48());
        if (doc.getMomDivCloseSlp288VsPpoHist288() != null) update.set("momDivCloseSlp288VsPpoHist288", doc.getMomDivCloseSlp288VsPpoHist288());

        // Consensus vol/shock
        if (doc.getMomMomentumConsensusVolW20() != null) update.set("momMomentumConsensusVolW20", doc.getMomMomentumConsensusVolW20());
        if (doc.getMomMomentumConsensusShock1() != null) update.set("momMomentumConsensusShock1", doc.getMomMomentumConsensusShock1());
        if (doc.getMomMomentumConsensusShock1StdnW20() != null) update.set("momMomentumConsensusShock1StdnW20", doc.getMomMomentumConsensusShock1StdnW20());

        // Consensus counts
        if (doc.getMomMomentumDisagreementCount() != null) update.set("momMomentumDisagreementCount", doc.getMomMomentumDisagreementCount());
        if (doc.getMomMomentumAgreementCount() != null) update.set("momMomentumAgreementCount", doc.getMomMomentumAgreementCount());

        // Consensus quality
        if (doc.getMomMomentumConsensusAbs() != null) update.set("momMomentumConsensusAbs", doc.getMomMomentumConsensusAbs());
        if (doc.getMomMomentumNeutralCount() != null) update.set("momMomentumNeutralCount", doc.getMomMomentumNeutralCount());
        if (doc.getMomMomentumVoteEntropy() != null) update.set("momMomentumVoteEntropy", doc.getMomMomentumVoteEntropy());
        if (doc.getMomMomentumConsensusRunLen() != null) update.set("momMomentumConsensusRunLen", doc.getMomMomentumConsensusRunLen());
        if (doc.getMomMomentumSignalQualityScore() != null) update.set("momMomentumSignalQualityScore", doc.getMomMomentumSignalQualityScore());

        // PPO hist slp w20
        if (doc.getMomPpoHist12269SlpW20() != null) update.set("momPpoHist12269SlpW20", doc.getMomPpoHist12269SlpW20());
        if (doc.getMomPpoHist481049SlpW20() != null) update.set("momPpoHist481049SlpW20", doc.getMomPpoHist481049SlpW20());
        if (doc.getMomPpoHist2885769SlpW20() != null) update.set("momPpoHist2885769SlpW20", doc.getMomPpoHist2885769SlpW20());

        // Consensus extremes
        if (doc.getMomMomentumConsensusZsc80() != null) update.set("momMomentumConsensusZsc80", doc.getMomMomentumConsensusZsc80());
        if (doc.getMomMomentumConsensusPctileW80() != null) update.set("momMomentumConsensusPctileW80", doc.getMomMomentumConsensusPctileW80());

        // TRIX hist slp w20
        if (doc.getMomTrixHist9SlpW20() != null) update.set("momTrixHist9SlpW20", doc.getMomTrixHist9SlpW20());
        if (doc.getMomTrixHist48SlpW20() != null) update.set("momTrixHist48SlpW20", doc.getMomTrixHist48SlpW20());
        if (doc.getMomTrixHist288SlpW20() != null) update.set("momTrixHist288SlpW20", doc.getMomTrixHist288SlpW20());

        // TSI hist slp w20
        if (doc.getMomTsiHist2513SlpW20() != null) update.set("momTsiHist2513SlpW20", doc.getMomTsiHist2513SlpW20());
        if (doc.getMomTsiHist4825SlpW20() != null) update.set("momTsiHist4825SlpW20", doc.getMomTsiHist4825SlpW20());
        if (doc.getMomTsiHist288144SlpW20() != null) update.set("momTsiHist288144SlpW20", doc.getMomTsiHist288144SlpW20());

        return update;
    }

    public static String collectionName(CandleIntervals interval) {
        return switch (interval) {
            case I1_MN -> "mom_1";
            case I5_MN -> "mom_5";
            case I15_MN -> "mom_15";
            case I30_MN -> "mom_30";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
    }
}
