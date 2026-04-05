package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.indicator.cache.AtrCache;
import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.SlopeCache;
import br.com.yacamin.rafael.application.service.indicator.cache.SmaCache;
import br.com.yacamin.rafael.application.service.indicator.cache.StdCache;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.*;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.*;
import br.com.yacamin.rafael.application.service.indicator.momentum.calc.*;
import br.com.yacamin.rafael.application.service.indicator.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.mongo.document.MomentumIndicatorDocument;
import br.com.yacamin.rafael.adapter.out.persistence.mikhael.MomentumIndicatorMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

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
public class MomentumIndicatorService {

    private final MomentumIndicatorMongoRepository repository;
    private final RsiCache rsiCache;
    private final CciCache cciCache;
    private final CmoCache cmoCache;
    private final RocCache rocCache;
    private final StochCache stochCache;
    private final WprCache wprCache;
    private final PpoCache ppoCache;
    private final TrixCache trixCache;
    private final TsiCache tsiCache;
    private final CloseReturnCache closeReturnCache;
    private final AtrCache atrCache;
    private final CloseCache closeCache;
    private final SmaCache smaCache;
    private final StdCache stdCache;
    private final PpoSignalCache ppoSignalCache;
    private final SlopeCache slopeCache;

    public void analyse(SymbolCandle candle, BarSeries series) {
        analyse(candle, series, false);
    }

    public void analyse(SymbolCandle candle, BarSeries series, boolean forceRecalculate) {
        MomentumIndicatorDocument doc = analyseBuffered(candle, series, forceRecalculate, null);
        repository.save(doc, candle.getInterval());
    }

