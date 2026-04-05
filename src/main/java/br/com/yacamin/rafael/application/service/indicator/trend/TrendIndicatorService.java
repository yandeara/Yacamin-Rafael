package br.com.yacamin.rafael.application.service.indicator.trend;

import br.com.yacamin.rafael.application.service.indicator.cache.AtrCache;
import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.SmaCache;
import br.com.yacamin.rafael.application.service.indicator.cache.StdCache;
import br.com.yacamin.rafael.application.service.indicator.cache.trend.AdxCache;
import br.com.yacamin.rafael.application.service.indicator.cache.trend.EmaCache;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.application.service.indicator.trend.calc.*;
import br.com.yacamin.rafael.application.service.indicator.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.mongo.document.TrendIndicatorDocument;
import br.com.yacamin.rafael.adapter.out.persistence.mikhael.TrendIndicatorMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
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
public class TrendIndicatorService {

    private final TrendIndicatorMongoRepository repository;
    private final CloseCache closeCache;
    private final AtrCache atrCache;
    private final SmaCache smaCache;
    private final StdCache stdCache;
    private final EmaCache emaCache;
    private final AdxCache adxCache;

    public void analyse(SymbolCandle candle, BarSeries series) {
        analyse(candle, series, false);
    }

    public void analyse(SymbolCandle candle, BarSeries series, boolean forceRecalculate) {
        TrendIndicatorDocument doc = analyseBuffered(candle, series, forceRecalculate, null);
        repository.save(doc, candle.getInterval());
    }

