package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.application.service.indicator.cache.AtrCache;
import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.SlopeCache;
import br.com.yacamin.rafael.application.service.indicator.cache.SmaCache;
import br.com.yacamin.rafael.application.service.indicator.cache.StdCache;
import br.com.yacamin.rafael.application.service.indicator.volatility.calc.*;
import br.com.yacamin.rafael.application.service.indicator.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.mongo.document.VolatilityIndicatorDocument;
import br.com.yacamin.rafael.adapter.out.persistence.mikhael.VolatilityIndicatorMongoRepository;
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
public class VolatilityIndicatorService {

    private final VolatilityIndicatorMongoRepository repository;
    private final CloseCache closeCache;
    private final AtrCache atrCache;
    private final SmaCache smaCache;
    private final StdCache stdCache;
    private final br.com.yacamin.rafael.application.service.indicator.cache.volatility.BollingerCache bollingerCache;
    private final br.com.yacamin.rafael.application.service.indicator.cache.volatility.KeltnerCache keltnerCache;
    private final br.com.yacamin.rafael.application.service.indicator.cache.volatility.RealizedVolCache realizedVolCache;
    private final SlopeCache slopeCache;

    public void analyse(SymbolCandle candle, BarSeries series) {
        analyse(candle, series, false);
    }

    public void analyse(SymbolCandle candle, BarSeries series, boolean forceRecalculate) {
        VolatilityIndicatorDocument doc = analyseBuffered(candle, series, forceRecalculate, null);
        repository.save(doc, candle.getInterval());
    }