    public MomentumIndicatorDocument analyseBuffered(SymbolCandle candle, BarSeries series, boolean forceRecalculate, MomentumIndicatorDocument preloadedDoc) {
        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var interval = candle.getInterval();
        int index = series.getEndIndex();

        log.info("[WARMUP][MOM] {} - {}", symbol, openTime);

        MomentumIndicatorDocument doc;
        if (forceRecalculate) {
            doc = new MomentumIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        } else if (preloadedDoc != null) {
            doc = preloadedDoc;
        } else {
            doc = new MomentumIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        }

        // ══════════════════════════════════════════════════════════════════════
        // WARMUP CACHES
        // ══════════════════════════════════════════════════════════════════════

        // Close / ATR / STD / SMA
        ClosePriceIndicator close = closeCache.getClosePrice(symbol, interval, series);
        ATRIndicator atr14 = atrCache.getAtr14(symbol, interval, series);
        // StandardDeviationIndicator std14 = stdCache.getStd(symbol, interval, series, 14); // fora da prod mask

        // SMA for close z-scores
        // SMAIndicator sma3   = smaCache.getSma(symbol, interval, series, 3); // fora da prod mask
        // SMAIndicator sma8   = smaCache.getSma(symbol, interval, series, 8); // fora da prod mask
        // SMAIndicator sma14  = smaCache.getSma(symbol, interval, series, 14); // fora da prod mask
        // SMAIndicator sma50  = smaCache.getSma(symbol, interval, series, 50); // fora da prod mask
        // SMAIndicator sma48  = smaCache.getSma(symbol, interval, series, 48); // fora da prod mask
        // SMAIndicator sma288 = smaCache.getSma(symbol, interval, series, 288); // fora da prod mask

        // STD for close z-scores (use STD(8) for short, STD(14) for mid, STD(50) for long)
        // StandardDeviationIndicator std8   = stdCache.getStd(symbol, interval, series, 8); // fora da prod mask
        // StandardDeviationIndicator std50  = stdCache.getStd(symbol, interval, series, 50); // fora da prod mask
        // StandardDeviationIndicator std48  = stdCache.getStd(symbol, interval, series, 48); // fora da prod mask
        // StandardDeviationIndicator std288 = stdCache.getStd(symbol, interval, series, 288); // fora da prod mask

        // Close Return caches (for all windows)
        CloseReturnExtension closeRet1   = closeReturnCache.getCloseReturn(symbol, interval, series, 1);
        CloseReturnExtension closeRet2   = closeReturnCache.getCloseReturn(symbol, interval, series, 2);
        CloseReturnExtension closeRet3   = closeReturnCache.getCloseReturn(symbol, interval, series, 3);
        CloseReturnExtension closeRet4   = closeReturnCache.getCloseReturn(symbol, interval, series, 4);
        CloseReturnExtension closeRet5   = closeReturnCache.getCloseReturn(symbol, interval, series, 5);
        CloseReturnExtension closeRet6   = closeReturnCache.getCloseReturn(symbol, interval, series, 6);
        CloseReturnExtension closeRet8   = closeReturnCache.getCloseReturn(symbol, interval, series, 8);
        CloseReturnExtension closeRet10  = closeReturnCache.getCloseReturn(symbol, interval, series, 10);
        CloseReturnExtension closeRet12  = closeReturnCache.getCloseReturn(symbol, interval, series, 12);
        CloseReturnExtension closeRet16  = closeReturnCache.getCloseReturn(symbol, interval, series, 16);
        // CloseReturnExtension closeRet24  = closeReturnCache.getCloseReturn(symbol, interval, series, 24); // fora da prod mask
        // CloseReturnExtension closeRet32  = closeReturnCache.getCloseReturn(symbol, interval, series, 32); // fora da prod mask
        // CloseReturnExtension closeRet48  = closeReturnCache.getCloseReturn(symbol, interval, series, 48); // fora da prod mask
        // CloseReturnExtension closeRet288 = closeReturnCache.getCloseReturn(symbol, interval, series, 288); // fora da prod mask

        // Burst / ContinuationRate / Decay / Impulse / ChopRatio
        BurstStrengthExtension burst10       = closeReturnCache.getBurstStrength(symbol, interval, series, 10);
        BurstStrengthExtension burst16       = closeReturnCache.getBurstStrength(symbol, interval, series, 16);
        BurstStrengthExtension burst32       = closeReturnCache.getBurstStrength(symbol, interval, series, 32);
        // BurstStrengthExtension burst48       = closeReturnCache.getBurstStrength(symbol, interval, series, 48); // fora da prod mask
        // BurstStrengthExtension burst288      = closeReturnCache.getBurstStrength(symbol, interval, series, 288); // fora da prod mask

        ContinuationRateExtension cntrate10  = closeReturnCache.getContinuationRate(symbol, interval, series, 10);
        ContinuationRateExtension cntrate16  = closeReturnCache.getContinuationRate(symbol, interval, series, 16);
        ContinuationRateExtension cntrate32  = closeReturnCache.getContinuationRate(symbol, interval, series, 32);
        // ContinuationRateExtension cntrate48  = closeReturnCache.getContinuationRate(symbol, interval, series, 48); // fora da prod mask
        // ContinuationRateExtension cntrate288 = closeReturnCache.getContinuationRate(symbol, interval, series, 288); // fora da prod mask

        DecayRateExtension decay10  = closeReturnCache.getDecayRate(symbol, interval, series, 10);
        DecayRateExtension decay16  = closeReturnCache.getDecayRate(symbol, interval, series, 16);
        DecayRateExtension decay32  = closeReturnCache.getDecayRate(symbol, interval, series, 32);
        // DecayRateExtension decay48  = closeReturnCache.getDecayRate(symbol, interval, series, 48); // fora da prod mask
        // DecayRateExtension decay288 = closeReturnCache.getDecayRate(symbol, interval, series, 288); // fora da prod mask

        ImpulseExtension impls10  = closeReturnCache.getImpulse(symbol, interval, series, 10);
        ImpulseExtension impls16  = closeReturnCache.getImpulse(symbol, interval, series, 16);
        ImpulseExtension impls32  = closeReturnCache.getImpulse(symbol, interval, series, 32);
        // ImpulseExtension impls48  = closeReturnCache.getImpulse(symbol, interval, series, 48); // fora da prod mask
        // ImpulseExtension impls288 = closeReturnCache.getImpulse(symbol, interval, series, 288); // fora da prod mask

        ChopRatioExtension chprt10  = closeReturnCache.getChopRatio(symbol, interval, series, 10);
        ChopRatioExtension chprt16  = closeReturnCache.getChopRatio(symbol, interval, series, 16);
        ChopRatioExtension chprt32  = closeReturnCache.getChopRatio(symbol, interval, series, 32);
        // ChopRatioExtension chprt48  = closeReturnCache.getChopRatio(symbol, interval, series, 48); // fora da prod mask
        // ChopRatioExtension chprt288 = closeReturnCache.getChopRatio(symbol, interval, series, 288); // fora da prod mask

        // RSI caches
        RSIIndicator rsi2   = rsiCache.getRsi(symbol, interval, series, 2);
        RSIIndicator rsi3   = rsiCache.getRsi(symbol, interval, series, 3);
        // RSIIndicator rsi4   = rsiCache.getRsi(symbol, interval, series, 4); // fora da prod mask
        RSIIndicator rsi5   = rsiCache.getRsi(symbol, interval, series, 5);
        // RSIIndicator rsi6   = rsiCache.getRsi(symbol, interval, series, 6); // fora da prod mask
        RSIIndicator rsi7   = rsiCache.getRsi(symbol, interval, series, 7);
        // RSIIndicator rsi8   = rsiCache.getRsi(symbol, interval, series, 8); // fora da prod mask
        // RSIIndicator rsi9   = rsiCache.getRsi(symbol, interval, series, 9); // fora da prod mask
        // RSIIndicator rsi10  = rsiCache.getRsi(symbol, interval, series, 10); // fora da prod mask
        // RSIIndicator rsi12  = rsiCache.getRsi(symbol, interval, series, 12); // fora da prod mask
        RSIIndicator rsi14  = rsiCache.getRsi(symbol, interval, series, 14);
        // RSIIndicator rsi16  = rsiCache.getRsi(symbol, interval, series, 16); // fora da prod mask
        // RSIIndicator rsi21  = rsiCache.getRsi(symbol, interval, series, 21); // fora da prod mask
        // RSIIndicator rsi24  = rsiCache.getRsi(symbol, interval, series, 24); // fora da prod mask
        // RSIIndicator rsi28  = rsiCache.getRsi(symbol, interval, series, 28); // fora da prod mask
        // RSIIndicator rsi32  = rsiCache.getRsi(symbol, interval, series, 32); // fora da prod mask
        RSIIndicator rsi48  = rsiCache.getRsi(symbol, interval, series, 48);
        RSIIndicator rsi288 = rsiCache.getRsi(symbol, interval, series, 288);

        // CCI caches
        CCIIndicator cci14  = cciCache.getCci(symbol, interval, series, 14);
        CCIIndicator cci20  = cciCache.getCci(symbol, interval, series, 20);
        // CCIIndicator cci48  = cciCache.getCci(symbol, interval, series, 48); // fora da prod mask
        // CCIIndicator cci288 = cciCache.getCci(symbol, interval, series, 288); // fora da prod mask

        // CMO caches
        CMOIndicator cmo14  = cmoCache.getCmo(symbol, interval, series, 14);
        CMOIndicator cmo20  = cmoCache.getCmo(symbol, interval, series, 20);
        // CMOIndicator cmo48  = cmoCache.getCmo(symbol, interval, series, 48); // fora da prod mask
        // CMOIndicator cmo288 = cmoCache.getCmo(symbol, interval, series, 288); // fora da prod mask

        // WPR caches
        WilliamsRIndicator wpr14  = wprCache.getWpr(symbol, interval, series, 14);
        WilliamsRIndicator wpr28  = wprCache.getWpr(symbol, interval, series, 28);
        WilliamsRIndicator wpr42  = wprCache.getWpr(symbol, interval, series, 42);
        WilliamsRIndicator wpr48  = wprCache.getWpr(symbol, interval, series, 48); // kept for mom_wpr_48_dst_mid
        // WilliamsRIndicator wpr288 = wprCache.getWpr(symbol, interval, series, 288); // fora da prod mask

        // Stochastic caches
        StochasticOscillatorKIndicator stochK14  = stochCache.getK(symbol, interval, series, 14);
        StochasticOscillatorDIndicator stochD14  = stochCache.getD(symbol, interval, series, 14);
        // StochasticOscillatorKIndicator stochK48  = stochCache.getK(symbol, interval, series, 48); // fora da prod mask
        // StochasticOscillatorDIndicator stochD48  = stochCache.getD(symbol, interval, series, 48); // fora da prod mask
        // StochasticOscillatorKIndicator stochK288 = stochCache.getK(symbol, interval, series, 288); // fora da prod mask
        // StochasticOscillatorDIndicator stochD288 = stochCache.getD(symbol, interval, series, 288); // fora da prod mask

        // ROC caches
        ROCIndicator roc1   = rocCache.getRoc(symbol, interval, series, 1);
        ROCIndicator roc2   = rocCache.getRoc(symbol, interval, series, 2);
        ROCIndicator roc3   = rocCache.getRoc(symbol, interval, series, 3);
        ROCIndicator roc5   = rocCache.getRoc(symbol, interval, series, 5);
        // ROCIndicator roc48  = rocCache.getRoc(symbol, interval, series, 48); // fora da prod mask
        // ROCIndicator roc288 = rocCache.getRoc(symbol, interval, series, 288); // fora da prod mask

        // PPO caches
        PPOIndicator ppo1226    = ppoCache.getPpo(symbol, interval, series, 12, 26);
        PPOIndicator ppo48104   = ppoCache.getPpo(symbol, interval, series, 48, 104);
        PPOIndicator ppo288576  = ppoCache.getPpo(symbol, interval, series, 288, 576);

        // PPO signal lines (EMA of PPO, period 9) — cached
        EMAIndicator ppoSig1226   = ppoSignalCache.getSignal(symbol, interval, ppo1226, 12, 26, 9);
        EMAIndicator ppoSig48104  = ppoSignalCache.getSignal(symbol, interval, ppo48104, 48, 104, 9);
        EMAIndicator ppoSig288576 = ppoSignalCache.getSignal(symbol, interval, ppo288576, 288, 576, 9);

        // TRIX — cached
        TrixExtension trix9   = trixCache.getTrix(symbol, interval, series, 9);
        TrixExtension trix48  = trixCache.getTrix(symbol, interval, series, 48);
        TrixExtension trix288 = trixCache.getTrix(symbol, interval, series, 288);

        // TSI caches
        TsiExtension tsi2513   = tsiCache.getTsi(symbol, interval, series, 25, 13);
        TsiExtension tsi4825   = tsiCache.getTsi(symbol, interval, series, 48, 25);
        TsiExtension tsi288144 = tsiCache.getTsi(symbol, interval, series, 288, 144);

        // Close slope indicators — cached
        var closeSlp3  = slopeCache.getSlope(symbol, interval, series, close, "close", 3);
        var closeSlp8  = slopeCache.getSlope(symbol, interval, series, close, "close", 8);
        var closeSlp14 = slopeCache.getSlope(symbol, interval, series, close, "close", 14);

        // RSI slope indicators — cached
        var rsiSlp7  = slopeCache.getSlope(symbol, interval, series, rsi7, "rsi7", 7);
        var rsiSlp14 = slopeCache.getSlope(symbol, interval, series, rsi14, "rsi14", 14);

        // Close slope acceleration — cached
        var closeSlpAcc3  = slopeCache.getAcceleration(symbol, interval, series, close, "close", 3);
        var closeSlpAcc8  = slopeCache.getAcceleration(symbol, interval, series, close, "close", 8);
        var closeSlpAcc14 = slopeCache.getAcceleration(symbol, interval, series, close, "close", 14);

        // ══════════════════════════════════════════════════════════════════════
        // BUILD TASKS
        // ══════════════════════════════════════════════════════════════════════

        List<Callable<Void>> tasks = new ArrayList<>();

        // ── CLOSE RETURN RAW ────────────────────────────────────────────────
        // ifNull("momCloseRet1",   doc::getMomCloseRet1,   () -> MomCloseRet1Calc.calculate(closeRet1, index),   doc::setMomCloseRet1,   tasks); // fora da prod mask
        // ifNull("momCloseRet2",   doc::getMomCloseRet2,   () -> MomCloseRet2Calc.calculate(closeRet2, index),   doc::setMomCloseRet2,   tasks); // fora da prod mask
        // ifNull("momCloseRet3",   doc::getMomCloseRet3,   () -> MomCloseRet3Calc.calculate(closeRet3, index),   doc::setMomCloseRet3,   tasks); // fora da prod mask
        // ifNull("momCloseRet4",   doc::getMomCloseRet4,   () -> MomCloseRet4Calc.calculate(closeRet4, index),   doc::setMomCloseRet4,   tasks); // fora da prod mask
        // ifNull("momCloseRet5",   doc::getMomCloseRet5,   () -> MomCloseRet5Calc.calculate(closeRet5, index),   doc::setMomCloseRet5,   tasks); // fora da prod mask
        // ifNull("momCloseRet6",   doc::getMomCloseRet6,   () -> MomCloseRet6Calc.calculate(closeRet6, index),   doc::setMomCloseRet6,   tasks); // fora da prod mask
        // ifNull("momCloseRet8",   doc::getMomCloseRet8,   () -> MomCloseRet8Calc.calculate(closeRet8, index),   doc::setMomCloseRet8,   tasks); // fora da prod mask
        // ifNull("momCloseRet10",  doc::getMomCloseRet10,  () -> MomCloseRet10Calc.calculate(closeRet10, index), doc::setMomCloseRet10,  tasks); // fora da prod mask
        // ifNull("momCloseRet12",  doc::getMomCloseRet12,  () -> MomCloseRet12Calc.calculate(closeRet12, index), doc::setMomCloseRet12,  tasks); // fora da prod mask
        // ifNull("momCloseRet16",  doc::getMomCloseRet16,  () -> MomCloseRet16Calc.calculate(closeRet16, index), doc::setMomCloseRet16,  tasks); // fora da prod mask
        // ifNull("momCloseRet24",  doc::getMomCloseRet24,  () -> MomCloseRet24Calc.calculate(closeRet24, index), doc::setMomCloseRet24,  tasks); // fora da prod mask
        // ifNull("momCloseRet32",  doc::getMomCloseRet32,  () -> MomCloseRet32Calc.calculate(closeRet32, index), doc::setMomCloseRet32,  tasks); // fora da prod mask
        // ifNull("momCloseRet48",  doc::getMomCloseRet48,  () -> MomCloseRet48Calc.calculate(closeRet48, index), doc::setMomCloseRet48,  tasks); // fora da prod mask
        // ifNull("momCloseRet288", doc::getMomCloseRet288, () -> MomCloseRet288Calc.calculate(closeRet288, index), doc::setMomCloseRet288, tasks); // fora da prod mask

        // ── CLOSE RETURN ATRN ───────────────────────────────────────────────
        ifNull("momCloseRet1Atrn",   doc::getMomCloseRet1Atrn,   () -> MomCloseRet1AtrnCalc.calculate(closeRet1, atr14, index),   doc::setMomCloseRet1Atrn,   tasks);
        ifNull("momCloseRet2Atrn",   doc::getMomCloseRet2Atrn,   () -> MomCloseRet2AtrnCalc.calculate(closeRet2, atr14, index),   doc::setMomCloseRet2Atrn,   tasks);
        ifNull("momCloseRet3Atrn",   doc::getMomCloseRet3Atrn,   () -> MomCloseRet3AtrnCalc.calculate(closeRet3, atr14, index),   doc::setMomCloseRet3Atrn,   tasks);
        ifNull("momCloseRet4Atrn",   doc::getMomCloseRet4Atrn,   () -> MomCloseRet4AtrnCalc.calculate(closeRet4, atr14, index),   doc::setMomCloseRet4Atrn,   tasks);
        ifNull("momCloseRet5Atrn",   doc::getMomCloseRet5Atrn,   () -> MomCloseRet5AtrnCalc.calculate(closeRet5, atr14, index),   doc::setMomCloseRet5Atrn,   tasks);
        ifNull("momCloseRet6Atrn",   doc::getMomCloseRet6Atrn,   () -> MomCloseRet6AtrnCalc.calculate(closeRet6, atr14, index),   doc::setMomCloseRet6Atrn,   tasks);
        ifNull("momCloseRet8Atrn",   doc::getMomCloseRet8Atrn,   () -> MomCloseRet8AtrnCalc.calculate(closeRet8, atr14, index),   doc::setMomCloseRet8Atrn,   tasks);
        ifNull("momCloseRet10Atrn",  doc::getMomCloseRet10Atrn,  () -> MomCloseRet10AtrnCalc.calculate(closeRet10, atr14, index), doc::setMomCloseRet10Atrn,  tasks);
        ifNull("momCloseRet12Atrn",  doc::getMomCloseRet12Atrn,  () -> MomCloseRet12AtrnCalc.calculate(closeRet12, atr14, index), doc::setMomCloseRet12Atrn,  tasks);
        ifNull("momCloseRet16Atrn",  doc::getMomCloseRet16Atrn,  () -> MomCloseRet16AtrnCalc.calculate(closeRet16, atr14, index), doc::setMomCloseRet16Atrn,  tasks);
        // ifNull("momCloseRet24Atrn",  doc::getMomCloseRet24Atrn,  () -> MomCloseRet24AtrnCalc.calculate(closeRet24, atr14, index), doc::setMomCloseRet24Atrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet32Atrn",  doc::getMomCloseRet32Atrn,  () -> MomCloseRet32AtrnCalc.calculate(closeRet32, atr14, index), doc::setMomCloseRet32Atrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet48Atrn",  doc::getMomCloseRet48Atrn,  () -> MomCloseRet48AtrnCalc.calculate(closeRet48, atr14, index), doc::setMomCloseRet48Atrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet288Atrn", doc::getMomCloseRet288Atrn, () -> MomCloseRet288AtrnCalc.calculate(closeRet288, atr14, index), doc::setMomCloseRet288Atrn, tasks); // fora da prod mask

        // ── CLOSE RETURN STDN ───────────────────────────────────────────────
        // ifNull("momCloseRet1Stdn",   doc::getMomCloseRet1Stdn,   () -> MomCloseRet1StdnCalc.calculate(closeRet1, std14, index),   doc::setMomCloseRet1Stdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet2Stdn",   doc::getMomCloseRet2Stdn,   () -> MomCloseRet2StdnCalc.calculate(closeRet2, std14, index),   doc::setMomCloseRet2Stdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet3Stdn",   doc::getMomCloseRet3Stdn,   () -> MomCloseRet3StdnCalc.calculate(closeRet3, std14, index),   doc::setMomCloseRet3Stdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet4Stdn",   doc::getMomCloseRet4Stdn,   () -> MomCloseRet4StdnCalc.calculate(closeRet4, std14, index),   doc::setMomCloseRet4Stdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet5Stdn",   doc::getMomCloseRet5Stdn,   () -> MomCloseRet5StdnCalc.calculate(closeRet5, std14, index),   doc::setMomCloseRet5Stdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet6Stdn",   doc::getMomCloseRet6Stdn,   () -> MomCloseRet6StdnCalc.calculate(closeRet6, std14, index),   doc::setMomCloseRet6Stdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet8Stdn",   doc::getMomCloseRet8Stdn,   () -> MomCloseRet8StdnCalc.calculate(closeRet8, std14, index),   doc::setMomCloseRet8Stdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet10Stdn",  doc::getMomCloseRet10Stdn,  () -> MomCloseRet10StdnCalc.calculate(closeRet10, std14, index), doc::setMomCloseRet10Stdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet12Stdn",  doc::getMomCloseRet12Stdn,  () -> MomCloseRet12StdnCalc.calculate(closeRet12, std14, index), doc::setMomCloseRet12Stdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet16Stdn",  doc::getMomCloseRet16Stdn,  () -> MomCloseRet16StdnCalc.calculate(closeRet16, std14, index), doc::setMomCloseRet16Stdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet24Stdn",  doc::getMomCloseRet24Stdn,  () -> MomCloseRet24StdnCalc.calculate(closeRet24, std14, index), doc::setMomCloseRet24Stdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet32Stdn",  doc::getMomCloseRet32Stdn,  () -> MomCloseRet32StdnCalc.calculate(closeRet32, std14, index), doc::setMomCloseRet32Stdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet48Stdn",  doc::getMomCloseRet48Stdn,  () -> MomCloseRet48StdnCalc.calculate(closeRet48, std14, index), doc::setMomCloseRet48Stdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet288Stdn", doc::getMomCloseRet288Stdn, () -> MomCloseRet288StdnCalc.calculate(closeRet288, std14, index), doc::setMomCloseRet288Stdn, tasks); // fora da prod mask

        // ── CLOSE RETURN ABS ────────────────────────────────────────────────
        // ifNull("momCloseRet1Abs",   doc::getMomCloseRet1Abs,   () -> MomCloseRet1AbsCalc.calculate(closeRet1, index),   doc::setMomCloseRet1Abs,   tasks); // fora da prod mask
        // ifNull("momCloseRet2Abs",   doc::getMomCloseRet2Abs,   () -> MomCloseRet2AbsCalc.calculate(closeRet2, index),   doc::setMomCloseRet2Abs,   tasks); // fora da prod mask
        // ifNull("momCloseRet3Abs",   doc::getMomCloseRet3Abs,   () -> MomCloseRet3AbsCalc.calculate(closeRet3, index),   doc::setMomCloseRet3Abs,   tasks); // fora da prod mask
        // ifNull("momCloseRet4Abs",   doc::getMomCloseRet4Abs,   () -> MomCloseRet4AbsCalc.calculate(closeRet4, index),   doc::setMomCloseRet4Abs,   tasks); // fora da prod mask
        // ifNull("momCloseRet5Abs",   doc::getMomCloseRet5Abs,   () -> MomCloseRet5AbsCalc.calculate(closeRet5, index),   doc::setMomCloseRet5Abs,   tasks); // fora da prod mask
        // ifNull("momCloseRet6Abs",   doc::getMomCloseRet6Abs,   () -> MomCloseRet6AbsCalc.calculate(closeRet6, index),   doc::setMomCloseRet6Abs,   tasks); // fora da prod mask
        // ifNull("momCloseRet8Abs",   doc::getMomCloseRet8Abs,   () -> MomCloseRet8AbsCalc.calculate(closeRet8, index),   doc::setMomCloseRet8Abs,   tasks); // fora da prod mask
        // ifNull("momCloseRet10Abs",  doc::getMomCloseRet10Abs,  () -> MomCloseRet10AbsCalc.calculate(closeRet10, index), doc::setMomCloseRet10Abs,  tasks); // fora da prod mask
        // ifNull("momCloseRet12Abs",  doc::getMomCloseRet12Abs,  () -> MomCloseRet12AbsCalc.calculate(closeRet12, index), doc::setMomCloseRet12Abs,  tasks); // fora da prod mask
        // ifNull("momCloseRet16Abs",  doc::getMomCloseRet16Abs,  () -> MomCloseRet16AbsCalc.calculate(closeRet16, index), doc::setMomCloseRet16Abs,  tasks); // fora da prod mask
        // ifNull("momCloseRet24Abs",  doc::getMomCloseRet24Abs,  () -> MomCloseRet24AbsCalc.calculate(closeRet24, index), doc::setMomCloseRet24Abs,  tasks); // fora da prod mask
        // ifNull("momCloseRet32Abs",  doc::getMomCloseRet32Abs,  () -> MomCloseRet32AbsCalc.calculate(closeRet32, index), doc::setMomCloseRet32Abs,  tasks); // fora da prod mask
        // ifNull("momCloseRet48Abs",  doc::getMomCloseRet48Abs,  () -> MomCloseRet48AbsCalc.calculate(closeRet48, index), doc::setMomCloseRet48Abs,  tasks); // fora da prod mask
        // ifNull("momCloseRet288Abs", doc::getMomCloseRet288Abs, () -> MomCloseRet288AbsCalc.calculate(closeRet288, index), doc::setMomCloseRet288Abs, tasks); // fora da prod mask

        // ── CLOSE RETURN ABS ATRN ───────────────────────────────────────────
        // ifNull("momCloseRet1AbsAtrn",   doc::getMomCloseRet1AbsAtrn,   () -> MomCloseRet1AbsAtrnCalc.calculate(MomCloseRet1AbsCalc.calculate(closeRet1, index), atr14, index),     doc::setMomCloseRet1AbsAtrn,   tasks); // fora da prod mask
        // ifNull("momCloseRet2AbsAtrn",   doc::getMomCloseRet2AbsAtrn,   () -> MomCloseRet2AbsAtrnCalc.calculate(MomCloseRet2AbsCalc.calculate(closeRet2, index), atr14, index),     doc::setMomCloseRet2AbsAtrn,   tasks); // fora da prod mask
        // ifNull("momCloseRet3AbsAtrn",   doc::getMomCloseRet3AbsAtrn,   () -> MomCloseRet3AbsAtrnCalc.calculate(MomCloseRet3AbsCalc.calculate(closeRet3, index), atr14, index),     doc::setMomCloseRet3AbsAtrn,   tasks); // fora da prod mask
        // ifNull("momCloseRet4AbsAtrn",   doc::getMomCloseRet4AbsAtrn,   () -> MomCloseRet4AbsAtrnCalc.calculate(MomCloseRet4AbsCalc.calculate(closeRet4, index), atr14, index),     doc::setMomCloseRet4AbsAtrn,   tasks); // fora da prod mask
        // ifNull("momCloseRet5AbsAtrn",   doc::getMomCloseRet5AbsAtrn,   () -> MomCloseRet5AbsAtrnCalc.calculate(MomCloseRet5AbsCalc.calculate(closeRet5, index), atr14, index),     doc::setMomCloseRet5AbsAtrn,   tasks); // fora da prod mask
        // ifNull("momCloseRet6AbsAtrn",   doc::getMomCloseRet6AbsAtrn,   () -> MomCloseRet6AbsAtrnCalc.calculate(MomCloseRet6AbsCalc.calculate(closeRet6, index), atr14, index),     doc::setMomCloseRet6AbsAtrn,   tasks); // fora da prod mask
        // ifNull("momCloseRet8AbsAtrn",   doc::getMomCloseRet8AbsAtrn,   () -> MomCloseRet8AbsAtrnCalc.calculate(MomCloseRet8AbsCalc.calculate(closeRet8, index), atr14, index),     doc::setMomCloseRet8AbsAtrn,   tasks); // fora da prod mask
        // ifNull("momCloseRet10AbsAtrn",  doc::getMomCloseRet10AbsAtrn,  () -> MomCloseRet10AbsAtrnCalc.calculate(MomCloseRet10AbsCalc.calculate(closeRet10, index), atr14, index),   doc::setMomCloseRet10AbsAtrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet12AbsAtrn",  doc::getMomCloseRet12AbsAtrn,  () -> MomCloseRet12AbsAtrnCalc.calculate(MomCloseRet12AbsCalc.calculate(closeRet12, index), atr14, index),   doc::setMomCloseRet12AbsAtrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet16AbsAtrn",  doc::getMomCloseRet16AbsAtrn,  () -> MomCloseRet16AbsAtrnCalc.calculate(MomCloseRet16AbsCalc.calculate(closeRet16, index), atr14, index),   doc::setMomCloseRet16AbsAtrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet24AbsAtrn",  doc::getMomCloseRet24AbsAtrn,  () -> MomCloseRet24AbsAtrnCalc.calculate(MomCloseRet24AbsCalc.calculate(closeRet24, index), atr14, index),   doc::setMomCloseRet24AbsAtrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet32AbsAtrn",  doc::getMomCloseRet32AbsAtrn,  () -> MomCloseRet32AbsAtrnCalc.calculate(MomCloseRet32AbsCalc.calculate(closeRet32, index), atr14, index),   doc::setMomCloseRet32AbsAtrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet48AbsAtrn",  doc::getMomCloseRet48AbsAtrn,  () -> MomCloseRet48AbsAtrnCalc.calculate(MomCloseRet48AbsCalc.calculate(closeRet48, index), atr14, index),   doc::setMomCloseRet48AbsAtrn,  tasks); // fora da prod mask
        // ifNull("momCloseRet288AbsAtrn", doc::getMomCloseRet288AbsAtrn, () -> MomCloseRet288AbsAtrnCalc.calculate(MomCloseRet288AbsCalc.calculate(closeRet288, index), atr14, index), doc::setMomCloseRet288AbsAtrn, tasks); // fora da prod mask

        // ── CLOSE RETURN ABS STDN ───────────────────────────────────────────
        // ifNull("momCloseRet1AbsStdn",   doc::getMomCloseRet1AbsStdn,   () -> MomCloseRet1AbsStdnCalc.calculate(MomCloseRet1AbsCalc.calculate(closeRet1, index), std14, index),     doc::setMomCloseRet1AbsStdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet2AbsStdn",   doc::getMomCloseRet2AbsStdn,   () -> MomCloseRet2AbsStdnCalc.calculate(MomCloseRet2AbsCalc.calculate(closeRet2, index), std14, index),     doc::setMomCloseRet2AbsStdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet3AbsStdn",   doc::getMomCloseRet3AbsStdn,   () -> MomCloseRet3AbsStdnCalc.calculate(MomCloseRet3AbsCalc.calculate(closeRet3, index), std14, index),     doc::setMomCloseRet3AbsStdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet4AbsStdn",   doc::getMomCloseRet4AbsStdn,   () -> MomCloseRet4AbsStdnCalc.calculate(MomCloseRet4AbsCalc.calculate(closeRet4, index), std14, index),     doc::setMomCloseRet4AbsStdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet5AbsStdn",   doc::getMomCloseRet5AbsStdn,   () -> MomCloseRet5AbsStdnCalc.calculate(MomCloseRet5AbsCalc.calculate(closeRet5, index), std14, index),     doc::setMomCloseRet5AbsStdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet6AbsStdn",   doc::getMomCloseRet6AbsStdn,   () -> MomCloseRet6AbsStdnCalc.calculate(MomCloseRet6AbsCalc.calculate(closeRet6, index), std14, index),     doc::setMomCloseRet6AbsStdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet8AbsStdn",   doc::getMomCloseRet8AbsStdn,   () -> MomCloseRet8AbsStdnCalc.calculate(MomCloseRet8AbsCalc.calculate(closeRet8, index), std14, index),     doc::setMomCloseRet8AbsStdn,   tasks); // fora da prod mask
        // ifNull("momCloseRet10AbsStdn",  doc::getMomCloseRet10AbsStdn,  () -> MomCloseRet10AbsStdnCalc.calculate(MomCloseRet10AbsCalc.calculate(closeRet10, index), std14, index),   doc::setMomCloseRet10AbsStdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet12AbsStdn",  doc::getMomCloseRet12AbsStdn,  () -> MomCloseRet12AbsStdnCalc.calculate(MomCloseRet12AbsCalc.calculate(closeRet12, index), std14, index),   doc::setMomCloseRet12AbsStdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet16AbsStdn",  doc::getMomCloseRet16AbsStdn,  () -> MomCloseRet16AbsStdnCalc.calculate(MomCloseRet16AbsCalc.calculate(closeRet16, index), std14, index),   doc::setMomCloseRet16AbsStdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet24AbsStdn",  doc::getMomCloseRet24AbsStdn,  () -> MomCloseRet24AbsStdnCalc.calculate(MomCloseRet24AbsCalc.calculate(closeRet24, index), std14, index),   doc::setMomCloseRet24AbsStdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet32AbsStdn",  doc::getMomCloseRet32AbsStdn,  () -> MomCloseRet32AbsStdnCalc.calculate(MomCloseRet32AbsCalc.calculate(closeRet32, index), std14, index),   doc::setMomCloseRet32AbsStdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet48AbsStdn",  doc::getMomCloseRet48AbsStdn,  () -> MomCloseRet48AbsStdnCalc.calculate(MomCloseRet48AbsCalc.calculate(closeRet48, index), std14, index),   doc::setMomCloseRet48AbsStdn,  tasks); // fora da prod mask
        // ifNull("momCloseRet288AbsStdn", doc::getMomCloseRet288AbsStdn, () -> MomCloseRet288AbsStdnCalc.calculate(MomCloseRet288AbsCalc.calculate(closeRet288, index), std14, index), doc::setMomCloseRet288AbsStdn, tasks); // fora da prod mask

        // ── BURST ───────────────────────────────────────────────────────────
        ifNull("momBurst10",  doc::getMomBurst10,  () -> MomBurst10Calc.calculate(burst10, index),   doc::setMomBurst10,  tasks);
        ifNull("momBurst16",  doc::getMomBurst16,  () -> MomBurst16Calc.calculate(burst16, index),   doc::setMomBurst16,  tasks);
        ifNull("momBurst32",  doc::getMomBurst32,  () -> MomBurst32Calc.calculate(burst32, index),   doc::setMomBurst32,  tasks);
        // ifNull("momBurst48",  doc::getMomBurst48,  () -> MomBurst48Calc.calculate(burst48, index),   doc::setMomBurst48,  tasks); // fora da prod mask
        // ifNull("momBurst288", doc::getMomBurst288, () -> MomBurst288Calc.calculate(burst288, index), doc::setMomBurst288, tasks); // fora da prod mask

        // ── CONTINUATION RATE ───────────────────────────────────────────────
        ifNull("momCntrate10",  doc::getMomCntrate10,  () -> MomCntrate10Calc.calculate(cntrate10, index),   doc::setMomCntrate10,  tasks);
        ifNull("momCntrate16",  doc::getMomCntrate16,  () -> MomCntrate16Calc.calculate(cntrate16, index),   doc::setMomCntrate16,  tasks);
        ifNull("momCntrate32",  doc::getMomCntrate32,  () -> MomCntrate32Calc.calculate(cntrate32, index),   doc::setMomCntrate32,  tasks);
        // ifNull("momCntrate48",  doc::getMomCntrate48,  () -> MomCntrate48Calc.calculate(cntrate48, index),   doc::setMomCntrate48,  tasks); // fora da prod mask
        // ifNull("momCntrate288", doc::getMomCntrate288, () -> MomCntrate288Calc.calculate(cntrate288, index), doc::setMomCntrate288, tasks); // fora da prod mask

        // ── DECAY ───────────────────────────────────────────────────────────
        ifNull("momDecay10",  doc::getMomDecay10,  () -> MomDecay10Calc.calculate(decay10, index),   doc::setMomDecay10,  tasks);
        ifNull("momDecay16",  doc::getMomDecay16,  () -> MomDecay16Calc.calculate(decay16, index),   doc::setMomDecay16,  tasks);
        ifNull("momDecay32",  doc::getMomDecay32,  () -> MomDecay32Calc.calculate(decay32, index),   doc::setMomDecay32,  tasks);
        // ifNull("momDecay48",  doc::getMomDecay48,  () -> MomDecay48Calc.calculate(decay48, index),   doc::setMomDecay48,  tasks); // fora da prod mask
        // ifNull("momDecay288", doc::getMomDecay288, () -> MomDecay288Calc.calculate(decay288, index), doc::setMomDecay288, tasks); // fora da prod mask

        // ── IMPULSE ─────────────────────────────────────────────────────────
        ifNull("momImpls10",  doc::getMomImpls10,  () -> MomImpls10Calc.calculate(impls10, index),   doc::setMomImpls10,  tasks);
        ifNull("momImpls16",  doc::getMomImpls16,  () -> MomImpls16Calc.calculate(impls16, index),   doc::setMomImpls16,  tasks);
        ifNull("momImpls32",  doc::getMomImpls32,  () -> MomImpls32Calc.calculate(impls32, index),   doc::setMomImpls32,  tasks);
        // ifNull("momImpls48",  doc::getMomImpls48,  () -> MomImpls48Calc.calculate(impls48, index),   doc::setMomImpls48,  tasks); // fora da prod mask
        // ifNull("momImpls288", doc::getMomImpls288, () -> MomImpls288Calc.calculate(impls288, index), doc::setMomImpls288, tasks); // fora da prod mask

        // ── CHOP RATIO ──────────────────────────────────────────────────────
        ifNull("momChprt10",  doc::getMomChprt10,  () -> MomChprt10Calc.calculate(chprt10, index),   doc::setMomChprt10,  tasks);
        ifNull("momChprt16",  doc::getMomChprt16,  () -> MomChprt16Calc.calculate(chprt16, index),   doc::setMomChprt16,  tasks);
        ifNull("momChprt32",  doc::getMomChprt32,  () -> MomChprt32Calc.calculate(chprt32, index),   doc::setMomChprt32,  tasks);
        // ifNull("momChprt48",  doc::getMomChprt48,  () -> MomChprt48Calc.calculate(chprt48, index),   doc::setMomChprt48,  tasks); // fora da prod mask
        // ifNull("momChprt288", doc::getMomChprt288, () -> MomChprt288Calc.calculate(chprt288, index), doc::setMomChprt288, tasks); // fora da prod mask

        // ── RSI RAW ─────────────────────────────────────────────────────────
        // ifNull("momRsi2",   doc::getMomRsi2,   () -> Rsi2Calc.calculate(rsi2, index),     doc::setMomRsi2,   tasks); // fora da prod mask
        // ifNull("momRsi3",   doc::getMomRsi3,   () -> Rsi3Calc.calculate(rsi3, index),     doc::setMomRsi3,   tasks); // fora da prod mask
        // ifNull("momRsi4",   doc::getMomRsi4,   () -> Rsi4Calc.calculate(rsi4, index),     doc::setMomRsi4,   tasks); // fora da prod mask
        // ifNull("momRsi5",   doc::getMomRsi5,   () -> Rsi5Calc.calculate(rsi5, index),     doc::setMomRsi5,   tasks); // fora da prod mask
        // ifNull("momRsi6",   doc::getMomRsi6,   () -> Rsi6Calc.calculate(rsi6, index),     doc::setMomRsi6,   tasks); // fora da prod mask
        // ifNull("momRsi7",   doc::getMomRsi7,   () -> Rsi7Calc.calculate(rsi7, index),     doc::setMomRsi7,   tasks); // fora da prod mask
        // ifNull("momRsi8",   doc::getMomRsi8,   () -> Rsi8Calc.calculate(rsi8, index),     doc::setMomRsi8,   tasks); // fora da prod mask
        // ifNull("momRsi9",   doc::getMomRsi9,   () -> Rsi9Calc.calculate(rsi9, index),     doc::setMomRsi9,   tasks); // fora da prod mask
        // ifNull("momRsi10",  doc::getMomRsi10,  () -> Rsi10Calc.calculate(rsi10, index),   doc::setMomRsi10,  tasks); // fora da prod mask
        // ifNull("momRsi12",  doc::getMomRsi12,  () -> Rsi12Calc.calculate(rsi12, index),   doc::setMomRsi12,  tasks); // fora da prod mask
        // ifNull("momRsi14",  doc::getMomRsi14,  () -> Rsi14Calc.calculate(rsi14, index),   doc::setMomRsi14,  tasks); // fora da prod mask (raw)
        // ifNull("momRsi16",  doc::getMomRsi16,  () -> Rsi16Calc.calculate(rsi16, index),   doc::setMomRsi16,  tasks); // fora da prod mask
        // ifNull("momRsi21",  doc::getMomRsi21,  () -> Rsi21Calc.calculate(rsi21, index),   doc::setMomRsi21,  tasks); // fora da prod mask
        // ifNull("momRsi24",  doc::getMomRsi24,  () -> Rsi24Calc.calculate(rsi24, index),   doc::setMomRsi24,  tasks); // fora da prod mask
        // ifNull("momRsi28",  doc::getMomRsi28,  () -> Rsi28Calc.calculate(rsi28, index),   doc::setMomRsi28,  tasks); // fora da prod mask
        // ifNull("momRsi32",  doc::getMomRsi32,  () -> Rsi32Calc.calculate(rsi32, index),   doc::setMomRsi32,  tasks); // fora da prod mask
        // ifNull("momRsi48",  doc::getMomRsi48,  () -> Rsi48Calc.calculate(rsi48, index),   doc::setMomRsi48,  tasks); // fora da prod mask (raw)
        // ifNull("momRsi288", doc::getMomRsi288, () -> Rsi288Calc.calculate(rsi288, index), doc::setMomRsi288, tasks); // fora da prod mask (raw)

        // ── RSI DLT ─────────────────────────────────────────────────────────
        ifNull("momRsi2Dlt",   doc::getMomRsi2Dlt,   () -> Rsi2DltCalc.calculate(rsi2, index),     doc::setMomRsi2Dlt,   tasks);
        ifNull("momRsi3Dlt",   doc::getMomRsi3Dlt,   () -> Rsi3DltCalc.calculate(rsi3, index),     doc::setMomRsi3Dlt,   tasks);
        ifNull("momRsi5Dlt",   doc::getMomRsi5Dlt,   () -> Rsi5DltCalc.calculate(rsi5, index),     doc::setMomRsi5Dlt,   tasks);
        ifNull("momRsi7Dlt",   doc::getMomRsi7Dlt,   () -> Rsi7DltCalc.calculate(rsi7, index),     doc::setMomRsi7Dlt,   tasks);
        ifNull("momRsi14Dlt",  doc::getMomRsi14Dlt,  () -> Rsi14DltCalc.calculate(rsi14, index),   doc::setMomRsi14Dlt,  tasks);

        // ── RSI ROC ─────────────────────────────────────────────────────────
        ifNull("momRsi2Roc",   doc::getMomRsi2Roc,   () -> Rsi2RocCalc.calculate(rsi2, index),     doc::setMomRsi2Roc,   tasks);
        ifNull("momRsi3Roc",   doc::getMomRsi3Roc,   () -> Rsi3RocCalc.calculate(rsi3, index),     doc::setMomRsi3Roc,   tasks);
        ifNull("momRsi5Roc",   doc::getMomRsi5Roc,   () -> Rsi5RocCalc.calculate(rsi5, index),     doc::setMomRsi5Roc,   tasks);
        ifNull("momRsi7Roc",   doc::getMomRsi7Roc,   () -> Rsi7RocCalc.calculate(rsi7, index),     doc::setMomRsi7Roc,   tasks);
        ifNull("momRsi14Roc",  doc::getMomRsi14Roc,  () -> Rsi14RocCalc.calculate(rsi14, index),   doc::setMomRsi14Roc,  tasks);

        // ── RSI SLP ─────────────────────────────────────────────────────────
        ifNull("momRsi7Slp",   doc::getMomRsi7Slp,   () -> Rsi7SlpCalc.calculate(rsiSlp7, index),     doc::setMomRsi7Slp,   tasks);
        ifNull("momRsi14Slp",  doc::getMomRsi14Slp,  () -> Rsi14SlpCalc.calculate(rsiSlp14, index),   doc::setMomRsi14Slp,  tasks);
        // ifNull("momRsi28Slp",  doc::getMomRsi28Slp,  () -> Rsi28SlpCalc.calculate(rsiSlp28, index),   doc::setMomRsi28Slp,  tasks); // fora da prod mask

        // ── RSI ATRN ────────────────────────────────────────────────────────
        ifNull("momRsi14Atrn", doc::getMomRsi14Atrn, () -> Rsi14AtrnCalc.calculate(rsi14, atr14, index), doc::setMomRsi14Atrn, tasks);

        // ── RSI VLT ─────────────────────────────────────────────────────────
        // ifNull("momRsi7Vlt",   doc::getMomRsi7Vlt,   () -> Rsi7VltCalc.calculate(rsi7, index),     doc::setMomRsi7Vlt,   tasks); // fora da prod mask
        // ifNull("momRsi14Vlt",  doc::getMomRsi14Vlt,  () -> Rsi14VltCalc.calculate(rsi14, index),   doc::setMomRsi14Vlt,  tasks); // fora da prod mask

        // ── RSI ACC ─────────────────────────────────────────────────────────
        ifNull("momRsi14Acc", doc::getMomRsi14Acc, () -> Rsi14AccCalc.calculate(rsi14, index), doc::setMomRsi14Acc, tasks);

        // ── RSI DST MID ─────────────────────────────────────────────────────
        ifNull("momRsi7DstMid",  doc::getMomRsi7DstMid,  () -> Rsi7DstMidCalc.calculate(rsi7, index),   doc::setMomRsi7DstMid,  tasks);
        ifNull("momRsi14DstMid", doc::getMomRsi14DstMid, () -> Rsi14DstMidCalc.calculate(rsi14, index), doc::setMomRsi14DstMid, tasks);

        // ── RSI TAIL ────────────────────────────────────────────────────────
        ifNull("momRsi7TailUp",  doc::getMomRsi7TailUp,  () -> Rsi7TailUpCalc.calculate(rsi7, index),   doc::setMomRsi7TailUp,  tasks);
        ifNull("momRsi7TailDw",  doc::getMomRsi7TailDw,  () -> Rsi7TailDwCalc.calculate(rsi7, index),   doc::setMomRsi7TailDw,  tasks);
        ifNull("momRsi14TailUp", doc::getMomRsi14TailUp, () -> Rsi14TailUpCalc.calculate(rsi14, index), doc::setMomRsi14TailUp, tasks);
        ifNull("momRsi14TailDw", doc::getMomRsi14TailDw, () -> Rsi14TailDwCalc.calculate(rsi14, index), doc::setMomRsi14TailDw, tasks);

        // ── RSI 48/288 DLT/ROC/SLP/VLT/DST_MID/TAIL ────────────────────────
        // ifNull("momRsi48Dlt",     doc::getMomRsi48Dlt,     () -> Rsi48DltCalc.calculate(rsi48, index),     doc::setMomRsi48Dlt,     tasks); // fora da prod mask
        // ifNull("momRsi288Dlt",    doc::getMomRsi288Dlt,    () -> Rsi288DltCalc.calculate(rsi288, index),   doc::setMomRsi288Dlt,    tasks); // fora da prod mask
        // ifNull("momRsi48Roc",     doc::getMomRsi48Roc,     () -> Rsi48RocCalc.calculate(rsi48, index),     doc::setMomRsi48Roc,     tasks); // fora da prod mask
        // ifNull("momRsi288Roc",    doc::getMomRsi288Roc,    () -> Rsi288RocCalc.calculate(rsi288, index),   doc::setMomRsi288Roc,    tasks); // fora da prod mask
        // ifNull("momRsi48Slp",     doc::getMomRsi48Slp,     () -> Rsi48SlpCalc.calculate(rsiSlp48, index),     doc::setMomRsi48Slp,     tasks); // fora da prod mask
        // ifNull("momRsi288Slp",    doc::getMomRsi288Slp,    () -> Rsi288SlpCalc.calculate(rsiSlp288, index),   doc::setMomRsi288Slp,    tasks); // fora da prod mask
        // ifNull("momRsi48Vlt",     doc::getMomRsi48Vlt,     () -> Rsi48VltCalc.calculate(rsi48, index),     doc::setMomRsi48Vlt,     tasks); // fora da prod mask
        // ifNull("momRsi288Vlt",    doc::getMomRsi288Vlt,    () -> Rsi288VltCalc.calculate(rsi288, index),   doc::setMomRsi288Vlt,    tasks); // fora da prod mask
        // ifNull("momRsi48DstMid",  doc::getMomRsi48DstMid,  () -> Rsi48DstMidCalc.calculate(rsi48, index), doc::setMomRsi48DstMid,  tasks); // fora da prod mask
        // ifNull("momRsi288DstMid", doc::getMomRsi288DstMid, () -> Rsi288DstMidCalc.calculate(rsi288, index), doc::setMomRsi288DstMid, tasks); // fora da prod mask
        // ifNull("momRsi48TailUp",  doc::getMomRsi48TailUp,  () -> Rsi48TailUpCalc.calculate(rsi48, index), doc::setMomRsi48TailUp,  tasks); // fora da prod mask
        // ifNull("momRsi48TailDw",  doc::getMomRsi48TailDw,  () -> Rsi48TailDwCalc.calculate(rsi48, index), doc::setMomRsi48TailDw,  tasks); // fora da prod mask
        // ifNull("momRsi288TailUp", doc::getMomRsi288TailUp, () -> Rsi288TailUpCalc.calculate(rsi288, index), doc::setMomRsi288TailUp, tasks); // fora da prod mask
        // ifNull("momRsi288TailDw", doc::getMomRsi288TailDw, () -> Rsi288TailDwCalc.calculate(rsi288, index), doc::setMomRsi288TailDw, tasks); // fora da prod mask

        // ── RSI REGIME ──────────────────────────────────────────────────────
        // ifNull("momRsi14RegimeState",     doc::getMomRsi14RegimeState,     () -> Rsi14RegimeStateCalc.calculate(rsi14, index),     doc::setMomRsi14RegimeState,     tasks); // fora da prod mask
        // ifNull("momRsi14RegimePrstW20",   doc::getMomRsi14RegimePrstW20,   () -> Rsi14RegimePrstW20Calc.calculate(rsi14, index),   doc::setMomRsi14RegimePrstW20,   tasks); // fora da prod mask

        // ── RSI ZSC/PCTILE/SHOCK ────────────────────────────────────────────
        // ifNull("momRsi14Zsc80",          doc::getMomRsi14Zsc80,          () -> Rsi14ZscW80Calc.calculate(rsi14, index),          doc::setMomRsi14Zsc80,          tasks); // fora da prod mask
        // ifNull("momRsi14PctileW80",      doc::getMomRsi14PctileW80,      () -> Rsi14PctileW80Calc.calculate(rsi14, index),       doc::setMomRsi14PctileW80,      tasks); // fora da prod mask
        // ifNull("momRsi14Shock1",         doc::getMomRsi14Shock1,         () -> Rsi14Shock1Calc.calculate(rsi14, index),          doc::setMomRsi14Shock1,         tasks); // fora da prod mask
        // ifNull("momRsi14Shock1Stdn80",   doc::getMomRsi14Shock1Stdn80,   () -> Rsi14Shock1StdnW80Calc.calculate(rsi14, index),   doc::setMomRsi14Shock1Stdn80,   tasks); // fora da prod mask

        // ── RSI 48 ZSC/PCTILE/SHOCK/REGIME ──────────────────────────────────
        // ifNull("momRsi48Zsc80",          doc::getMomRsi48Zsc80,          () -> Rsi48ZscW80Calc.calculate(rsi48, index),          doc::setMomRsi48Zsc80,          tasks); // fora da prod mask
        // ifNull("momRsi48PctileW80",      doc::getMomRsi48PctileW80,      () -> Rsi48PctileW80Calc.calculate(rsi48, index),       doc::setMomRsi48PctileW80,      tasks); // fora da prod mask
        // ifNull("momRsi48Shock1",         doc::getMomRsi48Shock1,         () -> Rsi48Shock1Calc.calculate(rsi48, index),          doc::setMomRsi48Shock1,         tasks); // fora da prod mask
        // ifNull("momRsi48Shock1Stdn80",   doc::getMomRsi48Shock1Stdn80,   () -> Rsi48Shock1StdnW80Calc.calculate(rsi48, index),   doc::setMomRsi48Shock1Stdn80,   tasks); // fora da prod mask
        // ifNull("momRsi48RegimeState",    doc::getMomRsi48RegimeState,    () -> Rsi48RegimeStateCalc.calculate(rsi48, index),     doc::setMomRsi48RegimeState,    tasks); // fora da prod mask
        // ifNull("momRsi48RegimePrstW20",  doc::getMomRsi48RegimePrstW20,  () -> Rsi48RegimePrstW20Calc.calculate(rsi48, index),   doc::setMomRsi48RegimePrstW20,  tasks); // fora da prod mask

        // ── RSI 288 ZSC/PCTILE/SHOCK/REGIME ─────────────────────────────────
        // ifNull("momRsi288Zsc80",         doc::getMomRsi288Zsc80,         () -> Rsi288ZscW80Calc.calculate(rsi288, index),        doc::setMomRsi288Zsc80,         tasks); // fora da prod mask
        // ifNull("momRsi288PctileW80",     doc::getMomRsi288PctileW80,     () -> Rsi288PctileW80Calc.calculate(rsi288, index),     doc::setMomRsi288PctileW80,     tasks); // fora da prod mask
        // ifNull("momRsi288Shock1",        doc::getMomRsi288Shock1,        () -> Rsi288Shock1Calc.calculate(rsi288, index),        doc::setMomRsi288Shock1,        tasks); // fora da prod mask
        // ifNull("momRsi288Shock1Stdn80",  doc::getMomRsi288Shock1Stdn80,  () -> Rsi288Shock1StdnW80Calc.calculate(rsi288, index), doc::setMomRsi288Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momRsi288RegimeState",   doc::getMomRsi288RegimeState,   () -> Rsi288RegimeStateCalc.calculate(rsi288, index),   doc::setMomRsi288RegimeState,   tasks); // fora da prod mask
        // ifNull("momRsi288RegimePrstW20", doc::getMomRsi288RegimePrstW20, () -> Rsi288RegimePrstW20Calc.calculate(rsi288, index), doc::setMomRsi288RegimePrstW20, tasks); // fora da prod mask

        // ── CMO ─────────────────────────────────────────────────────────────
        // ifNull("momCmo14",    doc::getMomCmo14,    () -> MomCmo14Calc.calculate(cmo14, index),    doc::setMomCmo14,    tasks); // fora da prod mask (raw)
        // ifNull("momCmo20",    doc::getMomCmo20,    () -> MomCmo20Calc.calculate(cmo20, index),    doc::setMomCmo20,    tasks); // fora da prod mask (raw)
        // ifNull("momCmo48",    doc::getMomCmo48,    () -> MomCmo48Calc.calculate(cmo48, index),    doc::setMomCmo48,    tasks); // fora da prod mask
        // ifNull("momCmo288",   doc::getMomCmo288,   () -> MomCmo288Calc.calculate(cmo288, index),  doc::setMomCmo288,   tasks); // fora da prod mask
        ifNull("momCmo14Dlt", doc::getMomCmo14Dlt, () -> MomCmo14DltCalc.calculate(cmo14, index), doc::setMomCmo14Dlt, tasks);
        ifNull("momCmo20Dlt", doc::getMomCmo20Dlt, () -> MomCmo20DltCalc.calculate(cmo20, index), doc::setMomCmo20Dlt, tasks);
        // ifNull("momCmo48Dlt", doc::getMomCmo48Dlt, () -> MomCmo48DltCalc.calculate(cmo48, index), doc::setMomCmo48Dlt, tasks); // fora da prod mask
        // ifNull("momCmo288Dlt", doc::getMomCmo288Dlt, () -> MomCmo288DltCalc.calculate(cmo288, index), doc::setMomCmo288Dlt, tasks); // fora da prod mask
        ifNull("momCmo14DstMid",  doc::getMomCmo14DstMid,  () -> MomCmo14DstMidCalc.calculate(cmo14, index),  doc::setMomCmo14DstMid,  tasks);
        ifNull("momCmo20DstMid",  doc::getMomCmo20DstMid,  () -> MomCmo20DstMidCalc.calculate(cmo20, index),  doc::setMomCmo20DstMid,  tasks);
        // ifNull("momCmo48DstMid",  doc::getMomCmo48DstMid,  () -> MomCmo48DstMidCalc.calculate(cmo48, index),  doc::setMomCmo48DstMid,  tasks); // fora da prod mask
        // ifNull("momCmo288DstMid", doc::getMomCmo288DstMid, () -> MomCmo288DstMidCalc.calculate(cmo288, index), doc::setMomCmo288DstMid, tasks); // fora da prod mask
        // ifNull("momCmo20Zsc80",         doc::getMomCmo20Zsc80,         () -> MomCmo20ZscW80Calc.calculate(cmo20, index),         doc::setMomCmo20Zsc80,         tasks); // fora da prod mask
        // ifNull("momCmo20PctileW80",     doc::getMomCmo20PctileW80,     () -> MomCmo20PctileW80Calc.calculate(cmo20, index),      doc::setMomCmo20PctileW80,     tasks); // fora da prod mask
        // ifNull("momCmo48Zsc80",         doc::getMomCmo48Zsc80,         () -> MomCmo48ZscW80Calc.calculate(cmo48, index),         doc::setMomCmo48Zsc80,         tasks); // fora da prod mask
        // ifNull("momCmo48PctileW80",     doc::getMomCmo48PctileW80,     () -> MomCmo48PctileW80Calc.calculate(cmo48, index),      doc::setMomCmo48PctileW80,     tasks); // fora da prod mask
        // ifNull("momCmo288Zsc80",        doc::getMomCmo288Zsc80,        () -> MomCmo288ZscW80Calc.calculate(cmo288, index),       doc::setMomCmo288Zsc80,        tasks); // fora da prod mask
        // ifNull("momCmo288PctileW80",    doc::getMomCmo288PctileW80,    () -> MomCmo288PctileW80Calc.calculate(cmo288, index),    doc::setMomCmo288PctileW80,    tasks); // fora da prod mask
        // ifNull("momCmo20Shock1",        doc::getMomCmo20Shock1,        () -> MomCmo20Shock1Calc.calculate(cmo20, index),         doc::setMomCmo20Shock1,        tasks); // fora da prod mask
        // ifNull("momCmo20Shock1Stdn80",  doc::getMomCmo20Shock1Stdn80,  () -> MomCmo20Shock1StdnW80Calc.calculate(cmo20, index),  doc::setMomCmo20Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momCmo48Shock1",        doc::getMomCmo48Shock1,        () -> MomCmo48Shock1Calc.calculate(cmo48, index),         doc::setMomCmo48Shock1,        tasks); // fora da prod mask
        // ifNull("momCmo48Shock1Stdn80",  doc::getMomCmo48Shock1Stdn80,  () -> MomCmo48Shock1StdnW80Calc.calculate(cmo48, index),  doc::setMomCmo48Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momCmo288Shock1",       doc::getMomCmo288Shock1,       () -> MomCmo288Shock1Calc.calculate(cmo288, index),       doc::setMomCmo288Shock1,       tasks); // fora da prod mask
        // ifNull("momCmo288Shock1Stdn80", doc::getMomCmo288Shock1Stdn80, () -> MomCmo288Shock1StdnW80Calc.calculate(cmo288, index), doc::setMomCmo288Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momCmo20RegimeState",     doc::getMomCmo20RegimeState,     () -> MomCmo20RegimeStateCalc.calculate(cmo20, index),     doc::setMomCmo20RegimeState,     tasks); // fora da prod mask
        // ifNull("momCmo20RegimePrstW20",   doc::getMomCmo20RegimePrstW20,   () -> MomCmo20RegimePrstW20Calc.calculate(cmo20, index),   doc::setMomCmo20RegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momCmo48RegimeState",     doc::getMomCmo48RegimeState,     () -> MomCmo48RegimeStateCalc.calculate(cmo48, index),     doc::setMomCmo48RegimeState,     tasks); // fora da prod mask
        // ifNull("momCmo48RegimePrstW20",   doc::getMomCmo48RegimePrstW20,   () -> MomCmo48RegimePrstW20Calc.calculate(cmo48, index),   doc::setMomCmo48RegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momCmo288RegimeState",    doc::getMomCmo288RegimeState,    () -> MomCmo288RegimeStateCalc.calculate(cmo288, index),   doc::setMomCmo288RegimeState,    tasks); // fora da prod mask
        // ifNull("momCmo288RegimePrstW20",  doc::getMomCmo288RegimePrstW20,  () -> MomCmo288RegimePrstW20Calc.calculate(cmo288, index), doc::setMomCmo288RegimePrstW20,  tasks); // fora da prod mask

        // ── WPR ─────────────────────────────────────────────────────────────
        // ifNull("momWpr14",     doc::getMomWpr14,     () -> MomWpr14Calc.calculate(wpr14, index),     doc::setMomWpr14,     tasks); // fora da prod mask (raw)
        ifNull("momWpr14Dlt",  doc::getMomWpr14Dlt,  () -> MomWpr14DltCalc.calculate(wpr14, index),  doc::setMomWpr14Dlt,  tasks);
        // ifNull("momWpr28",     doc::getMomWpr28,     () -> MomWpr28Calc.calculate(wpr28, index),     doc::setMomWpr28,     tasks); // fora da prod mask (raw)
        ifNull("momWpr28Dlt",  doc::getMomWpr28Dlt,  () -> MomWpr28DltCalc.calculate(wpr28, index),  doc::setMomWpr28Dlt,  tasks);
        // ifNull("momWpr42",     doc::getMomWpr42,     () -> MomWpr42Calc.calculate(wpr42, index),     doc::setMomWpr42,     tasks); // fora da prod mask (raw)
        ifNull("momWpr42Dlt",  doc::getMomWpr42Dlt,  () -> MomWpr42DltCalc.calculate(wpr42, index),  doc::setMomWpr42Dlt,  tasks);
        // ifNull("momWpr48",     doc::getMomWpr48,     () -> MomWpr48Calc.calculate(wpr48, index),     doc::setMomWpr48,     tasks); // fora da prod mask (raw)
        // ifNull("momWpr48Dlt",  doc::getMomWpr48Dlt,  () -> MomWpr48DltCalc.calculate(wpr48, index),  doc::setMomWpr48Dlt,  tasks); // fora da prod mask
        // ifNull("momWpr288",    doc::getMomWpr288,    () -> MomWpr288Calc.calculate(wpr288, index),   doc::setMomWpr288,    tasks); // fora da prod mask
        // ifNull("momWpr288Dlt", doc::getMomWpr288Dlt, () -> MomWpr288DltCalc.calculate(wpr288, index), doc::setMomWpr288Dlt, tasks); // fora da prod mask
        ifNull("momWpr14DstMid",  doc::getMomWpr14DstMid,  () -> MomWpr14DstMidCalc.calculate(wpr14, index),  doc::setMomWpr14DstMid,  tasks);
        ifNull("momWpr28DstMid",  doc::getMomWpr28DstMid,  () -> MomWpr28DstMidCalc.calculate(wpr28, index),  doc::setMomWpr28DstMid,  tasks);
        // ifNull("momWpr42DstMid",  doc::getMomWpr42DstMid,  () -> MomWpr42DstMidCalc.calculate(wpr42, index),  doc::setMomWpr42DstMid,  tasks); // fora da prod mask
        ifNull("momWpr48DstMid",  doc::getMomWpr48DstMid,  () -> MomWpr48DstMidCalc.calculate(wpr48, index),  doc::setMomWpr48DstMid,  tasks);
        // ifNull("momWpr288DstMid", doc::getMomWpr288DstMid, () -> MomWpr288DstMidCalc.calculate(wpr288, index), doc::setMomWpr288DstMid, tasks); // fora da prod mask
        // ifNull("momWpr28Zsc80",         doc::getMomWpr28Zsc80,         () -> MomWpr28ZscW80Calc.calculate(wpr28, index),         doc::setMomWpr28Zsc80,         tasks); // fora da prod mask
        // ifNull("momWpr28PctileW80",     doc::getMomWpr28PctileW80,     () -> MomWpr28PctileW80Calc.calculate(wpr28, index),      doc::setMomWpr28PctileW80,     tasks); // fora da prod mask
        // ifNull("momWpr48Zsc80",         doc::getMomWpr48Zsc80,         () -> MomWpr48ZscW80Calc.calculate(wpr48, index),         doc::setMomWpr48Zsc80,         tasks); // fora da prod mask
        // ifNull("momWpr48PctileW80",     doc::getMomWpr48PctileW80,     () -> MomWpr48PctileW80Calc.calculate(wpr48, index),      doc::setMomWpr48PctileW80,     tasks); // fora da prod mask
        // ifNull("momWpr288Zsc80",        doc::getMomWpr288Zsc80,        () -> MomWpr288ZscW80Calc.calculate(wpr288, index),       doc::setMomWpr288Zsc80,        tasks); // fora da prod mask
        // ifNull("momWpr288PctileW80",    doc::getMomWpr288PctileW80,    () -> MomWpr288PctileW80Calc.calculate(wpr288, index),    doc::setMomWpr288PctileW80,    tasks); // fora da prod mask
        // ifNull("momWpr28Shock1",        doc::getMomWpr28Shock1,        () -> MomWpr28Shock1Calc.calculate(wpr28, index),         doc::setMomWpr28Shock1,        tasks); // fora da prod mask
        // ifNull("momWpr28Shock1Stdn80",  doc::getMomWpr28Shock1Stdn80,  () -> MomWpr28Shock1StdnW80Calc.calculate(wpr28, index),  doc::setMomWpr28Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momWpr48Shock1",        doc::getMomWpr48Shock1,        () -> MomWpr48Shock1Calc.calculate(wpr48, index),         doc::setMomWpr48Shock1,        tasks); // fora da prod mask
        // ifNull("momWpr48Shock1Stdn80",  doc::getMomWpr48Shock1Stdn80,  () -> MomWpr48Shock1StdnW80Calc.calculate(wpr48, index),  doc::setMomWpr48Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momWpr288Shock1",       doc::getMomWpr288Shock1,       () -> MomWpr288Shock1Calc.calculate(wpr288, index),       doc::setMomWpr288Shock1,       tasks); // fora da prod mask
        // ifNull("momWpr288Shock1Stdn80", doc::getMomWpr288Shock1Stdn80, () -> MomWpr288Shock1StdnW80Calc.calculate(wpr288, index), doc::setMomWpr288Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momWpr28RegimeState",     doc::getMomWpr28RegimeState,     () -> MomWpr28RegimeStateCalc.calculate(wpr28, index),     doc::setMomWpr28RegimeState,     tasks); // fora da prod mask
        // ifNull("momWpr28RegimePrstW20",   doc::getMomWpr28RegimePrstW20,   () -> MomWpr28RegimePrstW20Calc.calculate(wpr28, index),   doc::setMomWpr28RegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momWpr48RegimeState",     doc::getMomWpr48RegimeState,     () -> MomWpr48RegimeStateCalc.calculate(wpr48, index),     doc::setMomWpr48RegimeState,     tasks); // fora da prod mask
        // ifNull("momWpr48RegimePrstW20",   doc::getMomWpr48RegimePrstW20,   () -> MomWpr48RegimePrstW20Calc.calculate(wpr48, index),   doc::setMomWpr48RegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momWpr288RegimeState",    doc::getMomWpr288RegimeState,    () -> MomWpr288RegimeStateCalc.calculate(wpr288, index),   doc::setMomWpr288RegimeState,    tasks); // fora da prod mask
        // ifNull("momWpr288RegimePrstW20",  doc::getMomWpr288RegimePrstW20,  () -> MomWpr288RegimePrstW20Calc.calculate(wpr288, index), doc::setMomWpr288RegimePrstW20,  tasks); // fora da prod mask

        // ── STOCHASTIC ──────────────────────────────────────────────────────
        // ifNull("momStoch14K",     doc::getMomStoch14K,     () -> MomStoch14KCalc.calculate(stochK14, index),     doc::setMomStoch14K,     tasks); // fora da prod mask (raw)
        // ifNull("momStoch14D",     doc::getMomStoch14D,     () -> MomStoch14DCalc.calculate(stochD14, index),     doc::setMomStoch14D,     tasks); // fora da prod mask (raw)
        ifNull("momStoch14KDlt",  doc::getMomStoch14KDlt,  () -> MomStoch14KDltCalc.calculate(stochK14, index),  doc::setMomStoch14KDlt,  tasks);
        ifNull("momStoch14DDlt",  doc::getMomStoch14DDlt,  () -> MomStoch14DDltCalc.calculate(stochD14, index),  doc::setMomStoch14DDlt,  tasks);
        // ifNull("momStoch48K",     doc::getMomStoch48K,     () -> MomStoch48KCalc.calculate(stochK48, index),     doc::setMomStoch48K,     tasks); // fora da prod mask
        // ifNull("momStoch48D",     doc::getMomStoch48D,     () -> MomStoch48DCalc.calculate(stochD48, index),     doc::setMomStoch48D,     tasks); // fora da prod mask
        // ifNull("momStoch48KDlt",  doc::getMomStoch48KDlt,  () -> MomStoch48KDltCalc.calculate(stochK48, index),  doc::setMomStoch48KDlt,  tasks); // fora da prod mask
        // ifNull("momStoch48DDlt",  doc::getMomStoch48DDlt,  () -> MomStoch48DDltCalc.calculate(stochD48, index),  doc::setMomStoch48DDlt,  tasks); // fora da prod mask
        // ifNull("momStoch288K",    doc::getMomStoch288K,    () -> MomStoch288KCalc.calculate(stochK288, index),   doc::setMomStoch288K,    tasks); // fora da prod mask
        // ifNull("momStoch288D",    doc::getMomStoch288D,    () -> MomStoch288DCalc.calculate(stochD288, index),   doc::setMomStoch288D,    tasks); // fora da prod mask
        // ifNull("momStoch288KDlt", doc::getMomStoch288KDlt, () -> MomStoch288KDltCalc.calculate(stochK288, index), doc::setMomStoch288KDlt, tasks); // fora da prod mask
        // ifNull("momStoch288DDlt", doc::getMomStoch288DDlt, () -> MomStoch288DDltCalc.calculate(stochD288, index), doc::setMomStoch288DDlt, tasks); // fora da prod mask
        ifNull("momStoch14Spread",     doc::getMomStoch14Spread,     () -> MomStoch14SpreadCalc.calculate(stochK14, stochD14, index),       doc::setMomStoch14Spread,     tasks);
        // ifNull("momStoch48Spread",     doc::getMomStoch48Spread,     () -> MomStoch48SpreadCalc.calculate(stochK48, stochD48, index),       doc::setMomStoch48Spread,     tasks); // fora da prod mask
        // ifNull("momStoch288Spread",    doc::getMomStoch288Spread,    () -> MomStoch288SpreadCalc.calculate(stochK288, stochD288, index),    doc::setMomStoch288Spread,    tasks); // fora da prod mask
        // ifNull("momStoch14CrossState", doc::getMomStoch14CrossState, () -> MomStoch14CrossStateCalc.calculate(stochK14, stochD14, index),   doc::setMomStoch14CrossState, tasks); // fora da prod mask
        // ifNull("momStoch48CrossState", doc::getMomStoch48CrossState, () -> MomStoch48CrossStateCalc.calculate(stochK48, stochD48, index),   doc::setMomStoch48CrossState, tasks); // fora da prod mask
        // ifNull("momStoch288CrossState", doc::getMomStoch288CrossState, () -> MomStoch288CrossStateCalc.calculate(stochK288, stochD288, index), doc::setMomStoch288CrossState, tasks); // fora da prod mask
        ifNull("momStoch14KDstMid",  doc::getMomStoch14KDstMid,  () -> MomStoch14KDstMidCalc.calculate(stochK14, index),  doc::setMomStoch14KDstMid,  tasks);
        // ifNull("momStoch48KDstMid",  doc::getMomStoch48KDstMid,  () -> MomStoch48KDstMidCalc.calculate(stochK48, index),  doc::setMomStoch48KDstMid,  tasks); // fora da prod mask
        // ifNull("momStoch288KDstMid", doc::getMomStoch288KDstMid, () -> MomStoch288KDstMidCalc.calculate(stochK288, index), doc::setMomStoch288KDstMid, tasks); // fora da prod mask
        // ifNull("momStoch14KZsc80",         doc::getMomStoch14KZsc80,         () -> MomStoch14KZsc80Calc.calculate(stochK14, index, 80),         doc::setMomStoch14KZsc80,         tasks); // fora da prod mask
        // ifNull("momStoch14KPctileW80",     doc::getMomStoch14KPctileW80,     () -> MomStoch14KPctileW80Calc.calculate(stochK14, index, 80),     doc::setMomStoch14KPctileW80,     tasks); // fora da prod mask
        // ifNull("momStoch48KZsc80",         doc::getMomStoch48KZsc80,         () -> MomStoch48KZsc80Calc.calculate(stochK48, index, 80),         doc::setMomStoch48KZsc80,         tasks); // fora da prod mask
        // ifNull("momStoch48KPctileW80",     doc::getMomStoch48KPctileW80,     () -> MomStoch48KPctileW80Calc.calculate(stochK48, index, 80),     doc::setMomStoch48KPctileW80,     tasks); // fora da prod mask
        // ifNull("momStoch288KZsc80",        doc::getMomStoch288KZsc80,        () -> MomStoch288KZsc80Calc.calculate(stochK288, index, 80),       doc::setMomStoch288KZsc80,        tasks); // fora da prod mask
        // ifNull("momStoch288KPctileW80",    doc::getMomStoch288KPctileW80,    () -> MomStoch288KPctileW80Calc.calculate(stochK288, index, 80),   doc::setMomStoch288KPctileW80,    tasks); // fora da prod mask
        // ifNull("momStoch14KShock1",        doc::getMomStoch14KShock1,        () -> MomStoch14KShock1Calc.calculate(stochK14, index),        doc::setMomStoch14KShock1,        tasks); // fora da prod mask
        // ifNull("momStoch14KShock1Stdn80",  doc::getMomStoch14KShock1Stdn80,  () -> MomStoch14KShock1Stdn80Calc.calculate(stochK14, index, 80),  doc::setMomStoch14KShock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momStoch48KShock1",        doc::getMomStoch48KShock1,        () -> MomStoch48KShock1Calc.calculate(stochK48, index),        doc::setMomStoch48KShock1,        tasks); // fora da prod mask
        // ifNull("momStoch48KShock1Stdn80",  doc::getMomStoch48KShock1Stdn80,  () -> MomStoch48KShock1Stdn80Calc.calculate(stochK48, index, 80),  doc::setMomStoch48KShock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momStoch288KShock1",       doc::getMomStoch288KShock1,       () -> MomStoch288KShock1Calc.calculate(stochK288, index),      doc::setMomStoch288KShock1,       tasks); // fora da prod mask
        // ifNull("momStoch288KShock1Stdn80", doc::getMomStoch288KShock1Stdn80, () -> MomStoch288KShock1Stdn80Calc.calculate(stochK288, index, 80), doc::setMomStoch288KShock1Stdn80, tasks); // fora da prod mask
        // ifNull("momStoch14KRegimeState",     doc::getMomStoch14KRegimeState,     () -> MomStoch14KRegimeStateCalc.calculate(stochK14, index),     doc::setMomStoch14KRegimeState,     tasks); // fora da prod mask
        // ifNull("momStoch14KRegimePrstW20",   doc::getMomStoch14KRegimePrstW20,   () -> MomStoch14KRegimePrstW20Calc.calculate(stochK14, index, 20),   doc::setMomStoch14KRegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momStoch48KRegimeState",     doc::getMomStoch48KRegimeState,     () -> MomStoch48KRegimeStateCalc.calculate(stochK48, index),     doc::setMomStoch48KRegimeState,     tasks); // fora da prod mask
        // ifNull("momStoch48KRegimePrstW20",   doc::getMomStoch48KRegimePrstW20,   () -> MomStoch48KRegimePrstW20Calc.calculate(stochK48, index, 20),   doc::setMomStoch48KRegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momStoch288KRegimeState",    doc::getMomStoch288KRegimeState,    () -> MomStoch288KRegimeStateCalc.calculate(stochK288, index),   doc::setMomStoch288KRegimeState,    tasks); // fora da prod mask
        // ifNull("momStoch288KRegimePrstW20",  doc::getMomStoch288KRegimePrstW20,  () -> MomStoch288KRegimePrstW20Calc.calculate(stochK288, index, 20), doc::setMomStoch288KRegimePrstW20,  tasks); // fora da prod mask

        // ── TRIX ────────────────────────────────────────────────────────────
        // ifNull("momTrix9",    doc::getMomTrix9,    () -> MomTrix9Calc.calculate(trix9, index),     doc::setMomTrix9,    tasks); // fora da prod mask (raw)
        // ifNull("momTrix48",   doc::getMomTrix48,   () -> MomTrix48Calc.calculate(trix48, index),   doc::setMomTrix48,   tasks); // fora da prod mask
        // ifNull("momTrix288",  doc::getMomTrix288,  () -> MomTrix288Calc.calculate(trix288, index), doc::setMomTrix288,  tasks); // fora da prod mask
        ifNull("momTrix9Dlt",   doc::getMomTrix9Dlt,   () -> MomTrix9DltCalc.calculate(trix9, index),     doc::setMomTrix9Dlt,   tasks);
        // ifNull("momTrix48Dlt",  doc::getMomTrix48Dlt,  () -> MomTrix48DltCalc.calculate(trix48, index),   doc::setMomTrix48Dlt,  tasks); // fora da prod mask
        // ifNull("momTrix288Dlt", doc::getMomTrix288Dlt, () -> MomTrix288DltCalc.calculate(trix288, index), doc::setMomTrix288Dlt, tasks); // fora da prod mask
        // ifNull("momTrix9Sig9",        doc::getMomTrix9Sig9,        () -> MomTrix9Sig9Calc.calculate(trix9, index),           doc::setMomTrix9Sig9,        tasks); // fora da prod mask
        ifNull("momTrix9Hist",        doc::getMomTrix9Hist,        () -> MomTrix9HistCalc.calculate(trix9, index),           doc::setMomTrix9Hist,        tasks);
        // ifNull("momTrix9CrossState",  doc::getMomTrix9CrossState,  () -> MomTrix9CrossStateCalc.calculate(trix9, index),     doc::setMomTrix9CrossState,  tasks); // fora da prod mask
        // ifNull("momTrix48Sig9",       doc::getMomTrix48Sig9,       () -> MomTrix48Sig9Calc.calculate(trix48, index),          doc::setMomTrix48Sig9,       tasks); // fora da prod mask
        // ifNull("momTrix48Hist",       doc::getMomTrix48Hist,       () -> MomTrix48HistCalc.calculate(trix48, index),          doc::setMomTrix48Hist,       tasks); // fora da prod mask
        // ifNull("momTrix48CrossState", doc::getMomTrix48CrossState, () -> MomTrix48CrossStateCalc.calculate(trix48, index),    doc::setMomTrix48CrossState, tasks); // fora da prod mask
        // ifNull("momTrix288Sig9",       doc::getMomTrix288Sig9,       () -> MomTrix288Sig9Calc.calculate(trix288, index),        doc::setMomTrix288Sig9,       tasks); // fora da prod mask
        // ifNull("momTrix288Hist",       doc::getMomTrix288Hist,       () -> MomTrix288HistCalc.calculate(trix288, index),        doc::setMomTrix288Hist,       tasks); // fora da prod mask
        // ifNull("momTrix288CrossState", doc::getMomTrix288CrossState, () -> MomTrix288CrossStateCalc.calculate(trix288, index),  doc::setMomTrix288CrossState, tasks); // fora da prod mask
        // ifNull("momTrix9Zsc80",          doc::getMomTrix9Zsc80,          () -> MomTrix9Zsc80Calc.calculate(trix9, index),          doc::setMomTrix9Zsc80,          tasks); // fora da prod mask
        // ifNull("momTrix9PctileW80",      doc::getMomTrix9PctileW80,      () -> MomTrix9PctileW80Calc.calculate(trix9, index),      doc::setMomTrix9PctileW80,      tasks); // fora da prod mask
        // ifNull("momTrix48Zsc80",         doc::getMomTrix48Zsc80,         () -> MomTrix48Zsc80Calc.calculate(trix48, index),        doc::setMomTrix48Zsc80,         tasks); // fora da prod mask
        // ifNull("momTrix48PctileW80",     doc::getMomTrix48PctileW80,     () -> MomTrix48PctileW80Calc.calculate(trix48, index),    doc::setMomTrix48PctileW80,     tasks); // fora da prod mask
        // ifNull("momTrix288Zsc80",        doc::getMomTrix288Zsc80,        () -> MomTrix288Zsc80Calc.calculate(trix288, index),      doc::setMomTrix288Zsc80,        tasks); // fora da prod mask
        // ifNull("momTrix288PctileW80",    doc::getMomTrix288PctileW80,    () -> MomTrix288PctileW80Calc.calculate(trix288, index),  doc::setMomTrix288PctileW80,    tasks); // fora da prod mask
        // ifNull("momTrix9Shock1",         doc::getMomTrix9Shock1,         () -> MomTrix9Shock1Calc.calculate(trix9, index),         doc::setMomTrix9Shock1,         tasks); // fora da prod mask
        // ifNull("momTrix9Shock1Stdn80",   doc::getMomTrix9Shock1Stdn80,   () -> MomTrix9Shock1Stdn80Calc.calculate(trix9, index),   doc::setMomTrix9Shock1Stdn80,   tasks); // fora da prod mask
        // ifNull("momTrix48Shock1",        doc::getMomTrix48Shock1,        () -> MomTrix48Shock1Calc.calculate(trix48, index),       doc::setMomTrix48Shock1,        tasks); // fora da prod mask
        // ifNull("momTrix48Shock1Stdn80",  doc::getMomTrix48Shock1Stdn80,  () -> MomTrix48Shock1Stdn80Calc.calculate(trix48, index), doc::setMomTrix48Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momTrix288Shock1",       doc::getMomTrix288Shock1,       () -> MomTrix288Shock1Calc.calculate(trix288, index),     doc::setMomTrix288Shock1,       tasks); // fora da prod mask
        // ifNull("momTrix288Shock1Stdn80", doc::getMomTrix288Shock1Stdn80, () -> MomTrix288Shock1Stdn80Calc.calculate(trix288, index), doc::setMomTrix288Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momTrix9RegimeState",      doc::getMomTrix9RegimeState,      () -> MomTrix9RegimeStateCalc.calculate(trix9, index),      doc::setMomTrix9RegimeState,      tasks); // fora da prod mask
        // ifNull("momTrix9RegimePrstW20",    doc::getMomTrix9RegimePrstW20,    () -> MomTrix9RegimePrstW20Calc.calculate(trix9, index),    doc::setMomTrix9RegimePrstW20,    tasks); // fora da prod mask
        // ifNull("momTrix48RegimeState",     doc::getMomTrix48RegimeState,     () -> MomTrix48RegimeStateCalc.calculate(trix48, index),    doc::setMomTrix48RegimeState,     tasks); // fora da prod mask
        // ifNull("momTrix48RegimePrstW20",   doc::getMomTrix48RegimePrstW20,   () -> MomTrix48RegimePrstW20Calc.calculate(trix48, index),  doc::setMomTrix48RegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momTrix288RegimeState",    doc::getMomTrix288RegimeState,    () -> MomTrix288RegimeStateCalc.calculate(trix288, index),  doc::setMomTrix288RegimeState,    tasks); // fora da prod mask
        // ifNull("momTrix288RegimePrstW20",  doc::getMomTrix288RegimePrstW20,  () -> MomTrix288RegimePrstW20Calc.calculate(trix288, index), doc::setMomTrix288RegimePrstW20, tasks); // fora da prod mask

        // ── TSI ─────────────────────────────────────────────────────────────
        // ifNull("momTsi2513",     doc::getMomTsi2513,     () -> MomTsi2513Calc.calculate(tsi2513, index),     doc::setMomTsi2513,     tasks); // fora da prod mask (raw)
        ifNull("momTsi2513Dlt",  doc::getMomTsi2513Dlt,  () -> MomTsi2513DltCalc.calculate(tsi2513, index),  doc::setMomTsi2513Dlt,  tasks);
        // ifNull("momTsi4825",     doc::getMomTsi4825,     () -> MomTsi4825Calc.calculate(tsi4825, index),     doc::setMomTsi4825,     tasks); // fora da prod mask
        // ifNull("momTsi4825Dlt",  doc::getMomTsi4825Dlt,  () -> MomTsi4825DltCalc.calculate(tsi4825, index),  doc::setMomTsi4825Dlt,  tasks); // fora da prod mask
        // ifNull("momTsi288144",    doc::getMomTsi288144,    () -> MomTsi288144Calc.calculate(tsi288144, index),   doc::setMomTsi288144,    tasks); // fora da prod mask
        // ifNull("momTsi288144Dlt", doc::getMomTsi288144Dlt, () -> MomTsi288144DltCalc.calculate(tsi288144, index), doc::setMomTsi288144Dlt, tasks); // fora da prod mask
        // ifNull("momTsi2513Sig7",       doc::getMomTsi2513Sig7,       () -> MomTsi2513Sig7Calc.calculate(tsi2513, index, 7),       doc::setMomTsi2513Sig7,       tasks); // fora da prod mask
        ifNull("momTsi2513Hist",       doc::getMomTsi2513Hist,       () -> MomTsi2513HistCalc.calculate(tsi2513, index, 7),       doc::setMomTsi2513Hist,       tasks);
        // ifNull("momTsi2513CrossState", doc::getMomTsi2513CrossState, () -> MomTsi2513CrossStateCalc.calculate(tsi2513, index, 7), doc::setMomTsi2513CrossState, tasks); // fora da prod mask
        // ifNull("momTsi4825Sig7",       doc::getMomTsi4825Sig7,       () -> MomTsi4825Sig7Calc.calculate(tsi4825, index, 7),       doc::setMomTsi4825Sig7,       tasks); // fora da prod mask
        // ifNull("momTsi4825Hist",       doc::getMomTsi4825Hist,       () -> MomTsi4825HistCalc.calculate(tsi4825, index, 7),       doc::setMomTsi4825Hist,       tasks); // fora da prod mask
        // ifNull("momTsi4825CrossState", doc::getMomTsi4825CrossState, () -> MomTsi4825CrossStateCalc.calculate(tsi4825, index, 7), doc::setMomTsi4825CrossState, tasks); // fora da prod mask
        // ifNull("momTsi288144Sig7",       doc::getMomTsi288144Sig7,       () -> MomTsi288144Sig7Calc.calculate(tsi288144, index, 7),       doc::setMomTsi288144Sig7,       tasks); // fora da prod mask
        // ifNull("momTsi288144Hist",       doc::getMomTsi288144Hist,       () -> MomTsi288144HistCalc.calculate(tsi288144, index, 7),       doc::setMomTsi288144Hist,       tasks); // fora da prod mask
        // ifNull("momTsi288144CrossState", doc::getMomTsi288144CrossState, () -> MomTsi288144CrossStateCalc.calculate(tsi288144, index, 7), doc::setMomTsi288144CrossState, tasks); // fora da prod mask
        ifNull("momTsi2513DstMid",   doc::getMomTsi2513DstMid,   () -> MomTsi2513DstMidCalc.calculate(tsi2513, index),     doc::setMomTsi2513DstMid,   tasks);
        // ifNull("momTsi4825DstMid",   doc::getMomTsi4825DstMid,   () -> MomTsi4825DstMidCalc.calculate(tsi4825, index),     doc::setMomTsi4825DstMid,   tasks); // fora da prod mask
        // ifNull("momTsi288144DstMid", doc::getMomTsi288144DstMid, () -> MomTsi288144DstMidCalc.calculate(tsi288144, index), doc::setMomTsi288144DstMid, tasks); // fora da prod mask
        // ifNull("momTsi2513Zsc80",          doc::getMomTsi2513Zsc80,          () -> MomTsi2513Zsc80Calc.calculate(tsi2513, index, 80),          doc::setMomTsi2513Zsc80,          tasks); // fora da prod mask
        // ifNull("momTsi2513PctileW80",      doc::getMomTsi2513PctileW80,      () -> MomTsi2513PctileW80Calc.calculate(tsi2513, index, 80),      doc::setMomTsi2513PctileW80,      tasks); // fora da prod mask
        // ifNull("momTsi4825Zsc80",          doc::getMomTsi4825Zsc80,          () -> MomTsi4825Zsc80Calc.calculate(tsi4825, index, 80),          doc::setMomTsi4825Zsc80,          tasks); // fora da prod mask
        // ifNull("momTsi4825PctileW80",      doc::getMomTsi4825PctileW80,      () -> MomTsi4825PctileW80Calc.calculate(tsi4825, index, 80),      doc::setMomTsi4825PctileW80,      tasks); // fora da prod mask
        // ifNull("momTsi288144Zsc80",        doc::getMomTsi288144Zsc80,        () -> MomTsi288144Zsc80Calc.calculate(tsi288144, index, 80),      doc::setMomTsi288144Zsc80,        tasks); // fora da prod mask
        // ifNull("momTsi288144PctileW80",    doc::getMomTsi288144PctileW80,    () -> MomTsi288144PctileW80Calc.calculate(tsi288144, index, 80),  doc::setMomTsi288144PctileW80,    tasks); // fora da prod mask
        // ifNull("momTsi2513Shock1",         doc::getMomTsi2513Shock1,         () -> MomTsi2513Shock1Calc.calculate(tsi2513, index),         doc::setMomTsi2513Shock1,         tasks); // fora da prod mask
        // ifNull("momTsi2513Shock1Stdn80",   doc::getMomTsi2513Shock1Stdn80,   () -> MomTsi2513Shock1Stdn80Calc.calculate(tsi2513, index, 80),   doc::setMomTsi2513Shock1Stdn80,   tasks); // fora da prod mask
        // ifNull("momTsi4825Shock1",         doc::getMomTsi4825Shock1,         () -> MomTsi4825Shock1Calc.calculate(tsi4825, index),         doc::setMomTsi4825Shock1,         tasks); // fora da prod mask
        // ifNull("momTsi4825Shock1Stdn80",   doc::getMomTsi4825Shock1Stdn80,   () -> MomTsi4825Shock1Stdn80Calc.calculate(tsi4825, index, 80),   doc::setMomTsi4825Shock1Stdn80,   tasks); // fora da prod mask
        // ifNull("momTsi288144Shock1",       doc::getMomTsi288144Shock1,       () -> MomTsi288144Shock1Calc.calculate(tsi288144, index),     doc::setMomTsi288144Shock1,       tasks); // fora da prod mask
        // ifNull("momTsi288144Shock1Stdn80", doc::getMomTsi288144Shock1Stdn80, () -> MomTsi288144Shock1Stdn80Calc.calculate(tsi288144, index, 80), doc::setMomTsi288144Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momTsi2513RegimeState",      doc::getMomTsi2513RegimeState,      () -> MomTsi2513RegimeStateCalc.calculate(tsi2513, index, 7),      doc::setMomTsi2513RegimeState,      tasks); // fora da prod mask
        // ifNull("momTsi2513RegimePrstW20",    doc::getMomTsi2513RegimePrstW20,    () -> MomTsi2513RegimePrstW20Calc.calculate(tsi2513, index, 20, 7),    doc::setMomTsi2513RegimePrstW20,    tasks); // fora da prod mask
        // ifNull("momTsi4825RegimeState",      doc::getMomTsi4825RegimeState,      () -> MomTsi4825RegimeStateCalc.calculate(tsi4825, index, 7),      doc::setMomTsi4825RegimeState,      tasks); // fora da prod mask
        // ifNull("momTsi4825RegimePrstW20",    doc::getMomTsi4825RegimePrstW20,    () -> MomTsi4825RegimePrstW20Calc.calculate(tsi4825, index, 20, 7),    doc::setMomTsi4825RegimePrstW20,    tasks); // fora da prod mask
        // ifNull("momTsi288144RegimeState",    doc::getMomTsi288144RegimeState,    () -> MomTsi288144RegimeStateCalc.calculate(tsi288144, index, 7),  doc::setMomTsi288144RegimeState,    tasks); // fora da prod mask
        // ifNull("momTsi288144RegimePrstW20",  doc::getMomTsi288144RegimePrstW20,  () -> MomTsi288144RegimePrstW20Calc.calculate(tsi288144, index, 20, 7), doc::setMomTsi288144RegimePrstW20, tasks); // fora da prod mask

        // ── PPO 12/26 ───────────────────────────────────────────────────────
        // ifNull("momPpo1226",            doc::getMomPpo1226,            () -> MomPpo1226Calc.calculate(ppo1226, index),                         doc::setMomPpo1226,            tasks); // fora da prod mask (raw)
        // ifNull("momPpoSig12269",        doc::getMomPpoSig12269,        () -> MomPpoSig12269Calc.calculate(ppoSig1226, index),                  doc::setMomPpoSig12269,        tasks); // fora da prod mask
        ifNull("momPpoHist12269",       doc::getMomPpoHist12269,       () -> MomPpoHist12269Calc.calculate(ppo1226, ppoSig1226, index),        doc::setMomPpoHist12269,       tasks);
        ifNull("momPpo1226Dlt",         doc::getMomPpo1226Dlt,         () -> MomPpo1226DltCalc.calculate(ppo1226, index),                      doc::setMomPpo1226Dlt,         tasks);
        ifNull("momPpoHist12269Dlt",    doc::getMomPpoHist12269Dlt,    () -> MomPpoHist12269DltCalc.calculate(ppo1226, ppoSig1226, index),     doc::setMomPpoHist12269Dlt,    tasks);
        // ifNull("momPpo1226Zsc80",       doc::getMomPpo1226Zsc80,       () -> MomPpo1226Zsc80Calc.calculate(ppo1226, index, 80),                    doc::setMomPpo1226Zsc80,       tasks); // fora da prod mask
        // ifNull("momPpo1226PctileW80",   doc::getMomPpo1226PctileW80,   () -> MomPpo1226PctileW80Calc.calculate(ppo1226, index, 80),                doc::setMomPpo1226PctileW80,   tasks); // fora da prod mask
        // ifNull("momPpoHist12269Zsc80",       doc::getMomPpoHist12269Zsc80,       () -> MomPpoHist12269Zsc80Calc.calculate(ppo1226, ppoSig1226, index, 80),       doc::setMomPpoHist12269Zsc80,       tasks); // fora da prod mask
        // ifNull("momPpoHist12269PctileW80",   doc::getMomPpoHist12269PctileW80,   () -> MomPpoHist12269PctileW80Calc.calculate(ppo1226, ppoSig1226, index, 80),   doc::setMomPpoHist12269PctileW80,   tasks); // fora da prod mask
        // ifNull("momPpoHist12269Shock1",      doc::getMomPpoHist12269Shock1,      () -> MomPpoHist12269Shock1Calc.calculate(ppo1226, ppoSig1226, index),          doc::setMomPpoHist12269Shock1,      tasks); // fora da prod mask
        // ifNull("momPpoHist12269Shock1Stdn80", doc::getMomPpoHist12269Shock1Stdn80, () -> MomPpoHist12269Shock1Stdn80Calc.calculate(ppo1226, ppoSig1226, index, 80), doc::setMomPpoHist12269Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momPpoRegimeState",     doc::getMomPpoRegimeState,     () -> MomPpoRegimeStateCalc.calculate(ppo1226, ppoSig1226, index),      doc::setMomPpoRegimeState,     tasks); // fora da prod mask
        // ifNull("momPpoRegimePrstW20",   doc::getMomPpoRegimePrstW20,   () -> MomPpoRegimePrstW20Calc.calculate(ppo1226, ppoSig1226, index, 20),    doc::setMomPpoRegimePrstW20,   tasks); // fora da prod mask

        // ── PPO 48/104 ──────────────────────────────────────────────────────
        // ifNull("momPpo48104",            doc::getMomPpo48104,            () -> MomPpo48104Calc.calculate(ppo48104, index),                          doc::setMomPpo48104,            tasks); // fora da prod mask
        // ifNull("momPpoSig481049",        doc::getMomPpoSig481049,        () -> MomPpoSig481049Calc.calculate(ppoSig48104, index),                   doc::setMomPpoSig481049,        tasks); // fora da prod mask
        // ifNull("momPpoHist481049",       doc::getMomPpoHist481049,       () -> MomPpoHist481049Calc.calculate(ppo48104, ppoSig48104, index),        doc::setMomPpoHist481049,       tasks); // fora da prod mask
        // ifNull("momPpo48104Dlt",         doc::getMomPpo48104Dlt,         () -> MomPpo48104DltCalc.calculate(ppo48104, index),                       doc::setMomPpo48104Dlt,         tasks); // fora da prod mask
        // ifNull("momPpoHist481049Dlt",    doc::getMomPpoHist481049Dlt,    () -> MomPpoHist481049DltCalc.calculate(ppo48104, ppoSig48104, index),     doc::setMomPpoHist481049Dlt,    tasks); // fora da prod mask
        // ifNull("momPpo48104Zsc80",       doc::getMomPpo48104Zsc80,       () -> MomPpo48104Zsc80Calc.calculate(ppo48104, index, 80),                     doc::setMomPpo48104Zsc80,       tasks); // fora da prod mask
        // ifNull("momPpo48104PctileW80",   doc::getMomPpo48104PctileW80,   () -> MomPpo48104PctileW80Calc.calculate(ppo48104, index, 80),                 doc::setMomPpo48104PctileW80,   tasks); // fora da prod mask
        // ifNull("momPpoHist481049Zsc80",       doc::getMomPpoHist481049Zsc80,       () -> MomPpoHist481049Zsc80Calc.calculate(ppo48104, ppoSig48104, index, 80),       doc::setMomPpoHist481049Zsc80,       tasks); // fora da prod mask
        // ifNull("momPpoHist481049PctileW80",   doc::getMomPpoHist481049PctileW80,   () -> MomPpoHist481049PctileW80Calc.calculate(ppo48104, ppoSig48104, index, 80),   doc::setMomPpoHist481049PctileW80,   tasks); // fora da prod mask
        // ifNull("momPpoHist481049Shock1",      doc::getMomPpoHist481049Shock1,      () -> MomPpoHist481049Shock1Calc.calculate(ppo48104, ppoSig48104, index),          doc::setMomPpoHist481049Shock1,      tasks); // fora da prod mask
        // ifNull("momPpoHist481049Shock1Stdn80", doc::getMomPpoHist481049Shock1Stdn80, () -> MomPpoHist481049Shock1Stdn80Calc.calculate(ppo48104, ppoSig48104, index, 80), doc::setMomPpoHist481049Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momPpo48104RegimeState",   doc::getMomPpo48104RegimeState,   () -> MomPpo48104RegimeStateCalc.calculate(ppo48104, ppoSig48104, index),   doc::setMomPpo48104RegimeState,   tasks); // fora da prod mask
        // ifNull("momPpo48104RegimePrstW20", doc::getMomPpo48104RegimePrstW20, () -> MomPpo48104RegimePrstW20Calc.calculate(ppo48104, ppoSig48104, index, 20), doc::setMomPpo48104RegimePrstW20, tasks); // fora da prod mask

        // ── PPO 288/576 ─────────────────────────────────────────────────────
        // ifNull("momPpo288576",            doc::getMomPpo288576,            () -> MomPpo288576Calc.calculate(ppo288576, index),                            doc::setMomPpo288576,            tasks); // fora da prod mask
        // ifNull("momPpoSig2885769",        doc::getMomPpoSig2885769,        () -> MomPpoSig2885769Calc.calculate(ppoSig288576, index),                     doc::setMomPpoSig2885769,        tasks); // fora da prod mask
        // ifNull("momPpoHist2885769",       doc::getMomPpoHist2885769,       () -> MomPpoHist2885769Calc.calculate(ppo288576, ppoSig288576, index),         doc::setMomPpoHist2885769,       tasks); // fora da prod mask
        // ifNull("momPpo288576Dlt",         doc::getMomPpo288576Dlt,         () -> MomPpo288576DltCalc.calculate(ppo288576, index),                         doc::setMomPpo288576Dlt,         tasks); // fora da prod mask
        // ifNull("momPpoHist2885769Dlt",    doc::getMomPpoHist2885769Dlt,    () -> MomPpoHist2885769DltCalc.calculate(ppo288576, ppoSig288576, index),      doc::setMomPpoHist2885769Dlt,    tasks); // fora da prod mask
        // ifNull("momPpo288576Zsc80",       doc::getMomPpo288576Zsc80,       () -> MomPpo288576Zsc80Calc.calculate(ppo288576, index, 80),                       doc::setMomPpo288576Zsc80,       tasks); // fora da prod mask
        // ifNull("momPpo288576PctileW80",   doc::getMomPpo288576PctileW80,   () -> MomPpo288576PctileW80Calc.calculate(ppo288576, index, 80),                   doc::setMomPpo288576PctileW80,   tasks); // fora da prod mask
        // ifNull("momPpoHist2885769Zsc80",       doc::getMomPpoHist2885769Zsc80,       () -> MomPpoHist2885769Zsc80Calc.calculate(ppo288576, ppoSig288576, index, 80),       doc::setMomPpoHist2885769Zsc80,       tasks); // fora da prod mask
        // ifNull("momPpoHist2885769PctileW80",   doc::getMomPpoHist2885769PctileW80,   () -> MomPpoHist2885769PctileW80Calc.calculate(ppo288576, ppoSig288576, index, 80),   doc::setMomPpoHist2885769PctileW80,   tasks); // fora da prod mask
        // ifNull("momPpoHist2885769Shock1",      doc::getMomPpoHist2885769Shock1,      () -> MomPpoHist2885769Shock1Calc.calculate(ppo288576, ppoSig288576, index),          doc::setMomPpoHist2885769Shock1,      tasks); // fora da prod mask
        // ifNull("momPpoHist2885769Shock1Stdn80", doc::getMomPpoHist2885769Shock1Stdn80, () -> MomPpoHist2885769Shock1Stdn80Calc.calculate(ppo288576, ppoSig288576, index, 80), doc::setMomPpoHist2885769Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momPpo288576RegimeState",   doc::getMomPpo288576RegimeState,   () -> MomPpo288576RegimeStateCalc.calculate(ppo288576, ppoSig288576, index),   doc::setMomPpo288576RegimeState,   tasks); // fora da prod mask
        // ifNull("momPpo288576RegimePrstW20", doc::getMomPpo288576RegimePrstW20, () -> MomPpo288576RegimePrstW20Calc.calculate(ppo288576, ppoSig288576, index, 20), doc::setMomPpo288576RegimePrstW20, tasks); // fora da prod mask

        // ── CLOSE SLOPE ─────────────────────────────────────────────────────
        ifNull("momClose3Slp",   doc::getMomClose3Slp,   () -> MomClose3SlpCalc.calculate(closeSlp3, index),   doc::setMomClose3Slp,   tasks);
        ifNull("momClose8Slp",   doc::getMomClose8Slp,   () -> MomClose8SlpCalc.calculate(closeSlp8, index),   doc::setMomClose8Slp,   tasks);
        ifNull("momClose14Slp",  doc::getMomClose14Slp,  () -> MomClose14SlpCalc.calculate(closeSlp14, index), doc::setMomClose14Slp,  tasks);
        // ifNull("momClose50Slp",  doc::getMomClose50Slp,  () -> MomClose50SlpCalc.calculate(closeSlp50, index), doc::setMomClose50Slp,  tasks); // fora da prod mask
        // ifNull("momClose48Slp",  doc::getMomClose48Slp,  () -> MomClose48SlpCalc.calculate(closeSlp48, index), doc::setMomClose48Slp,  tasks); // fora da prod mask
        // ifNull("momClose288Slp", doc::getMomClose288Slp, () -> MomClose288SlpCalc.calculate(closeSlp288, index), doc::setMomClose288Slp, tasks); // fora da prod mask

        // ── CLOSE SLOPE ATRN ────────────────────────────────────────────────
        ifNull("momClose3SlpAtrn",   doc::getMomClose3SlpAtrn,   () -> MomClose3SlpAtrnCalc.calculate(closeSlp3.getValue(index).doubleValue(), atr14, index),   doc::setMomClose3SlpAtrn,   tasks);
        ifNull("momClose8SlpAtrn",   doc::getMomClose8SlpAtrn,   () -> MomClose8SlpAtrnCalc.calculate(closeSlp8.getValue(index).doubleValue(), atr14, index),   doc::setMomClose8SlpAtrn,   tasks);
        ifNull("momClose14SlpAtrn",  doc::getMomClose14SlpAtrn,  () -> MomClose14SlpAtrnCalc.calculate(closeSlp14.getValue(index).doubleValue(), atr14, index), doc::setMomClose14SlpAtrn,  tasks);
        // ifNull("momClose50SlpAtrn",  doc::getMomClose50SlpAtrn,  () -> MomClose50SlpAtrnCalc.calculate(closeSlp50.getValue(index).doubleValue(), atr14, index), doc::setMomClose50SlpAtrn,  tasks); // fora da prod mask
        // ifNull("momClose48SlpAtrn",  doc::getMomClose48SlpAtrn,  () -> MomClose48SlpAtrnCalc.calculate(closeSlp48.getValue(index).doubleValue(), atr14, index), doc::setMomClose48SlpAtrn,  tasks); // fora da prod mask
        // ifNull("momClose288SlpAtrn", doc::getMomClose288SlpAtrn, () -> MomClose288SlpAtrnCalc.calculate(closeSlp288.getValue(index).doubleValue(), atr14, index), doc::setMomClose288SlpAtrn, tasks); // fora da prod mask

        // ── CLOSE SLOPE ACC ─────────────────────────────────────────────────
        ifNull("momClose3SlpAcc",   doc::getMomClose3SlpAcc,   () -> MomClose3SlpAccCalc.calculate(closeSlpAcc3, index),   doc::setMomClose3SlpAcc,   tasks);
        ifNull("momClose8SlpAcc",   doc::getMomClose8SlpAcc,   () -> MomClose8SlpAccCalc.calculate(closeSlpAcc8, index),   doc::setMomClose8SlpAcc,   tasks);
        ifNull("momClose14SlpAcc",  doc::getMomClose14SlpAcc,  () -> MomClose14SlpAccCalc.calculate(closeSlpAcc14, index), doc::setMomClose14SlpAcc,  tasks);
        // ifNull("momClose48SlpAcc",  doc::getMomClose48SlpAcc,  () -> MomClose48SlpAccCalc.calculate(closeSlpAcc48, index), doc::setMomClose48SlpAcc,  tasks); // fora da prod mask
        // ifNull("momClose288SlpAcc", doc::getMomClose288SlpAcc, () -> MomClose288SlpAccCalc.calculate(closeSlpAcc288, index), doc::setMomClose288SlpAcc, tasks); // fora da prod mask

        // ── CLOSE SLOPE ACC ATRN ────────────────────────────────────────────
        ifNull("momClose3SlpAccAtrn",   doc::getMomClose3SlpAccAtrn,   () -> MomClose3SlpAccAtrnCalc.calculate(closeSlpAcc3.getValue(index).doubleValue(), atr14, index),   doc::setMomClose3SlpAccAtrn,   tasks);
        ifNull("momClose8SlpAccAtrn",   doc::getMomClose8SlpAccAtrn,   () -> MomClose8SlpAccAtrnCalc.calculate(closeSlpAcc8.getValue(index).doubleValue(), atr14, index),   doc::setMomClose8SlpAccAtrn,   tasks);
        ifNull("momClose14SlpAccAtrn",  doc::getMomClose14SlpAccAtrn,  () -> MomClose14SlpAccAtrnCalc.calculate(closeSlpAcc14.getValue(index).doubleValue(), atr14, index), doc::setMomClose14SlpAccAtrn,  tasks);
        // ifNull("momClose48SlpAccAtrn",  doc::getMomClose48SlpAccAtrn,  () -> MomClose48SlpAccAtrnCalc.calculate(closeSlpAcc48.getValue(index).doubleValue(), atr14, index), doc::setMomClose48SlpAccAtrn,  tasks); // fora da prod mask
        // ifNull("momClose288SlpAccAtrn", doc::getMomClose288SlpAccAtrn, () -> MomClose288SlpAccAtrnCalc.calculate(closeSlpAcc288.getValue(index).doubleValue(), atr14, index), doc::setMomClose288SlpAccAtrn, tasks); // fora da prod mask

        // ── CLOSE Z-SCORE ───────────────────────────────────────────────────
        // ifNull("momClose3Zsc",   doc::getMomClose3Zsc,   () -> MomClose3ZscCalc.calculate(close, sma3, std8, index),      doc::setMomClose3Zsc,   tasks); // fora da prod mask
        // ifNull("momClose8Zsc",   doc::getMomClose8Zsc,   () -> MomClose8ZscCalc.calculate(close, sma8, std8, index),      doc::setMomClose8Zsc,   tasks); // fora da prod mask
        // ifNull("momClose14Zsc",  doc::getMomClose14Zsc,  () -> MomClose14ZscCalc.calculate(close, sma14, std14, index),   doc::setMomClose14Zsc,  tasks); // fora da prod mask
        // ifNull("momClose50Zsc",  doc::getMomClose50Zsc,  () -> MomClose50ZscCalc.calculate(close, sma50, std50, index),   doc::setMomClose50Zsc,  tasks); // fora da prod mask
        // ifNull("momClose48Zsc",  doc::getMomClose48Zsc,  () -> MomClose48ZscCalc.calculate(close, sma48, std48, index),   doc::setMomClose48Zsc,  tasks); // fora da prod mask
        // ifNull("momClose288Zsc", doc::getMomClose288Zsc, () -> MomClose288ZscCalc.calculate(close, sma288, std288, index), doc::setMomClose288Zsc, tasks); // fora da prod mask

        // ── CCI ─────────────────────────────────────────────────────────────
        // ifNull("momCci14",     doc::getMomCci14,     () -> MomCci14Calc.calculate(cci14, index),     doc::setMomCci14,     tasks); // fora da prod mask (raw)
        // ifNull("momCci20",     doc::getMomCci20,     () -> MomCci20Calc.calculate(cci20, index),     doc::setMomCci20,     tasks); // fora da prod mask (raw)
        // ifNull("momCci48",     doc::getMomCci48,     () -> MomCci48Calc.calculate(cci48, index),     doc::setMomCci48,     tasks); // fora da prod mask
        // ifNull("momCci288",    doc::getMomCci288,    () -> MomCci288Calc.calculate(cci288, index),   doc::setMomCci288,    tasks); // fora da prod mask
        ifNull("momCci14Dlt",  doc::getMomCci14Dlt,  () -> MomCci14DltCalc.calculate(cci14, index),  doc::setMomCci14Dlt,  tasks);
        ifNull("momCci20Dlt",  doc::getMomCci20Dlt,  () -> MomCci20DltCalc.calculate(cci20, index),  doc::setMomCci20Dlt,  tasks);
        // ifNull("momCci48Dlt",  doc::getMomCci48Dlt,  () -> MomCci48DltCalc.calculate(cci48, index),  doc::setMomCci48Dlt,  tasks); // fora da prod mask
        // ifNull("momCci288Dlt", doc::getMomCci288Dlt, () -> MomCci288DltCalc.calculate(cci288, index), doc::setMomCci288Dlt, tasks); // fora da prod mask
        ifNull("momCci14DstMid",  doc::getMomCci14DstMid,  () -> MomCci14DstMidCalc.calculate(cci14, index),  doc::setMomCci14DstMid,  tasks);
        ifNull("momCci20DstMid",  doc::getMomCci20DstMid,  () -> MomCci20DstMidCalc.calculate(cci20, index),  doc::setMomCci20DstMid,  tasks);
        // ifNull("momCci48DstMid",  doc::getMomCci48DstMid,  () -> MomCci48DstMidCalc.calculate(cci48, index),  doc::setMomCci48DstMid,  tasks); // fora da prod mask
        // ifNull("momCci288DstMid", doc::getMomCci288DstMid, () -> MomCci288DstMidCalc.calculate(cci288, index), doc::setMomCci288DstMid, tasks); // fora da prod mask
        // ifNull("momCci20Zsc80",         doc::getMomCci20Zsc80,         () -> MomCci20ZscW80Calc.calculate(cci20, index),         doc::setMomCci20Zsc80,         tasks); // fora da prod mask
        // ifNull("momCci20PctileW80",     doc::getMomCci20PctileW80,     () -> MomCci20PctileW80Calc.calculate(cci20, index),      doc::setMomCci20PctileW80,     tasks); // fora da prod mask
        // ifNull("momCci48Zsc80",         doc::getMomCci48Zsc80,         () -> MomCci48ZscW80Calc.calculate(cci48, index),         doc::setMomCci48Zsc80,         tasks); // fora da prod mask
        // ifNull("momCci48PctileW80",     doc::getMomCci48PctileW80,     () -> MomCci48PctileW80Calc.calculate(cci48, index),      doc::setMomCci48PctileW80,     tasks); // fora da prod mask
        // ifNull("momCci288Zsc80",        doc::getMomCci288Zsc80,        () -> MomCci288ZscW80Calc.calculate(cci288, index),       doc::setMomCci288Zsc80,        tasks); // fora da prod mask
        // ifNull("momCci288PctileW80",    doc::getMomCci288PctileW80,    () -> MomCci288PctileW80Calc.calculate(cci288, index),    doc::setMomCci288PctileW80,    tasks); // fora da prod mask
        // ifNull("momCci20Shock1",        doc::getMomCci20Shock1,        () -> MomCci20Shock1Calc.calculate(cci20, index),         doc::setMomCci20Shock1,        tasks); // fora da prod mask
        // ifNull("momCci20Shock1Stdn80",  doc::getMomCci20Shock1Stdn80,  () -> MomCci20Shock1StdnW80Calc.calculate(cci20, index),  doc::setMomCci20Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momCci48Shock1",        doc::getMomCci48Shock1,        () -> MomCci48Shock1Calc.calculate(cci48, index),         doc::setMomCci48Shock1,        tasks); // fora da prod mask
        // ifNull("momCci48Shock1Stdn80",  doc::getMomCci48Shock1Stdn80,  () -> MomCci48Shock1StdnW80Calc.calculate(cci48, index),  doc::setMomCci48Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momCci288Shock1",       doc::getMomCci288Shock1,       () -> MomCci288Shock1Calc.calculate(cci288, index),       doc::setMomCci288Shock1,       tasks); // fora da prod mask
        // ifNull("momCci288Shock1Stdn80", doc::getMomCci288Shock1Stdn80, () -> MomCci288Shock1StdnW80Calc.calculate(cci288, index), doc::setMomCci288Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momCci20RegimeState",     doc::getMomCci20RegimeState,     () -> MomCci20RegimeStateCalc.calculate(cci20, index),     doc::setMomCci20RegimeState,     tasks); // fora da prod mask
        // ifNull("momCci20RegimePrstW20",   doc::getMomCci20RegimePrstW20,   () -> MomCci20RegimePrstW20Calc.calculate(cci20, index),   doc::setMomCci20RegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momCci48RegimeState",     doc::getMomCci48RegimeState,     () -> MomCci48RegimeStateCalc.calculate(cci48, index),     doc::setMomCci48RegimeState,     tasks); // fora da prod mask
        // ifNull("momCci48RegimePrstW20",   doc::getMomCci48RegimePrstW20,   () -> MomCci48RegimePrstW20Calc.calculate(cci48, index),   doc::setMomCci48RegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momCci288RegimeState",    doc::getMomCci288RegimeState,    () -> MomCci288RegimeStateCalc.calculate(cci288, index),   doc::setMomCci288RegimeState,    tasks); // fora da prod mask
        // ifNull("momCci288RegimePrstW20",  doc::getMomCci288RegimePrstW20,  () -> MomCci288RegimePrstW20Calc.calculate(cci288, index), doc::setMomCci288RegimePrstW20,  tasks); // fora da prod mask

        // ── ROC ─────────────────────────────────────────────────────────────
        ifNull("momRoc1",    doc::getMomRoc1,    () -> MomRoc1Calc.calculate(roc1, index),     doc::setMomRoc1,    tasks);
        ifNull("momRoc2",    doc::getMomRoc2,    () -> MomRoc2Calc.calculate(roc2, index),     doc::setMomRoc2,    tasks);
        ifNull("momRoc3",    doc::getMomRoc3,    () -> MomRoc3Calc.calculate(roc3, index),     doc::setMomRoc3,    tasks);
        ifNull("momRoc5",    doc::getMomRoc5,    () -> MomRoc5Calc.calculate(roc5, index),     doc::setMomRoc5,    tasks);
        // ifNull("momRoc48",   doc::getMomRoc48,   () -> MomRoc48Calc.calculate(roc48, index),   doc::setMomRoc48,   tasks); // fora da prod mask
        // ifNull("momRoc288",  doc::getMomRoc288,  () -> MomRoc288Calc.calculate(roc288, index), doc::setMomRoc288,  tasks); // fora da prod mask
        // ifNull("momRoc1Abs",   doc::getMomRoc1Abs,   () -> MomRoc1AbsCalc.calculate(roc1, index),     doc::setMomRoc1Abs,   tasks); // fora da prod mask
        // ifNull("momRoc2Abs",   doc::getMomRoc2Abs,   () -> MomRoc2AbsCalc.calculate(roc2, index),     doc::setMomRoc2Abs,   tasks); // fora da prod mask
        // ifNull("momRoc3Abs",   doc::getMomRoc3Abs,   () -> MomRoc3AbsCalc.calculate(roc3, index),     doc::setMomRoc3Abs,   tasks); // fora da prod mask
        // ifNull("momRoc5Abs",   doc::getMomRoc5Abs,   () -> MomRoc5AbsCalc.calculate(roc5, index),     doc::setMomRoc5Abs,   tasks); // fora da prod mask
        // ifNull("momRoc48Abs",  doc::getMomRoc48Abs,  () -> MomRoc48AbsCalc.calculate(roc48, index),   doc::setMomRoc48Abs,  tasks); // fora da prod mask
        // ifNull("momRoc288Abs", doc::getMomRoc288Abs, () -> MomRoc288AbsCalc.calculate(roc288, index), doc::setMomRoc288Abs, tasks); // fora da prod mask
        // ifNull("momRoc5Zsc80",          doc::getMomRoc5Zsc80,          () -> MomRoc5ZscW80Calc.calculate(roc5, index),          doc::setMomRoc5Zsc80,          tasks); // fora da prod mask
        // ifNull("momRoc5PctileW80",      doc::getMomRoc5PctileW80,      () -> MomRoc5PctileW80Calc.calculate(roc5, index),       doc::setMomRoc5PctileW80,      tasks); // fora da prod mask
        // ifNull("momRoc48Zsc80",         doc::getMomRoc48Zsc80,         () -> MomRoc48ZscW80Calc.calculate(roc48, index),        doc::setMomRoc48Zsc80,         tasks); // fora da prod mask
        // ifNull("momRoc48PctileW80",     doc::getMomRoc48PctileW80,     () -> MomRoc48PctileW80Calc.calculate(roc48, index),     doc::setMomRoc48PctileW80,     tasks); // fora da prod mask
        // ifNull("momRoc288Zsc80",        doc::getMomRoc288Zsc80,        () -> MomRoc288ZscW80Calc.calculate(roc288, index),      doc::setMomRoc288Zsc80,        tasks); // fora da prod mask
        // ifNull("momRoc288PctileW80",    doc::getMomRoc288PctileW80,    () -> MomRoc288PctileW80Calc.calculate(roc288, index),   doc::setMomRoc288PctileW80,    tasks); // fora da prod mask
        // ifNull("momRoc5Shock1",         doc::getMomRoc5Shock1,         () -> MomRoc5Shock1Calc.calculate(roc5, index),          doc::setMomRoc5Shock1,         tasks); // fora da prod mask
        // ifNull("momRoc5Shock1Stdn80",   doc::getMomRoc5Shock1Stdn80,   () -> MomRoc5Shock1StdnW80Calc.calculate(roc5, index),   doc::setMomRoc5Shock1Stdn80,   tasks); // fora da prod mask
        // ifNull("momRoc48Shock1",        doc::getMomRoc48Shock1,        () -> MomRoc48Shock1Calc.calculate(roc48, index),        doc::setMomRoc48Shock1,        tasks); // fora da prod mask
        // ifNull("momRoc48Shock1Stdn80",  doc::getMomRoc48Shock1Stdn80,  () -> MomRoc48Shock1StdnW80Calc.calculate(roc48, index), doc::setMomRoc48Shock1Stdn80,  tasks); // fora da prod mask
        // ifNull("momRoc288Shock1",       doc::getMomRoc288Shock1,       () -> MomRoc288Shock1Calc.calculate(roc288, index),      doc::setMomRoc288Shock1,       tasks); // fora da prod mask
        // ifNull("momRoc288Shock1Stdn80", doc::getMomRoc288Shock1Stdn80, () -> MomRoc288Shock1StdnW80Calc.calculate(roc288, index), doc::setMomRoc288Shock1Stdn80, tasks); // fora da prod mask
        // ifNull("momRoc5RegimeState",      doc::getMomRoc5RegimeState,      () -> MomRoc5RegimeStateCalc.calculate(roc5, index),      doc::setMomRoc5RegimeState,      tasks); // fora da prod mask
        // ifNull("momRoc5RegimePrstW20",    doc::getMomRoc5RegimePrstW20,    () -> MomRoc5RegimePrstW20Calc.calculate(roc5, index),    doc::setMomRoc5RegimePrstW20,    tasks); // fora da prod mask
        // ifNull("momRoc48RegimeState",     doc::getMomRoc48RegimeState,     () -> MomRoc48RegimeStateCalc.calculate(roc48, index),    doc::setMomRoc48RegimeState,     tasks); // fora da prod mask
        // ifNull("momRoc48RegimePrstW20",   doc::getMomRoc48RegimePrstW20,   () -> MomRoc48RegimePrstW20Calc.calculate(roc48, index),  doc::setMomRoc48RegimePrstW20,   tasks); // fora da prod mask
        // ifNull("momRoc288RegimeState",    doc::getMomRoc288RegimeState,    () -> MomRoc288RegimeStateCalc.calculate(roc288, index),  doc::setMomRoc288RegimeState,    tasks); // fora da prod mask
        // ifNull("momRoc288RegimePrstW20",  doc::getMomRoc288RegimePrstW20,  () -> MomRoc288RegimePrstW20Calc.calculate(roc288, index), doc::setMomRoc288RegimePrstW20, tasks); // fora da prod mask

        // ══════════════════════════════════════════════════════════════════════
        // Execute first batch (all independent tasks)
        // ══════════════════════════════════════════════════════════════════════
        if (!tasks.isEmpty()) {
            execute(tasks);
        }

        // ══════════════════════════════════════════════════════════════════════
        // DEPENDENT TASKS (require first batch results)
        // ══════════════════════════════════════════════════════════════════════
        List<Callable<Void>> dependentTasks = new ArrayList<>();

        // Pre-compute alignment values for consensus
        double alignRsi14Ppo12   = safeGet(doc.getMomAlignRsi14PpoHist12269(),   () -> MomAlignRsi14PpoHist12269Calc.calculate(safeDouble(doc.getMomRsi14()), safeDouble(doc.getMomPpoHist12269())));
        double alignRsi48Ppo48   = safeGet(doc.getMomAlignRsi48PpoHist481049(),  () -> MomAlignRsi48PpoHist481049Calc.calculate(safeDouble(doc.getMomRsi48()), safeDouble(doc.getMomPpoHist481049())));
        double alignRsi288Ppo288 = safeGet(doc.getMomAlignRsi288PpoHist2885769(), () -> MomAlignRsi288PpoHist2885769Calc.calculate(safeDouble(doc.getMomRsi288()), safeDouble(doc.getMomPpoHist2885769())));
        double alignTrix9Tsi25   = safeGet(doc.getMomAlignTrixHist9TsiHist2513(),   () -> MomAlignTrixHist9TsiHist2513Calc.calculate(safeDouble(doc.getMomTrix9Hist()), safeDouble(doc.getMomTsi2513Hist())));
        double alignTrix48Tsi48  = safeGet(doc.getMomAlignTrixHist48TsiHist4825(),  () -> MomAlignTrixHist48TsiHist4825Calc.calculate(safeDouble(doc.getMomTrix48Hist()), safeDouble(doc.getMomTsi4825Hist())));
        double alignTrix288Tsi288 = safeGet(doc.getMomAlignTrixHist288TsiHist288144(), () -> MomAlignTrixHist288TsiHist288144Calc.calculate(safeDouble(doc.getMomTrix288Hist()), safeDouble(doc.getMomTsi288144Hist())));

        // ── ALIGNMENT ───────────────────────────────────────────────────────
        // ifNull("momAlignRsi14PpoHist12269",        doc::getMomAlignRsi14PpoHist12269,        () -> alignRsi14Ppo12,   doc::setMomAlignRsi14PpoHist12269,        dependentTasks); // fora da prod mask
        // ifNull("momAlignRsi48PpoHist481049",       doc::getMomAlignRsi48PpoHist481049,       () -> alignRsi48Ppo48,   doc::setMomAlignRsi48PpoHist481049,       dependentTasks); // fora da prod mask
        // ifNull("momAlignRsi288PpoHist2885769",     doc::getMomAlignRsi288PpoHist2885769,     () -> alignRsi288Ppo288, doc::setMomAlignRsi288PpoHist2885769,     dependentTasks); // fora da prod mask
        // ifNull("momAlignTrixHist9TsiHist2513",     doc::getMomAlignTrixHist9TsiHist2513,     () -> alignTrix9Tsi25,   doc::setMomAlignTrixHist9TsiHist2513,     dependentTasks); // fora da prod mask
        // ifNull("momAlignTrixHist48TsiHist4825",    doc::getMomAlignTrixHist48TsiHist4825,    () -> alignTrix48Tsi48,  doc::setMomAlignTrixHist48TsiHist4825,    dependentTasks); // fora da prod mask
        // ifNull("momAlignTrixHist288TsiHist288144", doc::getMomAlignTrixHist288TsiHist288144, () -> alignTrix288Tsi288, doc::setMomAlignTrixHist288TsiHist288144, dependentTasks); // fora da prod mask

        // ── CONSENSUS / CHOP ────────────────────────────────────────────────
        double consensusScore = MomMomentumConsensusScoreCalc.calculate(alignRsi14Ppo12, alignRsi48Ppo48, alignRsi288Ppo288, alignTrix9Tsi25, alignTrix48Tsi48, alignTrix288Tsi288);
        // ifNull("momMomentumConsensusScore", doc::getMomMomentumConsensusScore, () -> consensusScore, doc::setMomMomentumConsensusScore, dependentTasks); // fora da prod mask
        // ifNull("momChopScore", doc::getMomChopScore, () -> MomChopScoreCalc.calculate(series, index, 48, 288), doc::setMomChopScore, dependentTasks); // fora da prod mask

        // ── CONSENSUS DYNAMICS ──────────────────────────────────────────────
        // ifNull("momMomentumConflictScore", doc::getMomMomentumConflictScore, () -> MomMomentumConflictScoreCalc.calculate(consensusScore), doc::setMomMomentumConflictScore, dependentTasks); // fora da prod mask

        // Build consensus window for dynamics calcs
        double[] consensusW20 = buildConsensusWindow(series, index, 20, close, rsi14, rsi48, rsi288, ppo1226, ppoSig1226, ppo48104, ppoSig48104, ppo288576, ppoSig288576, trix9, trix48, trix288, tsi2513, tsi4825, tsi288144);
        double[] consensusW80 = buildConsensusWindow(series, index, 80, close, rsi14, rsi48, rsi288, ppo1226, ppoSig1226, ppo48104, ppoSig48104, ppo288576, ppoSig288576, trix9, trix48, trix288, tsi2513, tsi4825, tsi288144);

        // Consensus previous value for delta/shock
        double consensusPrev = (consensusW20 != null && consensusW20.length >= 2) ? consensusW20[consensusW20.length - 2] : 0.0;

        // ifNull("momMomentumConsensusDlt",     doc::getMomMomentumConsensusDlt,     () -> MomMomentumConsensusDltCalc.calculate(consensusScore, consensusPrev),     doc::setMomMomentumConsensusDlt,     dependentTasks); // fora da prod mask
        // ifNull("momMomentumConsensusPrstW20", doc::getMomMomentumConsensusPrstW20, () -> MomMomentumConsensusPrstW20Calc.calculate(consensusW20),                 doc::setMomMomentumConsensusPrstW20, dependentTasks); // fora da prod mask

        // ── PPO HIST FLIP RATE ──────────────────────────────────────────────
        double[] ppoHist12W20  = buildHistWindow(ppo1226, ppoSig1226, index, 20);
        double[] ppoHist48W20  = buildHistWindow(ppo48104, ppoSig48104, index, 20);
        double[] ppoHist288W20 = buildHistWindow(ppo288576, ppoSig288576, index, 20);

        // ifNull("momPpoHist12269FlipRateW20",  doc::getMomPpoHist12269FlipRateW20,  () -> MomPpoHist12269FlipRateW20Calc.calculate(ppoHist12W20),  doc::setMomPpoHist12269FlipRateW20,  dependentTasks); // fora da prod mask
        // ifNull("momPpoHist481049FlipRateW20", doc::getMomPpoHist481049FlipRateW20, () -> MomPpoHist481049FlipRateW20Calc.calculate(ppoHist48W20), doc::setMomPpoHist481049FlipRateW20, dependentTasks); // fora da prod mask
        // ifNull("momPpoHist2885769FlipRateW20", doc::getMomPpoHist2885769FlipRateW20, () -> MomPpoHist2885769FlipRateW20Calc.calculate(ppoHist288W20), doc::setMomPpoHist2885769FlipRateW20, dependentTasks); // fora da prod mask

        // ── TRIX HIST FLIP RATE ─────────────────────────────────────────────
        double[] trixHist9W20  = buildTrixHistWindow(trix9, index, 20);
        double[] trixHist48W20 = buildTrixHistWindow(trix48, index, 20);
        double[] trixHist288W20 = buildTrixHistWindow(trix288, index, 20);

        // ifNull("momTrixHist9FlipRateW20",   doc::getMomTrixHist9FlipRateW20,   () -> MomTrixHist9FlipRateW20Calc.calculate(trixHist9W20),   doc::setMomTrixHist9FlipRateW20,   dependentTasks); // fora da prod mask
        // ifNull("momTrixHist48FlipRateW20",  doc::getMomTrixHist48FlipRateW20,  () -> MomTrixHist48FlipRateW20Calc.calculate(trixHist48W20), doc::setMomTrixHist48FlipRateW20,  dependentTasks); // fora da prod mask
        // ifNull("momTrixHist288FlipRateW20", doc::getMomTrixHist288FlipRateW20, () -> MomTrixHist288FlipRateW20Calc.calculate(trixHist288W20), doc::setMomTrixHist288FlipRateW20, dependentTasks); // fora da prod mask

        // ── TSI HIST FLIP RATE ──────────────────────────────────────────────
        double[] tsiHist2513W20  = buildTsiHistWindow(tsi2513, index, 20, 7);
        double[] tsiHist4825W20  = buildTsiHistWindow(tsi4825, index, 20, 7);
        double[] tsiHist288144W20 = buildTsiHistWindow(tsi288144, index, 20, 7);

        // ifNull("momTsiHist2513FlipRateW20",   doc::getMomTsiHist2513FlipRateW20,   () -> MomTsiHist2513FlipRateW20Calc.calculate(tsiHist2513W20),   doc::setMomTsiHist2513FlipRateW20,   dependentTasks); // fora da prod mask
        // ifNull("momTsiHist4825FlipRateW20",   doc::getMomTsiHist4825FlipRateW20,   () -> MomTsiHist4825FlipRateW20Calc.calculate(tsiHist4825W20),   doc::setMomTsiHist4825FlipRateW20,   dependentTasks); // fora da prod mask
        // ifNull("momTsiHist288144FlipRateW20", doc::getMomTsiHist288144FlipRateW20, () -> MomTsiHist288144FlipRateW20Calc.calculate(tsiHist288144W20), doc::setMomTsiHist288144FlipRateW20, dependentTasks); // fora da prod mask

        // ── PPO HIST COHERENCE ──────────────────────────────────────────────
        // ifNull("momPpoHistCoh1226Vs48104",  doc::getMomPpoHistCoh1226Vs48104,  () -> MomPpoHistCoh1226Vs48104Calc.calculate(safeDouble(doc.getMomPpoHist12269()), safeDouble(doc.getMomPpoHist481049())),   doc::setMomPpoHistCoh1226Vs48104,  dependentTasks); // fora da prod mask
        // ifNull("momPpoHistCoh1226Vs288576", doc::getMomPpoHistCoh1226Vs288576, () -> MomPpoHistCoh1226Vs288576Calc.calculate(safeDouble(doc.getMomPpoHist12269()), safeDouble(doc.getMomPpoHist2885769())), doc::setMomPpoHistCoh1226Vs288576, dependentTasks); // fora da prod mask

        // ── TRIX HIST COHERENCE ─────────────────────────────────────────────
        // ifNull("momTrixHistCoh9Vs48", doc::getMomTrixHistCoh9Vs48, () -> MomTrixHistCoh9Vs48Calc.calculate(safeDouble(doc.getMomTrix9Hist()), safeDouble(doc.getMomTrix48Hist())), doc::setMomTrixHistCoh9Vs48, dependentTasks); // fora da prod mask

        // ── TSI HIST COHERENCE ──────────────────────────────────────────────
        // ifNull("momTsiHistCoh2513Vs4825", doc::getMomTsiHistCoh2513Vs4825, () -> MomTsiHistCoh2513Vs4825Calc.calculate(safeDouble(doc.getMomTsi2513Hist()), safeDouble(doc.getMomTsi4825Hist())), doc::setMomTsiHistCoh2513Vs4825, dependentTasks); // fora da prod mask

        // ── CONSENSUS SLP/FLIP ──────────────────────────────────────────────
        // ifNull("momMomentumConsensusSlpW20",      doc::getMomMomentumConsensusSlpW20,      () -> MomMomentumConsensusSlpW20Calc.calculate(consensusW20),      doc::setMomMomentumConsensusSlpW20,      dependentTasks); // fora da prod mask
        // ifNull("momMomentumConsensusFlipRateW20", doc::getMomMomentumConsensusFlipRateW20, () -> MomMomentumConsensusFlipRateW20Calc.calculate(consensusW20), doc::setMomMomentumConsensusFlipRateW20, dependentTasks); // fora da prod mask

        // ── DIVERGENCE ──────────────────────────────────────────────────────
        // ifNull("momDivCloseSlp48VsPpoHist48",   doc::getMomDivCloseSlp48VsPpoHist48,   () -> MomDivCloseSlp48VsPpoHist48Calc.calculate(safeDouble(doc.getMomClose48Slp()), safeDouble(doc.getMomPpoHist481049())),   doc::setMomDivCloseSlp48VsPpoHist48,   dependentTasks); // fora da prod mask
        // ifNull("momDivCloseSlp48VsRsi48",       doc::getMomDivCloseSlp48VsRsi48,       () -> MomDivCloseSlp48VsRsi48Calc.calculate(safeDouble(doc.getMomClose48Slp()), safeDouble(doc.getMomRsi48()) - 50.0),               doc::setMomDivCloseSlp48VsRsi48,       dependentTasks); // fora da prod mask
        // ifNull("momDivCloseSlp288VsPpoHist288", doc::getMomDivCloseSlp288VsPpoHist288, () -> MomDivCloseSlp288VsPpoHist288Calc.calculate(safeDouble(doc.getMomClose288Slp()), safeDouble(doc.getMomPpoHist2885769())), doc::setMomDivCloseSlp288VsPpoHist288, dependentTasks); // fora da prod mask

        // ── CONSENSUS VOL/SHOCK ─────────────────────────────────────────────
        // ifNull("momMomentumConsensusVolW20",         doc::getMomMomentumConsensusVolW20,         () -> MomMomentumConsensusVolW20Calc.calculate(consensusW20),         doc::setMomMomentumConsensusVolW20,         dependentTasks); // fora da prod mask
        // ifNull("momMomentumConsensusShock1",         doc::getMomMomentumConsensusShock1,         () -> MomMomentumConsensusShock1Calc.calculate(consensusScore, consensusPrev), doc::setMomMomentumConsensusShock1, dependentTasks); // fora da prod mask
        // ifNull("momMomentumConsensusShock1StdnW20",  doc::getMomMomentumConsensusShock1StdnW20,  () -> MomMomentumConsensusShock1StdnW20Calc.calculate(consensusW20), doc::setMomMomentumConsensusShock1StdnW20,  dependentTasks); // fora da prod mask

        // ── CONSENSUS COUNTS ────────────────────────────────────────────────
        double agreeCount = MomMomentumAgreementCountCalc.calculate(alignRsi14Ppo12, alignRsi48Ppo48, alignRsi288Ppo288, alignTrix9Tsi25, alignTrix48Tsi48, alignTrix288Tsi288);
        double disagreeCount = MomMomentumDisagreementCountCalc.calculate(alignRsi14Ppo12, alignRsi48Ppo48, alignRsi288Ppo288, alignTrix9Tsi25, alignTrix48Tsi48, alignTrix288Tsi288);
        double neutralCount = MomMomentumNeutralCountCalc.calculate(agreeCount, disagreeCount);

        // ifNull("momMomentumDisagreementCount", doc::getMomMomentumDisagreementCount, () -> disagreeCount, doc::setMomMomentumDisagreementCount, dependentTasks); // fora da prod mask
        // ifNull("momMomentumAgreementCount",    doc::getMomMomentumAgreementCount,    () -> agreeCount,    doc::setMomMomentumAgreementCount,    dependentTasks); // fora da prod mask

        // ── CONSENSUS QUALITY ───────────────────────────────────────────────
        double consensusAbs = MomMomentumConsensusAbsCalc.calculate(consensusScore);
        double flipRateW20 = (consensusW20 != null) ? MomMomentumConsensusFlipRateW20Calc.calculate(consensusW20) : 0.0;
        double chopScore = (safeDouble(doc.getMomChprt48()) + safeDouble(doc.getMomChprt288())) / 2.0;

        // ifNull("momMomentumConsensusAbs",       doc::getMomMomentumConsensusAbs,       () -> consensusAbs,       doc::setMomMomentumConsensusAbs,       dependentTasks); // fora da prod mask
        // ifNull("momMomentumNeutralCount",       doc::getMomMomentumNeutralCount,       () -> neutralCount,       doc::setMomMomentumNeutralCount,       dependentTasks); // fora da prod mask
        // ifNull("momMomentumVoteEntropy",        doc::getMomMomentumVoteEntropy,        () -> MomMomentumVoteEntropyCalc.calculate(agreeCount, disagreeCount, neutralCount), doc::setMomMomentumVoteEntropy, dependentTasks); // fora da prod mask
        // ifNull("momMomentumConsensusRunLen",    doc::getMomMomentumConsensusRunLen,    () -> MomMomentumConsensusRunLenCalc.calculate(consensusW20), doc::setMomMomentumConsensusRunLen,    dependentTasks); // fora da prod mask
        // ifNull("momMomentumSignalQualityScore", doc::getMomMomentumSignalQualityScore, () -> MomMomentumSignalQualityScoreCalc.calculate(consensusAbs, flipRateW20, chopScore), doc::setMomMomentumSignalQualityScore, dependentTasks); // fora da prod mask

        // ── PPO HIST SLP W20 ────────────────────────────────────────────────
        ifNull("momPpoHist12269SlpW20",  doc::getMomPpoHist12269SlpW20,  () -> MomPpoHist12269SlpW20Calc.calculate(ppoHist12W20),  doc::setMomPpoHist12269SlpW20,  dependentTasks);
        // ifNull("momPpoHist481049SlpW20", doc::getMomPpoHist481049SlpW20, () -> MomPpoHist481049SlpW20Calc.calculate(ppoHist48W20), doc::setMomPpoHist481049SlpW20, dependentTasks); // fora da prod mask
        // ifNull("momPpoHist2885769SlpW20", doc::getMomPpoHist2885769SlpW20, () -> MomPpoHist2885769SlpW20Calc.calculate(ppoHist288W20), doc::setMomPpoHist2885769SlpW20, dependentTasks); // fora da prod mask

        // ── CONSENSUS EXTREMES ──────────────────────────────────────────────
        // ifNull("momMomentumConsensusZsc80",      doc::getMomMomentumConsensusZsc80,      () -> MomMomentumConsensusZsc80Calc.calculate(consensusW80),      doc::setMomMomentumConsensusZsc80,      dependentTasks); // fora da prod mask
        // ifNull("momMomentumConsensusPctileW80",  doc::getMomMomentumConsensusPctileW80,  () -> MomMomentumConsensusPctileW80Calc.calculate(consensusW80),  doc::setMomMomentumConsensusPctileW80,  dependentTasks); // fora da prod mask

        // ── TRIX HIST SLP W20 ───────────────────────────────────────────────
        ifNull("momTrixHist9SlpW20",   doc::getMomTrixHist9SlpW20,   () -> MomTrixHist9SlpW20Calc.calculate(trixHist9W20),   doc::setMomTrixHist9SlpW20,   dependentTasks);
        // ifNull("momTrixHist48SlpW20",  doc::getMomTrixHist48SlpW20,  () -> MomTrixHist48SlpW20Calc.calculate(trixHist48W20), doc::setMomTrixHist48SlpW20,  dependentTasks); // fora da prod mask
        // ifNull("momTrixHist288SlpW20", doc::getMomTrixHist288SlpW20, () -> MomTrixHist288SlpW20Calc.calculate(trixHist288W20), doc::setMomTrixHist288SlpW20, dependentTasks); // fora da prod mask

        // ── TSI HIST SLP W20 ────────────────────────────────────────────────
        ifNull("momTsiHist2513SlpW20",   doc::getMomTsiHist2513SlpW20,   () -> MomTsiHist2513SlpW20Calc.calculate(tsiHist2513W20),   doc::setMomTsiHist2513SlpW20,   dependentTasks);
        // ifNull("momTsiHist4825SlpW20",   doc::getMomTsiHist4825SlpW20,   () -> MomTsiHist4825SlpW20Calc.calculate(tsiHist4825W20),   doc::setMomTsiHist4825SlpW20,   dependentTasks); // fora da prod mask
        // ifNull("momTsiHist288144SlpW20", doc::getMomTsiHist288144SlpW20, () -> MomTsiHist288144SlpW20Calc.calculate(tsiHist288144W20), doc::setMomTsiHist288144SlpW20, dependentTasks); // fora da prod mask

        if (!dependentTasks.isEmpty()) {
            execute(dependentTasks);
        }

        return doc;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════════

    private void ifNull(String name,
                        Supplier<Double> getter,
                        Supplier<Double> calculator,
                        Consumer<Double> setter,
                        List<Callable<Void>> tasks) {
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
            catch (Exception e) { throw new RuntimeException("[MOM] erro", e); }
        }
    }