    public TrendIndicatorDocument analyseBuffered(SymbolCandle candle, BarSeries series, boolean forceRecalculate, TrendIndicatorDocument preloadedDoc) {
        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var interval = candle.getInterval();
        int index = series.getEndIndex();

        log.info("[WARMUP][TRD] {} - {}", symbol, openTime);

        TrendIndicatorDocument doc;
        if (forceRecalculate) {
            doc = new TrendIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        } else if (preloadedDoc != null) {
            doc = preloadedDoc;
        } else {
            doc = new TrendIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        }

        // =====================================================================
        // Warmup caches
        // =====================================================================
        ClosePriceIndicator closeInd = closeCache.getClosePrice(symbol, interval, series);
        ATRIndicator atr14 = atrCache.getAtr14(symbol, interval, series);
        double closeNow = candle.getClose();

        // EMA raw — apenas 8, 20, 50 usados na prod mask
        EMAIndicator e8   = emaCache.getEma(symbol, interval, series, 8);
        // EMAIndicator e12  = emaCache.getEma(symbol, interval, series, 12);  // fora da prod mask
        // EMAIndicator e16  = emaCache.getEma(symbol, interval, series, 16);  // fora da prod mask
        EMAIndicator e20  = emaCache.getEma(symbol, interval, series, 20);
        // EMAIndicator e21  = emaCache.getEma(symbol, interval, series, 21);  // fora da prod mask
        // EMAIndicator e32  = emaCache.getEma(symbol, interval, series, 32);  // fora da prod mask
        // EMAIndicator e34  = emaCache.getEma(symbol, interval, series, 34);  // fora da prod mask
        EMAIndicator e50  = emaCache.getEma(symbol, interval, series, 50);
        // EMAIndicator e55  = emaCache.getEma(symbol, interval, series, 55);  // fora da prod mask
        // EMAIndicator e100 = emaCache.getEma(symbol, interval, series, 100); // fora da prod mask
        // EMAIndicator e144 = emaCache.getEma(symbol, interval, series, 144); // fora da prod mask
        // EMAIndicator e200 = emaCache.getEma(symbol, interval, series, 200); // fora da prod mask
        // EMAIndicator e233 = emaCache.getEma(symbol, interval, series, 233); // fora da prod mask

        // Slopes — apenas 8, 20, 50 usados na prod mask
        LinearRegressionSlopeIndicator s8  = emaCache.getEmaSlope(symbol, interval, series, 8);
        // LinearRegressionSlopeIndicator s16 = emaCache.getEmaSlope(symbol, interval, series, 16); // fora da prod mask
        LinearRegressionSlopeIndicator s20 = emaCache.getEmaSlope(symbol, interval, series, 20);
        // LinearRegressionSlopeIndicator s32 = emaCache.getEmaSlope(symbol, interval, series, 32); // fora da prod mask
        LinearRegressionSlopeIndicator s50 = emaCache.getEmaSlope(symbol, interval, series, 50);

        // Slope acc
        DifferenceIndicator a8  = emaCache.getEmaSlopeAcc(symbol, interval, series, 8);
        DifferenceIndicator a20 = emaCache.getEmaSlopeAcc(symbol, interval, series, 20);
        DifferenceIndicator a50 = emaCache.getEmaSlopeAcc(symbol, interval, series, 50);

        // TDS — fora da prod mask
        // var tds8  = emaCache.getEmaSlopeTds(symbol, interval, series, 8);
        // var tds20 = emaCache.getEmaSlopeTds(symbol, interval, series, 20);
        // var tds50 = emaCache.getEmaSlopeTds(symbol, interval, series, 50);

        // SMA / STD for z-score — fora da prod mask (usados apenas por composites)
        // SMAIndicator sma8  = smaCache.getSma(symbol, interval, series, 8);
        // SMAIndicator sma20 = smaCache.getSma(symbol, interval, series, 20);
        // SMAIndicator sma50 = smaCache.getSma(symbol, interval, series, 50);
        // StandardDeviationIndicator std8  = stdCache.getStd(symbol, interval, series, 8);
        // StandardDeviationIndicator std20 = stdCache.getStd(symbol, interval, series, 20);
        // StandardDeviationIndicator std50 = stdCache.getStd(symbol, interval, series, 50);

        // ADX
        ADXIndicator adx14  = adxCache.getAdx(symbol, interval, series, 14);
        PlusDIIndicator pdi14  = adxCache.getPdi(symbol, interval, series, 14);
        MinusDIIndicator mdi14 = adxCache.getMdi(symbol, interval, series, 14);

        // =====================================================================
        // Build tasks
        // =====================================================================
        List<Callable<Void>> tasks = new ArrayList<>();

        // 1) EMA RAW — fora da prod mask (valores brutos nao usados, apenas derivados)
        // ifNull("trdEma8",   doc::getTrdEma8,   () -> TrdEma8Calc.calculate(e8, index),     doc::setTrdEma8,   tasks);
        // ifNull("trdEma12",  doc::getTrdEma12,  () -> TrdEma12Calc.calculate(e12, index),   doc::setTrdEma12,  tasks);
        // ifNull("trdEma16",  doc::getTrdEma16,  () -> TrdEma16Calc.calculate(e16, index),   doc::setTrdEma16,  tasks);
        // ifNull("trdEma20",  doc::getTrdEma20,  () -> TrdEma20Calc.calculate(e20, index),   doc::setTrdEma20,  tasks);
        // ifNull("trdEma21",  doc::getTrdEma21,  () -> TrdEma21Calc.calculate(e21, index),   doc::setTrdEma21,  tasks);
        // ifNull("trdEma32",  doc::getTrdEma32,  () -> TrdEma32Calc.calculate(e32, index),   doc::setTrdEma32,  tasks);
        // ifNull("trdEma34",  doc::getTrdEma34,  () -> TrdEma34Calc.calculate(e34, index),   doc::setTrdEma34,  tasks);
        // ifNull("trdEma50",  doc::getTrdEma50,  () -> TrdEma50Calc.calculate(e50, index),   doc::setTrdEma50,  tasks);
        // ifNull("trdEma55",  doc::getTrdEma55,  () -> TrdEma55Calc.calculate(e55, index),   doc::setTrdEma55,  tasks);
        // ifNull("trdEma100", doc::getTrdEma100, () -> TrdEma100Calc.calculate(e100, index), doc::setTrdEma100, tasks);
        // ifNull("trdEma144", doc::getTrdEma144, () -> TrdEma144Calc.calculate(e144, index), doc::setTrdEma144, tasks);
        // ifNull("trdEma200", doc::getTrdEma200, () -> TrdEma200Calc.calculate(e200, index), doc::setTrdEma200, tasks);
        // ifNull("trdEma233", doc::getTrdEma233, () -> TrdEma233Calc.calculate(e233, index), doc::setTrdEma233, tasks);

        // 2) SLOPE RAW — fora da prod mask (apenas atrn usados)
        // ifNull("trdEma8Slp",  doc::getTrdEma8Slp,  () -> TrdEma8SlpCalc.calculate(s8, index),   doc::setTrdEma8Slp,  tasks);
        // ifNull("trdEma16Slp", doc::getTrdEma16Slp, () -> TrdEma16SlpCalc.calculate(s16, index), doc::setTrdEma16Slp, tasks);
        // ifNull("trdEma20Slp", doc::getTrdEma20Slp, () -> TrdEma20SlpCalc.calculate(s20, index), doc::setTrdEma20Slp, tasks);
        // ifNull("trdEma32Slp", doc::getTrdEma32Slp, () -> TrdEma32SlpCalc.calculate(s32, index), doc::setTrdEma32Slp, tasks);
        // ifNull("trdEma50Slp", doc::getTrdEma50Slp, () -> TrdEma50SlpCalc.calculate(s50, index), doc::setTrdEma50Slp, tasks);

        // 3) SLOPE ATR-N — prod mask: apenas 8, 20, 50
        ifNull("trdEma8SlpAtrn",  doc::getTrdEma8SlpAtrn,  () -> TrdEma8SlpAtrnCalc.calculate(s8, atr14, index),   doc::setTrdEma8SlpAtrn,  tasks);
        // ifNull("trdEma16SlpAtrn", doc::getTrdEma16SlpAtrn, () -> TrdEma16SlpAtrnCalc.calculate(s16, atr14, index), doc::setTrdEma16SlpAtrn, tasks); // fora da prod mask
        ifNull("trdEma20SlpAtrn", doc::getTrdEma20SlpAtrn, () -> TrdEma20SlpAtrnCalc.calculate(s20, atr14, index), doc::setTrdEma20SlpAtrn, tasks);
        // ifNull("trdEma32SlpAtrn", doc::getTrdEma32SlpAtrn, () -> TrdEma32SlpAtrnCalc.calculate(s32, atr14, index), doc::setTrdEma32SlpAtrn, tasks); // fora da prod mask
        ifNull("trdEma50SlpAtrn", doc::getTrdEma50SlpAtrn, () -> TrdEma50SlpAtrnCalc.calculate(s50, atr14, index), doc::setTrdEma50SlpAtrn, tasks);

        // 4) SLOPE ACC RAW — fora da prod mask (apenas atrn usados)
        // ifNull("trdEma8SlpAcc",  doc::getTrdEma8SlpAcc,  () -> TrdEma8SlpAccCalc.calculate(a8, index),   doc::setTrdEma8SlpAcc,  tasks);
        // ifNull("trdEma20SlpAcc", doc::getTrdEma20SlpAcc, () -> TrdEma20SlpAccCalc.calculate(a20, index), doc::setTrdEma20SlpAcc, tasks);
        // ifNull("trdEma50SlpAcc", doc::getTrdEma50SlpAcc, () -> TrdEma50SlpAccCalc.calculate(a50, index), doc::setTrdEma50SlpAcc, tasks);

        // 5) SLOPE ACC ATR-N (3)
        ifNull("trdEma8SlpAccAtrn",  doc::getTrdEma8SlpAccAtrn,  () -> TrdEma8SlpAccAtrnCalc.calculate(a8, atr14, index),   doc::setTrdEma8SlpAccAtrn,  tasks);
        ifNull("trdEma20SlpAccAtrn", doc::getTrdEma20SlpAccAtrn, () -> TrdEma20SlpAccAtrnCalc.calculate(a20, atr14, index), doc::setTrdEma20SlpAccAtrn, tasks);
        ifNull("trdEma50SlpAccAtrn", doc::getTrdEma50SlpAccAtrn, () -> TrdEma50SlpAccAtrnCalc.calculate(a50, atr14, index), doc::setTrdEma50SlpAccAtrn, tasks);

        // 6) TDS — fora da prod mask
        // ifNull("trdEma8SlpTds",  doc::getTrdEma8SlpTds,  () -> TrdEma8SlpTdsCalc.calculate(tds8, index),   doc::setTrdEma8SlpTds,  tasks);
        // ifNull("trdEma20SlpTds", doc::getTrdEma20SlpTds, () -> TrdEma20SlpTdsCalc.calculate(tds20, index), doc::setTrdEma20SlpTds, tasks);
        // ifNull("trdEma50SlpTds", doc::getTrdEma50SlpTds, () -> TrdEma50SlpTdsCalc.calculate(tds50, index), doc::setTrdEma50SlpTds, tasks);

        // 6) TVR — fora da prod mask
        // ifNull("trdEma8SlpTvr",  doc::getTrdEma8SlpTvr,  () -> TrdEma8SlpTvrCalc.calculate(atr14, s8, index),   doc::setTrdEma8SlpTvr,  tasks);
        // ifNull("trdEma20SlpTvr", doc::getTrdEma20SlpTvr, () -> TrdEma20SlpTvrCalc.calculate(atr14, s20, index), doc::setTrdEma20SlpTvr, tasks);
        // ifNull("trdEma50SlpTvr", doc::getTrdEma50SlpTvr, () -> TrdEma50SlpTvrCalc.calculate(atr14, s50, index), doc::setTrdEma50SlpTvr, tasks);

        // 7) DISTANCE ATR-N (8)
        ifNull("trdDistCloseEma8Atrn",  doc::getTrdDistCloseEma8Atrn,  () -> TrdDistCloseEma8AtrnCalc.calculate(closeInd, e8, atr14, index),   doc::setTrdDistCloseEma8Atrn,  tasks);
        ifNull("trdDistCloseEma20Atrn", doc::getTrdDistCloseEma20Atrn, () -> TrdDistCloseEma20AtrnCalc.calculate(closeInd, e20, atr14, index), doc::setTrdDistCloseEma20Atrn, tasks);
        ifNull("trdDistCloseEma50Atrn", doc::getTrdDistCloseEma50Atrn, () -> TrdDistCloseEma50AtrnCalc.calculate(closeInd, e50, atr14, index), doc::setTrdDistCloseEma50Atrn, tasks);
        // ifNull("trdDistCloseEma16Atrn", doc::getTrdDistCloseEma16Atrn, () -> TrdDistCloseEma16AtrnCalc.calculate(closeInd, e16, atr14, index), doc::setTrdDistCloseEma16Atrn, tasks); // fora da prod mask
        // ifNull("trdDistCloseEma32Atrn", doc::getTrdDistCloseEma32Atrn, () -> TrdDistCloseEma32AtrnCalc.calculate(closeInd, e32, atr14, index), doc::setTrdDistCloseEma32Atrn, tasks); // fora da prod mask
        ifNull("trdDistEma820Atrn",     doc::getTrdDistEma820Atrn,     () -> TrdDistEma820AtrnCalc.calculate(e8, e20, atr14, index),           doc::setTrdDistEma820Atrn,     tasks);
        ifNull("trdDistEma2050Atrn",    doc::getTrdDistEma2050Atrn,    () -> TrdDistEma2050AtrnCalc.calculate(e20, e50, atr14, index),         doc::setTrdDistEma2050Atrn,    tasks);
        ifNull("trdDistEma850Atrn",     doc::getTrdDistEma850Atrn,     () -> TrdDistEma850AtrnCalc.calculate(e8, e50, atr14, index),           doc::setTrdDistEma850Atrn,     tasks);

        // 8) RATIOS (3)
        ifNull("trdRatioEma820",  doc::getTrdRatioEma820,  () -> TrdRatioEma820Calc.calculate(e8, e20, index),   doc::setTrdRatioEma820,  tasks);
        ifNull("trdRatioEma2050", doc::getTrdRatioEma2050, () -> TrdRatioEma2050Calc.calculate(e20, e50, index), doc::setTrdRatioEma2050, tasks);
        ifNull("trdRatioEma850",  doc::getTrdRatioEma850,  () -> TrdRatioEma850Calc.calculate(e8, e50, index),   doc::setTrdRatioEma850,  tasks);

        // 9) ALIGNMENT (4)
        ifNull("trdAlignmentEma82050Score",      doc::getTrdAlignmentEma82050Score,      () -> TrdAlignmentEma82050ScoreCalc.calculate(e8, e20, e50, index),          doc::setTrdAlignmentEma82050Score,      tasks);
        ifNull("trdAlignmentEma82050Normalized", doc::getTrdAlignmentEma82050Normalized, () -> TrdAlignmentEma82050NormalizedCalc.calculate(e8, e20, e50, index),     doc::setTrdAlignmentEma82050Normalized, tasks);
        // ifNull("trdAligmentEma82050Binary",      doc::getTrdAligmentEma82050Binary,      () -> TrdAligmentEma82050BinaryCalc.calculate(e20, e50, index),              doc::setTrdAligmentEma82050Binary,      tasks); // fora da prod mask
        ifNull("trdAligmentEma82050Delta",       doc::getTrdAligmentEma82050Delta,       () -> TrdAligmentEma82050DeltaCalc.calculate(e20, e50, closeNow, index),     doc::setTrdAligmentEma82050Delta,       tasks);

        // 10) CROSSOVER 8/20 — prod mask: apenas DeltaAtrn
        // ifNull("trdCrossEma820Binary",    doc::getTrdCrossEma820Binary,    () -> TrdCrossEma820BinaryCalc.calculate(e8, e20, index),                  doc::setTrdCrossEma820Binary,    tasks); // fora da prod mask
        // ifNull("trdCrossEma820Delta",     doc::getTrdCrossEma820Delta,     () -> TrdCrossEma820DeltaCalc.calculate(e8, e20, closeNow, index),         doc::setTrdCrossEma820Delta,     tasks); // fora da prod mask
        ifNull("trdCrossEma820DeltaAtrn", doc::getTrdCrossEma820DeltaAtrn, () -> TrdCrossEma820DeltaAtrnCalc.calculate(e8, e20, closeNow, atr14, index), doc::setTrdCrossEma820DeltaAtrn, tasks);

        // 10) CROSSOVER 20/50 — prod mask: apenas DeltaAtrn
        // ifNull("trdCrossEma2050Binary",    doc::getTrdCrossEma2050Binary,    () -> TrdCrossEma2050BinaryCalc.calculate(e20, e50, index),                   doc::setTrdCrossEma2050Binary,    tasks); // fora da prod mask
        // ifNull("trdCrossEma2050Delta",     doc::getTrdCrossEma2050Delta,     () -> TrdCrossEma2050DeltaCalc.calculate(e20, e50, closeNow, index),          doc::setTrdCrossEma2050Delta,     tasks); // fora da prod mask
        ifNull("trdCrossEma2050DeltaAtrn", doc::getTrdCrossEma2050DeltaAtrn, () -> TrdCrossEma2050DeltaAtrnCalc.calculate(e20, e50, closeNow, atr14, index), doc::setTrdCrossEma2050DeltaAtrn, tasks);

        // 11) DURATION — fora da prod mask
        // ifNull("trdDurationEma820",  doc::getTrdDurationEma820,  () -> TrdDurationEma820Calc.calculate(e8, e20, index),   doc::setTrdDurationEma820,  tasks);
        // ifNull("trdDurationEma2050", doc::getTrdDurationEma2050, () -> TrdDurationEma2050Calc.calculate(e20, e50, index), doc::setTrdDurationEma2050, tasks);
        // ifNull("trdDurationEma850",  doc::getTrdDurationEma850,  () -> TrdDurationEma850Calc.calculate(e8, e50, index),   doc::setTrdDurationEma850,  tasks);

        // 12) DELTA SIGNED RAW — fora da prod mask (apenas atrn usados)
        // ifNull("trdDeltaCloseEma8",  doc::getTrdDeltaCloseEma8,  () -> TrdDeltaCloseEma8Calc.calculate(closeInd, e8, index),   doc::setTrdDeltaCloseEma8,  tasks);
        // ifNull("trdDeltaCloseEma20", doc::getTrdDeltaCloseEma20, () -> TrdDeltaCloseEma20Calc.calculate(closeInd, e20, index), doc::setTrdDeltaCloseEma20, tasks);
        // ifNull("trdDeltaCloseEma50", doc::getTrdDeltaCloseEma50, () -> TrdDeltaCloseEma50Calc.calculate(closeInd, e50, index), doc::setTrdDeltaCloseEma50, tasks);

        // 12) DELTA SIGNED ATRN (3)
        ifNull("trdDeltaCloseEma8Atrn",  doc::getTrdDeltaCloseEma8Atrn,  () -> TrdDeltaCloseEma8AtrnCalc.calculate(closeInd, e8, atr14, index),   doc::setTrdDeltaCloseEma8Atrn,  tasks);
        ifNull("trdDeltaCloseEma20Atrn", doc::getTrdDeltaCloseEma20Atrn, () -> TrdDeltaCloseEma20AtrnCalc.calculate(closeInd, e20, atr14, index), doc::setTrdDeltaCloseEma20Atrn, tasks);
        ifNull("trdDeltaCloseEma50Atrn", doc::getTrdDeltaCloseEma50Atrn, () -> TrdDeltaCloseEma50AtrnCalc.calculate(closeInd, e50, atr14, index), doc::setTrdDeltaCloseEma50Atrn, tasks);

        // 12) DELTA EMA-EMA — prod mask: apenas ATRN
        // ifNull("trdDeltaEma820",      doc::getTrdDeltaEma820,      () -> TrdDeltaEma820Calc.calculate(e8, e20, index),             doc::setTrdDeltaEma820,      tasks); // fora da prod mask
        // ifNull("trdDeltaEma2050",     doc::getTrdDeltaEma2050,     () -> TrdDeltaEma2050Calc.calculate(e20, e50, index),           doc::setTrdDeltaEma2050,     tasks); // fora da prod mask
        ifNull("trdDeltaEma820Atrn",  doc::getTrdDeltaEma820Atrn,  () -> TrdDeltaEma820AtrnCalc.calculate(e8, e20, atr14, index),  doc::setTrdDeltaEma820Atrn,  tasks);
        ifNull("trdDeltaEma2050Atrn", doc::getTrdDeltaEma2050Atrn, () -> TrdDeltaEma2050AtrnCalc.calculate(e20, e50, atr14, index), doc::setTrdDeltaEma2050Atrn, tasks);

        // 13) QUALITY / REGIME COMPOSITES — fora da prod mask
        // ifNull("trdCtsCloseEma20W10",  doc::getTrdCtsCloseEma20W10,  () -> TrdCtsCloseEma20W10Calc.calculate(series, e20, index),  doc::setTrdCtsCloseEma20W10,  tasks);
        // ifNull("trdCtsCloseEma20W50",  doc::getTrdCtsCloseEma20W50,  () -> TrdCtsCloseEma20W50Calc.calculate(series, e20, index),  doc::setTrdCtsCloseEma20W50,  tasks);
        // ifNull("trdCtsEma82050",       doc::getTrdCtsEma82050,       () -> TrdCtsEma82050Calc.calculate(e8, e20, e50, index),      doc::setTrdCtsEma82050,       tasks);
        // ifNull("trdEma82050SlpTcs",    doc::getTrdEma82050SlpTcs,    () -> TrdEma82050SlpTcsCalc.calculate(s8, s20, s50, index),   doc::setTrdEma82050SlpTcs,    tasks);
        // ifNull("trdTmEma820W10",       doc::getTrdTmEma820W10,       () -> TrdTmEma820W10Calc.calculate(e8, e20, index),           doc::setTrdTmEma820W10,       tasks);
        // ifNull("trdEma20SlpSnrW10",    doc::getTrdEma20SlpSnrW10,    () -> TrdEma20SlpSnrW10Calc.calculate(series, s20, index),    doc::setTrdEma20SlpSnrW10,    tasks);
        // ifNull("trdCloseTcpW50",       doc::getTrdCloseTcpW50,       () -> TrdCloseTcpW50Calc.calculate(series, index),            doc::setTrdCloseTcpW50,       tasks);
        // ifNull("trdEma8Zsc",           doc::getTrdEma8Zsc,           () -> TrdEma8ZscCalc.calculate(e8, sma8, std8, index),        doc::setTrdEma8Zsc,           tasks);
        // ifNull("trdEma20Zsc",          doc::getTrdEma20Zsc,          () -> TrdEma20ZscCalc.calculate(e20, sma20, std20, index),    doc::setTrdEma20Zsc,          tasks);
        // ifNull("trdEma50Zsc",          doc::getTrdEma50Zsc,          () -> TrdEma50ZscCalc.calculate(e50, sma50, std50, index),    doc::setTrdEma50Zsc,          tasks);

        // 14) ADX / DI — prod mask: apenas diDiff14
        // ifNull("trdAdx14",    doc::getTrdAdx14,    () -> TrdAdx14Calc.calculate(adx14, index),          doc::setTrdAdx14,    tasks); // fora da prod mask
        // ifNull("trdPdi14",    doc::getTrdPdi14,    () -> TrdPdi14Calc.calculate(pdi14, index),          doc::setTrdPdi14,    tasks); // fora da prod mask
        // ifNull("trdMdi14",    doc::getTrdMdi14,    () -> TrdMdi14Calc.calculate(mdi14, index),          doc::setTrdMdi14,    tasks); // fora da prod mask
        ifNull("trdDiDiff14", doc::getTrdDiDiff14, () -> TrdDiDiff14Calc.calculate(pdi14, mdi14, index), doc::setTrdDiDiff14, tasks);

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
            catch (Exception e) { throw new RuntimeException("[TRD] erro", e); }
        }
    }
}