    public VolatilityIndicatorDocument analyseBuffered(SymbolCandle candle, BarSeries series, boolean forceRecalculate, VolatilityIndicatorDocument preloadedDoc) {
        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var interval = candle.getInterval();
        int index = series.getEndIndex();

        log.info("[WARMUP][VLT] {} - {}", symbol, openTime);

        VolatilityIndicatorDocument doc;
        if (forceRecalculate) {
            doc = new VolatilityIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        } else if (preloadedDoc != null) {
            doc = preloadedDoc;
        } else {
            doc = new VolatilityIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        }

        List<Callable<Void>> tasks = new ArrayList<>();

        // =========================================================================
        // ATR — cache warmup
        // =========================================================================
        var atr7 = atrCache.getAtr(symbol, interval, series, 7);
        var atr14 = atrCache.getAtr(symbol, interval, series, 14);
        var atr21 = atrCache.getAtr(symbol, interval, series, 21);
        // var atr48 = atrCache.getAtr(symbol, interval, series, 48); // fora da prod mask
        // var atr96 = atrCache.getAtr(symbol, interval, series, 96); // fora da prod mask
        // var atr288 = atrCache.getAtr(symbol, interval, series, 288); // fora da prod mask
        var slp7  = slopeCache.getSlope(symbol, interval, series, atr7, "atr7", 7);
        var slp14 = slopeCache.getSlope(symbol, interval, series, atr14, "atr14", 14);
        var slp21 = slopeCache.getSlope(symbol, interval, series, atr21, "atr21", 21);
        // var slp48 = new LinearRegressionSlopeIndicator(series, atr48, 48); // fora da prod mask
        // var slp96 = new LinearRegressionSlopeIndicator(series, atr96, 96); // fora da prod mask
        // var slp288 = new LinearRegressionSlopeIndicator(series, atr288, 288); // fora da prod mask

        // ATR Level
        // ifNull("vltAtr7", doc::getVltAtr7, () -> VltAtr7Calc.calculate(atr7, index), doc::setVltAtr7, tasks); // fora da prod mask
        // ifNull("vltAtr14", doc::getVltAtr14, () -> VltAtr14Calc.calculate(atr14, index), doc::setVltAtr14, tasks); // fora da prod mask
        // ifNull("vltAtr21", doc::getVltAtr21, () -> VltAtr21Calc.calculate(atr21, index), doc::setVltAtr21, tasks); // fora da prod mask
        // ifNull("vltAtr48", doc::getVltAtr48, () -> VltAtr48Calc.calculate(atr48, index), doc::setVltAtr48, tasks); // fora da prod mask
        // ifNull("vltAtr96", doc::getVltAtr96, () -> VltAtr96Calc.calculate(atr96, index), doc::setVltAtr96, tasks); // fora da prod mask
        // ifNull("vltAtr288", doc::getVltAtr288, () -> VltAtr288Calc.calculate(atr288, index), doc::setVltAtr288, tasks); // fora da prod mask

        // ATR Change
        ifNull("vltAtr7Chg", doc::getVltAtr7Chg, () -> VltAtr7ChgCalc.calculate(atr7, index), doc::setVltAtr7Chg, tasks);
        ifNull("vltAtr14Chg", doc::getVltAtr14Chg, () -> VltAtr14ChgCalc.calculate(atr14, index), doc::setVltAtr14Chg, tasks);
        ifNull("vltAtr21Chg", doc::getVltAtr21Chg, () -> VltAtr21ChgCalc.calculate(atr21, index), doc::setVltAtr21Chg, tasks);
        // ifNull("vltAtr48Chg", doc::getVltAtr48Chg, () -> VltAtr48ChgCalc.calculate(atr48, index), doc::setVltAtr48Chg, tasks); // fora da prod mask
        // ifNull("vltAtr96Chg", doc::getVltAtr96Chg, () -> VltAtr96ChgCalc.calculate(atr96, index), doc::setVltAtr96Chg, tasks); // fora da prod mask
        // ifNull("vltAtr288Chg", doc::getVltAtr288Chg, () -> VltAtr288ChgCalc.calculate(atr288, index), doc::setVltAtr288Chg, tasks); // fora da prod mask

        // ATR Local
        ifNull("vltRangeAtr14Loc", doc::getVltRangeAtr14Loc, () -> VltRangeAtr14LocCalc.calculate(series, atr14, index), doc::setVltRangeAtr14Loc, tasks);
        ifNull("vltRangeAtr14LocChg", doc::getVltRangeAtr14LocChg, () -> VltRangeAtr14LocChgCalc.calculate(series, atr14, index), doc::setVltRangeAtr14LocChg, tasks);
        // ifNull("vltRangeAtr48Loc", doc::getVltRangeAtr48Loc, () -> VltRangeAtr48LocCalc.calculate(series, atr48, index), doc::setVltRangeAtr48Loc, tasks); // fora da prod mask
        // ifNull("vltRangeAtr48LocChg", doc::getVltRangeAtr48LocChg, () -> VltRangeAtr48LocChgCalc.calculate(series, atr48, index), doc::setVltRangeAtr48LocChg, tasks); // fora da prod mask
        // ifNull("vltRangeAtr96Loc", doc::getVltRangeAtr96Loc, () -> VltRangeAtr96LocCalc.calculate(series, atr96, index), doc::setVltRangeAtr96Loc, tasks); // fora da prod mask
        // ifNull("vltRangeAtr96LocChg", doc::getVltRangeAtr96LocChg, () -> VltRangeAtr96LocChgCalc.calculate(series, atr96, index), doc::setVltRangeAtr96LocChg, tasks); // fora da prod mask
        // ifNull("vltRangeAtr288Loc", doc::getVltRangeAtr288Loc, () -> VltRangeAtr288LocCalc.calculate(series, atr288, index), doc::setVltRangeAtr288Loc, tasks); // fora da prod mask
        // ifNull("vltRangeAtr288LocChg", doc::getVltRangeAtr288LocChg, () -> VltRangeAtr288LocChgCalc.calculate(series, atr288, index), doc::setVltRangeAtr288LocChg, tasks); // fora da prod mask

        // ATR Ratios
        ifNull("vltAtr7_21Ratio", doc::getVltAtr721Ratio, () -> VltAtr7_21RatioCalc.calculate(atr7, atr21, index), doc::setVltAtr721Ratio, tasks);
        // ifNull("vltAtr7_21Expr", doc::getVltAtr721Expr, () -> VltAtr7_21ExprCalc.calculate(atr7, atr21, index), doc::setVltAtr721Expr, tasks); // fora da prod mask
        ifNull("vltAtr7_21Expn", doc::getVltAtr721Expn, () -> VltAtr7_21ExpnCalc.calculate(atr7, atr21, index), doc::setVltAtr721Expn, tasks);
        ifNull("vltAtr7_21Cmpr", doc::getVltAtr721Cmpr, () -> VltAtr7_21CmprCalc.calculate(atr7, atr21, index), doc::setVltAtr721Cmpr, tasks);
        // ifNull("vltAtr7_48Ratio", doc::getVltAtr748Ratio, () -> VltAtr7_48RatioCalc.calculate(atr7, atr48, index), doc::setVltAtr748Ratio, tasks); // fora da prod mask
        // ifNull("vltAtr7_48Expn", doc::getVltAtr748Expn, () -> VltAtr7_48ExpnCalc.calculate(atr7, atr48, index), doc::setVltAtr748Expn, tasks); // fora da prod mask
        // ifNull("vltAtr7_48Cmpr", doc::getVltAtr748Cmpr, () -> VltAtr7_48CmprCalc.calculate(atr7, atr48, index), doc::setVltAtr748Cmpr, tasks); // fora da prod mask
        // ifNull("vltAtr14_48Ratio", doc::getVltAtr1448Ratio, () -> VltAtr14_48RatioCalc.calculate(atr14, atr48, index), doc::setVltAtr1448Ratio, tasks); // fora da prod mask
        // ifNull("vltAtr14_48Expn", doc::getVltAtr1448Expn, () -> VltAtr14_48ExpnCalc.calculate(atr14, atr48, index), doc::setVltAtr1448Expn, tasks); // fora da prod mask
        // ifNull("vltAtr14_48Cmpr", doc::getVltAtr1448Cmpr, () -> VltAtr14_48CmprCalc.calculate(atr14, atr48, index), doc::setVltAtr1448Cmpr, tasks); // fora da prod mask
        // ifNull("vltAtr14_96Ratio", doc::getVltAtr1496Ratio, () -> VltAtr14_96RatioCalc.calculate(atr14, atr96, index), doc::setVltAtr1496Ratio, tasks); // fora da prod mask
        // ifNull("vltAtr14_96Expn", doc::getVltAtr1496Expn, () -> VltAtr14_96ExpnCalc.calculate(atr14, atr96, index), doc::setVltAtr1496Expn, tasks); // fora da prod mask
        // ifNull("vltAtr14_96Cmpr", doc::getVltAtr1496Cmpr, () -> VltAtr14_96CmprCalc.calculate(atr14, atr96, index), doc::setVltAtr1496Cmpr, tasks); // fora da prod mask
        // ifNull("vltAtr14_288Ratio", doc::getVltAtr14288Ratio, () -> VltAtr14_288RatioCalc.calculate(atr14, atr288, index), doc::setVltAtr14288Ratio, tasks); // fora da prod mask
        // ifNull("vltAtr14_288Expn", doc::getVltAtr14288Expn, () -> VltAtr14_288ExpnCalc.calculate(atr14, atr288, index), doc::setVltAtr14288Expn, tasks); // fora da prod mask
        // ifNull("vltAtr14_288Cmpr", doc::getVltAtr14288Cmpr, () -> VltAtr14_288CmprCalc.calculate(atr14, atr288, index), doc::setVltAtr14288Cmpr, tasks); // fora da prod mask
        // ifNull("vltAtr48_288Ratio", doc::getVltAtr48288Ratio, () -> VltAtr48_288RatioCalc.calculate(atr48, atr288, index), doc::setVltAtr48288Ratio, tasks); // fora da prod mask
        // ifNull("vltAtr48_288Expn", doc::getVltAtr48288Expn, () -> VltAtr48_288ExpnCalc.calculate(atr48, atr288, index), doc::setVltAtr48288Expn, tasks); // fora da prod mask
        // ifNull("vltAtr48_288Cmpr", doc::getVltAtr48288Cmpr, () -> VltAtr48_288CmprCalc.calculate(atr48, atr288, index), doc::setVltAtr48288Cmpr, tasks); // fora da prod mask
        // ifNull("vltAtr96_288Ratio", doc::getVltAtr96288Ratio, () -> VltAtr96_288RatioCalc.calculate(atr96, atr288, index), doc::setVltAtr96288Ratio, tasks); // fora da prod mask
        // ifNull("vltAtr96_288Expn", doc::getVltAtr96288Expn, () -> VltAtr96_288ExpnCalc.calculate(atr96, atr288, index), doc::setVltAtr96288Expn, tasks); // fora da prod mask
        // ifNull("vltAtr96_288Cmpr", doc::getVltAtr96288Cmpr, () -> VltAtr96_288CmprCalc.calculate(atr96, atr288, index), doc::setVltAtr96288Cmpr, tasks); // fora da prod mask

        // ATR Slopes
        ifNull("vltAtr7Slp", doc::getVltAtr7Slp, () -> VltAtr7SlpCalc.calculate(slp7, index), doc::setVltAtr7Slp, tasks);
        ifNull("vltAtr14Slp", doc::getVltAtr14Slp, () -> VltAtr14SlpCalc.calculate(slp14, index), doc::setVltAtr14Slp, tasks);
        ifNull("vltAtr21Slp", doc::getVltAtr21Slp, () -> VltAtr21SlpCalc.calculate(slp21, index), doc::setVltAtr21Slp, tasks);
        // ifNull("vltAtr48Slp", doc::getVltAtr48Slp, () -> VltAtr48SlpCalc.calculate(slp48, index), doc::setVltAtr48Slp, tasks); // fora da prod mask
        // ifNull("vltAtr96Slp", doc::getVltAtr96Slp, () -> VltAtr96SlpCalc.calculate(slp96, index), doc::setVltAtr96Slp, tasks); // fora da prod mask
        // ifNull("vltAtr288Slp", doc::getVltAtr288Slp, () -> VltAtr288SlpCalc.calculate(slp288, index), doc::setVltAtr288Slp, tasks); // fora da prod mask

        // ATR Z-Score
        // ifNull("vltAtr7Zsc", doc::getVltAtr7Zsc, () -> VltAtr7ZscCalc.calculate(atr7, index), doc::setVltAtr7Zsc, tasks); // fora da prod mask
        // ifNull("vltAtr14Zsc", doc::getVltAtr14Zsc, () -> VltAtr14ZscCalc.calculate(atr14, index), doc::setVltAtr14Zsc, tasks); // fora da prod mask
        // ifNull("vltAtr21Zsc", doc::getVltAtr21Zsc, () -> VltAtr21ZscCalc.calculate(atr21, index), doc::setVltAtr21Zsc, tasks); // fora da prod mask
        // ifNull("vltAtr48Zsc", doc::getVltAtr48Zsc, () -> VltAtr48ZscCalc.calculate(atr48, index), doc::setVltAtr48Zsc, tasks); // fora da prod mask
        // ifNull("vltAtr96Zsc", doc::getVltAtr96Zsc, () -> VltAtr96ZscCalc.calculate(atr96, index), doc::setVltAtr96Zsc, tasks); // fora da prod mask
        // ifNull("vltAtr288Zsc", doc::getVltAtr288Zsc, () -> VltAtr288ZscCalc.calculate(atr288, index), doc::setVltAtr288Zsc, tasks); // fora da prod mask

        // ATR VoV
        // ifNull("vltAtr14VvW16", doc::getVltAtr14VvW16, () -> VltAtr14VvW16Calc.calculate(atr14, index), doc::setVltAtr14VvW16, tasks); // fora da prod mask
        // ifNull("vltAtr14VvW32", doc::getVltAtr14VvW32, () -> VltAtr14VvW32Calc.calculate(atr14, index), doc::setVltAtr14VvW32, tasks); // fora da prod mask
        // ifNull("vltAtr14VvW48", doc::getVltAtr14VvW48, () -> VltAtr14VvW48Calc.calculate(atr14, index), doc::setVltAtr14VvW48, tasks); // fora da prod mask
        // ifNull("vltAtr14VvW96", doc::getVltAtr14VvW96, () -> VltAtr14VvW96Calc.calculate(atr14, index), doc::setVltAtr14VvW96, tasks); // fora da prod mask
        // ifNull("vltAtr14VvW288", doc::getVltAtr14VvW288, () -> VltAtr14VvW288Calc.calculate(atr14, index), doc::setVltAtr14VvW288, tasks); // fora da prod mask
        // ifNull("vltAtr48VvW16", doc::getVltAtr48VvW16, () -> VltAtr48VvW16Calc.calculate(atr48, index), doc::setVltAtr48VvW16, tasks); // fora da prod mask
        // ifNull("vltAtr48VvW32", doc::getVltAtr48VvW32, () -> VltAtr48VvW32Calc.calculate(atr48, index), doc::setVltAtr48VvW32, tasks); // fora da prod mask
        // ifNull("vltAtr48VvW48", doc::getVltAtr48VvW48, () -> VltAtr48VvW48Calc.calculate(atr48, index), doc::setVltAtr48VvW48, tasks); // fora da prod mask
        // ifNull("vltAtr96VvW32", doc::getVltAtr96VvW32, () -> VltAtr96VvW32Calc.calculate(atr96, index), doc::setVltAtr96VvW32, tasks); // fora da prod mask
        // ifNull("vltAtr96VvW48", doc::getVltAtr96VvW48, () -> VltAtr96VvW48Calc.calculate(atr96, index), doc::setVltAtr96VvW48, tasks); // fora da prod mask
        // ifNull("vltAtr96VvW96", doc::getVltAtr96VvW96, () -> VltAtr96VvW96Calc.calculate(atr96, index), doc::setVltAtr96VvW96, tasks); // fora da prod mask
        // ifNull("vltAtr288VvW48", doc::getVltAtr288VvW48, () -> VltAtr288VvW48Calc.calculate(atr288, index), doc::setVltAtr288VvW48, tasks); // fora da prod mask
        // ifNull("vltAtr288VvW96", doc::getVltAtr288VvW96, () -> VltAtr288VvW96Calc.calculate(atr288, index), doc::setVltAtr288VvW96, tasks); // fora da prod mask
        // ifNull("vltAtr288VvW288", doc::getVltAtr288VvW288, () -> VltAtr288VvW288Calc.calculate(atr288, index), doc::setVltAtr288VvW288, tasks); // fora da prod mask

        // =========================================================================
        // BIG MOVE
        // =========================================================================
        // ifNull("vltVolBigmoveFreq20", doc::getVltVolBigmoveFreq20, () -> VltVolBigmoveFreq20Calc.calculate(series, atr14, index), doc::setVltVolBigmoveFreq20, tasks); // fora da prod mask
        // ifNull("vltVolBigmoveFreq50", doc::getVltVolBigmoveFreq50, () -> VltVolBigmoveFreq50Calc.calculate(series, atr14, index), doc::setVltVolBigmoveFreq50, tasks); // fora da prod mask
        // ifNull("vltVolBigmoveAge", doc::getVltVolBigmoveAge, () -> VltVolBigmoveAgeCalc.calculate(series, atr14, index), doc::setVltVolBigmoveAge, tasks); // fora da prod mask
        // ifNull("vltVolBigmoveClusterLen", doc::getVltVolBigmoveClusterLen, () -> VltVolBigmoveClusterLenCalc.calculate(series, atr14, index), doc::setVltVolBigmoveClusterLen, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctFreq48", doc::getVltBigmove1pctFreq48, () -> VltBigmove1pctFreq48Calc.calculate(series, index), doc::setVltBigmove1pctFreq48, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctAge48", doc::getVltBigmove1pctAge48, () -> VltBigmove1pctAge48Calc.calculate(series, index), doc::setVltBigmove1pctAge48, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctClusterLen48", doc::getVltBigmove1pctClusterLen48, () -> VltBigmove1pctClusterLen48Calc.calculate(series, index), doc::setVltBigmove1pctClusterLen48, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctFreq96", doc::getVltBigmove1pctFreq96, () -> VltBigmove1pctFreq96Calc.calculate(series, index), doc::setVltBigmove1pctFreq96, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctAge96", doc::getVltBigmove1pctAge96, () -> VltBigmove1pctAge96Calc.calculate(series, index), doc::setVltBigmove1pctAge96, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctClusterLen96", doc::getVltBigmove1pctClusterLen96, () -> VltBigmove1pctClusterLen96Calc.calculate(series, index), doc::setVltBigmove1pctClusterLen96, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctFreq288", doc::getVltBigmove1pctFreq288, () -> VltBigmove1pctFreq288Calc.calculate(series, index), doc::setVltBigmove1pctFreq288, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctAge288", doc::getVltBigmove1pctAge288, () -> VltBigmove1pctAge288Calc.calculate(series, index), doc::setVltBigmove1pctAge288, tasks); // fora da prod mask
        // ifNull("vltBigmove1pctClusterLen288", doc::getVltBigmove1pctClusterLen288, () -> VltBigmove1pctClusterLen288Calc.calculate(series, index), doc::setVltBigmove1pctClusterLen288, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctFreq48", doc::getVltBigmove2pctFreq48, () -> VltBigmove2pctFreq48Calc.calculate(series, index), doc::setVltBigmove2pctFreq48, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctAge48", doc::getVltBigmove2pctAge48, () -> VltBigmove2pctAge48Calc.calculate(series, index), doc::setVltBigmove2pctAge48, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctClusterLen48", doc::getVltBigmove2pctClusterLen48, () -> VltBigmove2pctClusterLen48Calc.calculate(series, index), doc::setVltBigmove2pctClusterLen48, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctFreq96", doc::getVltBigmove2pctFreq96, () -> VltBigmove2pctFreq96Calc.calculate(series, index), doc::setVltBigmove2pctFreq96, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctAge96", doc::getVltBigmove2pctAge96, () -> VltBigmove2pctAge96Calc.calculate(series, index), doc::setVltBigmove2pctAge96, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctClusterLen96", doc::getVltBigmove2pctClusterLen96, () -> VltBigmove2pctClusterLen96Calc.calculate(series, index), doc::setVltBigmove2pctClusterLen96, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctFreq288", doc::getVltBigmove2pctFreq288, () -> VltBigmove2pctFreq288Calc.calculate(series, index), doc::setVltBigmove2pctFreq288, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctAge288", doc::getVltBigmove2pctAge288, () -> VltBigmove2pctAge288Calc.calculate(series, index), doc::setVltBigmove2pctAge288, tasks); // fora da prod mask
        // ifNull("vltBigmove2pctClusterLen288", doc::getVltBigmove2pctClusterLen288, () -> VltBigmove2pctClusterLen288Calc.calculate(series, index), doc::setVltBigmove2pctClusterLen288, tasks); // fora da prod mask

        // =========================================================================
        // BOLLINGER WIDTH
        // =========================================================================
        var bw20 = bollingerCache.getWidth(symbol, interval, series, 20);
        // var bw48 = bollingerCache.getWidth(symbol, interval, series, 48); // fora da prod mask
        // var bw96 = bollingerCache.getWidth(symbol, interval, series, 96); // fora da prod mask
        // var bw288 = bollingerCache.getWidth(symbol, interval, series, 288); // fora da prod mask
        // ifNull("vltBoll20Width", doc::getVltBoll20Width, () -> VltBoll20WidthCalc.calculate(bw20, index), doc::setVltBoll20Width, tasks); // fora da prod mask
        ifNull("vltBoll20WidthChg", doc::getVltBoll20WidthChg, () -> VltBoll20WidthChgCalc.calculate(bw20, index), doc::setVltBoll20WidthChg, tasks);
        // ifNull("vltBoll20WidthZsc", doc::getVltBoll20WidthZsc, () -> VltBoll20WidthZscCalc.calculate(bw20, index), doc::setVltBoll20WidthZsc, tasks); // fora da prod mask
        // ifNull("vltBoll20WidthAtrn", doc::getVltBoll20WidthAtrn, () -> VltBoll20WidthAtrnCalc.calculate(bw20, atr14, index), doc::setVltBoll20WidthAtrn, tasks); // fora da prod mask
        // ifNull("vltBoll48Width", doc::getVltBoll48Width, () -> VltBoll48WidthCalc.calculate(bw48, index), doc::setVltBoll48Width, tasks); // fora da prod mask
        // ifNull("vltBoll48WidthChg", doc::getVltBoll48WidthChg, () -> VltBoll48WidthChgCalc.calculate(bw48, index), doc::setVltBoll48WidthChg, tasks); // fora da prod mask
        // ifNull("vltBoll48WidthZsc", doc::getVltBoll48WidthZsc, () -> VltBoll48WidthZscCalc.calculate(bw48, index), doc::setVltBoll48WidthZsc, tasks); // fora da prod mask
        // ifNull("vltBoll48WidthAtrn", doc::getVltBoll48WidthAtrn, () -> VltBoll48WidthAtrnCalc.calculate(bw48, atr14, index), doc::setVltBoll48WidthAtrn, tasks); // fora da prod mask
        // ifNull("vltBoll96Width", doc::getVltBoll96Width, () -> VltBoll96WidthCalc.calculate(bw96, index), doc::setVltBoll96Width, tasks); // fora da prod mask
        // ifNull("vltBoll96WidthChg", doc::getVltBoll96WidthChg, () -> VltBoll96WidthChgCalc.calculate(bw96, index), doc::setVltBoll96WidthChg, tasks); // fora da prod mask
        // ifNull("vltBoll96WidthZsc", doc::getVltBoll96WidthZsc, () -> VltBoll96WidthZscCalc.calculate(bw96, index), doc::setVltBoll96WidthZsc, tasks); // fora da prod mask
        // ifNull("vltBoll96WidthAtrn", doc::getVltBoll96WidthAtrn, () -> VltBoll96WidthAtrnCalc.calculate(bw96, atr14, index), doc::setVltBoll96WidthAtrn, tasks); // fora da prod mask
        // ifNull("vltBoll288Width", doc::getVltBoll288Width, () -> VltBoll288WidthCalc.calculate(bw288, index), doc::setVltBoll288Width, tasks); // fora da prod mask
        // ifNull("vltBoll288WidthChg", doc::getVltBoll288WidthChg, () -> VltBoll288WidthChgCalc.calculate(bw288, index), doc::setVltBoll288WidthChg, tasks); // fora da prod mask
        // ifNull("vltBoll288WidthZsc", doc::getVltBoll288WidthZsc, () -> VltBoll288WidthZscCalc.calculate(bw288, index), doc::setVltBoll288WidthZsc, tasks); // fora da prod mask
        // ifNull("vltBoll288WidthAtrn", doc::getVltBoll288WidthAtrn, () -> VltBoll288WidthAtrnCalc.calculate(bw288, atr14, index), doc::setVltBoll288WidthAtrn, tasks); // fora da prod mask

        // =========================================================================
        // EWMA VOL
        // =========================================================================
        // ifNull("vltEwmaVol20", doc::getVltEwmaVol20, () -> VltEwmaVol20Calc.calculate(series, index), doc::setVltEwmaVol20, tasks); // fora da prod mask
        // ifNull("vltEwmaVol32", doc::getVltEwmaVol32, () -> VltEwmaVol32Calc.calculate(series, index), doc::setVltEwmaVol32, tasks); // fora da prod mask
        // ifNull("vltEwmaVol48", doc::getVltEwmaVol48, () -> VltEwmaVol48Calc.calculate(series, index), doc::setVltEwmaVol48, tasks); // fora da prod mask
        // ifNull("vltEwmaVol96", doc::getVltEwmaVol96, () -> VltEwmaVol96Calc.calculate(series, index), doc::setVltEwmaVol96, tasks); // fora da prod mask
        // ifNull("vltEwmaVol288", doc::getVltEwmaVol288, () -> VltEwmaVol288Calc.calculate(series, index), doc::setVltEwmaVol288, tasks); // fora da prod mask
        // ifNull("vltEwmaVol20Zsc", doc::getVltEwmaVol20Zsc, () -> VltEwmaVol20ZscCalc.calculate(series, index), doc::setVltEwmaVol20Zsc, tasks); // fora da prod mask
        // ifNull("vltEwmaVol32Zsc", doc::getVltEwmaVol32Zsc, () -> VltEwmaVol32ZscCalc.calculate(series, index), doc::setVltEwmaVol32Zsc, tasks); // fora da prod mask
        // ifNull("vltEwmaVol48Zsc", doc::getVltEwmaVol48Zsc, () -> VltEwmaVol48ZscCalc.calculate(series, index), doc::setVltEwmaVol48Zsc, tasks); // fora da prod mask
        // ifNull("vltEwmaVol96Zsc", doc::getVltEwmaVol96Zsc, () -> VltEwmaVol96ZscCalc.calculate(series, index), doc::setVltEwmaVol96Zsc, tasks); // fora da prod mask
        // ifNull("vltEwmaVol288Zsc", doc::getVltEwmaVol288Zsc, () -> VltEwmaVol288ZscCalc.calculate(series, index), doc::setVltEwmaVol288Zsc, tasks); // fora da prod mask
        ifNull("vltEwmaVol20Slp", doc::getVltEwmaVol20Slp, () -> VltEwmaVol20SlpCalc.calculate(series, index), doc::setVltEwmaVol20Slp, tasks);
        ifNull("vltEwmaVol32Slp", doc::getVltEwmaVol32Slp, () -> VltEwmaVol32SlpCalc.calculate(series, index), doc::setVltEwmaVol32Slp, tasks);
        // ifNull("vltEwmaVol48Slp", doc::getVltEwmaVol48Slp, () -> VltEwmaVol48SlpCalc.calculate(series, index), doc::setVltEwmaVol48Slp, tasks); // fora da prod mask
        // ifNull("vltEwmaVol96Slp", doc::getVltEwmaVol96Slp, () -> VltEwmaVol96SlpCalc.calculate(series, index), doc::setVltEwmaVol96Slp, tasks); // fora da prod mask
        // ifNull("vltEwmaVol288Slp", doc::getVltEwmaVol288Slp, () -> VltEwmaVol288SlpCalc.calculate(series, index), doc::setVltEwmaVol288Slp, tasks); // fora da prod mask
        ifNull("vltEwmaVol20_48Ratio", doc::getVltEwmaVol2048Ratio, () -> VltEwmaVol20_48RatioCalc.calculate(series, index), doc::setVltEwmaVol2048Ratio, tasks);
        ifNull("vltEwmaVol32_48Ratio", doc::getVltEwmaVol3248Ratio, () -> VltEwmaVol32_48RatioCalc.calculate(series, index), doc::setVltEwmaVol3248Ratio, tasks);
        // ifNull("vltEwmaVol48_288Ratio", doc::getVltEwmaVol48288Ratio, () -> VltEwmaVol48_288RatioCalc.calculate(series, index), doc::setVltEwmaVol48288Ratio, tasks); // fora da prod mask
        // ifNull("vltEwmaVol96_288Ratio", doc::getVltEwmaVol96288Ratio, () -> VltEwmaVol96_288RatioCalc.calculate(series, index), doc::setVltEwmaVol96288Ratio, tasks); // fora da prod mask

        // =========================================================================
        // KELTNER WIDTH
        // =========================================================================
        // var kU20 = keltnerCache.getUpper(symbol, interval, series, 20, 14); // fora da prod mask
        // var kL20 = keltnerCache.getLower(symbol, interval, series, 20, 14); // fora da prod mask
        // var kM20 = keltnerCache.getMiddle(symbol, interval, series, 20); // fora da prod mask
        // var kU48 = keltnerCache.getUpper(symbol, interval, series, 48, 48); // fora da prod mask
        // var kL48 = keltnerCache.getLower(symbol, interval, series, 48, 48); // fora da prod mask
        // var kM48 = keltnerCache.getMiddle(symbol, interval, series, 48); // fora da prod mask
        // var kU96 = keltnerCache.getUpper(symbol, interval, series, 96, 96); // fora da prod mask
        // var kL96 = keltnerCache.getLower(symbol, interval, series, 96, 96); // fora da prod mask
        // var kM96 = keltnerCache.getMiddle(symbol, interval, series, 96); // fora da prod mask
        // var kU288 = keltnerCache.getUpper(symbol, interval, series, 288, 96); // fora da prod mask
        // var kL288 = keltnerCache.getLower(symbol, interval, series, 288, 96); // fora da prod mask
        // var kM288 = keltnerCache.getMiddle(symbol, interval, series, 288); // fora da prod mask

        // ifNull("vltKelt20Width", doc::getVltKelt20Width, () -> VltKelt20WidthCalc.calculate(atr14, index), doc::setVltKelt20Width, tasks); // fora da prod mask
        // ifNull("vltKelt48Width", doc::getVltKelt48Width, () -> VltKelt48WidthCalc.calculate(atr48, index), doc::setVltKelt48Width, tasks); // fora da prod mask
        // ifNull("vltKelt96Width", doc::getVltKelt96Width, () -> VltKelt96WidthCalc.calculate(atr96, index), doc::setVltKelt96Width, tasks); // fora da prod mask
        // ifNull("vltKelt288Width", doc::getVltKelt288Width, () -> VltKelt288WidthCalc.calculate(atr288, index), doc::setVltKelt288Width, tasks); // fora da prod mask

        // =========================================================================
        // SQUEEZE BB/KELT
        // =========================================================================
        // ifNull("vltVolSqzBbKelt", doc::getVltVolSqzBbKelt, () -> VltVolSqzBbKeltCalc.calculate(bw20, atr14, index), doc::setVltVolSqzBbKelt, tasks); // fora da prod mask
        ifNull("vltVolSqzBbKeltChg", doc::getVltVolSqzBbKeltChg, () -> VltVolSqzBbKeltChgCalc.calculate(bw20, atr14, index), doc::setVltVolSqzBbKeltChg, tasks);
        // ifNull("vltVolSqzBbKelt20", doc::getVltVolSqzBbKelt20, () -> VltVolSqzBbKelt20Calc.calculate(bw20, atr14, index), doc::setVltVolSqzBbKelt20, tasks); // fora da prod mask
        ifNull("vltVolSqzBbKelt20Chg", doc::getVltVolSqzBbKelt20Chg, () -> VltVolSqzBbKelt20ChgCalc.calculate(bw20, atr14, index), doc::setVltVolSqzBbKelt20Chg, tasks);
        // ifNull("vltVolSqzBbKelt20Zsc", doc::getVltVolSqzBbKelt20Zsc, () -> VltVolSqzBbKelt20ZscCalc.calculate(bw20, atr14, index), doc::setVltVolSqzBbKelt20Zsc, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt48", doc::getVltVolSqzBbKelt48, () -> VltVolSqzBbKelt48Calc.calculate(bw48, atr48, index), doc::setVltVolSqzBbKelt48, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt48Chg", doc::getVltVolSqzBbKelt48Chg, () -> VltVolSqzBbKelt48ChgCalc.calculate(bw48, atr48, index), doc::setVltVolSqzBbKelt48Chg, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt48Zsc", doc::getVltVolSqzBbKelt48Zsc, () -> VltVolSqzBbKelt48ZscCalc.calculate(bw48, atr48, index), doc::setVltVolSqzBbKelt48Zsc, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt96", doc::getVltVolSqzBbKelt96, () -> VltVolSqzBbKelt96Calc.calculate(bw96, atr96, index), doc::setVltVolSqzBbKelt96, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt96Chg", doc::getVltVolSqzBbKelt96Chg, () -> VltVolSqzBbKelt96ChgCalc.calculate(bw96, atr96, index), doc::setVltVolSqzBbKelt96Chg, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt96Zsc", doc::getVltVolSqzBbKelt96Zsc, () -> VltVolSqzBbKelt96ZscCalc.calculate(bw96, atr96, index), doc::setVltVolSqzBbKelt96Zsc, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt288", doc::getVltVolSqzBbKelt288, () -> VltVolSqzBbKelt288Calc.calculate(bw288, atr288, index), doc::setVltVolSqzBbKelt288, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt288Chg", doc::getVltVolSqzBbKelt288Chg, () -> VltVolSqzBbKelt288ChgCalc.calculate(bw288, atr288, index), doc::setVltVolSqzBbKelt288Chg, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt288Zsc", doc::getVltVolSqzBbKelt288Zsc, () -> VltVolSqzBbKelt288ZscCalc.calculate(bw288, atr288, index), doc::setVltVolSqzBbKelt288Zsc, tasks); // fora da prod mask

        // =========================================================================
        // MEAN ABS RETURN
        // =========================================================================
        // ifNull("vltRetAbsMean16", doc::getVltRetAbsMean16, () -> VltRetAbsMean16Calc.calculate(series, index), doc::setVltRetAbsMean16, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean32", doc::getVltRetAbsMean32, () -> VltRetAbsMean32Calc.calculate(series, index), doc::setVltRetAbsMean32, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean48", doc::getVltRetAbsMean48, () -> VltRetAbsMean48Calc.calculate(series, index), doc::setVltRetAbsMean48, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean96", doc::getVltRetAbsMean96, () -> VltRetAbsMean96Calc.calculate(series, index), doc::setVltRetAbsMean96, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean288", doc::getVltRetAbsMean288, () -> VltRetAbsMean288Calc.calculate(series, index), doc::setVltRetAbsMean288, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean16_32Ratio", doc::getVltRetAbsMean1632Ratio, () -> VltRetAbsMean16_32RatioCalc.calculate(series, index), doc::setVltRetAbsMean1632Ratio, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean16_48Ratio", doc::getVltRetAbsMean1648Ratio, () -> VltRetAbsMean16_48RatioCalc.calculate(series, index), doc::setVltRetAbsMean1648Ratio, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean32_48Ratio", doc::getVltRetAbsMean3248Ratio, () -> VltRetAbsMean32_48RatioCalc.calculate(series, index), doc::setVltRetAbsMean3248Ratio, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean48_288Ratio", doc::getVltRetAbsMean48288Ratio, () -> VltRetAbsMean48_288RatioCalc.calculate(series, index), doc::setVltRetAbsMean48288Ratio, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean96_288Ratio", doc::getVltRetAbsMean96288Ratio, () -> VltRetAbsMean96_288RatioCalc.calculate(series, index), doc::setVltRetAbsMean96288Ratio, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean16Zsc", doc::getVltRetAbsMean16Zsc, () -> VltRetAbsMean16ZscCalc.calculate(series, index), doc::setVltRetAbsMean16Zsc, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean32Zsc", doc::getVltRetAbsMean32Zsc, () -> VltRetAbsMean32ZscCalc.calculate(series, index), doc::setVltRetAbsMean32Zsc, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean48Zsc", doc::getVltRetAbsMean48Zsc, () -> VltRetAbsMean48ZscCalc.calculate(series, index), doc::setVltRetAbsMean48Zsc, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean96Zsc", doc::getVltRetAbsMean96Zsc, () -> VltRetAbsMean96ZscCalc.calculate(series, index), doc::setVltRetAbsMean96Zsc, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean288Zsc", doc::getVltRetAbsMean288Zsc, () -> VltRetAbsMean288ZscCalc.calculate(series, index), doc::setVltRetAbsMean288Zsc, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean16Slp", doc::getVltRetAbsMean16Slp, () -> VltRetAbsMean16SlpCalc.calculate(series, index), doc::setVltRetAbsMean16Slp, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean32Slp", doc::getVltRetAbsMean32Slp, () -> VltRetAbsMean32SlpCalc.calculate(series, index), doc::setVltRetAbsMean32Slp, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean48Slp", doc::getVltRetAbsMean48Slp, () -> VltRetAbsMean48SlpCalc.calculate(series, index), doc::setVltRetAbsMean48Slp, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean96Slp", doc::getVltRetAbsMean96Slp, () -> VltRetAbsMean96SlpCalc.calculate(series, index), doc::setVltRetAbsMean96Slp, tasks); // fora da prod mask
        // ifNull("vltRetAbsMean288Slp", doc::getVltRetAbsMean288Slp, () -> VltRetAbsMean288SlpCalc.calculate(series, index), doc::setVltRetAbsMean288Slp, tasks); // fora da prod mask

        // =========================================================================
        // RANGE VOL (GK, Parkinson, Rogers-Satchell)
        // =========================================================================
        // ifNull("vltVolGk16", doc::getVltVolGk16, () -> VltVolGk16Calc.calculate(series, index), doc::setVltVolGk16, tasks); // fora da prod mask
        // ifNull("vltVolGk32", doc::getVltVolGk32, () -> VltVolGk32Calc.calculate(series, index), doc::setVltVolGk32, tasks); // fora da prod mask
        // ifNull("vltVolGk48", doc::getVltVolGk48, () -> VltVolGk48Calc.calculate(series, index), doc::setVltVolGk48, tasks); // fora da prod mask
        // ifNull("vltVolGk96", doc::getVltVolGk96, () -> VltVolGk96Calc.calculate(series, index), doc::setVltVolGk96, tasks); // fora da prod mask
        // ifNull("vltVolGk288", doc::getVltVolGk288, () -> VltVolGk288Calc.calculate(series, index), doc::setVltVolGk288, tasks); // fora da prod mask
        // ifNull("vltVolPark16", doc::getVltVolPark16, () -> VltVolPark16Calc.calculate(series, index), doc::setVltVolPark16, tasks); // fora da prod mask
        // ifNull("vltVolPark32", doc::getVltVolPark32, () -> VltVolPark32Calc.calculate(series, index), doc::setVltVolPark32, tasks); // fora da prod mask
        // ifNull("vltVolPark48", doc::getVltVolPark48, () -> VltVolPark48Calc.calculate(series, index), doc::setVltVolPark48, tasks); // fora da prod mask
        // ifNull("vltVolPark96", doc::getVltVolPark96, () -> VltVolPark96Calc.calculate(series, index), doc::setVltVolPark96, tasks); // fora da prod mask
        // ifNull("vltVolPark288", doc::getVltVolPark288, () -> VltVolPark288Calc.calculate(series, index), doc::setVltVolPark288, tasks); // fora da prod mask
        // ifNull("vltVolRs16", doc::getVltVolRs16, () -> VltVolRs16Calc.calculate(series, index), doc::setVltVolRs16, tasks); // fora da prod mask
        // ifNull("vltVolRs32", doc::getVltVolRs32, () -> VltVolRs32Calc.calculate(series, index), doc::setVltVolRs32, tasks); // fora da prod mask
        // ifNull("vltVolRs48", doc::getVltVolRs48, () -> VltVolRs48Calc.calculate(series, index), doc::setVltVolRs48, tasks); // fora da prod mask
        // ifNull("vltVolRs96", doc::getVltVolRs96, () -> VltVolRs96Calc.calculate(series, index), doc::setVltVolRs96, tasks); // fora da prod mask
        // ifNull("vltVolRs288", doc::getVltVolRs288, () -> VltVolRs288Calc.calculate(series, index), doc::setVltVolRs288, tasks); // fora da prod mask
        // ifNull("vltVolGk16Zsc", doc::getVltVolGk16Zsc, () -> VltVolGk16ZscCalc.calculate(series, index), doc::setVltVolGk16Zsc, tasks); // fora da prod mask
        // ifNull("vltVolGk32Zsc", doc::getVltVolGk32Zsc, () -> VltVolGk32ZscCalc.calculate(series, index), doc::setVltVolGk32Zsc, tasks); // fora da prod mask
        // ifNull("vltVolGk48Zsc", doc::getVltVolGk48Zsc, () -> VltVolGk48ZscCalc.calculate(series, index), doc::setVltVolGk48Zsc, tasks); // fora da prod mask
        // ifNull("vltVolGk96Zsc", doc::getVltVolGk96Zsc, () -> VltVolGk96ZscCalc.calculate(series, index), doc::setVltVolGk96Zsc, tasks); // fora da prod mask
        // ifNull("vltVolGk288Zsc", doc::getVltVolGk288Zsc, () -> VltVolGk288ZscCalc.calculate(series, index), doc::setVltVolGk288Zsc, tasks); // fora da prod mask
        // ifNull("vltVolPark16Zsc", doc::getVltVolPark16Zsc, () -> VltVolPark16ZscCalc.calculate(series, index), doc::setVltVolPark16Zsc, tasks); // fora da prod mask
        // ifNull("vltVolPark32Zsc", doc::getVltVolPark32Zsc, () -> VltVolPark32ZscCalc.calculate(series, index), doc::setVltVolPark32Zsc, tasks); // fora da prod mask
        // ifNull("vltVolPark48Zsc", doc::getVltVolPark48Zsc, () -> VltVolPark48ZscCalc.calculate(series, index), doc::setVltVolPark48Zsc, tasks); // fora da prod mask
        // ifNull("vltVolPark96Zsc", doc::getVltVolPark96Zsc, () -> VltVolPark96ZscCalc.calculate(series, index), doc::setVltVolPark96Zsc, tasks); // fora da prod mask
        // ifNull("vltVolPark288Zsc", doc::getVltVolPark288Zsc, () -> VltVolPark288ZscCalc.calculate(series, index), doc::setVltVolPark288Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRs16Zsc", doc::getVltVolRs16Zsc, () -> VltVolRs16ZscCalc.calculate(series, index), doc::setVltVolRs16Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRs32Zsc", doc::getVltVolRs32Zsc, () -> VltVolRs32ZscCalc.calculate(series, index), doc::setVltVolRs32Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRs48Zsc", doc::getVltVolRs48Zsc, () -> VltVolRs48ZscCalc.calculate(series, index), doc::setVltVolRs48Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRs96Zsc", doc::getVltVolRs96Zsc, () -> VltVolRs96ZscCalc.calculate(series, index), doc::setVltVolRs96Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRs288Zsc", doc::getVltVolRs288Zsc, () -> VltVolRs288ZscCalc.calculate(series, index), doc::setVltVolRs288Zsc, tasks); // fora da prod mask
        ifNull("vltVolGk16Slp", doc::getVltVolGk16Slp, () -> VltVolGk16SlpCalc.calculate(series, index), doc::setVltVolGk16Slp, tasks);
        ifNull("vltVolGk32Slp", doc::getVltVolGk32Slp, () -> VltVolGk32SlpCalc.calculate(series, index), doc::setVltVolGk32Slp, tasks);
        // ifNull("vltVolGk48Slp", doc::getVltVolGk48Slp, () -> VltVolGk48SlpCalc.calculate(series, index), doc::setVltVolGk48Slp, tasks); // fora da prod mask
        // ifNull("vltVolGk96Slp", doc::getVltVolGk96Slp, () -> VltVolGk96SlpCalc.calculate(series, index), doc::setVltVolGk96Slp, tasks); // fora da prod mask
        // ifNull("vltVolGk288Slp", doc::getVltVolGk288Slp, () -> VltVolGk288SlpCalc.calculate(series, index), doc::setVltVolGk288Slp, tasks); // fora da prod mask
        ifNull("vltVolPark16Slp", doc::getVltVolPark16Slp, () -> VltVolPark16SlpCalc.calculate(series, index), doc::setVltVolPark16Slp, tasks);
        ifNull("vltVolPark32Slp", doc::getVltVolPark32Slp, () -> VltVolPark32SlpCalc.calculate(series, index), doc::setVltVolPark32Slp, tasks);
        // ifNull("vltVolPark48Slp", doc::getVltVolPark48Slp, () -> VltVolPark48SlpCalc.calculate(series, index), doc::setVltVolPark48Slp, tasks); // fora da prod mask
        // ifNull("vltVolPark96Slp", doc::getVltVolPark96Slp, () -> VltVolPark96SlpCalc.calculate(series, index), doc::setVltVolPark96Slp, tasks); // fora da prod mask
        // ifNull("vltVolPark288Slp", doc::getVltVolPark288Slp, () -> VltVolPark288SlpCalc.calculate(series, index), doc::setVltVolPark288Slp, tasks); // fora da prod mask
        ifNull("vltVolRs16Slp", doc::getVltVolRs16Slp, () -> VltVolRs16SlpCalc.calculate(series, index), doc::setVltVolRs16Slp, tasks);
        ifNull("vltVolRs32Slp", doc::getVltVolRs32Slp, () -> VltVolRs32SlpCalc.calculate(series, index), doc::setVltVolRs32Slp, tasks);
        // ifNull("vltVolRs48Slp", doc::getVltVolRs48Slp, () -> VltVolRs48SlpCalc.calculate(series, index), doc::setVltVolRs48Slp, tasks); // fora da prod mask
        // ifNull("vltVolRs96Slp", doc::getVltVolRs96Slp, () -> VltVolRs96SlpCalc.calculate(series, index), doc::setVltVolRs96Slp, tasks); // fora da prod mask
        // ifNull("vltVolRs288Slp", doc::getVltVolRs288Slp, () -> VltVolRs288SlpCalc.calculate(series, index), doc::setVltVolRs288Slp, tasks); // fora da prod mask
        ifNull("vltVolGk16_48Ratio", doc::getVltVolGk1648Ratio, () -> VltVolGk16_48RatioCalc.calculate(series, index), doc::setVltVolGk1648Ratio, tasks);
        ifNull("vltVolGk32_48Ratio", doc::getVltVolGk3248Ratio, () -> VltVolGk32_48RatioCalc.calculate(series, index), doc::setVltVolGk3248Ratio, tasks);
        // ifNull("vltVolGk48_288Ratio", doc::getVltVolGk48288Ratio, () -> VltVolGk48_288RatioCalc.calculate(series, index), doc::setVltVolGk48288Ratio, tasks); // fora da prod mask
        ifNull("vltVolPark16_48Ratio", doc::getVltVolPark1648Ratio, () -> VltVolPark16_48RatioCalc.calculate(series, index), doc::setVltVolPark1648Ratio, tasks);
        ifNull("vltVolPark32_48Ratio", doc::getVltVolPark3248Ratio, () -> VltVolPark32_48RatioCalc.calculate(series, index), doc::setVltVolPark3248Ratio, tasks);
        // ifNull("vltVolPark48_288Ratio", doc::getVltVolPark48288Ratio, () -> VltVolPark48_288RatioCalc.calculate(series, index), doc::setVltVolPark48288Ratio, tasks); // fora da prod mask
        ifNull("vltVolRs16_48Ratio", doc::getVltVolRs1648Ratio, () -> VltVolRs16_48RatioCalc.calculate(series, index), doc::setVltVolRs1648Ratio, tasks);
        ifNull("vltVolRs32_48Ratio", doc::getVltVolRs3248Ratio, () -> VltVolRs32_48RatioCalc.calculate(series, index), doc::setVltVolRs3248Ratio, tasks);
        // ifNull("vltVolRs48_288Ratio", doc::getVltVolRs48288Ratio, () -> VltVolRs48_288RatioCalc.calculate(series, index), doc::setVltVolRs48288Ratio, tasks); // fora da prod mask
        // ifNull("vltVolGk16_96Ratio", doc::getVltVolGk1696Ratio, () -> VltVolGk16_96RatioCalc.calculate(series, index), doc::setVltVolGk1696Ratio, tasks); // fora da prod mask
        // ifNull("vltVolPark16_96Ratio", doc::getVltVolPark1696Ratio, () -> VltVolPark16_96RatioCalc.calculate(series, index), doc::setVltVolPark1696Ratio, tasks); // fora da prod mask
        // ifNull("vltVolRs16_96Ratio", doc::getVltVolRs1696Ratio, () -> VltVolRs16_96RatioCalc.calculate(series, index), doc::setVltVolRs1696Ratio, tasks); // fora da prod mask

        // =========================================================================
        // REALIZED VOL
        // =========================================================================
        var rv10 = realizedVolCache.getRv(symbol, interval, series, 10);
        var rv30 = realizedVolCache.getRv(symbol, interval, series, 30);
        // var rv50 = realizedVolCache.getRv(symbol, interval, series, 50); // fora da prod mask
        var rv48 = realizedVolCache.getRv(symbol, interval, series, 48);
        // var rv96 = realizedVolCache.getRv(symbol, interval, series, 96); // fora da prod mask
        // var rv288 = realizedVolCache.getRv(symbol, interval, series, 288); // fora da prod mask
        // ifNull("vltVolRv10", doc::getVltVolRv10, () -> VltVolRv10Calc.calculate(rv10, index), doc::setVltVolRv10, tasks); // fora da prod mask
        // ifNull("vltVolRv30", doc::getVltVolRv30, () -> VltVolRv30Calc.calculate(rv30, index), doc::setVltVolRv30, tasks); // fora da prod mask
        // ifNull("vltVolRv50", doc::getVltVolRv50, () -> VltVolRv50Calc.calculate(rv50, index), doc::setVltVolRv50, tasks); // fora da prod mask
        // ifNull("vltVolRv48", doc::getVltVolRv48, () -> VltVolRv48Calc.calculate(rv48, index), doc::setVltVolRv48, tasks); // fora da prod mask
        // ifNull("vltVolRv96", doc::getVltVolRv96, () -> VltVolRv96Calc.calculate(rv96, index), doc::setVltVolRv96, tasks); // fora da prod mask
        // ifNull("vltVolRv288", doc::getVltVolRv288, () -> VltVolRv288Calc.calculate(rv288, index), doc::setVltVolRv288, tasks); // fora da prod mask
        // ifNull("vltVolRv10Zsc", doc::getVltVolRv10Zsc, () -> VltVolRv10ZscCalc.calculate(rv10, index), doc::setVltVolRv10Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRv30Zsc", doc::getVltVolRv30Zsc, () -> VltVolRv30ZscCalc.calculate(rv30, index), doc::setVltVolRv30Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRv50Zsc", doc::getVltVolRv50Zsc, () -> VltVolRv50ZscCalc.calculate(rv50, index), doc::setVltVolRv50Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRv48Zsc", doc::getVltVolRv48Zsc, () -> VltVolRv48ZscCalc.calculate(rv48, index), doc::setVltVolRv48Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRv96Zsc", doc::getVltVolRv96Zsc, () -> VltVolRv96ZscCalc.calculate(rv96, index), doc::setVltVolRv96Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRv288Zsc", doc::getVltVolRv288Zsc, () -> VltVolRv288ZscCalc.calculate(rv288, index), doc::setVltVolRv288Zsc, tasks); // fora da prod mask
        // ifNull("vltVolRv10PctileW80", doc::getVltVolRv10PctileW80, () -> VltVolRv10PctileW80Calc.calculate(rv10, index), doc::setVltVolRv10PctileW80, tasks); // fora da prod mask
        // ifNull("vltVolRv30PctileW80", doc::getVltVolRv30PctileW80, () -> VltVolRv30PctileW80Calc.calculate(rv30, index), doc::setVltVolRv30PctileW80, tasks); // fora da prod mask
        // ifNull("vltVolRv50PctileW80", doc::getVltVolRv50PctileW80, () -> VltVolRv50PctileW80Calc.calculate(rv50, index), doc::setVltVolRv50PctileW80, tasks); // fora da prod mask
        // ifNull("vltVolRv48PctileW80", doc::getVltVolRv48PctileW80, () -> VltVolRv48PctileW80Calc.calculate(rv48, index), doc::setVltVolRv48PctileW80, tasks); // fora da prod mask
        // ifNull("vltVolRv96PctileW80", doc::getVltVolRv96PctileW80, () -> VltVolRv96PctileW80Calc.calculate(rv96, index), doc::setVltVolRv96PctileW80, tasks); // fora da prod mask
        // ifNull("vltVolRv288PctileW80", doc::getVltVolRv288PctileW80, () -> VltVolRv288PctileW80Calc.calculate(rv288, index), doc::setVltVolRv288PctileW80, tasks); // fora da prod mask
        // ifNull("vltVolRv10_50Ratio", doc::getVltVolRv1050Ratio, () -> VltVolRv10_50RatioCalc.calculate(rv10, rv50, index), doc::setVltVolRv1050Ratio, tasks); // fora da prod mask
        ifNull("vltVolRv10_30Ratio", doc::getVltVolRv1030Ratio, () -> VltVolRv10_30RatioCalc.calculate(rv10, rv30, index), doc::setVltVolRv1030Ratio, tasks);
        ifNull("vltVolRv10_48Ratio", doc::getVltVolRv1048Ratio, () -> VltVolRv10_48RatioCalc.calculate(rv10, rv48, index), doc::setVltVolRv1048Ratio, tasks);
        // ifNull("vltVolRv10_96Ratio", doc::getVltVolRv1096Ratio, () -> VltVolRv10_96RatioCalc.calculate(rv10, rv96, index), doc::setVltVolRv1096Ratio, tasks); // fora da prod mask
        // ifNull("vltVolRv10_288Ratio", doc::getVltVolRv10288Ratio, () -> VltVolRv10_288RatioCalc.calculate(rv10, rv288, index), doc::setVltVolRv10288Ratio, tasks); // fora da prod mask
        // ifNull("vltVolRv30_48Ratio", doc::getVltVolRv3048Ratio, () -> VltVolRv30_48RatioCalc.calculate(rv30, rv48, index), doc::setVltVolRv3048Ratio, tasks); // fora da prod mask
        // ifNull("vltVolRv30_96Ratio", doc::getVltVolRv3096Ratio, () -> VltVolRv30_96RatioCalc.calculate(rv30, rv96, index), doc::setVltVolRv3096Ratio, tasks); // fora da prod mask
        // ifNull("vltVolRv30_288Ratio", doc::getVltVolRv30288Ratio, () -> VltVolRv30_288RatioCalc.calculate(rv30, rv288, index), doc::setVltVolRv30288Ratio, tasks); // fora da prod mask
        // ifNull("vltVolRv48_288Ratio", doc::getVltVolRv48288Ratio, () -> VltVolRv48_288RatioCalc.calculate(rv48, rv288, index), doc::setVltVolRv48288Ratio, tasks); // fora da prod mask
        // ifNull("vltVolRv96_288Ratio", doc::getVltVolRv96288Ratio, () -> VltVolRv96_288RatioCalc.calculate(rv96, rv288, index), doc::setVltVolRv96288Ratio, tasks); // fora da prod mask
        ifNull("vltVolRv10Slp", doc::getVltVolRv10Slp, () -> VltVolRv10SlpCalc.calculate(rv10, index), doc::setVltVolRv10Slp, tasks);
        ifNull("vltVolRv30Slp", doc::getVltVolRv30Slp, () -> VltVolRv30SlpCalc.calculate(rv30, index), doc::setVltVolRv30Slp, tasks);
        // ifNull("vltVolRv50Slp", doc::getVltVolRv50Slp, () -> VltVolRv50SlpCalc.calculate(rv50, index), doc::setVltVolRv50Slp, tasks); // fora da prod mask
        // ifNull("vltVolRv48Slp", doc::getVltVolRv48Slp, () -> VltVolRv48SlpCalc.calculate(rv48, index), doc::setVltVolRv48Slp, tasks); // fora da prod mask
        // ifNull("vltVolRv96Slp", doc::getVltVolRv96Slp, () -> VltVolRv96SlpCalc.calculate(rv96, index), doc::setVltVolRv96Slp, tasks); // fora da prod mask
        // ifNull("vltVolRv288Slp", doc::getVltVolRv288Slp, () -> VltVolRv288SlpCalc.calculate(rv288, index), doc::setVltVolRv288Slp, tasks); // fora da prod mask
        // ifNull("vltVolRv30VvW20", doc::getVltVolRv30VvW20, () -> VltVolRv30VvW20Calc.calculate(rv30, index), doc::setVltVolRv30VvW20, tasks); // fora da prod mask
        // ifNull("vltVolRv48VvW20", doc::getVltVolRv48VvW20, () -> VltVolRv48VvW20Calc.calculate(rv48, index), doc::setVltVolRv48VvW20, tasks); // fora da prod mask
        // ifNull("vltVolRv96VvW20", doc::getVltVolRv96VvW20, () -> VltVolRv96VvW20Calc.calculate(rv96, index), doc::setVltVolRv96VvW20, tasks); // fora da prod mask
        // ifNull("vltVolRv288VvW20", doc::getVltVolRv288VvW20, () -> VltVolRv288VvW20Calc.calculate(rv288, index), doc::setVltVolRv288VvW20, tasks); // fora da prod mask

        // =========================================================================
        // RETURN DISTRIBUTION
        // =========================================================================
        // ifNull("vltRet10Skew", doc::getVltRet10Skew, () -> VltRet10SkewCalc.calculate(series, index), doc::setVltRet10Skew, tasks); // fora da prod mask
        // ifNull("vltRet10Kurt", doc::getVltRet10Kurt, () -> VltRet10KurtCalc.calculate(series, index), doc::setVltRet10Kurt, tasks); // fora da prod mask
        // ifNull("vltRet30Skew", doc::getVltRet30Skew, () -> VltRet30SkewCalc.calculate(series, index), doc::setVltRet30Skew, tasks); // fora da prod mask
        // ifNull("vltRet30Kurt", doc::getVltRet30Kurt, () -> VltRet30KurtCalc.calculate(series, index), doc::setVltRet30Kurt, tasks); // fora da prod mask
        // ifNull("vltRet50Skew", doc::getVltRet50Skew, () -> VltRet50SkewCalc.calculate(series, index), doc::setVltRet50Skew, tasks); // fora da prod mask
        // ifNull("vltRet50Kurt", doc::getVltRet50Kurt, () -> VltRet50KurtCalc.calculate(series, index), doc::setVltRet50Kurt, tasks); // fora da prod mask
        // ifNull("vltRet96Skew", doc::getVltRet96Skew, () -> VltRet96SkewCalc.calculate(series, index), doc::setVltRet96Skew, tasks); // fora da prod mask
        // ifNull("vltRet96Kurt", doc::getVltRet96Kurt, () -> VltRet96KurtCalc.calculate(series, index), doc::setVltRet96Kurt, tasks); // fora da prod mask
        // ifNull("vltRet288Skew", doc::getVltRet288Skew, () -> VltRet288SkewCalc.calculate(series, index), doc::setVltRet288Skew, tasks); // fora da prod mask
        // ifNull("vltRet288Kurt", doc::getVltRet288Kurt, () -> VltRet288KurtCalc.calculate(series, index), doc::setVltRet288Kurt, tasks); // fora da prod mask

        // =========================================================================
        // SEASONALITY
        // =========================================================================
        // ifNull("vltAtr14Season", doc::getVltAtr14Season, () -> VltAtr14SeasonCalc.calculate(series, atr14, index), doc::setVltAtr14Season, tasks); // fora da prod mask
        // ifNull("vltRangeSeason", doc::getVltRangeSeason, () -> VltRangeSeasonCalc.calculate(series, atr14, index), doc::setVltRangeSeason, tasks); // fora da prod mask
        // ifNull("vltAtr48Season", doc::getVltAtr48Season, () -> VltAtr48SeasonCalc.calculate(series, atr48, index), doc::setVltAtr48Season, tasks); // fora da prod mask
        // ifNull("vltAtr96Season", doc::getVltAtr96Season, () -> VltAtr96SeasonCalc.calculate(series, atr96, index), doc::setVltAtr96Season, tasks); // fora da prod mask
        // ifNull("vltAtr288Season", doc::getVltAtr288Season, () -> VltAtr288SeasonCalc.calculate(series, atr288, index), doc::setVltAtr288Season, tasks); // fora da prod mask
        // var std20ind = stdCache.getStd(symbol, interval, series, 20); // fora da prod mask
        // var std48ind = stdCache.getStd(symbol, interval, series, 48); // fora da prod mask
        // var std96ind = stdCache.getStd(symbol, interval, series, 96); // fora da prod mask
        // var std288ind = stdCache.getStd(symbol, interval, series, 288); // fora da prod mask
        // ifNull("vltStd20Season", doc::getVltStd20Season, () -> VltStd20SeasonCalc.calculate(series, std20ind, index), doc::setVltStd20Season, tasks); // fora da prod mask
        // ifNull("vltStd48Season", doc::getVltStd48Season, () -> VltStd48SeasonCalc.calculate(series, std48ind, index), doc::setVltStd48Season, tasks); // fora da prod mask
        // ifNull("vltStd96Season", doc::getVltStd96Season, () -> VltStd96SeasonCalc.calculate(series, std96ind, index), doc::setVltStd96Season, tasks); // fora da prod mask
        // ifNull("vltStd288Season", doc::getVltStd288Season, () -> VltStd288SeasonCalc.calculate(series, std288ind, index), doc::setVltStd288Season, tasks); // fora da prod mask
        // ifNull("vltBoll20WidthSeason", doc::getVltBoll20WidthSeason, () -> VltBoll20WidthSeasonCalc.calculate(series, bw20, index), doc::setVltBoll20WidthSeason, tasks); // fora da prod mask
        // ifNull("vltBoll48WidthSeason", doc::getVltBoll48WidthSeason, () -> VltBoll48WidthSeasonCalc.calculate(series, bw48, index), doc::setVltBoll48WidthSeason, tasks); // fora da prod mask
        // ifNull("vltBoll96WidthSeason", doc::getVltBoll96WidthSeason, () -> VltBoll96WidthSeasonCalc.calculate(series, bw96, index), doc::setVltBoll96WidthSeason, tasks); // fora da prod mask
        // ifNull("vltBoll288WidthSeason", doc::getVltBoll288WidthSeason, () -> VltBoll288WidthSeasonCalc.calculate(series, bw288, index), doc::setVltBoll288WidthSeason, tasks); // fora da prod mask
        // ifNull("vltVolRv30Season", doc::getVltVolRv30Season, () -> VltVolRv30SeasonCalc.calculate(series, rv30, index), doc::setVltVolRv30Season, tasks); // fora da prod mask
        // ifNull("vltVolRv48Season", doc::getVltVolRv48Season, () -> VltVolRv48SeasonCalc.calculate(series, rv48, index), doc::setVltVolRv48Season, tasks); // fora da prod mask
        // ifNull("vltVolRv96Season", doc::getVltVolRv96Season, () -> VltVolRv96SeasonCalc.calculate(series, rv96, index), doc::setVltVolRv96Season, tasks); // fora da prod mask
        // ifNull("vltVolRv288Season", doc::getVltVolRv288Season, () -> VltVolRv288SeasonCalc.calculate(series, rv288, index), doc::setVltVolRv288Season, tasks); // fora da prod mask

        // =========================================================================
        // SQUEEZE (Boll + BB/Kelt)
        // =========================================================================
        // ifNull("vltBoll20SqueezeLen", doc::getVltBoll20SqueezeLen, () -> VltBoll20SqueezeLenCalc.calculate(bw20, index), doc::setVltBoll20SqueezeLen, tasks); // fora da prod mask
        // ifNull("vltBoll20SqueezeAge", doc::getVltBoll20SqueezeAge, () -> VltBoll20SqueezeAgeCalc.calculate(bw20, index), doc::setVltBoll20SqueezeAge, tasks); // fora da prod mask
        // ifNull("vltBoll20SqueezePrstW20", doc::getVltBoll20SqueezePrstW20, () -> VltBoll20SqueezePrstW20Calc.calculate(bw20, index), doc::setVltBoll20SqueezePrstW20, tasks); // fora da prod mask
        // ifNull("vltBoll48SqueezeLen", doc::getVltBoll48SqueezeLen, () -> VltBoll48SqueezeLenCalc.calculate(bw48, index), doc::setVltBoll48SqueezeLen, tasks); // fora da prod mask
        // ifNull("vltBoll48SqueezeAge", doc::getVltBoll48SqueezeAge, () -> VltBoll48SqueezeAgeCalc.calculate(bw48, index), doc::setVltBoll48SqueezeAge, tasks); // fora da prod mask
        // ifNull("vltBoll48SqueezePrstW20", doc::getVltBoll48SqueezePrstW20, () -> VltBoll48SqueezePrstW20Calc.calculate(bw48, index), doc::setVltBoll48SqueezePrstW20, tasks); // fora da prod mask
        // ifNull("vltBoll96SqueezeLen", doc::getVltBoll96SqueezeLen, () -> VltBoll96SqueezeLenCalc.calculate(bw96, index), doc::setVltBoll96SqueezeLen, tasks); // fora da prod mask
        // ifNull("vltBoll96SqueezeAge", doc::getVltBoll96SqueezeAge, () -> VltBoll96SqueezeAgeCalc.calculate(bw96, index), doc::setVltBoll96SqueezeAge, tasks); // fora da prod mask
        // ifNull("vltBoll96SqueezePrstW20", doc::getVltBoll96SqueezePrstW20, () -> VltBoll96SqueezePrstW20Calc.calculate(bw96, index), doc::setVltBoll96SqueezePrstW20, tasks); // fora da prod mask
        // ifNull("vltBoll288SqueezeLen", doc::getVltBoll288SqueezeLen, () -> VltBoll288SqueezeLenCalc.calculate(bw288, index), doc::setVltBoll288SqueezeLen, tasks); // fora da prod mask
        // ifNull("vltBoll288SqueezeAge", doc::getVltBoll288SqueezeAge, () -> VltBoll288SqueezeAgeCalc.calculate(bw288, index), doc::setVltBoll288SqueezeAge, tasks); // fora da prod mask
        // ifNull("vltBoll288SqueezePrstW20", doc::getVltBoll288SqueezePrstW20, () -> VltBoll288SqueezePrstW20Calc.calculate(bw288, index), doc::setVltBoll288SqueezePrstW20, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt20PrstW20", doc::getVltVolSqzBbKelt20PrstW20, () -> VltVolSqzBbKelt20PrstW20Calc.calculate(bw20, atr14, index), doc::setVltVolSqzBbKelt20PrstW20, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt48PrstW20", doc::getVltVolSqzBbKelt48PrstW20, () -> VltVolSqzBbKelt48PrstW20Calc.calculate(bw48, atr48, index), doc::setVltVolSqzBbKelt48PrstW20, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt96PrstW20", doc::getVltVolSqzBbKelt96PrstW20, () -> VltVolSqzBbKelt96PrstW20Calc.calculate(bw96, atr96, index), doc::setVltVolSqzBbKelt96PrstW20, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt288PrstW20", doc::getVltVolSqzBbKelt288PrstW20, () -> VltVolSqzBbKelt288PrstW20Calc.calculate(bw288, atr288, index), doc::setVltVolSqzBbKelt288PrstW20, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt20State", doc::getVltVolSqzBbKelt20State, () -> VltVolSqzBbKelt20StateCalc.calculate(bw20, atr14, index), doc::setVltVolSqzBbKelt20State, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt48State", doc::getVltVolSqzBbKelt48State, () -> VltVolSqzBbKelt48StateCalc.calculate(bw48, atr48, index), doc::setVltVolSqzBbKelt48State, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt96State", doc::getVltVolSqzBbKelt96State, () -> VltVolSqzBbKelt96StateCalc.calculate(bw96, atr96, index), doc::setVltVolSqzBbKelt96State, tasks); // fora da prod mask
        // ifNull("vltVolSqzBbKelt288State", doc::getVltVolSqzBbKelt288State, () -> VltVolSqzBbKelt288StateCalc.calculate(bw288, atr288, index), doc::setVltVolSqzBbKelt288State, tasks); // fora da prod mask

        // =========================================================================
        // STD
        // =========================================================================
        var std14 = stdCache.getStd(symbol, interval, series, 14);
        var std20 = stdCache.getStd(symbol, interval, series, 20);
        var std50 = stdCache.getStd(symbol, interval, series, 50);
        var std48 = stdCache.getStd(symbol, interval, series, 48);
        // var std96 = stdCache.getStd(symbol, interval, series, 96); // fora da prod mask
        // var std288 = stdCache.getStd(symbol, interval, series, 288); // fora da prod mask
        var stdSlp14 = slopeCache.getSlope(symbol, interval, series, std14, "std14", 14);
        var stdSlp20 = slopeCache.getSlope(symbol, interval, series, std20, "std20", 20);
        var stdSlp50 = slopeCache.getSlope(symbol, interval, series, std50, "std50", 50);
        // var stdSlp48 = new br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator(series, std48, 48); // fora da prod mask
        // var stdSlp96 = new br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator(series, std96, 96); // fora da prod mask
        // var stdSlp288 = new br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator(series, std288, 288); // fora da prod mask

        // ifNull("vltStd14", doc::getVltStd14, () -> VltStd14Calc.calculate(std14, index), doc::setVltStd14, tasks); // fora da prod mask
        // ifNull("vltStd20", doc::getVltStd20, () -> VltStd20Calc.calculate(std20, index), doc::setVltStd20, tasks); // fora da prod mask
        // ifNull("vltStd50", doc::getVltStd50, () -> VltStd50Calc.calculate(std50, index), doc::setVltStd50, tasks); // fora da prod mask
        // ifNull("vltStd48", doc::getVltStd48, () -> VltStd48Calc.calculate(std48, index), doc::setVltStd48, tasks); // fora da prod mask
        // ifNull("vltStd96", doc::getVltStd96, () -> VltStd96Calc.calculate(std96, index), doc::setVltStd96, tasks); // fora da prod mask
        // ifNull("vltStd288", doc::getVltStd288, () -> VltStd288Calc.calculate(std288, index), doc::setVltStd288, tasks); // fora da prod mask
        ifNull("vltStd14Chg", doc::getVltStd14Chg, () -> VltStd14ChgCalc.calculate(std14, index), doc::setVltStd14Chg, tasks);
        ifNull("vltStd20Chg", doc::getVltStd20Chg, () -> VltStd20ChgCalc.calculate(std20, index), doc::setVltStd20Chg, tasks);
        ifNull("vltStd50Chg", doc::getVltStd50Chg, () -> VltStd50ChgCalc.calculate(std50, index), doc::setVltStd50Chg, tasks);
        // ifNull("vltStd48Chg", doc::getVltStd48Chg, () -> VltStd48ChgCalc.calculate(std48, index), doc::setVltStd48Chg, tasks); // fora da prod mask
        // ifNull("vltStd96Chg", doc::getVltStd96Chg, () -> VltStd96ChgCalc.calculate(std96, index), doc::setVltStd96Chg, tasks); // fora da prod mask
        // ifNull("vltStd288Chg", doc::getVltStd288Chg, () -> VltStd288ChgCalc.calculate(std288, index), doc::setVltStd288Chg, tasks); // fora da prod mask
        ifNull("vltStd14_50Ratio", doc::getVltStd1450Ratio, () -> VltStd14_50RatioCalc.calculate(std14, std50, index), doc::setVltStd1450Ratio, tasks);
        ifNull("vltStd14_50Expn", doc::getVltStd1450Expn, () -> VltStd14_50ExpnCalc.calculate(std14, std50, index), doc::setVltStd1450Expn, tasks);
        ifNull("vltStd14_50Cmpr", doc::getVltStd1450Cmpr, () -> VltStd14_50CmprCalc.calculate(std14, std50, index), doc::setVltStd1450Cmpr, tasks);
        ifNull("vltStd14_48Ratio", doc::getVltStd1448Ratio, () -> VltStd14_48RatioCalc.calculate(std14, std48, index), doc::setVltStd1448Ratio, tasks);
        ifNull("vltStd14_48Expn", doc::getVltStd1448Expn, () -> VltStd14_48ExpnCalc.calculate(std14, std48, index), doc::setVltStd1448Expn, tasks);
        ifNull("vltStd14_48Cmpr", doc::getVltStd1448Cmpr, () -> VltStd14_48CmprCalc.calculate(std14, std48, index), doc::setVltStd1448Cmpr, tasks);
        // ifNull("vltStd14_96Ratio", doc::getVltStd1496Ratio, () -> VltStd14_96RatioCalc.calculate(std14, std96, index), doc::setVltStd1496Ratio, tasks); // fora da prod mask
        // ifNull("vltStd14_96Expn", doc::getVltStd1496Expn, () -> VltStd14_96ExpnCalc.calculate(std14, std96, index), doc::setVltStd1496Expn, tasks); // fora da prod mask
        // ifNull("vltStd14_96Cmpr", doc::getVltStd1496Cmpr, () -> VltStd14_96CmprCalc.calculate(std14, std96, index), doc::setVltStd1496Cmpr, tasks); // fora da prod mask
        // ifNull("vltStd14_288Ratio", doc::getVltStd14288Ratio, () -> VltStd14_288RatioCalc.calculate(std14, std288, index), doc::setVltStd14288Ratio, tasks); // fora da prod mask
        // ifNull("vltStd14_288Expn", doc::getVltStd14288Expn, () -> VltStd14_288ExpnCalc.calculate(std14, std288, index), doc::setVltStd14288Expn, tasks); // fora da prod mask
        // ifNull("vltStd14_288Cmpr", doc::getVltStd14288Cmpr, () -> VltStd14_288CmprCalc.calculate(std14, std288, index), doc::setVltStd14288Cmpr, tasks); // fora da prod mask
        // ifNull("vltStd20_48Ratio", doc::getVltStd2048Ratio, () -> VltStd20_48RatioCalc.calculate(std20, std48, index), doc::setVltStd2048Ratio, tasks); // fora da prod mask
        // ifNull("vltStd20_48Expn", doc::getVltStd2048Expn, () -> VltStd20_48ExpnCalc.calculate(std20, std48, index), doc::setVltStd2048Expn, tasks); // fora da prod mask
        // ifNull("vltStd20_48Cmpr", doc::getVltStd2048Cmpr, () -> VltStd20_48CmprCalc.calculate(std20, std48, index), doc::setVltStd2048Cmpr, tasks); // fora da prod mask
        // ifNull("vltStd48_288Ratio", doc::getVltStd48288Ratio, () -> VltStd48_288RatioCalc.calculate(std48, std288, index), doc::setVltStd48288Ratio, tasks); // fora da prod mask
        // ifNull("vltStd48_288Expn", doc::getVltStd48288Expn, () -> VltStd48_288ExpnCalc.calculate(std48, std288, index), doc::setVltStd48288Expn, tasks); // fora da prod mask
        // ifNull("vltStd48_288Cmpr", doc::getVltStd48288Cmpr, () -> VltStd48_288CmprCalc.calculate(std48, std288, index), doc::setVltStd48288Cmpr, tasks); // fora da prod mask
        // ifNull("vltStd96_288Ratio", doc::getVltStd96288Ratio, () -> VltStd96_288RatioCalc.calculate(std96, std288, index), doc::setVltStd96288Ratio, tasks); // fora da prod mask
        // ifNull("vltStd96_288Expn", doc::getVltStd96288Expn, () -> VltStd96_288ExpnCalc.calculate(std96, std288, index), doc::setVltStd96288Expn, tasks); // fora da prod mask
        // ifNull("vltStd96_288Cmpr", doc::getVltStd96288Cmpr, () -> VltStd96_288CmprCalc.calculate(std96, std288, index), doc::setVltStd96288Cmpr, tasks); // fora da prod mask
        // ifNull("vltStd14Zsc", doc::getVltStd14Zsc, () -> VltStd14ZscCalc.calculate(std14, index), doc::setVltStd14Zsc, tasks); // fora da prod mask
        // ifNull("vltStd20Zsc", doc::getVltStd20Zsc, () -> VltStd20ZscCalc.calculate(std20, index), doc::setVltStd20Zsc, tasks); // fora da prod mask
        // ifNull("vltStd50Zsc", doc::getVltStd50Zsc, () -> VltStd50ZscCalc.calculate(std50, index), doc::setVltStd50Zsc, tasks); // fora da prod mask
        // ifNull("vltStd48Zsc", doc::getVltStd48Zsc, () -> VltStd48ZscCalc.calculate(std48, index), doc::setVltStd48Zsc, tasks); // fora da prod mask
        // ifNull("vltStd96Zsc", doc::getVltStd96Zsc, () -> VltStd96ZscCalc.calculate(std96, index), doc::setVltStd96Zsc, tasks); // fora da prod mask
        // ifNull("vltStd288Zsc", doc::getVltStd288Zsc, () -> VltStd288ZscCalc.calculate(std288, index), doc::setVltStd288Zsc, tasks); // fora da prod mask
        ifNull("vltStd14Slp", doc::getVltStd14Slp, () -> VltStd14SlpCalc.calculate(stdSlp14, index), doc::setVltStd14Slp, tasks);
        ifNull("vltStd20Slp", doc::getVltStd20Slp, () -> VltStd20SlpCalc.calculate(stdSlp20, index), doc::setVltStd20Slp, tasks);
        ifNull("vltStd50Slp", doc::getVltStd50Slp, () -> VltStd50SlpCalc.calculate(stdSlp50, index), doc::setVltStd50Slp, tasks);
        // ifNull("vltStd48Slp", doc::getVltStd48Slp, () -> VltStd48SlpCalc.calculate(stdSlp48, index), doc::setVltStd48Slp, tasks); // fora da prod mask
        // ifNull("vltStd96Slp", doc::getVltStd96Slp, () -> VltStd96SlpCalc.calculate(stdSlp96, index), doc::setVltStd96Slp, tasks); // fora da prod mask
        // ifNull("vltStd288Slp", doc::getVltStd288Slp, () -> VltStd288SlpCalc.calculate(stdSlp288, index), doc::setVltStd288Slp, tasks); // fora da prod mask
        // ifNull("vltStd14VvW20", doc::getVltStd14VvW20, () -> VltStd14VvW20Calc.calculate(std14, index), doc::setVltStd14VvW20, tasks); // fora da prod mask
        // ifNull("vltStd20VvW20", doc::getVltStd20VvW20, () -> VltStd20VvW20Calc.calculate(std20, index), doc::setVltStd20VvW20, tasks); // fora da prod mask
        // ifNull("vltStd50VvW20", doc::getVltStd50VvW20, () -> VltStd50VvW20Calc.calculate(std50, index), doc::setVltStd50VvW20, tasks); // fora da prod mask
        // ifNull("vltStd48VvW20", doc::getVltStd48VvW20, () -> VltStd48VvW20Calc.calculate(std48, index), doc::setVltStd48VvW20, tasks); // fora da prod mask
        // ifNull("vltStd96VvW20", doc::getVltStd96VvW20, () -> VltStd96VvW20Calc.calculate(std96, index), doc::setVltStd96VvW20, tasks); // fora da prod mask
        // ifNull("vltStd288VvW20", doc::getVltStd288VvW20, () -> VltStd288VvW20Calc.calculate(std288, index), doc::setVltStd288VvW20, tasks); // fora da prod mask
        // ifNull("vltStd20VvW48", doc::getVltStd20VvW48, () -> VltStd20VvW48Calc.calculate(std20, index), doc::setVltStd20VvW48, tasks); // fora da prod mask
        // ifNull("vltStd48VvW48", doc::getVltStd48VvW48, () -> VltStd48VvW48Calc.calculate(std48, index), doc::setVltStd48VvW48, tasks); // fora da prod mask
        // ifNull("vltStd96VvW48", doc::getVltStd96VvW48, () -> VltStd96VvW48Calc.calculate(std96, index), doc::setVltStd96VvW48, tasks); // fora da prod mask
        // ifNull("vltStd288VvW48", doc::getVltStd288VvW48, () -> VltStd288VvW48Calc.calculate(std288, index), doc::setVltStd288VvW48, tasks); // fora da prod mask

        // =========================================================================
        // STRUCTURAL VOL
        // =========================================================================
        // ifNull("vltHurst50", doc::getVltHurst50, () -> VltHurst50Calc.calculate(series, index), doc::setVltHurst50, tasks); // fora da prod mask
        // ifNull("vltHurst100", doc::getVltHurst100, () -> VltHurst100Calc.calculate(series, index), doc::setVltHurst100, tasks); // fora da prod mask
        // ifNull("vltHurst200", doc::getVltHurst200, () -> VltHurst200Calc.calculate(series, index), doc::setVltHurst200, tasks); // fora da prod mask
        // ifNull("vltHurst400", doc::getVltHurst400, () -> VltHurst400Calc.calculate(series, index), doc::setVltHurst400, tasks); // fora da prod mask
        // ifNull("vltHurst800", doc::getVltHurst800, () -> VltHurst800Calc.calculate(series, index), doc::setVltHurst800, tasks); // fora da prod mask
        // ifNull("vltEntropyRet50", doc::getVltEntropyRet50, () -> VltEntropyRet50Calc.calculate(series, index), doc::setVltEntropyRet50, tasks); // fora da prod mask
        // ifNull("vltEntropyRet100", doc::getVltEntropyRet100, () -> VltEntropyRet100Calc.calculate(series, index), doc::setVltEntropyRet100, tasks); // fora da prod mask
        // ifNull("vltEntropyRet200", doc::getVltEntropyRet200, () -> VltEntropyRet200Calc.calculate(series, index), doc::setVltEntropyRet200, tasks); // fora da prod mask
        // ifNull("vltEntropyRet400", doc::getVltEntropyRet400, () -> VltEntropyRet400Calc.calculate(series, index), doc::setVltEntropyRet400, tasks); // fora da prod mask
        // ifNull("vltEntropyAbsret50", doc::getVltEntropyAbsret50, () -> VltEntropyAbsret50Calc.calculate(series, index), doc::setVltEntropyAbsret50, tasks); // fora da prod mask
        // ifNull("vltEntropyAbsret100", doc::getVltEntropyAbsret100, () -> VltEntropyAbsret100Calc.calculate(series, index), doc::setVltEntropyAbsret100, tasks); // fora da prod mask
        // ifNull("vltEntropyAbsret200", doc::getVltEntropyAbsret200, () -> VltEntropyAbsret200Calc.calculate(series, index), doc::setVltEntropyAbsret200, tasks); // fora da prod mask

        // =========================================================================
        // TARGET VIABILITY
        // =========================================================================
        // ifNull("vltAtr14Pct", doc::getVltAtr14Pct, () -> VltAtr14PctCalc.calculate(atr14, series, index), doc::setVltAtr14Pct, tasks); // fora da prod mask
        // ifNull("vltAtr48Pct", doc::getVltAtr48Pct, () -> VltAtr48PctCalc.calculate(atr48, series, index), doc::setVltAtr48Pct, tasks); // fora da prod mask
        // ifNull("vltAtr96Pct", doc::getVltAtr96Pct, () -> VltAtr96PctCalc.calculate(atr96, series, index), doc::setVltAtr96Pct, tasks); // fora da prod mask
        // ifNull("vltAtr288Pct", doc::getVltAtr288Pct, () -> VltAtr288PctCalc.calculate(atr288, series, index), doc::setVltAtr288Pct, tasks); // fora da prod mask
        // ifNull("vltStd20Pct", doc::getVltStd20Pct, () -> VltStd20PctCalc.calculate(std20, series, index), doc::setVltStd20Pct, tasks); // fora da prod mask
        // ifNull("vltStd48Pct", doc::getVltStd48Pct, () -> VltStd48PctCalc.calculate(std48, series, index), doc::setVltStd48Pct, tasks); // fora da prod mask
        // ifNull("vltStd96Pct", doc::getVltStd96Pct, () -> VltStd96PctCalc.calculate(std96, series, index), doc::setVltStd96Pct, tasks); // fora da prod mask
        // ifNull("vltStd288Pct", doc::getVltStd288Pct, () -> VltStd288PctCalc.calculate(std288, series, index), doc::setVltStd288Pct, tasks); // fora da prod mask
        // ifNull("vltVolRv30Pct", doc::getVltVolRv30Pct, () -> VltVolRv30PctCalc.calculate(rv30, series, index), doc::setVltVolRv30Pct, tasks); // fora da prod mask
        // ifNull("vltVolRv48Pct", doc::getVltVolRv48Pct, () -> VltVolRv48PctCalc.calculate(rv48, series, index), doc::setVltVolRv48Pct, tasks); // fora da prod mask
        // ifNull("vltVolRv96Pct", doc::getVltVolRv96Pct, () -> VltVolRv96PctCalc.calculate(rv96, series, index), doc::setVltVolRv96Pct, tasks); // fora da prod mask
        // ifNull("vltVolRv288Pct", doc::getVltVolRv288Pct, () -> VltVolRv288PctCalc.calculate(rv288, series, index), doc::setVltVolRv288Pct, tasks); // fora da prod mask
        // ifNull("vltEwmaVol32Pct", doc::getVltEwmaVol32Pct, () -> VltEwmaVol32PctCalc.calculate(series, index), doc::setVltEwmaVol32Pct, tasks); // fora da prod mask
        // ifNull("vltEwmaVol48Pct", doc::getVltEwmaVol48Pct, () -> VltEwmaVol48PctCalc.calculate(series, index), doc::setVltEwmaVol48Pct, tasks); // fora da prod mask
        // ifNull("vltEwmaVol96Pct", doc::getVltEwmaVol96Pct, () -> VltEwmaVol96PctCalc.calculate(series, index), doc::setVltEwmaVol96Pct, tasks); // fora da prod mask
        // ifNull("vltEwmaVol288Pct", doc::getVltEwmaVol288Pct, () -> VltEwmaVol288PctCalc.calculate(series, index), doc::setVltEwmaVol288Pct, tasks); // fora da prod mask

        // Target in vol units — volPct = vol/close
        // double closePrice = series.getBar(index).getClosePrice().doubleValue(); // fora da prod mask
        // double atr14Pct = atr14.getValue(index).doubleValue() / closePrice; // fora da prod mask
        // double rv48Pct = rv48.getValue(index).doubleValue() / closePrice; // fora da prod mask
        // double rv96Pct = rv96.getValue(index).doubleValue() / closePrice; // fora da prod mask
        // double rv288Pct = rv288.getValue(index).doubleValue() / closePrice; // fora da prod mask
        // double ewma48Pct = VltEwmaVol48PctCalc.calculate(series, index); // fora da prod mask
        // double ewma96Pct = VltEwmaVol96PctCalc.calculate(series, index); // fora da prod mask
        // double ewma288Pct = VltEwmaVol288PctCalc.calculate(series, index); // fora da prod mask

        // ifNull("vltTarget1pctInAtr14", doc::getVltTarget1pctInAtr14, () -> VltTarget1pctInAtr14Calc.calculate(atr14Pct), doc::setVltTarget1pctInAtr14, tasks); // fora da prod mask
        // ifNull("vltTarget2pctInAtr14", doc::getVltTarget2pctInAtr14, () -> VltTarget2pctInAtr14Calc.calculate(atr14Pct), doc::setVltTarget2pctInAtr14, tasks); // fora da prod mask
        // ifNull("vltTarget1pctInRv48", doc::getVltTarget1pctInRv48, () -> VltTarget1pctInRv48Calc.calculate(rv48Pct), doc::setVltTarget1pctInRv48, tasks); // fora da prod mask
        // ifNull("vltTarget2pctInRv48", doc::getVltTarget2pctInRv48, () -> VltTarget2pctInRv48Calc.calculate(rv48Pct), doc::setVltTarget2pctInRv48, tasks); // fora da prod mask
        // ifNull("vltTarget1pctInEwma48", doc::getVltTarget1pctInEwma48, () -> VltTarget1pctInEwma48Calc.calculate(ewma48Pct), doc::setVltTarget1pctInEwma48, tasks); // fora da prod mask
        // ifNull("vltTarget2pctInEwma48", doc::getVltTarget2pctInEwma48, () -> VltTarget2pctInEwma48Calc.calculate(ewma48Pct), doc::setVltTarget2pctInEwma48, tasks); // fora da prod mask
        // ifNull("vltTarget1pctInRv96", doc::getVltTarget1pctInRv96, () -> VltTarget1pctInRv96Calc.calculate(rv96Pct), doc::setVltTarget1pctInRv96, tasks); // fora da prod mask
        // ifNull("vltTarget2pctInRv96", doc::getVltTarget2pctInRv96, () -> VltTarget2pctInRv96Calc.calculate(rv96Pct), doc::setVltTarget2pctInRv96, tasks); // fora da prod mask
        // ifNull("vltTarget1pctInRv288", doc::getVltTarget1pctInRv288, () -> VltTarget1pctInRv288Calc.calculate(rv288Pct), doc::setVltTarget1pctInRv288, tasks); // fora da prod mask
        // ifNull("vltTarget2pctInRv288", doc::getVltTarget2pctInRv288, () -> VltTarget2pctInRv288Calc.calculate(rv288Pct), doc::setVltTarget2pctInRv288, tasks); // fora da prod mask
        // ifNull("vltTarget1pctInEwma96", doc::getVltTarget1pctInEwma96, () -> VltTarget1pctInEwma96Calc.calculate(ewma96Pct), doc::setVltTarget1pctInEwma96, tasks); // fora da prod mask
        // ifNull("vltTarget2pctInEwma96", doc::getVltTarget2pctInEwma96, () -> VltTarget2pctInEwma96Calc.calculate(ewma96Pct), doc::setVltTarget2pctInEwma96, tasks); // fora da prod mask
        // ifNull("vltTarget1pctInEwma288", doc::getVltTarget1pctInEwma288, () -> VltTarget1pctInEwma288Calc.calculate(ewma288Pct), doc::setVltTarget1pctInEwma288, tasks); // fora da prod mask
        // ifNull("vltTarget2pctInEwma288", doc::getVltTarget2pctInEwma288, () -> VltTarget2pctInEwma288Calc.calculate(ewma288Pct), doc::setVltTarget2pctInEwma288, tasks); // fora da prod mask

        // =========================================================================
        // VOL REGIME
        // =========================================================================
        // double sqzNow = VltVolSqzBbKeltCalc.calculate(bw20, atr14, index); // fora da prod mask
        // double atrRatioNow = atr7.getValue(index).doubleValue() / atr21.getValue(index).doubleValue(); // fora da prod mask

        // ifNull("vltRegimeState", doc::getVltRegimeState, () -> VltRegimeStateCalc.calculate(sqzNow, atrRatioNow), doc::setVltRegimeState, tasks); // fora da prod mask
        // ifNull("vltRegimeConf", doc::getVltRegimeConf, () -> VltRegimeConfCalc.calculate(sqzNow, atrRatioNow), doc::setVltRegimeConf, tasks); // fora da prod mask

        // Build state history for persistence/flipRate
        // int regW = 50; // fora da prod mask
        // int regStart = Math.max(1, index - regW + 1); // fora da prod mask
        // int regN = index - regStart + 1; // fora da prod mask
        // double[] sqzArr = new double[regN]; // fora da prod mask
        // double[] ratioArr = new double[regN]; // fora da prod mask
        // for (int k = 0; k < regN; k++) { // fora da prod mask
        //     int i = regStart + k; // fora da prod mask
        //     sqzArr[k] = VltVolSqzBbKeltCalc.calculate(bw20, atr14, i); // fora da prod mask
        //     ratioArr[k] = atr7.getValue(i).doubleValue() / atr21.getValue(i).doubleValue(); // fora da prod mask
        // } // fora da prod mask
        // double[] regStates = new double[regN]; // fora da prod mask
        // for (int k2 = 0; k2 < regN; k2++) regStates[k2] = VltRegimeStateCalc.calculate(sqzArr[k2], ratioArr[k2]); // fora da prod mask
        // int regLast = regN - 1; // fora da prod mask

        // ifNull("vltRegimePrstW20", doc::getVltRegimePrstW20, () -> VltRegimePrstW20Calc.calculate(regStates, regLast), doc::setVltRegimePrstW20, tasks); // fora da prod mask
        // ifNull("vltRegimeFlipRateW50", doc::getVltRegimeFlipRateW50, () -> VltRegimeFlipRateW50Calc.calculate(regStates, regLast), doc::setVltRegimeFlipRateW50, tasks); // fora da prod mask

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
            catch (Exception e) { throw new RuntimeException("[VLT] erro", e); }
        }
    }
}