    private static double safeDouble(Double val) {
        return (val != null) ? val : 0.0;
    }

    private static double safeGet(Double existing, Supplier<Double> fallback) {
        return (existing != null) ? existing : fallback.get();
    }

    /**
     * Builds a window of consensus scores for the last W bars up to and including index.
     */
    private double[] buildConsensusWindow(BarSeries series, int index, int window,
                                           ClosePriceIndicator close,
                                           RSIIndicator rsi14, RSIIndicator rsi48, RSIIndicator rsi288,
                                           PPOIndicator ppo1226, EMAIndicator ppoSig1226,
                                           PPOIndicator ppo48104, EMAIndicator ppoSig48104,
                                           PPOIndicator ppo288576, EMAIndicator ppoSig288576,
                                           TrixExtension trix9, TrixExtension trix48, TrixExtension trix288,
                                           TsiExtension tsi2513, TsiExtension tsi4825, TsiExtension tsi288144) {
        int start = Math.max(1, index - window + 1);
        int len = index - start + 1;
        if (len <= 0) return null;

        double[] result = new double[len];
        for (int i = 0; i < len; i++) {
            int idx = start + i;
            double r14 = rsi14.getValue(idx).doubleValue();
            double r48 = rsi48.getValue(idx).doubleValue();
            double r288 = rsi288.getValue(idx).doubleValue();
            double ph12 = ppo1226.getValue(idx).doubleValue() - ppoSig1226.getValue(idx).doubleValue();
            double ph48 = ppo48104.getValue(idx).doubleValue() - ppoSig48104.getValue(idx).doubleValue();
            double ph288 = ppo288576.getValue(idx).doubleValue() - ppoSig288576.getValue(idx).doubleValue();
            double th9 = MomTrix9HistCalc.calculate(trix9, idx);
            double th48 = MomTrix48HistCalc.calculate(trix48, idx);
            double th288 = MomTrix288HistCalc.calculate(trix288, idx);
            double tsh25 = MomTsi2513HistCalc.calculate(tsi2513, idx, 7);
            double tsh48 = MomTsi4825HistCalc.calculate(tsi4825, idx, 7);
            double tsh288 = MomTsi288144HistCalc.calculate(tsi288144, idx, 7);

            double a1 = MomAlignRsi14PpoHist12269Calc.calculate(r14, ph12);
            double a2 = MomAlignRsi48PpoHist481049Calc.calculate(r48, ph48);
            double a3 = MomAlignRsi288PpoHist2885769Calc.calculate(r288, ph288);
            double a4 = MomAlignTrixHist9TsiHist2513Calc.calculate(th9, tsh25);
            double a5 = MomAlignTrixHist48TsiHist4825Calc.calculate(th48, tsh48);
            double a6 = MomAlignTrixHist288TsiHist288144Calc.calculate(th288, tsh288);

            result[i] = MomMomentumConsensusScoreCalc.calculate(a1, a2, a3, a4, a5, a6);
        }
        return result;
    }

