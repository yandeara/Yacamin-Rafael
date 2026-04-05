package br.com.yacamin.rafael.application.service.indicator.volume;

import br.com.yacamin.rafael.application.service.indicator.cache.AtrCache;
import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.OfiCache;
import br.com.yacamin.rafael.application.service.indicator.cache.SvrCache;
import br.com.yacamin.rafael.application.service.indicator.volume.calc.*;
import br.com.yacamin.rafael.application.service.indicator.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.mongo.document.VolumeIndicatorDocument;
import br.com.yacamin.rafael.adapter.out.persistence.mikhael.VolumeIndicatorMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolumeIndicatorService {

    private final VolumeIndicatorMongoRepository repository;
    private final CloseCache closeCache;
    private final AtrCache atrCache;
    private final SvrCache svrCache;
    private final OfiCache ofiCache;

    public void analyse(SymbolCandle candle, BarSeries series) {
        analyse(candle, series, false);
    }

    public void analyse(SymbolCandle candle, BarSeries series, boolean forceRecalculate) {
        VolumeIndicatorDocument doc = analyseBuffered(candle, series, forceRecalculate, null);
        repository.save(doc, candle.getInterval());
    }

    public VolumeIndicatorDocument analyseBuffered(SymbolCandle candle, BarSeries series, boolean forceRecalculate, VolumeIndicatorDocument preloadedDoc) {
        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var interval = candle.getInterval();
        int index = series.getEndIndex();

        log.info("[WARMUP][VOL] {} - {}", symbol, openTime);

        VolumeIndicatorDocument doc;
        if (forceRecalculate) {
            doc = new VolumeIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        } else if (preloadedDoc != null) {
            doc = preloadedDoc;
        } else {
            doc = new VolumeIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        }

        List<Callable<Void>> tasks = new ArrayList<>();

        // =========================================================================
        // VOLUME ACCELERATION
        // =========================================================================
        // ifNull("volVolumeAcceleration", doc::getVolVolumeAcceleration, // fora da prod mask
        //         () -> VolVolumeAccelerationCalc.calculate(series),
        //         doc::setVolVolumeAcceleration, tasks);

        // =========================================================================
        // ACTIVITY PRESSURE — TRADES (sp / acc / chop) — 16/32/48/96/288
        // =========================================================================
        ifNull("volActTradesSp16", doc::getVolActTradesSp16,
                () -> VolActTradesSp16Calc.calculate(series),
                doc::setVolActTradesSp16, tasks);
        ifNull("volActTradesSp32", doc::getVolActTradesSp32,
                () -> VolActTradesSp32Calc.calculate(series),
                doc::setVolActTradesSp32, tasks);
        // ifNull("volActTradesSp48", doc::getVolActTradesSp48, // fora da prod mask
        //         () -> VolActTradesSp48Calc.calculate(series),
        //         doc::setVolActTradesSp48, tasks);
        // ifNull("volActTradesSp96", doc::getVolActTradesSp96, // fora da prod mask
        //         () -> VolActTradesSp96Calc.calculate(series),
        //         doc::setVolActTradesSp96, tasks);
        // ifNull("volActTradesSp288", doc::getVolActTradesSp288, // fora da prod mask
        //         () -> VolActTradesSp288Calc.calculate(series),
        //         doc::setVolActTradesSp288, tasks);

        ifNull("volActTradesAcc16", doc::getVolActTradesAcc16,
                () -> VolActTradesAcc16Calc.calculate(series),
                doc::setVolActTradesAcc16, tasks);
        ifNull("volActTradesAcc32", doc::getVolActTradesAcc32,
                () -> VolActTradesAcc32Calc.calculate(series),
                doc::setVolActTradesAcc32, tasks);
        // ifNull("volActTradesAcc48", doc::getVolActTradesAcc48, // fora da prod mask
        //         () -> VolActTradesAcc48Calc.calculate(series),
        //         doc::setVolActTradesAcc48, tasks);
        // ifNull("volActTradesAcc96", doc::getVolActTradesAcc96, // fora da prod mask
        //         () -> VolActTradesAcc96Calc.calculate(series),
        //         doc::setVolActTradesAcc96, tasks);
        // ifNull("volActTradesAcc288", doc::getVolActTradesAcc288, // fora da prod mask
        //         () -> VolActTradesAcc288Calc.calculate(series),
        //         doc::setVolActTradesAcc288, tasks);

        // ifNull("volActTradesChop16", doc::getVolActTradesChop16, // fora da prod mask
        //         () -> VolActTradesChop16Calc.calculate(series),
        //         doc::setVolActTradesChop16, tasks);
        // ifNull("volActTradesChop32", doc::getVolActTradesChop32, // fora da prod mask
        //         () -> VolActTradesChop32Calc.calculate(series),
        //         doc::setVolActTradesChop32, tasks);
        // ifNull("volActTradesChop48", doc::getVolActTradesChop48, // fora da prod mask
        //         () -> VolActTradesChop48Calc.calculate(series),
        //         doc::setVolActTradesChop48, tasks);
        // ifNull("volActTradesChop96", doc::getVolActTradesChop96, // fora da prod mask
        //         () -> VolActTradesChop96Calc.calculate(series),
        //         doc::setVolActTradesChop96, tasks);
        // ifNull("volActTradesChop288", doc::getVolActTradesChop288, // fora da prod mask
        //         () -> VolActTradesChop288Calc.calculate(series),
        //         doc::setVolActTradesChop288, tasks);

        // =========================================================================
        // ACTIVITY PRESSURE — QUOTE (sp / acc / chop) — 16/32/48/96/288
        // =========================================================================
        ifNull("volActQuoteSp16", doc::getVolActQuoteSp16,
                () -> VolActQuoteSp16Calc.calculate(series),
                doc::setVolActQuoteSp16, tasks);
        ifNull("volActQuoteSp32", doc::getVolActQuoteSp32,
                () -> VolActQuoteSp32Calc.calculate(series),
                doc::setVolActQuoteSp32, tasks);
        // ifNull("volActQuoteSp48", doc::getVolActQuoteSp48, // fora da prod mask
        //         () -> VolActQuoteSp48Calc.calculate(series),
        //         doc::setVolActQuoteSp48, tasks);
        // ifNull("volActQuoteSp96", doc::getVolActQuoteSp96, // fora da prod mask
        //         () -> VolActQuoteSp96Calc.calculate(series),
        //         doc::setVolActQuoteSp96, tasks);
        // ifNull("volActQuoteSp288", doc::getVolActQuoteSp288, // fora da prod mask
        //         () -> VolActQuoteSp288Calc.calculate(series),
        //         doc::setVolActQuoteSp288, tasks);

        ifNull("volActQuoteAcc16", doc::getVolActQuoteAcc16,
                () -> VolActQuoteAcc16Calc.calculate(series),
                doc::setVolActQuoteAcc16, tasks);
        ifNull("volActQuoteAcc32", doc::getVolActQuoteAcc32,
                () -> VolActQuoteAcc32Calc.calculate(series),
                doc::setVolActQuoteAcc32, tasks);
        // ifNull("volActQuoteAcc48", doc::getVolActQuoteAcc48, // fora da prod mask
        //         () -> VolActQuoteAcc48Calc.calculate(series),
        //         doc::setVolActQuoteAcc48, tasks);
        // ifNull("volActQuoteAcc96", doc::getVolActQuoteAcc96, // fora da prod mask
        //         () -> VolActQuoteAcc96Calc.calculate(series),
        //         doc::setVolActQuoteAcc96, tasks);
        // ifNull("volActQuoteAcc288", doc::getVolActQuoteAcc288, // fora da prod mask
        //         () -> VolActQuoteAcc288Calc.calculate(series),
        //         doc::setVolActQuoteAcc288, tasks);

        // ifNull("volActQuoteChop16", doc::getVolActQuoteChop16, // fora da prod mask
        //         () -> VolActQuoteChop16Calc.calculate(series),
        //         doc::setVolActQuoteChop16, tasks);
        // ifNull("volActQuoteChop32", doc::getVolActQuoteChop32, // fora da prod mask
        //         () -> VolActQuoteChop32Calc.calculate(series),
        //         doc::setVolActQuoteChop32, tasks);
        // ifNull("volActQuoteChop48", doc::getVolActQuoteChop48, // fora da prod mask
        //         () -> VolActQuoteChop48Calc.calculate(series),
        //         doc::setVolActQuoteChop48, tasks);
        // ifNull("volActQuoteChop96", doc::getVolActQuoteChop96, // fora da prod mask
        //         () -> VolActQuoteChop96Calc.calculate(series),
        //         doc::setVolActQuoteChop96, tasks);
        // ifNull("volActQuoteChop288", doc::getVolActQuoteChop288, // fora da prod mask
        //         () -> VolActQuoteChop288Calc.calculate(series),
        //         doc::setVolActQuoteChop288, tasks);

        // =========================================================================
        // BAP (Bid/Ask Pressure)
        // =========================================================================
        ifNull("volBap", doc::getVolBap,
                () -> VolBapCalc.calculate(series, index),
                doc::setVolBap, tasks);
        ifNull("volBapSlope16", doc::getVolBapSlope16,
                () -> VolBapSlope16Calc.calculate(series, index),
                doc::setVolBapSlope16, tasks);
        ifNull("volBapAcc16", doc::getVolBapAcc16,
                () -> VolBapAcc16Calc.calculate(series, index),
                doc::setVolBapAcc16, tasks);
        // ifNull("volBapRel16", doc::getVolBapRel16, // fora da prod mask
        //         () -> VolBapRel16Calc.calculate(series, index),
        //         doc::setVolBapRel16, tasks);
        // ifNull("volBapZscore32", doc::getVolBapZscore32, // fora da prod mask
        //         () -> VolBapZscore32Calc.calculate(series, index),
        //         doc::setVolBapZscore32, tasks);
        // ifNull("volBapFlipRateW20", doc::getVolBapFlipRateW20, // fora da prod mask
        //         () -> VolBapFlipRateW20Calc.calculate(series, index),
        //         doc::setVolBapFlipRateW20, tasks);
        // ifNull("volBapPrstW20", doc::getVolBapPrstW20, // fora da prod mask
        //         () -> VolBapPrstW20Calc.calculate(series, index),
        //         doc::setVolBapPrstW20, tasks);
        // ifNull("volBapVvW20", doc::getVolBapVvW20, // fora da prod mask
        //         () -> VolBapVvW20Calc.calculate(series, index),
        //         doc::setVolBapVvW20, tasks);

        // =========================================================================
        // VOLUME DELTA
        // =========================================================================
        ifNull("volVolumeDelta1", doc::getVolVolumeDelta1, () -> VolVolumeDelta1Calc.calculate(series), doc::setVolVolumeDelta1, tasks);
        ifNull("volVolumeDelta3", doc::getVolVolumeDelta3, () -> VolVolumeDelta3Calc.calculate(series), doc::setVolVolumeDelta3, tasks);
        ifNull("volTradesDelta1", doc::getVolTradesDelta1, () -> VolTradesDelta1Calc.calculate(series), doc::setVolTradesDelta1, tasks);
        ifNull("volTradesDelta3", doc::getVolTradesDelta3, () -> VolTradesDelta3Calc.calculate(series), doc::setVolTradesDelta3, tasks);
        ifNull("volQuoteVolumeDelta1", doc::getVolQuoteVolumeDelta1, () -> VolQuoteVolumeDelta1Calc.calculate(series), doc::setVolQuoteVolumeDelta1, tasks);
        ifNull("volQuoteVolumeDelta3", doc::getVolQuoteVolumeDelta3, () -> VolQuoteVolumeDelta3Calc.calculate(series), doc::setVolQuoteVolumeDelta3, tasks);

        // =========================================================================
        // EXHAUSTION
        // =========================================================================
        // ifNull("volExhaustionClimaxScore", doc::getVolExhaustionClimaxScore, () -> VolExhaustionClimaxScore32Calc.calculate(series), doc::setVolExhaustionClimaxScore, tasks); // fora da prod mask
        // ifNull("volExhaustionClimaxScore48", doc::getVolExhaustionClimaxScore48, () -> VolExhaustionClimaxScore48Calc.calculate(series), doc::setVolExhaustionClimaxScore48, tasks); // fora da prod mask
        // ifNull("volExhaustionClimaxScore96", doc::getVolExhaustionClimaxScore96, () -> VolExhaustionClimaxScore96Calc.calculate(series), doc::setVolExhaustionClimaxScore96, tasks); // fora da prod mask
        // ifNull("volVolumeDryupScore32", doc::getVolVolumeDryupScore32, () -> VolVolumeDryupScore32Calc.calculate(series), doc::setVolVolumeDryupScore32, tasks); // fora da prod mask
        // ifNull("volVolumeDryupScore48", doc::getVolVolumeDryupScore48, () -> VolVolumeDryupScore48Calc.calculate(series), doc::setVolVolumeDryupScore48, tasks); // fora da prod mask
        // ifNull("volVolumeDryupScore96", doc::getVolVolumeDryupScore96, () -> VolVolumeDryupScore96Calc.calculate(series), doc::setVolVolumeDryupScore96, tasks); // fora da prod mask
        // ifNull("volVolumeDryupScore288", doc::getVolVolumeDryupScore288, () -> VolVolumeDryupScore288Calc.calculate(series), doc::setVolVolumeDryupScore288, tasks); // fora da prod mask
        // ifNull("volExhaustionDryupAfterTrend32", doc::getVolExhaustionDryupAfterTrend32, () -> VolExhaustionDryupAfterTrend32Calc.calculate(series), doc::setVolExhaustionDryupAfterTrend32, tasks); // fora da prod mask
        // ifNull("volExhaustionDryupAfterTrend48", doc::getVolExhaustionDryupAfterTrend48, () -> VolExhaustionDryupAfterTrend48Calc.calculate(series), doc::setVolExhaustionDryupAfterTrend48, tasks); // fora da prod mask
        // ifNull("volExhaustionDryupAfterTrend96", doc::getVolExhaustionDryupAfterTrend96, () -> VolExhaustionDryupAfterTrend96Calc.calculate(series), doc::setVolExhaustionDryupAfterTrend96, tasks); // fora da prod mask
        // ifNull("volExhaustionDryupAfterTrend288", doc::getVolExhaustionDryupAfterTrend288, () -> VolExhaustionDryupAfterTrend288Calc.calculate(series), doc::setVolExhaustionDryupAfterTrend288, tasks); // fora da prod mask

        // =========================================================================
        // MICROBURST
        // =========================================================================
        ifNull("volVolumeSpikeScore16", doc::getVolVolumeSpikeScore16, () -> VolVolumeSpikeScore16Calc.calculate(series), doc::setVolVolumeSpikeScore16, tasks);
        // ifNull("volVolumeSpikeScore32", doc::getVolVolumeSpikeScore32, () -> VolVolumeSpikeScore32Calc.calculate(series), doc::setVolVolumeSpikeScore32, tasks); // fora da prod mask
        // ifNull("volVolumeSpikeScore48", doc::getVolVolumeSpikeScore48, () -> VolVolumeSpikeScore48Calc.calculate(series), doc::setVolVolumeSpikeScore48, tasks); // fora da prod mask
        // ifNull("volVolumeSpikeScore96", doc::getVolVolumeSpikeScore96, () -> VolVolumeSpikeScore96Calc.calculate(series), doc::setVolVolumeSpikeScore96, tasks); // fora da prod mask
        ifNull("volTradesSpikeScore16", doc::getVolTradesSpikeScore16, () -> VolTradesSpikeScore16Calc.calculate(series), doc::setVolTradesSpikeScore16, tasks);
        // ifNull("volTradesSpikeScore32", doc::getVolTradesSpikeScore32, () -> VolTradesSpikeScore32Calc.calculate(series), doc::setVolTradesSpikeScore32, tasks); // fora da prod mask
        // ifNull("volTradesSpikeScore48", doc::getVolTradesSpikeScore48, () -> VolTradesSpikeScore48Calc.calculate(series), doc::setVolTradesSpikeScore48, tasks); // fora da prod mask
        // ifNull("volTradesSpikeScore96", doc::getVolTradesSpikeScore96, () -> VolTradesSpikeScore96Calc.calculate(series), doc::setVolTradesSpikeScore96, tasks); // fora da prod mask
        ifNull("volMicroburstVolumeIntensity16", doc::getVolMicroburstVolumeIntensity16, () -> VolMicroburstVolumeIntensity16Calc.calculate(series), doc::setVolMicroburstVolumeIntensity16, tasks);
        // ifNull("volMicroburstVolumeIntensity32", doc::getVolMicroburstVolumeIntensity32, () -> VolMicroburstVolumeIntensity32Calc.calculate(series), doc::setVolMicroburstVolumeIntensity32, tasks); // fora da prod mask
        // ifNull("volMicroburstVolumeIntensity48", doc::getVolMicroburstVolumeIntensity48, () -> VolMicroburstVolumeIntensity48Calc.calculate(series), doc::setVolMicroburstVolumeIntensity48, tasks); // fora da prod mask
        // ifNull("volMicroburstVolumeIntensity96", doc::getVolMicroburstVolumeIntensity96, () -> VolMicroburstVolumeIntensity96Calc.calculate(series), doc::setVolMicroburstVolumeIntensity96, tasks); // fora da prod mask
        ifNull("volMicroburstTradesIntensity16", doc::getVolMicroburstTradesIntensity16, () -> VolMicroburstTradesIntensity16Calc.calculate(series), doc::setVolMicroburstTradesIntensity16, tasks);
        // ifNull("volMicroburstTradesIntensity32", doc::getVolMicroburstTradesIntensity32, () -> VolMicroburstTradesIntensity32Calc.calculate(series), doc::setVolMicroburstTradesIntensity32, tasks); // fora da prod mask
        // ifNull("volMicroburstTradesIntensity48", doc::getVolMicroburstTradesIntensity48, () -> VolMicroburstTradesIntensity48Calc.calculate(series), doc::setVolMicroburstTradesIntensity48, tasks); // fora da prod mask
        // ifNull("volMicroburstTradesIntensity96", doc::getVolMicroburstTradesIntensity96, () -> VolMicroburstTradesIntensity96Calc.calculate(series), doc::setVolMicroburstTradesIntensity96, tasks); // fora da prod mask
        ifNull("volMicroburstCombo16", doc::getVolMicroburstCombo16, () -> VolMicroburstCombo16Calc.calculate(series), doc::setVolMicroburstCombo16, tasks);
        // ifNull("volMicroburstCombo32", doc::getVolMicroburstCombo32, () -> VolMicroburstCombo32Calc.calculate(series), doc::setVolMicroburstCombo32, tasks); // fora da prod mask
        // ifNull("volMicroburstCombo48", doc::getVolMicroburstCombo48, () -> VolMicroburstCombo48Calc.calculate(series), doc::setVolMicroburstCombo48, tasks); // fora da prod mask
        // ifNull("volMicroburstCombo96", doc::getVolMicroburstCombo96, () -> VolMicroburstCombo96Calc.calculate(series), doc::setVolMicroburstCombo96, tasks); // fora da prod mask

        // =========================================================================
        // OFI
        // =========================================================================
        var ofiExt = ofiCache.getOfi(symbol, interval, series);
        ifNull("volOfi", doc::getVolOfi, () -> VolOfiCalc.calculate(ofiExt, index), doc::setVolOfi, tasks);
        ifNull("volOfiRel16", doc::getVolOfiRel16, () -> VolOfiRel16Calc.calculate(ofiExt, index), doc::setVolOfiRel16, tasks);
        // ifNull("volOfiRel48", doc::getVolOfiRel48, () -> VolOfiRel48Calc.calculate(ofiExt, index), doc::setVolOfiRel48, tasks); // fora da prod mask
        // ifNull("volOfiRel96", doc::getVolOfiRel96, () -> VolOfiRel96Calc.calculate(ofiExt, index), doc::setVolOfiRel96, tasks); // fora da prod mask
        // ifNull("volOfiRel288", doc::getVolOfiRel288, () -> VolOfiRel288Calc.calculate(ofiExt, index), doc::setVolOfiRel288, tasks); // fora da prod mask
        // ifNull("volOfiZscore32", doc::getVolOfiZscore32, () -> VolOfiZscore32Calc.calculate(ofiExt, index), doc::setVolOfiZscore32, tasks); // fora da prod mask
        // ifNull("volOfiZscore96", doc::getVolOfiZscore96, () -> VolOfiZscore96Calc.calculate(ofiExt, index), doc::setVolOfiZscore96, tasks); // fora da prod mask
        // ifNull("volOfiZscore288", doc::getVolOfiZscore288, () -> VolOfiZscore288Calc.calculate(ofiExt, index), doc::setVolOfiZscore288, tasks); // fora da prod mask
        ifNull("volOfiSlpW20", doc::getVolOfiSlpW20, () -> VolOfiSlpW20Calc.calculate(ofiExt, index), doc::setVolOfiSlpW20, tasks);
        // ifNull("volOfiFlipRateW20", doc::getVolOfiFlipRateW20, () -> VolOfiFlipRateW20Calc.calculate(ofiExt, index), doc::setVolOfiFlipRateW20, tasks); // fora da prod mask
        // ifNull("volOfiPrstW20", doc::getVolOfiPrstW20, () -> VolOfiPrstW20Calc.calculate(ofiExt, index), doc::setVolOfiPrstW20, tasks); // fora da prod mask
        // ifNull("volOfiVvW20", doc::getVolOfiVvW20, () -> VolOfiVvW20Calc.calculate(ofiExt, index), doc::setVolOfiVvW20, tasks); // fora da prod mask

        // =========================================================================
        // RAW MICROSTRUCTURE
        // =========================================================================
        // ifNull("volVolume", doc::getVolVolume, () -> VolVolumeCalc.calculate(candle), doc::setVolVolume, tasks); // fora da prod mask
        // ifNull("volQuoteVolume", doc::getVolQuoteVolume, () -> VolQuoteVolumeCalc.calculate(candle), doc::setVolQuoteVolume, tasks); // fora da prod mask
        // ifNull("volNumberOfTrades", doc::getVolNumberOfTrades, () -> VolNumberOfTradesCalc.calculate(candle), doc::setVolNumberOfTrades, tasks); // fora da prod mask
        // ifNull("volTakerBuyBaseVolume", doc::getVolTakerBuyBaseVolume, () -> VolTakerBuyBaseVolumeCalc.calculate(candle), doc::setVolTakerBuyBaseVolume, tasks); // fora da prod mask
        // ifNull("volTakerSellBaseVolume", doc::getVolTakerSellBaseVolume, () -> VolTakerSellBaseVolumeCalc.calculate(candle), doc::setVolTakerSellBaseVolume, tasks); // fora da prod mask
        // ifNull("volTakerBuyQuoteVolume", doc::getVolTakerBuyQuoteVolume, () -> VolTakerBuyQuoteVolumeCalc.calculate(candle), doc::setVolTakerBuyQuoteVolume, tasks); // fora da prod mask
        // ifNull("volTakerSellQuoteVolume", doc::getVolTakerSellQuoteVolume, () -> VolTakerSellQuoteVolumeCalc.calculate(candle), doc::setVolTakerSellQuoteVolume, tasks); // fora da prod mask
        ifNull("volTakerBuyRatio", doc::getVolTakerBuyRatio, () -> VolTakerBuyRatioCalc.calculate(series, index), doc::setVolTakerBuyRatio, tasks);
        ifNull("volTakerBuySellImbalance", doc::getVolTakerBuySellImbalance, () -> VolTakerBuySellImbalanceCalc.calculate(series, index), doc::setVolTakerBuySellImbalance, tasks);
        // ifNull("volLogVolume", doc::getVolLogVolume, () -> VolLogVolumeCalc.calculate(series, index), doc::setVolLogVolume, tasks); // fora da prod mask
        ifNull("volTakerBuyRatioRel16", doc::getVolTakerBuyRatioRel16, () -> VolTakerBuyRatioRel16Calc.calculate(series, index), doc::setVolTakerBuyRatioRel16, tasks);
        // ifNull("volTakerBuyRatioRel48", doc::getVolTakerBuyRatioRel48, () -> VolTakerBuyRatioRel48Calc.calculate(series, index), doc::setVolTakerBuyRatioRel48, tasks); // fora da prod mask
        // ifNull("volTakerBuyRatioRel96", doc::getVolTakerBuyRatioRel96, () -> VolTakerBuyRatioRel96Calc.calculate(series, index), doc::setVolTakerBuyRatioRel96, tasks); // fora da prod mask
        // ifNull("volTakerBuyRatioRel288", doc::getVolTakerBuyRatioRel288, () -> VolTakerBuyRatioRel288Calc.calculate(series, index), doc::setVolTakerBuyRatioRel288, tasks); // fora da prod mask
        // ifNull("volTakerBuyRatioZscore32", doc::getVolTakerBuyRatioZscore32, () -> VolTakerBuyRatioZscore32Calc.calculate(series, index), doc::setVolTakerBuyRatioZscore32, tasks); // fora da prod mask
        // ifNull("volTakerBuyRatioZscore96", doc::getVolTakerBuyRatioZscore96, () -> VolTakerBuyRatioZscore96Calc.calculate(series, index), doc::setVolTakerBuyRatioZscore96, tasks); // fora da prod mask
        // ifNull("volTakerBuyRatioZscore288", doc::getVolTakerBuyRatioZscore288, () -> VolTakerBuyRatioZscore288Calc.calculate(series, index), doc::setVolTakerBuyRatioZscore288, tasks); // fora da prod mask
        ifNull("volTakerBuyRatioSlpW20", doc::getVolTakerBuyRatioSlpW20, () -> VolTakerBuyRatioSlpW20Calc.calculate(series, index), doc::setVolTakerBuyRatioSlpW20, tasks);
        // ifNull("volTakerBuyRatioFlipRateW20", doc::getVolTakerBuyRatioFlipRateW20, () -> VolTakerBuyRatioFlipRateW20Calc.calculate(series, index), doc::setVolTakerBuyRatioFlipRateW20, tasks); // fora da prod mask
        // ifNull("volTakerBuyRatioPrstW20", doc::getVolTakerBuyRatioPrstW20, () -> VolTakerBuyRatioPrstW20Calc.calculate(series, index), doc::setVolTakerBuyRatioPrstW20, tasks); // fora da prod mask
        // ifNull("volTakerBuySellImbalanceZscore32", doc::getVolTakerBuySellImbalanceZscore32, () -> VolTakerBuySellImbalanceZscore32Calc.calculate(series, index), doc::setVolTakerBuySellImbalanceZscore32, tasks); // fora da prod mask
        // ifNull("volTakerBuySellImbalanceZscore96", doc::getVolTakerBuySellImbalanceZscore96, () -> VolTakerBuySellImbalanceZscore96Calc.calculate(series, index), doc::setVolTakerBuySellImbalanceZscore96, tasks); // fora da prod mask
        ifNull("volTakerBuySellImbalanceSlpW20", doc::getVolTakerBuySellImbalanceSlpW20, () -> VolTakerBuySellImbalanceSlpW20Calc.calculate(series, index), doc::setVolTakerBuySellImbalanceSlpW20, tasks);
        // ifNull("volTakerBuySellImbalanceFlipRateW20", doc::getVolTakerBuySellImbalanceFlipRateW20, () -> VolTakerBuySellImbalanceFlipRateW20Calc.calculate(series, index), doc::setVolTakerBuySellImbalanceFlipRateW20, tasks); // fora da prod mask
        // ifNull("volTakerBuySellImbalancePrstW20", doc::getVolTakerBuySellImbalancePrstW20, () -> VolTakerBuySellImbalancePrstW20Calc.calculate(series, index), doc::setVolTakerBuySellImbalancePrstW20, tasks); // fora da prod mask

        // =========================================================================
        // VOLUME REGIME COMPOSITE
        // =========================================================================
        // ifNull("volRegimeState", doc::getVolRegimeState, () -> VolRegimeStateCalc.calculate(series, index, ofiExt), doc::setVolRegimeState, tasks); // fora da prod mask
        // ifNull("volRegimeConf", doc::getVolRegimeConf, () -> VolRegimeConfCalc.calculate(series, index, ofiExt), doc::setVolRegimeConf, tasks); // fora da prod mask
        // ifNull("volRegimePrstW20", doc::getVolRegimePrstW20, () -> VolRegimePrstW20Calc.calculate(series, index, ofiExt), doc::setVolRegimePrstW20, tasks); // fora da prod mask
        // ifNull("volRegimeFlipRateW50", doc::getVolRegimeFlipRateW50, () -> VolRegimeFlipRateW50Calc.calculate(series, index, ofiExt), doc::setVolRegimeFlipRateW50, tasks); // fora da prod mask

        // =========================================================================
        // VOLUME REGIME (Freq/Age/Cluster)
        // =========================================================================
        // ifNull("volHighVolumeFreq32", doc::getVolHighVolumeFreq32, () -> VolHighVolumeFreq32Calc.calculate(series), doc::setVolHighVolumeFreq32, tasks); // fora da prod mask
        // ifNull("volHighVolumeFreq48", doc::getVolHighVolumeFreq48, () -> VolHighVolumeFreq48Calc.calculate(series), doc::setVolHighVolumeFreq48, tasks); // fora da prod mask
        // ifNull("volHighVolumeFreq96", doc::getVolHighVolumeFreq96, () -> VolHighVolumeFreq96Calc.calculate(series), doc::setVolHighVolumeFreq96, tasks); // fora da prod mask
        // ifNull("volHighVolumeFreq288", doc::getVolHighVolumeFreq288, () -> VolHighVolumeFreq288Calc.calculate(series), doc::setVolHighVolumeFreq288, tasks); // fora da prod mask
        // ifNull("volHighVolumeAge", doc::getVolHighVolumeAge, () -> VolHighVolumeAgeCalc.calculate(series), doc::setVolHighVolumeAge, tasks); // fora da prod mask
        // ifNull("volHighVolumeAge32", doc::getVolHighVolumeAge32, () -> VolHighVolumeAge32Calc.calculate(series), doc::setVolHighVolumeAge32, tasks); // fora da prod mask
        // ifNull("volHighVolumeAge96", doc::getVolHighVolumeAge96, () -> VolHighVolumeAge96Calc.calculate(series), doc::setVolHighVolumeAge96, tasks); // fora da prod mask
        // ifNull("volHighVolumeAge288", doc::getVolHighVolumeAge288, () -> VolHighVolumeAge288Calc.calculate(series), doc::setVolHighVolumeAge288, tasks); // fora da prod mask
        // ifNull("volHighVolumeClusterLen", doc::getVolHighVolumeClusterLen, () -> VolHighVolumeClusterLenCalc.calculate(series), doc::setVolHighVolumeClusterLen, tasks); // fora da prod mask
        // ifNull("volHighVolumeClusterLen32", doc::getVolHighVolumeClusterLen32, () -> VolHighVolumeClusterLen32Calc.calculate(series), doc::setVolHighVolumeClusterLen32, tasks); // fora da prod mask
        // ifNull("volHighVolumeClusterLen96", doc::getVolHighVolumeClusterLen96, () -> VolHighVolumeClusterLen96Calc.calculate(series), doc::setVolHighVolumeClusterLen96, tasks); // fora da prod mask
        // ifNull("volHighVolumeClusterLen288", doc::getVolHighVolumeClusterLen288, () -> VolHighVolumeClusterLen288Calc.calculate(series), doc::setVolHighVolumeClusterLen288, tasks); // fora da prod mask

        // =========================================================================
        // VOLUME RELATIVE
        // =========================================================================
        // ifNull("volVolumeRel16", doc::getVolVolumeRel16, () -> VolVolumeRel16Calc.calculate(series, index), doc::setVolVolumeRel16, tasks); // fora da prod mask
        // ifNull("volVolumeRel32", doc::getVolVolumeRel32, () -> VolVolumeRel32Calc.calculate(series, index), doc::setVolVolumeRel32, tasks); // fora da prod mask
        // ifNull("volVolumeRel48", doc::getVolVolumeRel48, () -> VolVolumeRel48Calc.calculate(series, index), doc::setVolVolumeRel48, tasks); // fora da prod mask
        // ifNull("volVolumeRel96", doc::getVolVolumeRel96, () -> VolVolumeRel96Calc.calculate(series, index), doc::setVolVolumeRel96, tasks); // fora da prod mask
        // ifNull("volVolumeRel288", doc::getVolVolumeRel288, () -> VolVolumeRel288Calc.calculate(series, index), doc::setVolVolumeRel288, tasks); // fora da prod mask
        // ifNull("volTradesRel16", doc::getVolTradesRel16, () -> VolTradesRel16Calc.calculate(series, index), doc::setVolTradesRel16, tasks); // fora da prod mask
        // ifNull("volTradesRel32", doc::getVolTradesRel32, () -> VolTradesRel32Calc.calculate(series, index), doc::setVolTradesRel32, tasks); // fora da prod mask
        // ifNull("volTradesRel48", doc::getVolTradesRel48, () -> VolTradesRel48Calc.calculate(series, index), doc::setVolTradesRel48, tasks); // fora da prod mask
        // ifNull("volTradesRel96", doc::getVolTradesRel96, () -> VolTradesRel96Calc.calculate(series, index), doc::setVolTradesRel96, tasks); // fora da prod mask
        // ifNull("volTradesRel288", doc::getVolTradesRel288, () -> VolTradesRel288Calc.calculate(series, index), doc::setVolTradesRel288, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeRel16", doc::getVolQuoteVolumeRel16, () -> VolQuoteVolumeRel16Calc.calculate(series, index), doc::setVolQuoteVolumeRel16, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeRel32", doc::getVolQuoteVolumeRel32, () -> VolQuoteVolumeRel32Calc.calculate(series, index), doc::setVolQuoteVolumeRel32, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeRel48", doc::getVolQuoteVolumeRel48, () -> VolQuoteVolumeRel48Calc.calculate(series, index), doc::setVolQuoteVolumeRel48, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeRel96", doc::getVolQuoteVolumeRel96, () -> VolQuoteVolumeRel96Calc.calculate(series, index), doc::setVolQuoteVolumeRel96, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeRel288", doc::getVolQuoteVolumeRel288, () -> VolQuoteVolumeRel288Calc.calculate(series, index), doc::setVolQuoteVolumeRel288, tasks); // fora da prod mask

        // =========================================================================
        // VOLUME SLOPE
        // =========================================================================
        ifNull("volPressureSlope16", doc::getVolPressureSlope16, () -> VolPressureSlope16Calc.calculate(series, index), doc::setVolPressureSlope16, tasks);
        // ifNull("volPressureSlope32", doc::getVolPressureSlope32, () -> VolPressureSlope32Calc.calculate(series, index), doc::setVolPressureSlope32, tasks); // fora da prod mask
        // ifNull("volPressureSlope48", doc::getVolPressureSlope48, () -> VolPressureSlope48Calc.calculate(series, index), doc::setVolPressureSlope48, tasks); // fora da prod mask
        // ifNull("volPressureSlope96", doc::getVolPressureSlope96, () -> VolPressureSlope96Calc.calculate(series, index), doc::setVolPressureSlope96, tasks); // fora da prod mask
        ifNull("volMicroburstSlope16", doc::getVolMicroburstSlope16, () -> VolMicroburstSlope16Calc.calculate(series, index), doc::setVolMicroburstSlope16, tasks);
        // ifNull("volMicroburstSlope32", doc::getVolMicroburstSlope32, () -> VolMicroburstSlope32Calc.calculate(series, index), doc::setVolMicroburstSlope32, tasks); // fora da prod mask
        // ifNull("volMicroburstSlope48", doc::getVolMicroburstSlope48, () -> VolMicroburstSlope48Calc.calculate(series, index), doc::setVolMicroburstSlope48, tasks); // fora da prod mask
        // ifNull("volMicroburstSlope96", doc::getVolMicroburstSlope96, () -> VolMicroburstSlope96Calc.calculate(series, index), doc::setVolMicroburstSlope96, tasks); // fora da prod mask

        // =========================================================================
        // SVR
        // =========================================================================
        ifNull("volSvr", doc::getVolSvr, () -> VolSvrCalc.calculate(series, index), doc::setVolSvr, tasks);
        // ifNull("volSvrRel16", doc::getVolSvrRel16, () -> VolSvrRel16Calc.calculate(series, index), doc::setVolSvrRel16, tasks); // fora da prod mask
        // ifNull("volSvrRel48", doc::getVolSvrRel48, () -> VolSvrRel48Calc.calculate(series, index), doc::setVolSvrRel48, tasks); // fora da prod mask
        // ifNull("volSvrRel96", doc::getVolSvrRel96, () -> VolSvrRel96Calc.calculate(series, index), doc::setVolSvrRel96, tasks); // fora da prod mask
        // ifNull("volSvrRel288", doc::getVolSvrRel288, () -> VolSvrRel288Calc.calculate(series, index), doc::setVolSvrRel288, tasks); // fora da prod mask
        // ifNull("volSvrZscore32", doc::getVolSvrZscore32, () -> VolSvrZscore32Calc.calculate(series, index), doc::setVolSvrZscore32, tasks); // fora da prod mask
        // ifNull("volSvrZscore96", doc::getVolSvrZscore96, () -> VolSvrZscore96Calc.calculate(series, index), doc::setVolSvrZscore96, tasks); // fora da prod mask
        // ifNull("volSvrZscore288", doc::getVolSvrZscore288, () -> VolSvrZscore288Calc.calculate(series, index), doc::setVolSvrZscore288, tasks); // fora da prod mask
        ifNull("volSvrSlpW20", doc::getVolSvrSlpW20, () -> VolSvrSlpW20Calc.calculate(series, index), doc::setVolSvrSlpW20, tasks);
        // ifNull("volSvrFlipRateW20", doc::getVolSvrFlipRateW20, () -> VolSvrFlipRateW20Calc.calculate(series, index), doc::setVolSvrFlipRateW20, tasks); // fora da prod mask
        // ifNull("volSvrPrstW20", doc::getVolSvrPrstW20, () -> VolSvrPrstW20Calc.calculate(series, index), doc::setVolSvrPrstW20, tasks); // fora da prod mask
        // ifNull("volSvrVvW20", doc::getVolSvrVvW20, () -> VolSvrVvW20Calc.calculate(series, index), doc::setVolSvrVvW20, tasks); // fora da prod mask
        ifNull("volSvrAcc5", doc::getVolSvrAcc5, () -> VolSvrAcc5Calc.calculate(series, index), doc::setVolSvrAcc5, tasks);
        ifNull("volSvrAcc10", doc::getVolSvrAcc10, () -> VolSvrAcc10Calc.calculate(series, index), doc::setVolSvrAcc10, tasks);

        // =========================================================================
        // TRADE SIZE
        // =========================================================================
        // ifNull("volAvgTradeSize", doc::getVolAvgTradeSize, () -> VolAvgTradeSizeCalc.calculate(series, index), doc::setVolAvgTradeSize, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTrade", doc::getVolAvgQuotePerTrade, () -> VolAvgQuotePerTradeCalc.calculate(series, index), doc::setVolAvgQuotePerTrade, tasks); // fora da prod mask
        // ifNull("volAvgTradeSizeRel32", doc::getVolAvgTradeSizeRel32, () -> VolAvgTradeSizeRel32Calc.calculate(series), doc::setVolAvgTradeSizeRel32, tasks); // fora da prod mask
        // ifNull("volAvgTradeSizeRel48", doc::getVolAvgTradeSizeRel48, () -> VolAvgTradeSizeRel48Calc.calculate(series), doc::setVolAvgTradeSizeRel48, tasks); // fora da prod mask
        // ifNull("volAvgTradeSizeRel96", doc::getVolAvgTradeSizeRel96, () -> VolAvgTradeSizeRel96Calc.calculate(series), doc::setVolAvgTradeSizeRel96, tasks); // fora da prod mask
        // ifNull("volAvgTradeSizeRel288", doc::getVolAvgTradeSizeRel288, () -> VolAvgTradeSizeRel288Calc.calculate(series), doc::setVolAvgTradeSizeRel288, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTradeRel32", doc::getVolAvgQuotePerTradeRel32, () -> VolAvgQuotePerTradeRel32Calc.calculate(series), doc::setVolAvgQuotePerTradeRel32, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTradeRel48", doc::getVolAvgQuotePerTradeRel48, () -> VolAvgQuotePerTradeRel48Calc.calculate(series), doc::setVolAvgQuotePerTradeRel48, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTradeRel96", doc::getVolAvgQuotePerTradeRel96, () -> VolAvgQuotePerTradeRel96Calc.calculate(series), doc::setVolAvgQuotePerTradeRel96, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTradeRel288", doc::getVolAvgQuotePerTradeRel288, () -> VolAvgQuotePerTradeRel288Calc.calculate(series), doc::setVolAvgQuotePerTradeRel288, tasks); // fora da prod mask
        // ifNull("volAvgTradeSizeZscore32", doc::getVolAvgTradeSizeZscore32, () -> VolAvgTradeSizeZscore32Calc.calculate(series), doc::setVolAvgTradeSizeZscore32, tasks); // fora da prod mask
        // ifNull("volAvgTradeSizeZscore48", doc::getVolAvgTradeSizeZscore48, () -> VolAvgTradeSizeZscore48Calc.calculate(series), doc::setVolAvgTradeSizeZscore48, tasks); // fora da prod mask
        // ifNull("volAvgTradeSizeZscore96", doc::getVolAvgTradeSizeZscore96, () -> VolAvgTradeSizeZscore96Calc.calculate(series), doc::setVolAvgTradeSizeZscore96, tasks); // fora da prod mask
        // ifNull("volAvgTradeSizeZscore288", doc::getVolAvgTradeSizeZscore288, () -> VolAvgTradeSizeZscore288Calc.calculate(series), doc::setVolAvgTradeSizeZscore288, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTradeZscore32", doc::getVolAvgQuotePerTradeZscore32, () -> VolAvgQuotePerTradeZscore32Calc.calculate(series), doc::setVolAvgQuotePerTradeZscore32, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTradeZscore48", doc::getVolAvgQuotePerTradeZscore48, () -> VolAvgQuotePerTradeZscore48Calc.calculate(series), doc::setVolAvgQuotePerTradeZscore48, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTradeZscore96", doc::getVolAvgQuotePerTradeZscore96, () -> VolAvgQuotePerTradeZscore96Calc.calculate(series), doc::setVolAvgQuotePerTradeZscore96, tasks); // fora da prod mask
        // ifNull("volAvgQuotePerTradeZscore288", doc::getVolAvgQuotePerTradeZscore288, () -> VolAvgQuotePerTradeZscore288Calc.calculate(series), doc::setVolAvgQuotePerTradeZscore288, tasks); // fora da prod mask

        // =========================================================================
        // VoV
        // =========================================================================
        // ifNull("volVov16", doc::getVolVov16, () -> VolVov16Calc.calculate(series, index), doc::setVolVov16, tasks); // fora da prod mask
        // ifNull("volVov32", doc::getVolVov32, () -> VolVov32Calc.calculate(series, index), doc::setVolVov32, tasks); // fora da prod mask
        // ifNull("volVov48", doc::getVolVov48, () -> VolVov48Calc.calculate(series, index), doc::setVolVov48, tasks); // fora da prod mask
        // ifNull("volVov96", doc::getVolVov96, () -> VolVov96Calc.calculate(series, index), doc::setVolVov96, tasks); // fora da prod mask
        // ifNull("volVov288", doc::getVolVov288, () -> VolVov288Calc.calculate(series, index), doc::setVolVov288, tasks); // fora da prod mask
        // ifNull("volVovZscore32", doc::getVolVovZscore32, () -> VolVovZscore32Calc.calculate(series, index), doc::setVolVovZscore32, tasks); // fora da prod mask
        // ifNull("volVovZscore96", doc::getVolVovZscore96, () -> VolVovZscore96Calc.calculate(series, index), doc::setVolVovZscore96, tasks); // fora da prod mask
        // ifNull("volVovZscore288", doc::getVolVovZscore288, () -> VolVovZscore288Calc.calculate(series, index), doc::setVolVovZscore288, tasks); // fora da prod mask

        // =========================================================================
        // VPIN
        // =========================================================================
        // ifNull("volVpin50", doc::getVolVpin50, () -> VolVpin50Calc.calculate(series, index), doc::setVolVpin50, tasks); // fora da prod mask
        // ifNull("volVpin100", doc::getVolVpin100, () -> VolVpin100Calc.calculate(series, index), doc::setVolVpin100, tasks); // fora da prod mask
        // ifNull("volVpin200", doc::getVolVpin200, () -> VolVpin200Calc.calculate(series, index), doc::setVolVpin200, tasks); // fora da prod mask
        // ifNull("volVpin100Zscore200", doc::getVolVpin100Zscore200, () -> VolVpin100Zscore200Calc.calculate(series, index), doc::setVolVpin100Zscore200, tasks); // fora da prod mask
        // ifNull("volVpin100Delta1", doc::getVolVpin100Delta1, () -> VolVpin100Delta1Calc.calculate(series, index), doc::setVolVpin100Delta1, tasks); // fora da prod mask
        // ifNull("volVpin100Delta3", doc::getVolVpin100Delta3, () -> VolVpin100Delta3Calc.calculate(series, index), doc::setVolVpin100Delta3, tasks); // fora da prod mask

        // =========================================================================
        // VWAP
        // =========================================================================
        var atr14 = atrCache.getAtr14(candle.getSymbol(), candle.getInterval(), series);
        // ifNull("volVwap", doc::getVolVwap, () -> VolVwapCalc.calculate(series), doc::setVolVwap, tasks); // fora da prod mask
        ifNull("volVwapDistance", doc::getVolVwapDistance, () -> VolVwapDistanceCalc.calculate(series, index, atr14), doc::setVolVwapDistance, tasks);

        // =========================================================================
        // VOLUME Z-SCORES
        // =========================================================================
        // ifNull("volVolumeZscore32", doc::getVolVolumeZscore32, () -> VolVolumeZscore32Calc.calculate(series), doc::setVolVolumeZscore32, tasks); // fora da prod mask
        // ifNull("volVolumeZscore48", doc::getVolVolumeZscore48, () -> VolVolumeZscore48Calc.calculate(series), doc::setVolVolumeZscore48, tasks); // fora da prod mask
        // ifNull("volVolumeZscore96", doc::getVolVolumeZscore96, () -> VolVolumeZscore96Calc.calculate(series), doc::setVolVolumeZscore96, tasks); // fora da prod mask
        // ifNull("volVolumeZscore288", doc::getVolVolumeZscore288, () -> VolVolumeZscore288Calc.calculate(series), doc::setVolVolumeZscore288, tasks); // fora da prod mask
        // ifNull("volTradesZscore32", doc::getVolTradesZscore32, () -> VolTradesZscore32Calc.calculate(series), doc::setVolTradesZscore32, tasks); // fora da prod mask
        // ifNull("volTradesZscore48", doc::getVolTradesZscore48, () -> VolTradesZscore48Calc.calculate(series), doc::setVolTradesZscore48, tasks); // fora da prod mask
        // ifNull("volTradesZscore96", doc::getVolTradesZscore96, () -> VolTradesZscore96Calc.calculate(series), doc::setVolTradesZscore96, tasks); // fora da prod mask
        // ifNull("volTradesZscore288", doc::getVolTradesZscore288, () -> VolTradesZscore288Calc.calculate(series), doc::setVolTradesZscore288, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeZscore32", doc::getVolQuoteVolumeZscore32, () -> VolQuoteVolumeZscore32Calc.calculate(series), doc::setVolQuoteVolumeZscore32, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeZscore48", doc::getVolQuoteVolumeZscore48, () -> VolQuoteVolumeZscore48Calc.calculate(series), doc::setVolQuoteVolumeZscore48, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeZscore96", doc::getVolQuoteVolumeZscore96, () -> VolQuoteVolumeZscore96Calc.calculate(series), doc::setVolQuoteVolumeZscore96, tasks); // fora da prod mask
        // ifNull("volQuoteVolumeZscore288", doc::getVolQuoteVolumeZscore288, () -> VolQuoteVolumeZscore288Calc.calculate(series), doc::setVolQuoteVolumeZscore288, tasks); // fora da prod mask

        if (!tasks.isEmpty()) {
            execute(tasks);
        }

        return doc;
    }

    private void ifNull(String name, Supplier<Double> getter, Supplier<Double> calculator,
                        Consumer<Double> setter, List<Callable<Void>> tasks) {
        if (getter.get() == null) {
            tasks.add(() -> {
                setter.accept(DoubleValidator.validate(calculator.get(), name));
                return null;
            });
        }
    }

    private void execute(List<Callable<Void>> tasks) {
        for (var task : tasks) {
            try { task.call(); }
            catch (Exception e) { throw new RuntimeException("[VOL] erro", e); }
        }
    }
}
