package br.com.yacamin.rafael.adapter.out.persistence.mikhael;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.VolumeIndicatorDocument;
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
public class VolumeIndicatorMongoRepository {

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

    public Optional<VolumeIndicatorDocument> findBySymbolAndOpenTime(String symbol, Instant openTime, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").is(openTime));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, VolumeIndicatorDocument.class, collectionName(interval)));
    }

    public List<VolumeIndicatorDocument> findByRangeExclEnd(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        return mongoTemplate.find(query, VolumeIndicatorDocument.class, collectionName(interval));
    }

    public List<Instant> findOpenTimesByRange(String symbol, Instant start, Instant end, CandleIntervals interval) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").gte(start).lt(end))
                .with(Sort.by(Sort.Direction.ASC, "openTime"));
        query.fields().include("openTime").exclude("_id");

        return mongoTemplate.find(query, VolumeIndicatorDocument.class, collectionName(interval))
                .stream()
                .map(VolumeIndicatorDocument::getOpenTime)
                .toList();
    }

    public void save(VolumeIndicatorDocument doc, CandleIntervals interval) {
        String collection = collectionName(interval);
        Query query = new Query(Criteria.where("symbol").is(doc.getSymbol())
                .and("openTime").is(doc.getOpenTime()));
        mongoTemplate.upsert(query, buildUpdate(doc), collection);
    }

    public void saveBatch(List<VolumeIndicatorDocument> documents, CandleIntervals interval) {
        if (documents.isEmpty()) return;
        String collection = collectionName(interval);
        var bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        int count = 0;
        for (VolumeIndicatorDocument doc : documents) {
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

    private Update buildUpdate(VolumeIndicatorDocument doc) {
        Update update = new Update();

        // 1) RAW MICROSTRUCTURE
        if (doc.getVolVolume() != null) update.set("volVolume", doc.getVolVolume());
        if (doc.getVolQuoteVolume() != null) update.set("volQuoteVolume", doc.getVolQuoteVolume());
        if (doc.getVolNumberOfTrades() != null) update.set("volNumberOfTrades", doc.getVolNumberOfTrades());
        if (doc.getVolTakerBuyBaseVolume() != null) update.set("volTakerBuyBaseVolume", doc.getVolTakerBuyBaseVolume());
        if (doc.getVolTakerSellBaseVolume() != null) update.set("volTakerSellBaseVolume", doc.getVolTakerSellBaseVolume());
        if (doc.getVolTakerBuyQuoteVolume() != null) update.set("volTakerBuyQuoteVolume", doc.getVolTakerBuyQuoteVolume());
        if (doc.getVolTakerSellQuoteVolume() != null) update.set("volTakerSellQuoteVolume", doc.getVolTakerSellQuoteVolume());
        if (doc.getVolTakerBuyRatio() != null) update.set("volTakerBuyRatio", doc.getVolTakerBuyRatio());
        if (doc.getVolTakerBuySellImbalance() != null) update.set("volTakerBuySellImbalance", doc.getVolTakerBuySellImbalance());
        if (doc.getVolLogVolume() != null) update.set("volLogVolume", doc.getVolLogVolume());

        // 1.1) TAKER PRESSURE DYNAMICS
        if (doc.getVolTakerBuyRatioRel16() != null) update.set("volTakerBuyRatioRel16", doc.getVolTakerBuyRatioRel16());
        if (doc.getVolTakerBuyRatioRel48() != null) update.set("volTakerBuyRatioRel48", doc.getVolTakerBuyRatioRel48());
        if (doc.getVolTakerBuyRatioRel96() != null) update.set("volTakerBuyRatioRel96", doc.getVolTakerBuyRatioRel96());
        if (doc.getVolTakerBuyRatioRel288() != null) update.set("volTakerBuyRatioRel288", doc.getVolTakerBuyRatioRel288());
        if (doc.getVolTakerBuyRatioZscore32() != null) update.set("volTakerBuyRatioZscore32", doc.getVolTakerBuyRatioZscore32());
        if (doc.getVolTakerBuyRatioZscore96() != null) update.set("volTakerBuyRatioZscore96", doc.getVolTakerBuyRatioZscore96());
        if (doc.getVolTakerBuyRatioZscore288() != null) update.set("volTakerBuyRatioZscore288", doc.getVolTakerBuyRatioZscore288());
        if (doc.getVolTakerBuyRatioSlpW20() != null) update.set("volTakerBuyRatioSlpW20", doc.getVolTakerBuyRatioSlpW20());
        if (doc.getVolTakerBuyRatioFlipRateW20() != null) update.set("volTakerBuyRatioFlipRateW20", doc.getVolTakerBuyRatioFlipRateW20());
        if (doc.getVolTakerBuyRatioPrstW20() != null) update.set("volTakerBuyRatioPrstW20", doc.getVolTakerBuyRatioPrstW20());
        if (doc.getVolTakerBuySellImbalanceZscore32() != null) update.set("volTakerBuySellImbalanceZscore32", doc.getVolTakerBuySellImbalanceZscore32());
        if (doc.getVolTakerBuySellImbalanceZscore96() != null) update.set("volTakerBuySellImbalanceZscore96", doc.getVolTakerBuySellImbalanceZscore96());
        if (doc.getVolTakerBuySellImbalanceSlpW20() != null) update.set("volTakerBuySellImbalanceSlpW20", doc.getVolTakerBuySellImbalanceSlpW20());
        if (doc.getVolTakerBuySellImbalanceFlipRateW20() != null) update.set("volTakerBuySellImbalanceFlipRateW20", doc.getVolTakerBuySellImbalanceFlipRateW20());
        if (doc.getVolTakerBuySellImbalancePrstW20() != null) update.set("volTakerBuySellImbalancePrstW20", doc.getVolTakerBuySellImbalancePrstW20());

        // 2) RELATIVE VOLUME (SMA-based)
        if (doc.getVolVolumeRel16() != null) update.set("volVolumeRel16", doc.getVolVolumeRel16());
        if (doc.getVolVolumeRel32() != null) update.set("volVolumeRel32", doc.getVolVolumeRel32());
        if (doc.getVolVolumeRel48() != null) update.set("volVolumeRel48", doc.getVolVolumeRel48());
        if (doc.getVolVolumeRel96() != null) update.set("volVolumeRel96", doc.getVolVolumeRel96());
        if (doc.getVolVolumeRel288() != null) update.set("volVolumeRel288", doc.getVolVolumeRel288());
        if (doc.getVolTradesRel16() != null) update.set("volTradesRel16", doc.getVolTradesRel16());
        if (doc.getVolTradesRel32() != null) update.set("volTradesRel32", doc.getVolTradesRel32());
        if (doc.getVolTradesRel48() != null) update.set("volTradesRel48", doc.getVolTradesRel48());
        if (doc.getVolTradesRel96() != null) update.set("volTradesRel96", doc.getVolTradesRel96());
        if (doc.getVolTradesRel288() != null) update.set("volTradesRel288", doc.getVolTradesRel288());
        if (doc.getVolQuoteVolumeRel16() != null) update.set("volQuoteVolumeRel16", doc.getVolQuoteVolumeRel16());
        if (doc.getVolQuoteVolumeRel32() != null) update.set("volQuoteVolumeRel32", doc.getVolQuoteVolumeRel32());
        if (doc.getVolQuoteVolumeRel48() != null) update.set("volQuoteVolumeRel48", doc.getVolQuoteVolumeRel48());
        if (doc.getVolQuoteVolumeRel96() != null) update.set("volQuoteVolumeRel96", doc.getVolQuoteVolumeRel96());
        if (doc.getVolQuoteVolumeRel288() != null) update.set("volQuoteVolumeRel288", doc.getVolQuoteVolumeRel288());

        // 3) Z-SCORES
        if (doc.getVolVolumeZscore32() != null) update.set("volVolumeZscore32", doc.getVolVolumeZscore32());
        if (doc.getVolVolumeZscore48() != null) update.set("volVolumeZscore48", doc.getVolVolumeZscore48());
        if (doc.getVolVolumeZscore96() != null) update.set("volVolumeZscore96", doc.getVolVolumeZscore96());
        if (doc.getVolVolumeZscore288() != null) update.set("volVolumeZscore288", doc.getVolVolumeZscore288());
        if (doc.getVolTradesZscore32() != null) update.set("volTradesZscore32", doc.getVolTradesZscore32());
        if (doc.getVolTradesZscore48() != null) update.set("volTradesZscore48", doc.getVolTradesZscore48());
        if (doc.getVolTradesZscore96() != null) update.set("volTradesZscore96", doc.getVolTradesZscore96());
        if (doc.getVolTradesZscore288() != null) update.set("volTradesZscore288", doc.getVolTradesZscore288());
        if (doc.getVolQuoteVolumeZscore32() != null) update.set("volQuoteVolumeZscore32", doc.getVolQuoteVolumeZscore32());
        if (doc.getVolQuoteVolumeZscore48() != null) update.set("volQuoteVolumeZscore48", doc.getVolQuoteVolumeZscore48());
        if (doc.getVolQuoteVolumeZscore96() != null) update.set("volQuoteVolumeZscore96", doc.getVolQuoteVolumeZscore96());
        if (doc.getVolQuoteVolumeZscore288() != null) update.set("volQuoteVolumeZscore288", doc.getVolQuoteVolumeZscore288());

        // 4) DELTAS (short-term)
        if (doc.getVolVolumeDelta1() != null) update.set("volVolumeDelta1", doc.getVolVolumeDelta1());
        if (doc.getVolVolumeDelta3() != null) update.set("volVolumeDelta3", doc.getVolVolumeDelta3());
        if (doc.getVolTradesDelta1() != null) update.set("volTradesDelta1", doc.getVolTradesDelta1());
        if (doc.getVolTradesDelta3() != null) update.set("volTradesDelta3", doc.getVolTradesDelta3());
        if (doc.getVolQuoteVolumeDelta1() != null) update.set("volQuoteVolumeDelta1", doc.getVolQuoteVolumeDelta1());
        if (doc.getVolQuoteVolumeDelta3() != null) update.set("volQuoteVolumeDelta3", doc.getVolQuoteVolumeDelta3());

        // 5) ACTIVITY PRESSURE (TRADES)
        if (doc.getVolActTradesSp16() != null) update.set("volActTradesSp16", doc.getVolActTradesSp16());
        if (doc.getVolActTradesSp32() != null) update.set("volActTradesSp32", doc.getVolActTradesSp32());
        if (doc.getVolActTradesSp48() != null) update.set("volActTradesSp48", doc.getVolActTradesSp48());
        if (doc.getVolActTradesSp96() != null) update.set("volActTradesSp96", doc.getVolActTradesSp96());
        if (doc.getVolActTradesSp288() != null) update.set("volActTradesSp288", doc.getVolActTradesSp288());
        if (doc.getVolActTradesAcc16() != null) update.set("volActTradesAcc16", doc.getVolActTradesAcc16());
        if (doc.getVolActTradesAcc32() != null) update.set("volActTradesAcc32", doc.getVolActTradesAcc32());
        if (doc.getVolActTradesAcc48() != null) update.set("volActTradesAcc48", doc.getVolActTradesAcc48());
        if (doc.getVolActTradesAcc96() != null) update.set("volActTradesAcc96", doc.getVolActTradesAcc96());
        if (doc.getVolActTradesAcc288() != null) update.set("volActTradesAcc288", doc.getVolActTradesAcc288());
        if (doc.getVolActTradesChop16() != null) update.set("volActTradesChop16", doc.getVolActTradesChop16());
        if (doc.getVolActTradesChop32() != null) update.set("volActTradesChop32", doc.getVolActTradesChop32());
        if (doc.getVolActTradesChop48() != null) update.set("volActTradesChop48", doc.getVolActTradesChop48());
        if (doc.getVolActTradesChop96() != null) update.set("volActTradesChop96", doc.getVolActTradesChop96());
        if (doc.getVolActTradesChop288() != null) update.set("volActTradesChop288", doc.getVolActTradesChop288());

        // 5) ACTIVITY PRESSURE (QUOTE)
        if (doc.getVolActQuoteSp16() != null) update.set("volActQuoteSp16", doc.getVolActQuoteSp16());
        if (doc.getVolActQuoteSp32() != null) update.set("volActQuoteSp32", doc.getVolActQuoteSp32());
        if (doc.getVolActQuoteSp48() != null) update.set("volActQuoteSp48", doc.getVolActQuoteSp48());
        if (doc.getVolActQuoteSp96() != null) update.set("volActQuoteSp96", doc.getVolActQuoteSp96());
        if (doc.getVolActQuoteSp288() != null) update.set("volActQuoteSp288", doc.getVolActQuoteSp288());
        if (doc.getVolActQuoteAcc16() != null) update.set("volActQuoteAcc16", doc.getVolActQuoteAcc16());
        if (doc.getVolActQuoteAcc32() != null) update.set("volActQuoteAcc32", doc.getVolActQuoteAcc32());
        if (doc.getVolActQuoteAcc48() != null) update.set("volActQuoteAcc48", doc.getVolActQuoteAcc48());
        if (doc.getVolActQuoteAcc96() != null) update.set("volActQuoteAcc96", doc.getVolActQuoteAcc96());
        if (doc.getVolActQuoteAcc288() != null) update.set("volActQuoteAcc288", doc.getVolActQuoteAcc288());
        if (doc.getVolActQuoteChop16() != null) update.set("volActQuoteChop16", doc.getVolActQuoteChop16());
        if (doc.getVolActQuoteChop32() != null) update.set("volActQuoteChop32", doc.getVolActQuoteChop32());
        if (doc.getVolActQuoteChop48() != null) update.set("volActQuoteChop48", doc.getVolActQuoteChop48());
        if (doc.getVolActQuoteChop96() != null) update.set("volActQuoteChop96", doc.getVolActQuoteChop96());
        if (doc.getVolActQuoteChop288() != null) update.set("volActQuoteChop288", doc.getVolActQuoteChop288());

        // 6) MICROBURST / SPIKE
        if (doc.getVolVolumeSpikeScore16() != null) update.set("volVolumeSpikeScore16", doc.getVolVolumeSpikeScore16());
        if (doc.getVolTradesSpikeScore16() != null) update.set("volTradesSpikeScore16", doc.getVolTradesSpikeScore16());
        if (doc.getVolMicroburstVolumeIntensity16() != null) update.set("volMicroburstVolumeIntensity16", doc.getVolMicroburstVolumeIntensity16());
        if (doc.getVolMicroburstTradesIntensity16() != null) update.set("volMicroburstTradesIntensity16", doc.getVolMicroburstTradesIntensity16());
        if (doc.getVolMicroburstCombo16() != null) update.set("volMicroburstCombo16", doc.getVolMicroburstCombo16());
        if (doc.getVolVolumeSpikeScore32() != null) update.set("volVolumeSpikeScore32", doc.getVolVolumeSpikeScore32());
        if (doc.getVolVolumeSpikeScore48() != null) update.set("volVolumeSpikeScore48", doc.getVolVolumeSpikeScore48());
        if (doc.getVolVolumeSpikeScore96() != null) update.set("volVolumeSpikeScore96", doc.getVolVolumeSpikeScore96());
        if (doc.getVolTradesSpikeScore32() != null) update.set("volTradesSpikeScore32", doc.getVolTradesSpikeScore32());
        if (doc.getVolTradesSpikeScore48() != null) update.set("volTradesSpikeScore48", doc.getVolTradesSpikeScore48());
        if (doc.getVolTradesSpikeScore96() != null) update.set("volTradesSpikeScore96", doc.getVolTradesSpikeScore96());
        if (doc.getVolMicroburstVolumeIntensity32() != null) update.set("volMicroburstVolumeIntensity32", doc.getVolMicroburstVolumeIntensity32());
        if (doc.getVolMicroburstVolumeIntensity48() != null) update.set("volMicroburstVolumeIntensity48", doc.getVolMicroburstVolumeIntensity48());
        if (doc.getVolMicroburstVolumeIntensity96() != null) update.set("volMicroburstVolumeIntensity96", doc.getVolMicroburstVolumeIntensity96());
        if (doc.getVolMicroburstTradesIntensity32() != null) update.set("volMicroburstTradesIntensity32", doc.getVolMicroburstTradesIntensity32());
        if (doc.getVolMicroburstTradesIntensity48() != null) update.set("volMicroburstTradesIntensity48", doc.getVolMicroburstTradesIntensity48());
        if (doc.getVolMicroburstTradesIntensity96() != null) update.set("volMicroburstTradesIntensity96", doc.getVolMicroburstTradesIntensity96());
        if (doc.getVolMicroburstCombo32() != null) update.set("volMicroburstCombo32", doc.getVolMicroburstCombo32());
        if (doc.getVolMicroburstCombo48() != null) update.set("volMicroburstCombo48", doc.getVolMicroburstCombo48());
        if (doc.getVolMicroburstCombo96() != null) update.set("volMicroburstCombo96", doc.getVolMicroburstCombo96());

        // 7) EXHAUSTION SIGNALS / DRYUP
        if (doc.getVolExhaustionClimaxScore() != null) update.set("volExhaustionClimaxScore", doc.getVolExhaustionClimaxScore());
        if (doc.getVolExhaustionClimaxScore48() != null) update.set("volExhaustionClimaxScore48", doc.getVolExhaustionClimaxScore48());
        if (doc.getVolExhaustionClimaxScore96() != null) update.set("volExhaustionClimaxScore96", doc.getVolExhaustionClimaxScore96());
        if (doc.getVolVolumeDryupScore32() != null) update.set("volVolumeDryupScore32", doc.getVolVolumeDryupScore32());
        if (doc.getVolVolumeDryupScore48() != null) update.set("volVolumeDryupScore48", doc.getVolVolumeDryupScore48());
        if (doc.getVolVolumeDryupScore96() != null) update.set("volVolumeDryupScore96", doc.getVolVolumeDryupScore96());
        if (doc.getVolVolumeDryupScore288() != null) update.set("volVolumeDryupScore288", doc.getVolVolumeDryupScore288());
        if (doc.getVolExhaustionDryupAfterTrend32() != null) update.set("volExhaustionDryupAfterTrend32", doc.getVolExhaustionDryupAfterTrend32());
        if (doc.getVolExhaustionDryupAfterTrend48() != null) update.set("volExhaustionDryupAfterTrend48", doc.getVolExhaustionDryupAfterTrend48());
        if (doc.getVolExhaustionDryupAfterTrend96() != null) update.set("volExhaustionDryupAfterTrend96", doc.getVolExhaustionDryupAfterTrend96());
        if (doc.getVolExhaustionDryupAfterTrend288() != null) update.set("volExhaustionDryupAfterTrend288", doc.getVolExhaustionDryupAfterTrend288());

        // 8) VOLUME REGIME / CLUSTERS
        if (doc.getVolHighVolumeFreq32() != null) update.set("volHighVolumeFreq32", doc.getVolHighVolumeFreq32());
        if (doc.getVolHighVolumeFreq48() != null) update.set("volHighVolumeFreq48", doc.getVolHighVolumeFreq48());
        if (doc.getVolHighVolumeFreq96() != null) update.set("volHighVolumeFreq96", doc.getVolHighVolumeFreq96());
        if (doc.getVolHighVolumeFreq288() != null) update.set("volHighVolumeFreq288", doc.getVolHighVolumeFreq288());
        if (doc.getVolHighVolumeAge() != null) update.set("volHighVolumeAge", doc.getVolHighVolumeAge());
        if (doc.getVolHighVolumeClusterLen() != null) update.set("volHighVolumeClusterLen", doc.getVolHighVolumeClusterLen());
        if (doc.getVolHighVolumeAge32() != null) update.set("volHighVolumeAge32", doc.getVolHighVolumeAge32());
        if (doc.getVolHighVolumeAge96() != null) update.set("volHighVolumeAge96", doc.getVolHighVolumeAge96());
        if (doc.getVolHighVolumeAge288() != null) update.set("volHighVolumeAge288", doc.getVolHighVolumeAge288());
        if (doc.getVolHighVolumeClusterLen32() != null) update.set("volHighVolumeClusterLen32", doc.getVolHighVolumeClusterLen32());
        if (doc.getVolHighVolumeClusterLen96() != null) update.set("volHighVolumeClusterLen96", doc.getVolHighVolumeClusterLen96());
        if (doc.getVolHighVolumeClusterLen288() != null) update.set("volHighVolumeClusterLen288", doc.getVolHighVolumeClusterLen288());

        // 9) MICROSTRUCTURE - TRADE SIZE
        if (doc.getVolAvgTradeSize() != null) update.set("volAvgTradeSize", doc.getVolAvgTradeSize());
        if (doc.getVolAvgTradeSizeRel32() != null) update.set("volAvgTradeSizeRel32", doc.getVolAvgTradeSizeRel32());
        if (doc.getVolAvgTradeSizeRel48() != null) update.set("volAvgTradeSizeRel48", doc.getVolAvgTradeSizeRel48());
        if (doc.getVolAvgTradeSizeRel96() != null) update.set("volAvgTradeSizeRel96", doc.getVolAvgTradeSizeRel96());
        if (doc.getVolAvgTradeSizeRel288() != null) update.set("volAvgTradeSizeRel288", doc.getVolAvgTradeSizeRel288());
        if (doc.getVolAvgTradeSizeZscore32() != null) update.set("volAvgTradeSizeZscore32", doc.getVolAvgTradeSizeZscore32());
        if (doc.getVolAvgTradeSizeZscore48() != null) update.set("volAvgTradeSizeZscore48", doc.getVolAvgTradeSizeZscore48());
        if (doc.getVolAvgTradeSizeZscore96() != null) update.set("volAvgTradeSizeZscore96", doc.getVolAvgTradeSizeZscore96());
        if (doc.getVolAvgTradeSizeZscore288() != null) update.set("volAvgTradeSizeZscore288", doc.getVolAvgTradeSizeZscore288());
        if (doc.getVolAvgQuotePerTrade() != null) update.set("volAvgQuotePerTrade", doc.getVolAvgQuotePerTrade());
        if (doc.getVolAvgQuotePerTradeRel32() != null) update.set("volAvgQuotePerTradeRel32", doc.getVolAvgQuotePerTradeRel32());
        if (doc.getVolAvgQuotePerTradeRel48() != null) update.set("volAvgQuotePerTradeRel48", doc.getVolAvgQuotePerTradeRel48());
        if (doc.getVolAvgQuotePerTradeRel96() != null) update.set("volAvgQuotePerTradeRel96", doc.getVolAvgQuotePerTradeRel96());
        if (doc.getVolAvgQuotePerTradeRel288() != null) update.set("volAvgQuotePerTradeRel288", doc.getVolAvgQuotePerTradeRel288());
        if (doc.getVolAvgQuotePerTradeZscore32() != null) update.set("volAvgQuotePerTradeZscore32", doc.getVolAvgQuotePerTradeZscore32());
        if (doc.getVolAvgQuotePerTradeZscore48() != null) update.set("volAvgQuotePerTradeZscore48", doc.getVolAvgQuotePerTradeZscore48());
        if (doc.getVolAvgQuotePerTradeZscore96() != null) update.set("volAvgQuotePerTradeZscore96", doc.getVolAvgQuotePerTradeZscore96());
        if (doc.getVolAvgQuotePerTradeZscore288() != null) update.set("volAvgQuotePerTradeZscore288", doc.getVolAvgQuotePerTradeZscore288());

        // 10) VWAP DISTANCE
        if (doc.getVolVwap() != null) update.set("volVwap", doc.getVolVwap());
        if (doc.getVolVwapDistance() != null) update.set("volVwapDistance", doc.getVolVwapDistance());

        // 11) MICROBURST / PRESSURE SLOPES
        if (doc.getVolMicroburstSlope16() != null) update.set("volMicroburstSlope16", doc.getVolMicroburstSlope16());
        if (doc.getVolPressureSlope16() != null) update.set("volPressureSlope16", doc.getVolPressureSlope16());
        if (doc.getVolMicroburstSlope32() != null) update.set("volMicroburstSlope32", doc.getVolMicroburstSlope32());
        if (doc.getVolMicroburstSlope48() != null) update.set("volMicroburstSlope48", doc.getVolMicroburstSlope48());
        if (doc.getVolMicroburstSlope96() != null) update.set("volMicroburstSlope96", doc.getVolMicroburstSlope96());
        if (doc.getVolPressureSlope32() != null) update.set("volPressureSlope32", doc.getVolPressureSlope32());
        if (doc.getVolPressureSlope48() != null) update.set("volPressureSlope48", doc.getVolPressureSlope48());
        if (doc.getVolPressureSlope96() != null) update.set("volPressureSlope96", doc.getVolPressureSlope96());

        // 12) VOLUME ACCELERATION
        if (doc.getVolVolumeAcceleration() != null) update.set("volVolumeAcceleration", doc.getVolVolumeAcceleration());

        // 13) ORDER FLOW IMBALANCE (OFI)
        if (doc.getVolOfi() != null) update.set("volOfi", doc.getVolOfi());
        if (doc.getVolOfiRel16() != null) update.set("volOfiRel16", doc.getVolOfiRel16());
        if (doc.getVolOfiRel48() != null) update.set("volOfiRel48", doc.getVolOfiRel48());
        if (doc.getVolOfiRel96() != null) update.set("volOfiRel96", doc.getVolOfiRel96());
        if (doc.getVolOfiRel288() != null) update.set("volOfiRel288", doc.getVolOfiRel288());
        if (doc.getVolOfiZscore32() != null) update.set("volOfiZscore32", doc.getVolOfiZscore32());
        if (doc.getVolOfiZscore96() != null) update.set("volOfiZscore96", doc.getVolOfiZscore96());
        if (doc.getVolOfiZscore288() != null) update.set("volOfiZscore288", doc.getVolOfiZscore288());
        if (doc.getVolOfiSlpW20() != null) update.set("volOfiSlpW20", doc.getVolOfiSlpW20());
        if (doc.getVolOfiFlipRateW20() != null) update.set("volOfiFlipRateW20", doc.getVolOfiFlipRateW20());
        if (doc.getVolOfiPrstW20() != null) update.set("volOfiPrstW20", doc.getVolOfiPrstW20());
        if (doc.getVolOfiVvW20() != null) update.set("volOfiVvW20", doc.getVolOfiVvW20());

        // 14) VPIN
        if (doc.getVolVpin50() != null) update.set("volVpin50", doc.getVolVpin50());
        if (doc.getVolVpin100() != null) update.set("volVpin100", doc.getVolVpin100());
        if (doc.getVolVpin200() != null) update.set("volVpin200", doc.getVolVpin200());
        if (doc.getVolVpin100Zscore200() != null) update.set("volVpin100Zscore200", doc.getVolVpin100Zscore200());
        if (doc.getVolVpin100Delta1() != null) update.set("volVpin100Delta1", doc.getVolVpin100Delta1());
        if (doc.getVolVpin100Delta3() != null) update.set("volVpin100Delta3", doc.getVolVpin100Delta3());

        // 15) BID/ASK PRESSURE PROXY (BAP)
        if (doc.getVolBap() != null) update.set("volBap", doc.getVolBap());
        if (doc.getVolBapSlope16() != null) update.set("volBapSlope16", doc.getVolBapSlope16());
        if (doc.getVolBapAcc16() != null) update.set("volBapAcc16", doc.getVolBapAcc16());
        if (doc.getVolBapRel16() != null) update.set("volBapRel16", doc.getVolBapRel16());
        if (doc.getVolBapZscore32() != null) update.set("volBapZscore32", doc.getVolBapZscore32());
        if (doc.getVolBapFlipRateW20() != null) update.set("volBapFlipRateW20", doc.getVolBapFlipRateW20());
        if (doc.getVolBapPrstW20() != null) update.set("volBapPrstW20", doc.getVolBapPrstW20());
        if (doc.getVolBapVvW20() != null) update.set("volBapVvW20", doc.getVolBapVvW20());

        // 16) SIGNED VOLUME RATIO (SVR)
        if (doc.getVolSvr() != null) update.set("volSvr", doc.getVolSvr());
        if (doc.getVolSvrRel16() != null) update.set("volSvrRel16", doc.getVolSvrRel16());
        if (doc.getVolSvrRel48() != null) update.set("volSvrRel48", doc.getVolSvrRel48());
        if (doc.getVolSvrRel96() != null) update.set("volSvrRel96", doc.getVolSvrRel96());
        if (doc.getVolSvrRel288() != null) update.set("volSvrRel288", doc.getVolSvrRel288());
        if (doc.getVolSvrZscore32() != null) update.set("volSvrZscore32", doc.getVolSvrZscore32());
        if (doc.getVolSvrZscore96() != null) update.set("volSvrZscore96", doc.getVolSvrZscore96());
        if (doc.getVolSvrZscore288() != null) update.set("volSvrZscore288", doc.getVolSvrZscore288());
        if (doc.getVolSvrSlpW20() != null) update.set("volSvrSlpW20", doc.getVolSvrSlpW20());
        if (doc.getVolSvrFlipRateW20() != null) update.set("volSvrFlipRateW20", doc.getVolSvrFlipRateW20());
        if (doc.getVolSvrPrstW20() != null) update.set("volSvrPrstW20", doc.getVolSvrPrstW20());
        if (doc.getVolSvrVvW20() != null) update.set("volSvrVvW20", doc.getVolSvrVvW20());
        if (doc.getVolSvrAcc5() != null) update.set("volSvrAcc5", doc.getVolSvrAcc5());
        if (doc.getVolSvrAcc10() != null) update.set("volSvrAcc10", doc.getVolSvrAcc10());

        // 17) VOLATILITY OF VOLUME (VoV)
        if (doc.getVolVov16() != null) update.set("volVov16", doc.getVolVov16());
        if (doc.getVolVov32() != null) update.set("volVov32", doc.getVolVov32());
        if (doc.getVolVovZscore32() != null) update.set("volVovZscore32", doc.getVolVovZscore32());
        if (doc.getVolVov48() != null) update.set("volVov48", doc.getVolVov48());
        if (doc.getVolVov96() != null) update.set("volVov96", doc.getVolVov96());
        if (doc.getVolVov288() != null) update.set("volVov288", doc.getVolVov288());
        if (doc.getVolVovZscore96() != null) update.set("volVovZscore96", doc.getVolVovZscore96());
        if (doc.getVolVovZscore288() != null) update.set("volVovZscore288", doc.getVolVovZscore288());

        // 18) VOLUME REGIME - COMPOSITES
        if (doc.getVolRegimeState() != null) update.set("volRegimeState", doc.getVolRegimeState());
        if (doc.getVolRegimeConf() != null) update.set("volRegimeConf", doc.getVolRegimeConf());
        if (doc.getVolRegimePrstW20() != null) update.set("volRegimePrstW20", doc.getVolRegimePrstW20());
        if (doc.getVolRegimeFlipRateW50() != null) update.set("volRegimeFlipRateW50", doc.getVolRegimeFlipRateW50());

        return update;
    }

    public static String collectionName(CandleIntervals interval) {
        return switch (interval) {
            case I1_MN -> "vol_1";
            case I5_MN -> "vol_5";
            case I15_MN -> "vol_15";
            case I30_MN -> "vol_30";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
    }
}