    private double[] buildHistWindow(PPOIndicator ppo, EMAIndicator signal, int index, int window) {
        int start = Math.max(1, index - window + 1);
        int len = index - start + 1;
        if (len <= 0) return null;
        double[] result = new double[len];
        for (int i = 0; i < len; i++) {
            int idx = start + i;
            result[i] = ppo.getValue(idx).doubleValue() - signal.getValue(idx).doubleValue();
        }
        return result;
    }

    private double[] buildTrixHistWindow(TrixExtension trix, int index, int window) {
        int start = Math.max(1, index - window + 1);
        int len = index - start + 1;
        if (len <= 0) return null;
        double[] result = new double[len];
        for (int i = 0; i < len; i++) {
            int idx = start + i;
            result[i] = MomTrix9HistCalc.calculate(trix, idx);
        }
        return result;
    }

    private double[] buildTsiHistWindow(TsiExtension tsi, int index, int window, int sigPeriod) {
        int start = Math.max(1, index - window + 1);
        int len = index - start + 1;
        if (len <= 0) return null;
        double[] result = new double[len];
        for (int i = 0; i < len; i++) {
            int idx = start + i;
            result[i] = MomTsi2513HistCalc.calculate(tsi, idx, sigPeriod);
        }
        return result;
    }
}
