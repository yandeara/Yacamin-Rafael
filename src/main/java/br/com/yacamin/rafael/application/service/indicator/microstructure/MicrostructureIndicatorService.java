package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.indicator.cache.AtrCache;
import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.AmihudCache;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension.*;
import br.com.yacamin.rafael.application.service.indicator.cache.SvrCache;
import br.com.yacamin.rafael.application.service.indicator.cache.OfiCache;
import br.com.yacamin.rafael.application.service.indicator.cache.body.BodyCache;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.*;
import br.com.yacamin.rafael.application.service.indicator.cache.hasbrouck.HasbrouckCache;
import br.com.yacamin.rafael.application.service.indicator.cache.hasbrouck.extension.*;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.KyleCache;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.*;
import br.com.yacamin.rafael.application.service.indicator.cache.extension.OfiExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.VwapCache;
import br.com.yacamin.rafael.application.service.indicator.cache.extension.VwapExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.range.RangeCache;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.*;
import br.com.yacamin.rafael.application.service.indicator.cache.roll.RollCache;
import br.com.yacamin.rafael.application.service.indicator.cache.roll.extension.*;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.WickCache;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.*;
import br.com.yacamin.rafael.application.service.indicator.cache.shape.ShapeCache;
import br.com.yacamin.rafael.application.service.indicator.cache.shape.extension.*;
import br.com.yacamin.rafael.application.service.indicator.microstructure.calc.*;
import br.com.yacamin.rafael.application.service.indicator.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.mongo.document.MicrostructureIndicatorDocument;
import br.com.yacamin.rafael.adapter.out.persistence.mikhael.MicrostructureIndicatorMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

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
public class MicrostructureIndicatorService {

    private final MicrostructureIndicatorMongoRepository repository;
    private final AmihudCache amihudCache;
    private final BodyCache bodyCache;
    private final SvrCache svrCache;
    private final OfiCache ofiCache;
    private final HasbrouckCache hasbrouckCache;
    private final KyleCache kyleCache;
    private final VwapCache vwapCache;
    private final RangeCache rangeCache;
    private final AtrCache atrCache;
    private final CloseCache closeCache;
    private final RollCache rollCache;
    private final WickCache wickCache;
    private final ShapeCache shapeCache;

    public void analyse(SymbolCandle candle, BarSeries series) {
        analyse(candle, series, false);
    }

    public void analyse(SymbolCandle candle, BarSeries series, boolean forceRecalculate) {
        MicrostructureIndicatorDocument doc = analyseBuffered(candle, series, forceRecalculate, null);
        repository.save(doc, candle.getInterval());
    }

    public MicrostructureIndicatorDocument analyseBuffered(SymbolCandle candle, BarSeries series, boolean forceRecalculate, MicrostructureIndicatorDocument preloadedDoc) {
        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var interval = candle.getInterval();
        int index = series.getEndIndex();

        log.info("[WARMUP][MIC] {} - {}", symbol, openTime);

        MicrostructureIndicatorDocument doc;
        if (forceRecalculate) {
            doc = new MicrostructureIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        } else if (preloadedDoc != null) {
            doc = preloadedDoc;
        } else {
            doc = new MicrostructureIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        }

        // Warmup caches locais do Amihud
        AmihudExtension amihudRaw = amihudCache.getAmihudRaw(symbol, interval, series);
        // fora da prod mask: AmihudSmaExtension sma10 = amihudCache.getSma(symbol, interval, series, 10);
        // fora da prod mask: AmihudSmaExtension sma20 = amihudCache.getSma(symbol, interval, series, 20);
        // fora da prod mask: AmihudSmaExtension sma30 = amihudCache.getSma(symbol, interval, series, 30);
        // fora da prod mask: AmihudSmaExtension sma40 = amihudCache.getSma(symbol, interval, series, 40);
        // fora da prod mask: AmihudStdExtension std10 = amihudCache.getStd(symbol, interval, series, 10);
        // fora da prod mask: AmihudStdExtension std20 = amihudCache.getStd(symbol, interval, series, 20);
        // fora da prod mask: AmihudStdExtension std40 = amihudCache.getStd(symbol, interval, series, 40);
        // fora da prod mask: AmihudZscoreExtension zscore20 = amihudCache.getZscore(symbol, interval, series, 20);
        // fora da prod mask: AmihudZscoreExtension zscore80 = amihudCache.getZscore(symbol, interval, series, 80);
        AmihudSlopeExtension slopeW4 = amihudCache.getSlope(symbol, interval, series, 4);
        AmihudSlopeExtension slopeW20 = amihudCache.getSlope(symbol, interval, series, 20);
        // fora da prod mask: AmihudSlopeExtension slopeW50 = amihudCache.getSlope(symbol, interval, series, 50);
        ATRIndicator atr14 = atrCache.getAtr14(symbol, interval, series);
        ClosePriceIndicator close = closeCache.getClosePrice(symbol, interval, series);

        List<Callable<Void>> tasks = new ArrayList<>();

        // Amihud raw
        // fora da prod mask: ifNull("micAmihud", doc::getMicAmihud, () -> AmihudCalc.calculate(amihudRaw, index), doc::setMicAmihud, tasks);

        // Z-score
        // fora da prod mask: ifNull("micAmihudZscore20", doc::getMicAmihudZscore20, () -> AmihudZscore20Calc.calculate(zscore20, index), doc::setMicAmihudZscore20, tasks);
        // fora da prod mask: ifNull("micAmihudZscore80", doc::getMicAmihudZscore80, () -> AmihudZscore80Calc.calculate(zscore80, index), doc::setMicAmihudZscore80, tasks);

        // Relative
        // fora da prod mask: ifNull("micAmihudRel10", doc::getMicAmihudRel10, () -> AmihudRel10Calc.calculate(amihudRaw, index), doc::setMicAmihudRel10, tasks);
        // fora da prod mask: ifNull("micAmihudRel40", doc::getMicAmihudRel40, () -> AmihudRel40Calc.calculate(amihudRaw, index), doc::setMicAmihudRel40, tasks);

        // Slope
        ifNull("micAmihudSlpW4", doc::getMicAmihudSlpW4, () -> AmihudSlpW4Calc.calculate(slopeW4, index), doc::setMicAmihudSlpW4, tasks);
        ifNull("micAmihudSlpW20", doc::getMicAmihudSlpW20, () -> AmihudSlpW20Calc.calculate(slopeW20, index), doc::setMicAmihudSlpW20, tasks);
        // fora da prod mask: ifNull("micAmihudSlpW50", doc::getMicAmihudSlpW50, () -> AmihudSlpW50Calc.calculate(slopeW50, index), doc::setMicAmihudSlpW50, tasks);

        // Acceleration
        ifNull("micAmihudAccW4", doc::getMicAmihudAccW4, () -> AmihudAccW4Calc.calculate(amihudRaw, index), doc::setMicAmihudAccW4, tasks);
        ifNull("micAmihudAccW5", doc::getMicAmihudAccW5, () -> AmihudAccW5Calc.calculate(amihudRaw, index), doc::setMicAmihudAccW5, tasks);
        ifNull("micAmihudAccW10", doc::getMicAmihudAccW10, () -> AmihudAccW10Calc.calculate(amihudRaw, index), doc::setMicAmihudAccW10, tasks);
        ifNull("micAmihudAccW16", doc::getMicAmihudAccW16, () -> AmihudAccW16Calc.calculate(amihudRaw, index), doc::setMicAmihudAccW16, tasks);

        // Moving average
        // fora da prod mask: ifNull("micAmihudMa10", doc::getMicAmihudMa10, () -> AmihudMa10Calc.calculate(sma10, index), doc::setMicAmihudMa10, tasks);
        // fora da prod mask: ifNull("micAmihudMa20", doc::getMicAmihudMa20, () -> AmihudMa20Calc.calculate(sma20, index), doc::setMicAmihudMa20, tasks);
        // fora da prod mask: ifNull("micAmihudMa30", doc::getMicAmihudMa30, () -> AmihudMa30Calc.calculate(sma30, index), doc::setMicAmihudMa30, tasks);

        // Volatility (std)
        // fora da prod mask: ifNull("micAmihudVol10", doc::getMicAmihudVol10, () -> AmihudVol10Calc.calculate(std10, index), doc::setMicAmihudVol10, tasks);
        // fora da prod mask: ifNull("micAmihudVol20", doc::getMicAmihudVol20, () -> AmihudVol20Calc.calculate(std20, index), doc::setMicAmihudVol20, tasks);
        // fora da prod mask: ifNull("micAmihudVol40", doc::getMicAmihudVol40, () -> AmihudVol40Calc.calculate(std40, index), doc::setMicAmihudVol40, tasks);

        // Turnover
        // fora da prod mask: ifNull("micAmihudTurnover", doc::getMicAmihudTurnover, () -> AmihudTurnoverCalc.calculate(close, series, index), doc::setMicAmihudTurnover, tasks);

        // Percentile
        // fora da prod mask: ifNull("micAmihudPctileW20", doc::getMicAmihudPctileW20, () -> AmihudPctileW20Calc.calculate(amihudRaw, index), doc::setMicAmihudPctileW20, tasks);

        // Signed
        // fora da prod mask: ifNull("micAmihudSigned", doc::getMicAmihudSigned, () -> AmihudSignedCalc.calculate(close, series, index), doc::setMicAmihudSigned, tasks);

        // LRMR
        // fora da prod mask: ifNull("micAmihudLrmr1040", doc::getMicAmihudLrmr1040, () -> AmihudLrmrCalc.calculate(sma10, sma40, index), doc::setMicAmihudLrmr1040, tasks);

        // Stability
        // fora da prod mask: ifNull("micAmihudStability40", doc::getMicAmihudStability40, () -> AmihudStability40Calc.calculate(std40, sma40, index), doc::setMicAmihudStability40, tasks);

        // Vol relative
        // fora da prod mask: ifNull("micAmihudVolRel40", doc::getMicAmihudVolRel40, () -> AmihudVolRel40Calc.calculate(std10, std40, index), doc::setMicAmihudVolRel40, tasks);

        // ATRN
        // fora da prod mask: ifNull("micAmihudAtrn", doc::getMicAmihudAtrn, () -> AmihudAtrnCalc.calculate(amihudRaw, atr14, index), doc::setMicAmihudAtrn, tasks);

        // Persistence
        // fora da prod mask: ifNull("micAmihudPrstW10", doc::getMicAmihudPrstW10, () -> AmihudPrstW10Calc.calculate(amihudRaw, index), doc::setMicAmihudPrstW10, tasks);
        // fora da prod mask: ifNull("micAmihudPrstW20", doc::getMicAmihudPrstW20, () -> AmihudPrstW20Calc.calculate(amihudRaw, index), doc::setMicAmihudPrstW20, tasks);
        // fora da prod mask: ifNull("micAmihudPrstW40", doc::getMicAmihudPrstW40, () -> AmihudPrstW40Calc.calculate(amihudRaw, index), doc::setMicAmihudPrstW40, tasks);

        // Divergence
        // fora da prod mask: ifNull("micAmihudDvgc", doc::getMicAmihudDvgc, () -> AmihudDvgcCalc.calculate(amihudRaw, index), doc::setMicAmihudDvgc, tasks);

        // Regime state
        // fora da prod mask: ifNull("micAmihudRegimeState", doc::getMicAmihudRegimeState, () -> AmihudRegimeStateCalc.calculate(zscore80, std40, sma40, atr14, amihudRaw, index), doc::setMicAmihudRegimeState, tasks);

        // Trend alignment
        // fora da prod mask: ifNull("micAmihudTrendAlignment", doc::getMicAmihudTrendAlignment, () -> AmihudTrendAlignmentCalc.calculate(close, slopeW20, index), doc::setMicAmihudTrendAlignment, tasks);

        // Breakdown risk
        // fora da prod mask: ifNull("micAmihudBreakdownRisk", doc::getMicAmihudBreakdownRisk, () -> AmihudBreakdownRiskCalc.calculate(zscore80, std40, sma40, index), doc::setMicAmihudBreakdownRisk, tasks);

        // Regime confidence
        // fora da prod mask: ifNull("micAmihudRegimeConf", doc::getMicAmihudRegimeConf, () -> AmihudRegimeConfCalc.calculate(std40, sma40, index), doc::setMicAmihudRegimeConf, tasks);

        // ── Body ──────────────────────────────────
        BodyExtension bodyRaw = bodyCache.getBody(symbol, interval, series);
        BodyAbsExtension bodyAbsExt = bodyCache.getBodyAbs(symbol, interval, series);
        BodyRatioExtension bodyRatioExt = bodyCache.getBodyRatio(symbol, interval, series);
        BodyEnergyExtension bodyEnergyExt = bodyCache.getBodyEnergy(symbol, interval, series);
        BodyAbsSlopeExtension bodyAbsSlpW10 = bodyCache.getBodyAbsSlope(symbol, interval, series, 10);
        BodyAbsSlopeExtension bodyAbsSlpW20 = bodyCache.getBodyAbsSlope(symbol, interval, series, 20);
        // fora da prod mask: BodyAbsSmaExtension bodyAbsSma10 = bodyCache.getBodyAbsSma(symbol, interval, series, 10);
        // fora da prod mask: BodyAbsSmaExtension bodyAbsSma20 = bodyCache.getBodyAbsSma(symbol, interval, series, 20);
        // fora da prod mask: BodyAbsStdExtension bodyAbsStd10 = bodyCache.getBodyAbsStd(symbol, interval, series, 10);
        // fora da prod mask: BodyAbsStdExtension bodyAbsStd20 = bodyCache.getBodyAbsStd(symbol, interval, series, 20);
        BodyRatioSlopeExtension bodyRatioSlpW10 = bodyCache.getBodyRatioSlope(symbol, interval, series, 10);
        // fora da prod mask: BodyRatioStdExtension bodyRatioStdW10 = bodyCache.getBodyRatioStd(symbol, interval, series, 10);
        BodySignPersistenceExtension bodySignPrst20 = bodyCache.getSignPersistence(symbol, interval, series, 20);
        BodyRunLenExtension bodyRunLenExt = bodyCache.getRunLen(symbol, interval, series);

        // Body raw
        // fora da prod mask: ifNull("micCandleBody", doc::getMicCandleBody, () -> BodyCalc.calculate(bodyRaw, index), doc::setMicCandleBody, tasks);
        // fora da prod mask: ifNull("micCandleBodyAbs", doc::getMicCandleBodyAbs, () -> BodyAbsCalc.calculate(bodyAbsExt, index), doc::setMicCandleBodyAbs, tasks);
        ifNull("micBodyRatio", doc::getMicBodyRatio, () -> BodyRatioCalc.calculate(bodyRatioExt, index), doc::setMicBodyRatio, tasks);
        // fora da prod mask: ifNull("micCandleEnergyRaw", doc::getMicCandleEnergyRaw, () -> BodyEnergyRawCalc.calculate(bodyEnergyExt, index), doc::setMicCandleEnergyRaw, tasks);

        // Body slopes
        ifNull("micCandleBodySlpW10", doc::getMicCandleBodySlpW10, () -> BodySlpW10Calc.calculate(bodyAbsSlpW10, index), doc::setMicCandleBodySlpW10, tasks);
        ifNull("micCandleBodySlpW20", doc::getMicCandleBodySlpW20, () -> BodySlpW20Calc.calculate(bodyAbsSlpW20, index), doc::setMicCandleBodySlpW20, tasks);

        // Body MA
        // fora da prod mask: ifNull("micCandleBodyMa10", doc::getMicCandleBodyMa10, () -> BodyMa10Calc.calculate(bodyAbsSma10, index), doc::setMicCandleBodyMa10, tasks);
        // fora da prod mask: ifNull("micCandleBodyMa20", doc::getMicCandleBodyMa20, () -> BodyMa20Calc.calculate(bodyAbsSma20, index), doc::setMicCandleBodyMa20, tasks);

        // Body volatility
        // fora da prod mask: ifNull("micCandleBodyVol10", doc::getMicCandleBodyVol10, () -> BodyVol10Calc.calculate(bodyAbsStd10, index), doc::setMicCandleBodyVol10, tasks);
        // fora da prod mask: ifNull("micCandleBodyVol20", doc::getMicCandleBodyVol20, () -> BodyVol20Calc.calculate(bodyAbsStd20, index), doc::setMicCandleBodyVol20, tasks);

        // Body ratio derivatives
        ifNull("micBodyRatioSlpW10", doc::getMicBodyRatioSlpW10, () -> BodyRatioSlpW10Calc.calculate(bodyRatioSlpW10, index), doc::setMicBodyRatioSlpW10, tasks);
        // fora da prod mask: ifNull("micBodyRatioVolW10", doc::getMicBodyRatioVolW10, () -> BodyRatioVolW10Calc.calculate(bodyRatioStdW10, index), doc::setMicBodyRatioVolW10, tasks);

        // Body ATR normalized
        ifNull("micBodyAtrRatio", doc::getMicBodyAtrRatio, () -> BodyAtrRatioCalc.calculate(bodyAbsExt, atr14, index), doc::setMicBodyAtrRatio, tasks);
        ifNull("micCandleEnergyAtrn", doc::getMicCandleEnergyAtrn, () -> BodyEnergyAtrnCalc.calculate(bodyEnergyExt, atr14, index), doc::setMicCandleEnergyAtrn, tasks);

        // Body position/pressure/strength
        ifNull("micCandleBodyCenterPosition", doc::getMicCandleBodyCenterPosition, () -> BodyCenterPositionCalc.calculate(series, index), doc::setMicCandleBodyCenterPosition, tasks);
        ifNull("micCandlePressureRaw", doc::getMicCandlePressureRaw, () -> BodyPressureRawCalc.calculate(bodyRatioExt, index), doc::setMicCandlePressureRaw, tasks);
        ifNull("micCandleStrength", doc::getMicCandleStrength, () -> BodyStrengthCalc.calculate(bodyRatioExt, index), doc::setMicCandleStrength, tasks);
        ifNull("micCandleBodyStrengthScore", doc::getMicCandleBodyStrengthScore, () -> BodyStrengthScoreCalc.calculate(bodyRatioExt, index), doc::setMicCandleBodyStrengthScore, tasks);

        // Body percentage
        ifNull("micCandleBodyPct", doc::getMicCandleBodyPct, () -> BodyPctCalc.calculate(bodyAbsExt, series, index), doc::setMicCandleBodyPct, tasks);
        ifNull("micBodyPerc", doc::getMicBodyPerc, () -> BodyPercCalc.calculate(bodyRaw, series, index), doc::setMicBodyPerc, tasks);
        ifNull("micCandleBodyRatio", doc::getMicCandleBodyRatio, () -> BodyRatioAltCalc.calculate(bodyRatioExt, index), doc::setMicCandleBodyRatio, tasks);
        ifNull("micBodyReturn", doc::getMicBodyReturn, () -> BodyReturnCalc.calculate(bodyRaw, close, index), doc::setMicBodyReturn, tasks);

        // Body shock/persistence/run
        ifNull("micBodyShockAtrn", doc::getMicBodyShockAtrn, () -> BodyShockAtrnCalc.calculate(bodyAbsExt, atr14, index), doc::setMicBodyShockAtrn, tasks);
        ifNull("micBodySignPrstW20", doc::getMicBodySignPrstW20, () -> BodySignPrstW20Calc.calculate(bodySignPrst20, index), doc::setMicBodySignPrstW20, tasks);
        ifNull("micBodyRunLen", doc::getMicBodyRunLen, () -> BodyRunLenCalc.calculate(bodyRunLenExt, index), doc::setMicBodyRunLen, tasks);

        // ── Hasbrouck ─────────────────────────────

        // Hasbrouck W16
        // fora da prod mask: var hasbLambdaW16 = hasbrouckCache.getLambda(symbol, interval, series, 16);
        // fora da prod mask: var hasbZscW16 = hasbrouckCache.getZscore(symbol, interval, series, 16, 40);
        // fora da prod mask: var hasbSma20W16 = hasbrouckCache.getSma(symbol, interval, series, 16, 20);
        // fora da prod mask: var hasbSma40W16 = hasbrouckCache.getSma(symbol, interval, series, 16, 40);
        var hasbSlpW16 = hasbrouckCache.getSlope(symbol, interval, series, 16, 20);
        // fora da prod mask: var hasbStdW16 = hasbrouckCache.getStd(symbol, interval, series, 16, 40);
        // fora da prod mask: var hasbPctileW16 = hasbrouckCache.getPercentile(symbol, interval, series, 16, 40);

        // fora da prod mask: ifNull("micHasbLambdaW16", doc::getMicHasbLambdaW16, () -> HasbLambdaW16Calc.calculate(hasbLambdaW16, index), doc::setMicHasbLambdaW16, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW16Zsc40", doc::getMicHasbLambdaW16Zsc40, () -> HasbZscW16Calc.calculate(hasbZscW16, index), doc::setMicHasbLambdaW16Zsc40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW16Ma20", doc::getMicHasbLambdaW16Ma20, () -> HasbMa20W16Calc.calculate(hasbSma20W16, index), doc::setMicHasbLambdaW16Ma20, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW16Dvgc", doc::getMicHasbLambdaW16Dvgc, () -> HasbDvgcW16Calc.calculate(hasbLambdaW16, hasbSma20W16, index), doc::setMicHasbLambdaW16Dvgc, tasks);
        ifNull("micHasbLambdaW16SlpW20", doc::getMicHasbLambdaW16SlpW20, () -> HasbSlpW16Calc.calculate(hasbSlpW16, index), doc::setMicHasbLambdaW16SlpW20, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW16Vol40", doc::getMicHasbLambdaW16Vol40, () -> HasbVol40W16Calc.calculate(hasbStdW16, index), doc::setMicHasbLambdaW16Vol40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW16Stability40", doc::getMicHasbLambdaW16Stability40, () -> HasbStabilityW16Calc.calculate(hasbStdW16, hasbSma40W16, index), doc::setMicHasbLambdaW16Stability40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW16PctileW40", doc::getMicHasbLambdaW16PctileW40, () -> HasbPctileW16Calc.calculate(hasbPctileW16, index), doc::setMicHasbLambdaW16PctileW40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW16Atrn", doc::getMicHasbLambdaW16Atrn, () -> HasbAtrnW16Calc.calculate(hasbLambdaW16, atr14, index), doc::setMicHasbLambdaW16Atrn, tasks);

        // Hasbrouck W48
        // fora da prod mask: var hasbLambdaW48 = hasbrouckCache.getLambda(symbol, interval, series, 48);
        // fora da prod mask: var hasbZscW48 = hasbrouckCache.getZscore(symbol, interval, series, 48, 40);
        // fora da prod mask: var hasbSma20W48 = hasbrouckCache.getSma(symbol, interval, series, 48, 20);
        // fora da prod mask: var hasbSma40W48 = hasbrouckCache.getSma(symbol, interval, series, 48, 40);
        var hasbSlpW48 = hasbrouckCache.getSlope(symbol, interval, series, 48, 20);
        // fora da prod mask: var hasbStdW48 = hasbrouckCache.getStd(symbol, interval, series, 48, 40);
        // fora da prod mask: var hasbPctileW48 = hasbrouckCache.getPercentile(symbol, interval, series, 48, 40);

        // fora da prod mask: ifNull("micHasbLambdaW48", doc::getMicHasbLambdaW48, () -> HasbLambdaW48Calc.calculate(hasbLambdaW48, index), doc::setMicHasbLambdaW48, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW48Zsc40", doc::getMicHasbLambdaW48Zsc40, () -> HasbZscW48Calc.calculate(hasbZscW48, index), doc::setMicHasbLambdaW48Zsc40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW48Ma20", doc::getMicHasbLambdaW48Ma20, () -> HasbMa20W48Calc.calculate(hasbSma20W48, index), doc::setMicHasbLambdaW48Ma20, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW48Dvgc", doc::getMicHasbLambdaW48Dvgc, () -> HasbDvgcW48Calc.calculate(hasbLambdaW48, hasbSma20W48, index), doc::setMicHasbLambdaW48Dvgc, tasks);
        ifNull("micHasbLambdaW48SlpW20", doc::getMicHasbLambdaW48SlpW20, () -> HasbSlpW48Calc.calculate(hasbSlpW48, index), doc::setMicHasbLambdaW48SlpW20, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW48Vol40", doc::getMicHasbLambdaW48Vol40, () -> HasbVol40W48Calc.calculate(hasbStdW48, index), doc::setMicHasbLambdaW48Vol40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW48Stability40", doc::getMicHasbLambdaW48Stability40, () -> HasbStabilityW48Calc.calculate(hasbStdW48, hasbSma40W48, index), doc::setMicHasbLambdaW48Stability40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW48PctileW40", doc::getMicHasbLambdaW48PctileW40, () -> HasbPctileW48Calc.calculate(hasbPctileW48, index), doc::setMicHasbLambdaW48PctileW40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW48Atrn", doc::getMicHasbLambdaW48Atrn, () -> HasbAtrnW48Calc.calculate(hasbLambdaW48, atr14, index), doc::setMicHasbLambdaW48Atrn, tasks);

        // Hasbrouck W64
        // fora da prod mask: var hasbLambdaW64 = hasbrouckCache.getLambda(symbol, interval, series, 64);
        // fora da prod mask: var hasbZscW64 = hasbrouckCache.getZscore(symbol, interval, series, 64, 40);
        // fora da prod mask: var hasbSma20W64 = hasbrouckCache.getSma(symbol, interval, series, 64, 20);
        // fora da prod mask: var hasbSma40W64 = hasbrouckCache.getSma(symbol, interval, series, 64, 40);
        // fora da prod mask: var hasbSlpW64 = hasbrouckCache.getSlope(symbol, interval, series, 64, 20);
        // fora da prod mask: var hasbStdW64 = hasbrouckCache.getStd(symbol, interval, series, 64, 40);
        // fora da prod mask: var hasbPctileW64 = hasbrouckCache.getPercentile(symbol, interval, series, 64, 40);

        // fora da prod mask: ifNull("micHasbLambdaW64", doc::getMicHasbLambdaW64, () -> HasbLambdaW64Calc.calculate(hasbLambdaW64, index), doc::setMicHasbLambdaW64, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW64Zsc40", doc::getMicHasbLambdaW64Zsc40, () -> HasbZscW64Calc.calculate(hasbZscW64, index), doc::setMicHasbLambdaW64Zsc40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW64Ma20", doc::getMicHasbLambdaW64Ma20, () -> HasbMa20W64Calc.calculate(hasbSma20W64, index), doc::setMicHasbLambdaW64Ma20, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW64Dvgc", doc::getMicHasbLambdaW64Dvgc, () -> HasbDvgcW64Calc.calculate(hasbLambdaW64, hasbSma20W64, index), doc::setMicHasbLambdaW64Dvgc, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW64SlpW20", doc::getMicHasbLambdaW64SlpW20, () -> HasbSlpW64Calc.calculate(hasbSlpW64, index), doc::setMicHasbLambdaW64SlpW20, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW64Vol40", doc::getMicHasbLambdaW64Vol40, () -> HasbVol40W64Calc.calculate(hasbStdW64, index), doc::setMicHasbLambdaW64Vol40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW64Stability40", doc::getMicHasbLambdaW64Stability40, () -> HasbStabilityW64Calc.calculate(hasbStdW64, hasbSma40W64, index), doc::setMicHasbLambdaW64Stability40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW64PctileW40", doc::getMicHasbLambdaW64PctileW40, () -> HasbPctileW64Calc.calculate(hasbPctileW64, index), doc::setMicHasbLambdaW64PctileW40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW64Atrn", doc::getMicHasbLambdaW64Atrn, () -> HasbAtrnW64Calc.calculate(hasbLambdaW64, atr14, index), doc::setMicHasbLambdaW64Atrn, tasks);

        // Hasbrouck W288
        // fora da prod mask: var hasbLambdaW288 = hasbrouckCache.getLambda(symbol, interval, series, 288);
        // fora da prod mask: var hasbZscW288 = hasbrouckCache.getZscore(symbol, interval, series, 288, 40);
        // fora da prod mask: var hasbSma20W288 = hasbrouckCache.getSma(symbol, interval, series, 288, 20);
        // fora da prod mask: var hasbSma40W288 = hasbrouckCache.getSma(symbol, interval, series, 288, 40);
        // fora da prod mask: var hasbSlpW288 = hasbrouckCache.getSlope(symbol, interval, series, 288, 20);
        // fora da prod mask: var hasbStdW288 = hasbrouckCache.getStd(symbol, interval, series, 288, 40);
        // fora da prod mask: var hasbPctileW288 = hasbrouckCache.getPercentile(symbol, interval, series, 288, 40);

        // fora da prod mask: ifNull("micHasbLambdaW288", doc::getMicHasbLambdaW288, () -> HasbLambdaW288Calc.calculate(hasbLambdaW288, index), doc::setMicHasbLambdaW288, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW288Zsc40", doc::getMicHasbLambdaW288Zsc40, () -> HasbZscW288Calc.calculate(hasbZscW288, index), doc::setMicHasbLambdaW288Zsc40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW288Ma20", doc::getMicHasbLambdaW288Ma20, () -> HasbMa20W288Calc.calculate(hasbSma20W288, index), doc::setMicHasbLambdaW288Ma20, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW288Dvgc", doc::getMicHasbLambdaW288Dvgc, () -> HasbDvgcW288Calc.calculate(hasbLambdaW288, hasbSma20W288, index), doc::setMicHasbLambdaW288Dvgc, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW288SlpW20", doc::getMicHasbLambdaW288SlpW20, () -> HasbSlpW288Calc.calculate(hasbSlpW288, index), doc::setMicHasbLambdaW288SlpW20, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW288Vol40", doc::getMicHasbLambdaW288Vol40, () -> HasbVol40W288Calc.calculate(hasbStdW288, index), doc::setMicHasbLambdaW288Vol40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW288Stability40", doc::getMicHasbLambdaW288Stability40, () -> HasbStabilityW288Calc.calculate(hasbStdW288, hasbSma40W288, index), doc::setMicHasbLambdaW288Stability40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW288PctileW40", doc::getMicHasbLambdaW288PctileW40, () -> HasbPctileW288Calc.calculate(hasbPctileW288, index), doc::setMicHasbLambdaW288PctileW40, tasks);
        // fora da prod mask: ifNull("micHasbLambdaW288Atrn", doc::getMicHasbLambdaW288Atrn, () -> HasbAtrnW288Calc.calculate(hasbLambdaW288, atr14, index), doc::setMicHasbLambdaW288Atrn, tasks);

        // Hasbrouck-to-Kyle ratios
        // fora da prod mask: var kyleForRatio16 = kyleCache.getLambda(symbol, interval, series, 16);
        // fora da prod mask: var kyleForRatio48 = kyleCache.getLambda(symbol, interval, series, 48);
        // fora da prod mask: var kyleForRatio64 = kyleCache.getLambda(symbol, interval, series, 96);
        // fora da prod mask: var kyleForRatio288 = kyleCache.getLambda(symbol, interval, series, 288);
        // fora da prod mask: ifNull("micHasbToKyleRatioW16", doc::getMicHasbToKyleRatioW16, () -> HasbToKyleRatioW16Calc.calculate(hasbLambdaW16, kyleForRatio16, index), doc::setMicHasbToKyleRatioW16, tasks);
        // fora da prod mask: ifNull("micHasbToKyleRatioW48", doc::getMicHasbToKyleRatioW48, () -> HasbToKyleRatioW48Calc.calculate(hasbLambdaW48, kyleForRatio48, index), doc::setMicHasbToKyleRatioW48, tasks);
        // fora da prod mask: ifNull("micHasbToKyleRatioW64", doc::getMicHasbToKyleRatioW64, () -> HasbToKyleRatioW64Calc.calculate(hasbLambdaW64, kyleForRatio64, index), doc::setMicHasbToKyleRatioW64, tasks);
        // fora da prod mask: ifNull("micHasbToKyleRatioW288", doc::getMicHasbToKyleRatioW288, () -> HasbToKyleRatioW288Calc.calculate(hasbLambdaW288, kyleForRatio288, index), doc::setMicHasbToKyleRatioW288, tasks);

        // ── Kyle ──────────────────────────────────

        // Kyle W4
        var kyleLambdaW4 = kyleCache.getLambda(symbol, interval, series, 4);
        // fora da prod mask: var kyleZscW4 = kyleCache.getZscore(symbol, interval, series, 4, 20);
        var kyleSlpW4W4 = kyleCache.getSlope(symbol, interval, series, 4, 4);
        var kyleSlpW20W4 = kyleCache.getSlope(symbol, interval, series, 4, 20);
        // fora da prod mask: var kyleSlpW50W4 = kyleCache.getSlope(symbol, interval, series, 4, 50);
        // fora da prod mask: var kyleSma10W4 = kyleCache.getSma(symbol, interval, series, 4, 10);
        // fora da prod mask: var kyleSma20W4 = kyleCache.getSma(symbol, interval, series, 4, 20);
        // fora da prod mask: var kyleSma30W4 = kyleCache.getSma(symbol, interval, series, 4, 30);
        // fora da prod mask: var kyleSma40W4 = kyleCache.getSma(symbol, interval, series, 4, 40);
        // fora da prod mask: var kyleStd10W4 = kyleCache.getStd(symbol, interval, series, 4, 10);
        // fora da prod mask: var kyleStd20W4 = kyleCache.getStd(symbol, interval, series, 4, 20);
        // fora da prod mask: var kyleStd40W4 = kyleCache.getStd(symbol, interval, series, 4, 40);
        // fora da prod mask: var kylePctileW4 = kyleCache.getPercentile(symbol, interval, series, 4, 20);

        // fora da prod mask: ifNull("micKyleLambdaW4", doc::getMicKyleLambdaW4, () -> KyleLambdaW4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Atrn", doc::getMicKyleLambdaW4Atrn, () -> KyleAtrnW4Calc.calculate(kyleLambdaW4, atr14, index), doc::setMicKyleLambdaW4Atrn, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Zsc20", doc::getMicKyleLambdaW4Zsc20, () -> KyleZsc20W4Calc.calculate(kyleZscW4, index), doc::setMicKyleLambdaW4Zsc20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Rel10", doc::getMicKyleLambdaW4Rel10, () -> KyleRel10W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4Rel10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Rel40", doc::getMicKyleLambdaW4Rel40, () -> KyleRel40W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4Rel40, tasks);
        ifNull("micKyleLambdaW4SlpW4", doc::getMicKyleLambdaW4SlpW4, () -> KyleSlpW4W4Calc.calculate(kyleSlpW4W4, index), doc::setMicKyleLambdaW4SlpW4, tasks);
        ifNull("micKyleLambdaW4SlpW20", doc::getMicKyleLambdaW4SlpW20, () -> KyleSlpW20W4Calc.calculate(kyleSlpW20W4, index), doc::setMicKyleLambdaW4SlpW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4SlpW50", doc::getMicKyleLambdaW4SlpW50, () -> KyleSlpW50W4Calc.calculate(kyleSlpW50W4, index), doc::setMicKyleLambdaW4SlpW50, tasks);
        ifNull("micKyleLambdaW4AccW4", doc::getMicKyleLambdaW4AccW4, () -> KyleAccW4W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4AccW4, tasks);
        ifNull("micKyleLambdaW4AccW5", doc::getMicKyleLambdaW4AccW5, () -> KyleAccW5W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4AccW5, tasks);
        ifNull("micKyleLambdaW4AccW10", doc::getMicKyleLambdaW4AccW10, () -> KyleAccW10W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4AccW10, tasks);
        ifNull("micKyleLambdaW4AccW16", doc::getMicKyleLambdaW4AccW16, () -> KyleAccW16W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4AccW16, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Ma10", doc::getMicKyleLambdaW4Ma10, () -> KyleMa10W4Calc.calculate(kyleSma10W4, index), doc::setMicKyleLambdaW4Ma10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Ma20", doc::getMicKyleLambdaW4Ma20, () -> KyleMa20W4Calc.calculate(kyleSma20W4, index), doc::setMicKyleLambdaW4Ma20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Ma30", doc::getMicKyleLambdaW4Ma30, () -> KyleMa30W4Calc.calculate(kyleSma30W4, index), doc::setMicKyleLambdaW4Ma30, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Vol10", doc::getMicKyleLambdaW4Vol10, () -> KyleVol10W4Calc.calculate(kyleStd10W4, index), doc::setMicKyleLambdaW4Vol10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Vol20", doc::getMicKyleLambdaW4Vol20, () -> KyleVol20W4Calc.calculate(kyleStd20W4, index), doc::setMicKyleLambdaW4Vol20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Vol40", doc::getMicKyleLambdaW4Vol40, () -> KyleVol40W4Calc.calculate(kyleStd40W4, index), doc::setMicKyleLambdaW4Vol40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4VolRel40", doc::getMicKyleLambdaW4VolRel40, () -> KyleVolRel40W4Calc.calculate(kyleStd10W4, kyleStd40W4, index), doc::setMicKyleLambdaW4VolRel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4PrstW10", doc::getMicKyleLambdaW4PrstW10, () -> KylePrstW10W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4PrstW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4PrstW20", doc::getMicKyleLambdaW4PrstW20, () -> KylePrstW20W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4PrstW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4PrstW40", doc::getMicKyleLambdaW4PrstW40, () -> KylePrstW40W4Calc.calculate(kyleLambdaW4, index), doc::setMicKyleLambdaW4PrstW40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Dvgc", doc::getMicKyleLambdaW4Dvgc, () -> KyleDvgcW4Calc.calculate(kyleLambdaW4, kyleSma20W4, index), doc::setMicKyleLambdaW4Dvgc, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4PctileW20", doc::getMicKyleLambdaW4PctileW20, () -> KylePctileW20W4Calc.calculate(kylePctileW4, index), doc::setMicKyleLambdaW4PctileW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Lrmr1040", doc::getMicKyleLambdaW4Lrmr1040, () -> KyleLrmrW4Calc.calculate(kyleSma10W4, kyleSma40W4, index), doc::setMicKyleLambdaW4Lrmr1040, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW4Stability40", doc::getMicKyleLambdaW4Stability40, () -> KyleStabilityW4Calc.calculate(kyleStd40W4, kyleSma40W4, index), doc::setMicKyleLambdaW4Stability40, tasks);

        // Kyle W16
        var kyleLambdaW16 = kyleCache.getLambda(symbol, interval, series, 16);
        // fora da prod mask: var kyleZscW16 = kyleCache.getZscore(symbol, interval, series, 16, 20);
        var kyleSlpW4W16 = kyleCache.getSlope(symbol, interval, series, 16, 4);
        var kyleSlpW20W16 = kyleCache.getSlope(symbol, interval, series, 16, 20);
        // fora da prod mask: var kyleSlpW50W16 = kyleCache.getSlope(symbol, interval, series, 16, 50);
        // fora da prod mask: var kyleSma10W16 = kyleCache.getSma(symbol, interval, series, 16, 10);
        // fora da prod mask: var kyleSma20W16 = kyleCache.getSma(symbol, interval, series, 16, 20);
        // fora da prod mask: var kyleSma30W16 = kyleCache.getSma(symbol, interval, series, 16, 30);
        // fora da prod mask: var kyleSma40W16 = kyleCache.getSma(symbol, interval, series, 16, 40);
        // fora da prod mask: var kyleStd10W16 = kyleCache.getStd(symbol, interval, series, 16, 10);
        // fora da prod mask: var kyleStd20W16 = kyleCache.getStd(symbol, interval, series, 16, 20);
        // fora da prod mask: var kyleStd40W16 = kyleCache.getStd(symbol, interval, series, 16, 40);
        // fora da prod mask: var kylePctileW16 = kyleCache.getPercentile(symbol, interval, series, 16, 20);

        // fora da prod mask: ifNull("micKyleLambdaW16", doc::getMicKyleLambdaW16, () -> KyleLambdaW16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Atrn", doc::getMicKyleLambdaW16Atrn, () -> KyleAtrnW16Calc.calculate(kyleLambdaW16, atr14, index), doc::setMicKyleLambdaW16Atrn, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Zsc20", doc::getMicKyleLambdaW16Zsc20, () -> KyleZsc20W16Calc.calculate(kyleZscW16, index), doc::setMicKyleLambdaW16Zsc20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Rel10", doc::getMicKyleLambdaW16Rel10, () -> KyleRel10W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16Rel10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Rel40", doc::getMicKyleLambdaW16Rel40, () -> KyleRel40W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16Rel40, tasks);
        ifNull("micKyleLambdaW16SlpW4", doc::getMicKyleLambdaW16SlpW4, () -> KyleSlpW4W16Calc.calculate(kyleSlpW4W16, index), doc::setMicKyleLambdaW16SlpW4, tasks);
        ifNull("micKyleLambdaW16SlpW20", doc::getMicKyleLambdaW16SlpW20, () -> KyleSlpW20W16Calc.calculate(kyleSlpW20W16, index), doc::setMicKyleLambdaW16SlpW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16SlpW50", doc::getMicKyleLambdaW16SlpW50, () -> KyleSlpW50W16Calc.calculate(kyleSlpW50W16, index), doc::setMicKyleLambdaW16SlpW50, tasks);
        ifNull("micKyleLambdaW16AccW4", doc::getMicKyleLambdaW16AccW4, () -> KyleAccW4W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16AccW4, tasks);
        ifNull("micKyleLambdaW16AccW5", doc::getMicKyleLambdaW16AccW5, () -> KyleAccW5W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16AccW5, tasks);
        ifNull("micKyleLambdaW16AccW10", doc::getMicKyleLambdaW16AccW10, () -> KyleAccW10W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16AccW10, tasks);
        ifNull("micKyleLambdaW16AccW16", doc::getMicKyleLambdaW16AccW16, () -> KyleAccW16W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16AccW16, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Ma10", doc::getMicKyleLambdaW16Ma10, () -> KyleMa10W16Calc.calculate(kyleSma10W16, index), doc::setMicKyleLambdaW16Ma10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Ma20", doc::getMicKyleLambdaW16Ma20, () -> KyleMa20W16Calc.calculate(kyleSma20W16, index), doc::setMicKyleLambdaW16Ma20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Ma30", doc::getMicKyleLambdaW16Ma30, () -> KyleMa30W16Calc.calculate(kyleSma30W16, index), doc::setMicKyleLambdaW16Ma30, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Vol10", doc::getMicKyleLambdaW16Vol10, () -> KyleVol10W16Calc.calculate(kyleStd10W16, index), doc::setMicKyleLambdaW16Vol10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Vol20", doc::getMicKyleLambdaW16Vol20, () -> KyleVol20W16Calc.calculate(kyleStd20W16, index), doc::setMicKyleLambdaW16Vol20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Vol40", doc::getMicKyleLambdaW16Vol40, () -> KyleVol40W16Calc.calculate(kyleStd40W16, index), doc::setMicKyleLambdaW16Vol40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16VolRel40", doc::getMicKyleLambdaW16VolRel40, () -> KyleVolRel40W16Calc.calculate(kyleStd10W16, kyleStd40W16, index), doc::setMicKyleLambdaW16VolRel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16PrstW10", doc::getMicKyleLambdaW16PrstW10, () -> KylePrstW10W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16PrstW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16PrstW20", doc::getMicKyleLambdaW16PrstW20, () -> KylePrstW20W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16PrstW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16PrstW40", doc::getMicKyleLambdaW16PrstW40, () -> KylePrstW40W16Calc.calculate(kyleLambdaW16, index), doc::setMicKyleLambdaW16PrstW40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Dvgc", doc::getMicKyleLambdaW16Dvgc, () -> KyleDvgcW16Calc.calculate(kyleLambdaW16, kyleSma20W16, index), doc::setMicKyleLambdaW16Dvgc, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16PctileW20", doc::getMicKyleLambdaW16PctileW20, () -> KylePctileW20W16Calc.calculate(kylePctileW16, index), doc::setMicKyleLambdaW16PctileW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Lrmr1040", doc::getMicKyleLambdaW16Lrmr1040, () -> KyleLrmrW16Calc.calculate(kyleSma10W16, kyleSma40W16, index), doc::setMicKyleLambdaW16Lrmr1040, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW16Stability40", doc::getMicKyleLambdaW16Stability40, () -> KyleStabilityW16Calc.calculate(kyleStd40W16, kyleSma40W16, index), doc::setMicKyleLambdaW16Stability40, tasks);

        // Kyle W48
        // fora da prod mask: var kyleLambdaW48 = kyleCache.getLambda(symbol, interval, series, 48);
        // fora da prod mask: var kyleZscW48 = kyleCache.getZscore(symbol, interval, series, 48, 20);
        // fora da prod mask: var kyleSlpW4W48 = kyleCache.getSlope(symbol, interval, series, 48, 4);
        // fora da prod mask: var kyleSlpW20W48 = kyleCache.getSlope(symbol, interval, series, 48, 20);
        // fora da prod mask: var kyleSlpW50W48 = kyleCache.getSlope(symbol, interval, series, 48, 50);
        // fora da prod mask: var kyleSma10W48 = kyleCache.getSma(symbol, interval, series, 48, 10);
        // fora da prod mask: var kyleSma20W48 = kyleCache.getSma(symbol, interval, series, 48, 20);
        // fora da prod mask: var kyleSma30W48 = kyleCache.getSma(symbol, interval, series, 48, 30);
        // fora da prod mask: var kyleSma40W48 = kyleCache.getSma(symbol, interval, series, 48, 40);
        // fora da prod mask: var kyleStd10W48 = kyleCache.getStd(symbol, interval, series, 48, 10);
        // fora da prod mask: var kyleStd20W48 = kyleCache.getStd(symbol, interval, series, 48, 20);
        // fora da prod mask: var kyleStd40W48 = kyleCache.getStd(symbol, interval, series, 48, 40);
        // fora da prod mask: var kylePctileW48 = kyleCache.getPercentile(symbol, interval, series, 48, 20);

        // fora da prod mask: ifNull("micKyleLambdaW48", doc::getMicKyleLambdaW48, () -> KyleLambdaW48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Atrn", doc::getMicKyleLambdaW48Atrn, () -> KyleAtrnW48Calc.calculate(kyleLambdaW48, atr14, index), doc::setMicKyleLambdaW48Atrn, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Zsc20", doc::getMicKyleLambdaW48Zsc20, () -> KyleZsc20W48Calc.calculate(kyleZscW48, index), doc::setMicKyleLambdaW48Zsc20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Rel10", doc::getMicKyleLambdaW48Rel10, () -> KyleRel10W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48Rel10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Rel40", doc::getMicKyleLambdaW48Rel40, () -> KyleRel40W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48Rel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48SlpW4", doc::getMicKyleLambdaW48SlpW4, () -> KyleSlpW4W48Calc.calculate(kyleSlpW4W48, index), doc::setMicKyleLambdaW48SlpW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48SlpW20", doc::getMicKyleLambdaW48SlpW20, () -> KyleSlpW20W48Calc.calculate(kyleSlpW20W48, index), doc::setMicKyleLambdaW48SlpW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48SlpW50", doc::getMicKyleLambdaW48SlpW50, () -> KyleSlpW50W48Calc.calculate(kyleSlpW50W48, index), doc::setMicKyleLambdaW48SlpW50, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48AccW4", doc::getMicKyleLambdaW48AccW4, () -> KyleAccW4W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48AccW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48AccW5", doc::getMicKyleLambdaW48AccW5, () -> KyleAccW5W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48AccW5, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48AccW10", doc::getMicKyleLambdaW48AccW10, () -> KyleAccW10W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48AccW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48AccW16", doc::getMicKyleLambdaW48AccW16, () -> KyleAccW16W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48AccW16, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Ma10", doc::getMicKyleLambdaW48Ma10, () -> KyleMa10W48Calc.calculate(kyleSma10W48, index), doc::setMicKyleLambdaW48Ma10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Ma20", doc::getMicKyleLambdaW48Ma20, () -> KyleMa20W48Calc.calculate(kyleSma20W48, index), doc::setMicKyleLambdaW48Ma20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Ma30", doc::getMicKyleLambdaW48Ma30, () -> KyleMa30W48Calc.calculate(kyleSma30W48, index), doc::setMicKyleLambdaW48Ma30, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Vol10", doc::getMicKyleLambdaW48Vol10, () -> KyleVol10W48Calc.calculate(kyleStd10W48, index), doc::setMicKyleLambdaW48Vol10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Vol20", doc::getMicKyleLambdaW48Vol20, () -> KyleVol20W48Calc.calculate(kyleStd20W48, index), doc::setMicKyleLambdaW48Vol20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Vol40", doc::getMicKyleLambdaW48Vol40, () -> KyleVol40W48Calc.calculate(kyleStd40W48, index), doc::setMicKyleLambdaW48Vol40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48VolRel40", doc::getMicKyleLambdaW48VolRel40, () -> KyleVolRel40W48Calc.calculate(kyleStd10W48, kyleStd40W48, index), doc::setMicKyleLambdaW48VolRel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48PrstW10", doc::getMicKyleLambdaW48PrstW10, () -> KylePrstW10W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48PrstW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48PrstW20", doc::getMicKyleLambdaW48PrstW20, () -> KylePrstW20W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48PrstW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48PrstW40", doc::getMicKyleLambdaW48PrstW40, () -> KylePrstW40W48Calc.calculate(kyleLambdaW48, index), doc::setMicKyleLambdaW48PrstW40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Dvgc", doc::getMicKyleLambdaW48Dvgc, () -> KyleDvgcW48Calc.calculate(kyleLambdaW48, kyleSma20W48, index), doc::setMicKyleLambdaW48Dvgc, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48PctileW20", doc::getMicKyleLambdaW48PctileW20, () -> KylePctileW20W48Calc.calculate(kylePctileW48, index), doc::setMicKyleLambdaW48PctileW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Lrmr1040", doc::getMicKyleLambdaW48Lrmr1040, () -> KyleLrmrW48Calc.calculate(kyleSma10W48, kyleSma40W48, index), doc::setMicKyleLambdaW48Lrmr1040, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW48Stability40", doc::getMicKyleLambdaW48Stability40, () -> KyleStabilityW48Calc.calculate(kyleStd40W48, kyleSma40W48, index), doc::setMicKyleLambdaW48Stability40, tasks);

        // Kyle W96
        // fora da prod mask: var kyleLambdaW96 = kyleCache.getLambda(symbol, interval, series, 96);
        // fora da prod mask: var kyleZscW96 = kyleCache.getZscore(symbol, interval, series, 96, 20);
        // fora da prod mask: var kyleSlpW4W96 = kyleCache.getSlope(symbol, interval, series, 96, 4);
        // fora da prod mask: var kyleSlpW20W96 = kyleCache.getSlope(symbol, interval, series, 96, 20);
        // fora da prod mask: var kyleSlpW50W96 = kyleCache.getSlope(symbol, interval, series, 96, 50);
        // fora da prod mask: var kyleSma10W96 = kyleCache.getSma(symbol, interval, series, 96, 10);
        // fora da prod mask: var kyleSma20W96 = kyleCache.getSma(symbol, interval, series, 96, 20);
        // fora da prod mask: var kyleSma30W96 = kyleCache.getSma(symbol, interval, series, 96, 30);
        // fora da prod mask: var kyleSma40W96 = kyleCache.getSma(symbol, interval, series, 96, 40);
        // fora da prod mask: var kyleStd10W96 = kyleCache.getStd(symbol, interval, series, 96, 10);
        // fora da prod mask: var kyleStd20W96 = kyleCache.getStd(symbol, interval, series, 96, 20);
        // fora da prod mask: var kyleStd40W96 = kyleCache.getStd(symbol, interval, series, 96, 40);
        // fora da prod mask: var kylePctileW96 = kyleCache.getPercentile(symbol, interval, series, 96, 20);

        // fora da prod mask: ifNull("micKyleLambdaW96", doc::getMicKyleLambdaW96, () -> KyleLambdaW96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Atrn", doc::getMicKyleLambdaW96Atrn, () -> KyleAtrnW96Calc.calculate(kyleLambdaW96, atr14, index), doc::setMicKyleLambdaW96Atrn, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Zsc20", doc::getMicKyleLambdaW96Zsc20, () -> KyleZsc20W96Calc.calculate(kyleZscW96, index), doc::setMicKyleLambdaW96Zsc20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Rel10", doc::getMicKyleLambdaW96Rel10, () -> KyleRel10W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96Rel10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Rel40", doc::getMicKyleLambdaW96Rel40, () -> KyleRel40W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96Rel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96SlpW4", doc::getMicKyleLambdaW96SlpW4, () -> KyleSlpW4W96Calc.calculate(kyleSlpW4W96, index), doc::setMicKyleLambdaW96SlpW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96SlpW20", doc::getMicKyleLambdaW96SlpW20, () -> KyleSlpW20W96Calc.calculate(kyleSlpW20W96, index), doc::setMicKyleLambdaW96SlpW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96SlpW50", doc::getMicKyleLambdaW96SlpW50, () -> KyleSlpW50W96Calc.calculate(kyleSlpW50W96, index), doc::setMicKyleLambdaW96SlpW50, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96AccW4", doc::getMicKyleLambdaW96AccW4, () -> KyleAccW4W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96AccW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96AccW5", doc::getMicKyleLambdaW96AccW5, () -> KyleAccW5W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96AccW5, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96AccW10", doc::getMicKyleLambdaW96AccW10, () -> KyleAccW10W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96AccW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96AccW16", doc::getMicKyleLambdaW96AccW16, () -> KyleAccW16W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96AccW16, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Ma10", doc::getMicKyleLambdaW96Ma10, () -> KyleMa10W96Calc.calculate(kyleSma10W96, index), doc::setMicKyleLambdaW96Ma10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Ma20", doc::getMicKyleLambdaW96Ma20, () -> KyleMa20W96Calc.calculate(kyleSma20W96, index), doc::setMicKyleLambdaW96Ma20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Ma30", doc::getMicKyleLambdaW96Ma30, () -> KyleMa30W96Calc.calculate(kyleSma30W96, index), doc::setMicKyleLambdaW96Ma30, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Vol10", doc::getMicKyleLambdaW96Vol10, () -> KyleVol10W96Calc.calculate(kyleStd10W96, index), doc::setMicKyleLambdaW96Vol10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Vol20", doc::getMicKyleLambdaW96Vol20, () -> KyleVol20W96Calc.calculate(kyleStd20W96, index), doc::setMicKyleLambdaW96Vol20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Vol40", doc::getMicKyleLambdaW96Vol40, () -> KyleVol40W96Calc.calculate(kyleStd40W96, index), doc::setMicKyleLambdaW96Vol40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96VolRel40", doc::getMicKyleLambdaW96VolRel40, () -> KyleVolRel40W96Calc.calculate(kyleStd10W96, kyleStd40W96, index), doc::setMicKyleLambdaW96VolRel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96PrstW10", doc::getMicKyleLambdaW96PrstW10, () -> KylePrstW10W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96PrstW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96PrstW20", doc::getMicKyleLambdaW96PrstW20, () -> KylePrstW20W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96PrstW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96PrstW40", doc::getMicKyleLambdaW96PrstW40, () -> KylePrstW40W96Calc.calculate(kyleLambdaW96, index), doc::setMicKyleLambdaW96PrstW40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Dvgc", doc::getMicKyleLambdaW96Dvgc, () -> KyleDvgcW96Calc.calculate(kyleLambdaW96, kyleSma20W96, index), doc::setMicKyleLambdaW96Dvgc, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96PctileW20", doc::getMicKyleLambdaW96PctileW20, () -> KylePctileW20W96Calc.calculate(kylePctileW96, index), doc::setMicKyleLambdaW96PctileW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Lrmr1040", doc::getMicKyleLambdaW96Lrmr1040, () -> KyleLrmrW96Calc.calculate(kyleSma10W96, kyleSma40W96, index), doc::setMicKyleLambdaW96Lrmr1040, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW96Stability40", doc::getMicKyleLambdaW96Stability40, () -> KyleStabilityW96Calc.calculate(kyleStd40W96, kyleSma40W96, index), doc::setMicKyleLambdaW96Stability40, tasks);

        // Kyle W200
        // fora da prod mask: var kyleLambdaW200 = kyleCache.getLambda(symbol, interval, series, 200);
        // fora da prod mask: var kyleZscW200 = kyleCache.getZscore(symbol, interval, series, 200, 20);
        // fora da prod mask: var kyleSlpW4W200 = kyleCache.getSlope(symbol, interval, series, 200, 4);
        // fora da prod mask: var kyleSlpW20W200 = kyleCache.getSlope(symbol, interval, series, 200, 20);
        // fora da prod mask: var kyleSlpW50W200 = kyleCache.getSlope(symbol, interval, series, 200, 50);
        // fora da prod mask: var kyleSma10W200 = kyleCache.getSma(symbol, interval, series, 200, 10);
        // fora da prod mask: var kyleSma20W200 = kyleCache.getSma(symbol, interval, series, 200, 20);
        // fora da prod mask: var kyleSma30W200 = kyleCache.getSma(symbol, interval, series, 200, 30);
        // fora da prod mask: var kyleSma40W200 = kyleCache.getSma(symbol, interval, series, 200, 40);
        // fora da prod mask: var kyleStd10W200 = kyleCache.getStd(symbol, interval, series, 200, 10);
        // fora da prod mask: var kyleStd20W200 = kyleCache.getStd(symbol, interval, series, 200, 20);
        // fora da prod mask: var kyleStd40W200 = kyleCache.getStd(symbol, interval, series, 200, 40);
        // fora da prod mask: var kylePctileW200 = kyleCache.getPercentile(symbol, interval, series, 200, 20);

        // fora da prod mask: ifNull("micKyleLambdaW200", doc::getMicKyleLambdaW200, () -> KyleLambdaW200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Atrn", doc::getMicKyleLambdaW200Atrn, () -> KyleAtrnW200Calc.calculate(kyleLambdaW200, atr14, index), doc::setMicKyleLambdaW200Atrn, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Zsc20", doc::getMicKyleLambdaW200Zsc20, () -> KyleZsc20W200Calc.calculate(kyleZscW200, index), doc::setMicKyleLambdaW200Zsc20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Rel10", doc::getMicKyleLambdaW200Rel10, () -> KyleRel10W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200Rel10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Rel40", doc::getMicKyleLambdaW200Rel40, () -> KyleRel40W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200Rel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200SlpW4", doc::getMicKyleLambdaW200SlpW4, () -> KyleSlpW4W200Calc.calculate(kyleSlpW4W200, index), doc::setMicKyleLambdaW200SlpW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200SlpW20", doc::getMicKyleLambdaW200SlpW20, () -> KyleSlpW20W200Calc.calculate(kyleSlpW20W200, index), doc::setMicKyleLambdaW200SlpW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200SlpW50", doc::getMicKyleLambdaW200SlpW50, () -> KyleSlpW50W200Calc.calculate(kyleSlpW50W200, index), doc::setMicKyleLambdaW200SlpW50, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200AccW4", doc::getMicKyleLambdaW200AccW4, () -> KyleAccW4W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200AccW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200AccW5", doc::getMicKyleLambdaW200AccW5, () -> KyleAccW5W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200AccW5, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200AccW10", doc::getMicKyleLambdaW200AccW10, () -> KyleAccW10W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200AccW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200AccW16", doc::getMicKyleLambdaW200AccW16, () -> KyleAccW16W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200AccW16, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Ma10", doc::getMicKyleLambdaW200Ma10, () -> KyleMa10W200Calc.calculate(kyleSma10W200, index), doc::setMicKyleLambdaW200Ma10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Ma20", doc::getMicKyleLambdaW200Ma20, () -> KyleMa20W200Calc.calculate(kyleSma20W200, index), doc::setMicKyleLambdaW200Ma20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Ma30", doc::getMicKyleLambdaW200Ma30, () -> KyleMa30W200Calc.calculate(kyleSma30W200, index), doc::setMicKyleLambdaW200Ma30, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Vol10", doc::getMicKyleLambdaW200Vol10, () -> KyleVol10W200Calc.calculate(kyleStd10W200, index), doc::setMicKyleLambdaW200Vol10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Vol20", doc::getMicKyleLambdaW200Vol20, () -> KyleVol20W200Calc.calculate(kyleStd20W200, index), doc::setMicKyleLambdaW200Vol20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Vol40", doc::getMicKyleLambdaW200Vol40, () -> KyleVol40W200Calc.calculate(kyleStd40W200, index), doc::setMicKyleLambdaW200Vol40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200VolRel40", doc::getMicKyleLambdaW200VolRel40, () -> KyleVolRel40W200Calc.calculate(kyleStd10W200, kyleStd40W200, index), doc::setMicKyleLambdaW200VolRel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200PrstW10", doc::getMicKyleLambdaW200PrstW10, () -> KylePrstW10W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200PrstW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200PrstW20", doc::getMicKyleLambdaW200PrstW20, () -> KylePrstW20W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200PrstW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200PrstW40", doc::getMicKyleLambdaW200PrstW40, () -> KylePrstW40W200Calc.calculate(kyleLambdaW200, index), doc::setMicKyleLambdaW200PrstW40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Dvgc", doc::getMicKyleLambdaW200Dvgc, () -> KyleDvgcW200Calc.calculate(kyleLambdaW200, kyleSma20W200, index), doc::setMicKyleLambdaW200Dvgc, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200PctileW20", doc::getMicKyleLambdaW200PctileW20, () -> KylePctileW20W200Calc.calculate(kylePctileW200, index), doc::setMicKyleLambdaW200PctileW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Lrmr1040", doc::getMicKyleLambdaW200Lrmr1040, () -> KyleLrmrW200Calc.calculate(kyleSma10W200, kyleSma40W200, index), doc::setMicKyleLambdaW200Lrmr1040, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW200Stability40", doc::getMicKyleLambdaW200Stability40, () -> KyleStabilityW200Calc.calculate(kyleStd40W200, kyleSma40W200, index), doc::setMicKyleLambdaW200Stability40, tasks);

        // Kyle W288
        // fora da prod mask: var kyleLambdaW288 = kyleCache.getLambda(symbol, interval, series, 288);
        // fora da prod mask: var kyleZscW288 = kyleCache.getZscore(symbol, interval, series, 288, 20);
        // fora da prod mask: var kyleSlpW4W288 = kyleCache.getSlope(symbol, interval, series, 288, 4);
        // fora da prod mask: var kyleSlpW20W288 = kyleCache.getSlope(symbol, interval, series, 288, 20);
        // fora da prod mask: var kyleSlpW50W288 = kyleCache.getSlope(symbol, interval, series, 288, 50);
        // fora da prod mask: var kyleSma10W288 = kyleCache.getSma(symbol, interval, series, 288, 10);
        // fora da prod mask: var kyleSma20W288 = kyleCache.getSma(symbol, interval, series, 288, 20);
        // fora da prod mask: var kyleSma30W288 = kyleCache.getSma(symbol, interval, series, 288, 30);
        // fora da prod mask: var kyleSma40W288 = kyleCache.getSma(symbol, interval, series, 288, 40);
        // fora da prod mask: var kyleStd10W288 = kyleCache.getStd(symbol, interval, series, 288, 10);
        // fora da prod mask: var kyleStd20W288 = kyleCache.getStd(symbol, interval, series, 288, 20);
        // fora da prod mask: var kyleStd40W288 = kyleCache.getStd(symbol, interval, series, 288, 40);
        // fora da prod mask: var kylePctileW288 = kyleCache.getPercentile(symbol, interval, series, 288, 20);

        // fora da prod mask: ifNull("micKyleLambdaW288", doc::getMicKyleLambdaW288, () -> KyleLambdaW288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Atrn", doc::getMicKyleLambdaW288Atrn, () -> KyleAtrnW288Calc.calculate(kyleLambdaW288, atr14, index), doc::setMicKyleLambdaW288Atrn, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Zsc20", doc::getMicKyleLambdaW288Zsc20, () -> KyleZsc20W288Calc.calculate(kyleZscW288, index), doc::setMicKyleLambdaW288Zsc20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Rel10", doc::getMicKyleLambdaW288Rel10, () -> KyleRel10W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288Rel10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Rel40", doc::getMicKyleLambdaW288Rel40, () -> KyleRel40W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288Rel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288SlpW4", doc::getMicKyleLambdaW288SlpW4, () -> KyleSlpW4W288Calc.calculate(kyleSlpW4W288, index), doc::setMicKyleLambdaW288SlpW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288SlpW20", doc::getMicKyleLambdaW288SlpW20, () -> KyleSlpW20W288Calc.calculate(kyleSlpW20W288, index), doc::setMicKyleLambdaW288SlpW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288SlpW50", doc::getMicKyleLambdaW288SlpW50, () -> KyleSlpW50W288Calc.calculate(kyleSlpW50W288, index), doc::setMicKyleLambdaW288SlpW50, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288AccW4", doc::getMicKyleLambdaW288AccW4, () -> KyleAccW4W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288AccW4, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288AccW5", doc::getMicKyleLambdaW288AccW5, () -> KyleAccW5W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288AccW5, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288AccW10", doc::getMicKyleLambdaW288AccW10, () -> KyleAccW10W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288AccW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288AccW16", doc::getMicKyleLambdaW288AccW16, () -> KyleAccW16W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288AccW16, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Ma10", doc::getMicKyleLambdaW288Ma10, () -> KyleMa10W288Calc.calculate(kyleSma10W288, index), doc::setMicKyleLambdaW288Ma10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Ma20", doc::getMicKyleLambdaW288Ma20, () -> KyleMa20W288Calc.calculate(kyleSma20W288, index), doc::setMicKyleLambdaW288Ma20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Ma30", doc::getMicKyleLambdaW288Ma30, () -> KyleMa30W288Calc.calculate(kyleSma30W288, index), doc::setMicKyleLambdaW288Ma30, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Vol10", doc::getMicKyleLambdaW288Vol10, () -> KyleVol10W288Calc.calculate(kyleStd10W288, index), doc::setMicKyleLambdaW288Vol10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Vol20", doc::getMicKyleLambdaW288Vol20, () -> KyleVol20W288Calc.calculate(kyleStd20W288, index), doc::setMicKyleLambdaW288Vol20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Vol40", doc::getMicKyleLambdaW288Vol40, () -> KyleVol40W288Calc.calculate(kyleStd40W288, index), doc::setMicKyleLambdaW288Vol40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288VolRel40", doc::getMicKyleLambdaW288VolRel40, () -> KyleVolRel40W288Calc.calculate(kyleStd10W288, kyleStd40W288, index), doc::setMicKyleLambdaW288VolRel40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288PrstW10", doc::getMicKyleLambdaW288PrstW10, () -> KylePrstW10W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288PrstW10, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288PrstW20", doc::getMicKyleLambdaW288PrstW20, () -> KylePrstW20W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288PrstW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288PrstW40", doc::getMicKyleLambdaW288PrstW40, () -> KylePrstW40W288Calc.calculate(kyleLambdaW288, index), doc::setMicKyleLambdaW288PrstW40, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Dvgc", doc::getMicKyleLambdaW288Dvgc, () -> KyleDvgcW288Calc.calculate(kyleLambdaW288, kyleSma20W288, index), doc::setMicKyleLambdaW288Dvgc, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288PctileW20", doc::getMicKyleLambdaW288PctileW20, () -> KylePctileW20W288Calc.calculate(kylePctileW288, index), doc::setMicKyleLambdaW288PctileW20, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Lrmr1040", doc::getMicKyleLambdaW288Lrmr1040, () -> KyleLrmrW288Calc.calculate(kyleSma10W288, kyleSma40W288, index), doc::setMicKyleLambdaW288Lrmr1040, tasks);
        // fora da prod mask: ifNull("micKyleLambdaW288Stability40", doc::getMicKyleLambdaW288Stability40, () -> KyleStabilityW288Calc.calculate(kyleStd40W288, kyleSma40W288, index), doc::setMicKyleLambdaW288Stability40, tasks);

        // Kyle signed
        // fora da prod mask: OfiExtension ofiExt = ofiCache.getOfi(symbol, interval, series);
        // fora da prod mask: ifNull("micKyleLambdaSigned", doc::getMicKyleLambdaSigned, () -> KyleLambdaSignedCalc.calculate(close, ofiExt, index), doc::setMicKyleLambdaSigned, tasks);

        // ── PositionBalance ──────────────────────────────────
        var vwapExt = vwapCache.getVwap(symbol, interval, series);

        ifNull("micCloseOpenRatio", doc::getMicCloseOpenRatio, () -> PosCloseOpenRatioCalc.calculate(series, index), doc::setMicCloseOpenRatio, tasks);
        ifNull("micCloseOpenNorm", doc::getMicCloseOpenNorm, () -> PosCloseOpenNormCalc.calculate(series, index), doc::setMicCloseOpenNorm, tasks);
        ifNull("micCloseToHighNorm", doc::getMicCloseToHighNorm, () -> PosCloseToHighNormCalc.calculate(series, index), doc::setMicCloseToHighNorm, tasks);
        ifNull("micCloseToLowNorm", doc::getMicCloseToLowNorm, () -> PosCloseToLowNormCalc.calculate(series, index), doc::setMicCloseToLowNorm, tasks);
        ifNull("micClosePosNorm", doc::getMicClosePosNorm, () -> PosClosePosNormCalc.calculate(series, index), doc::setMicClosePosNorm, tasks);
        ifNull("micCandleClosePosNorm", doc::getMicCandleClosePosNorm, () -> PosCandleClosePosNormCalc.calculate(series, index), doc::setMicCandleClosePosNorm, tasks);
        // fora da prod mask: ifNull("micCloseHlc3Delta", doc::getMicCloseHlc3Delta, () -> PosCloseHlc3DeltaCalc.calculate(series, index), doc::setMicCloseHlc3Delta, tasks);
        // fora da prod mask: ifNull("micCloseVwapDelta", doc::getMicCloseVwapDelta, () -> PosCloseVwapDeltaCalc.calculate(vwapExt, series, index), doc::setMicCloseVwapDelta, tasks);
        ifNull("micCloseHlc3Atrn", doc::getMicCloseHlc3Atrn, () -> PosCloseHlc3AtrnCalc.calculate(series, atr14, index), doc::setMicCloseHlc3Atrn, tasks);
        ifNull("micCloseVwapAtrn", doc::getMicCloseVwapAtrn, () -> PosCloseVwapAtrnCalc.calculate(vwapExt, series, atr14, index), doc::setMicCloseVwapAtrn, tasks);
        ifNull("micCandleBalanceScore", doc::getMicCandleBalanceScore, () -> PosCandleBalanceScoreCalc.calculate(series, index), doc::setMicCandleBalanceScore, tasks);
        // fora da prod mask: ifNull("micClosePosZscore20", doc::getMicClosePosZscore20, () -> PosClosePosZscore20Calc.calculate(series, index), doc::setMicClosePosZscore20, tasks);
        // fora da prod mask: ifNull("micCandleBalanceZscore20", doc::getMicCandleBalanceZscore20, () -> PosCandleBalanceZscore20Calc.calculate(series, index), doc::setMicCandleBalanceZscore20, tasks);
        // fora da prod mask: ifNull("micClosePosMa10", doc::getMicClosePosMa10, () -> PosClosePosMa10Calc.calculate(series, index), doc::setMicClosePosMa10, tasks);
        // fora da prod mask: ifNull("micClosePosVol10", doc::getMicClosePosVol10, () -> PosClosePosVol10Calc.calculate(series, index), doc::setMicClosePosVol10, tasks);
        // fora da prod mask: ifNull("micCloseOpenRatioMa10", doc::getMicCloseOpenRatioMa10, () -> PosCloseOpenRatioMa10Calc.calculate(series, index), doc::setMicCloseOpenRatioMa10, tasks);
        // fora da prod mask: ifNull("micCloseOpenRatioVol10", doc::getMicCloseOpenRatioVol10, () -> PosCloseOpenRatioVol10Calc.calculate(series, index), doc::setMicCloseOpenRatioVol10, tasks);
        // fora da prod mask: ifNull("micCandleBalanceMa10", doc::getMicCandleBalanceMa10, () -> PosCandleBalanceMa10Calc.calculate(series, index), doc::setMicCandleBalanceMa10, tasks);
        // fora da prod mask: ifNull("micCandleBalanceVol10", doc::getMicCandleBalanceVol10, () -> PosCandleBalanceVol10Calc.calculate(series, index), doc::setMicCandleBalanceVol10, tasks);
        // fora da prod mask: ifNull("micBalanceState", doc::getMicBalanceState, () -> PosBalanceStateCalc.calculate(series, index), doc::setMicBalanceState, tasks);
        ifNull("micCloseTriangleScoreAtrn", doc::getMicCloseTriangleScoreAtrn, () -> PosCloseTriangleScoreAtrnCalc.calculate(series, atr14, index), doc::setMicCloseTriangleScoreAtrn, tasks);
        // fora da prod mask: ifNull("micCloseTriangleScore", doc::getMicCloseTriangleScore, () -> PosCloseTriangleScoreCalc.calculate(series, index), doc::setMicCloseTriangleScore, tasks);

        // ── Range ──────────────────────────────────
        var rangeExt = rangeCache.getRange(symbol, interval, series);
        var trueRangeExt = rangeCache.getTrueRange(symbol, interval, series);
        // fora da prod mask: var hlc3Ext = rangeCache.getHlc3(symbol, interval, series);
        // fora da prod mask: var logRangeExt = rangeCache.getLogRange(symbol, interval, series);
        // fora da prod mask: var rangeSma10 = rangeCache.getRangeSma(symbol, interval, series, 10);
        var rangeSma20 = rangeCache.getRangeSma(symbol, interval, series, 20);
        // fora da prod mask: var rangeSma30 = rangeCache.getRangeSma(symbol, interval, series, 30);
        // fora da prod mask: var rangeStd10 = rangeCache.getRangeStd(symbol, interval, series, 10);
        var rangeStd20 = rangeCache.getRangeStd(symbol, interval, series, 20);
        var rangeSlp10 = rangeCache.getRangeSlope(symbol, interval, series, 10);
        var rangeSlp20 = rangeCache.getRangeSlope(symbol, interval, series, 20);
        // fora da prod mask: var hlc3Sma10 = rangeCache.getHlc3Sma(symbol, interval, series, 10);
        // fora da prod mask: var hlc3Sma20 = rangeCache.getHlc3Sma(symbol, interval, series, 20);
        // fora da prod mask: var hlc3Slp20 = rangeCache.getHlc3Slope(symbol, interval, series, 20);
        // fora da prod mask: var hlc3Std10 = rangeCache.getHlc3Std(symbol, interval, series, 10);
        // fora da prod mask: var logRangeSma10 = rangeCache.getLogRangeSma(symbol, interval, series, 10);
        var logRangeSlp20 = rangeCache.getLogRangeSlope(symbol, interval, series, 20);
        // fora da prod mask: var logRangeStd10 = rangeCache.getLogRangeStd(symbol, interval, series, 10);
        // fora da prod mask: var logRangePctile48 = rangeCache.getLogRangePercentile(symbol, interval, series, 48);
        var rangeLaggedMean20 = rangeCache.getRangeLaggedMean(symbol, interval, series, 20);

        // fora da prod mask: ifNull("micRange", doc::getMicRange, () -> RangeCalc.calculate(rangeExt, index), doc::setMicRange, tasks);
        ifNull("micTrueRange", doc::getMicTrueRange, () -> TrueRangeCalc.calculate(trueRangeExt, index), doc::setMicTrueRange, tasks);
        // fora da prod mask: ifNull("micHlc3", doc::getMicHlc3, () -> Hlc3Calc.calculate(hlc3Ext, index), doc::setMicHlc3, tasks);
        // fora da prod mask: ifNull("micLogRange", doc::getMicLogRange, () -> LogRangeCalc.calculate(logRangeExt, index), doc::setMicLogRange, tasks);
        ifNull("micRangeSlpW10", doc::getMicRangeSlpW10, () -> RangeSlpW10Calc.calculate(rangeSlp10, index), doc::setMicRangeSlpW10, tasks);
        ifNull("micRangeSlpW20", doc::getMicRangeSlpW20, () -> RangeSlpW20Calc.calculate(rangeSlp20, index), doc::setMicRangeSlpW20, tasks);
        ifNull("micRangeAccW5", doc::getMicRangeAccW5, () -> RangeAccW5Calc.calculate(rangeExt, index), doc::setMicRangeAccW5, tasks);
        ifNull("micRangeAccW10", doc::getMicRangeAccW10, () -> RangeAccW10Calc.calculate(rangeExt, index), doc::setMicRangeAccW10, tasks);
        // fora da prod mask: ifNull("micRangeMa10", doc::getMicRangeMa10, () -> RangeMa10Calc.calculate(rangeSma10, index), doc::setMicRangeMa10, tasks);
        // fora da prod mask: ifNull("micRangeMa20", doc::getMicRangeMa20, () -> RangeMa20Calc.calculate(rangeSma20, index), doc::setMicRangeMa20, tasks);
        // fora da prod mask: ifNull("micRangeMa30", doc::getMicRangeMa30, () -> RangeMa30Calc.calculate(rangeSma30, index), doc::setMicRangeMa30, tasks);
        // fora da prod mask: ifNull("micRangeVol10", doc::getMicRangeVol10, () -> RangeVol10Calc.calculate(rangeStd10, index), doc::setMicRangeVol10, tasks);
        // fora da prod mask: ifNull("micRangeVol20", doc::getMicRangeVol20, () -> RangeVol20Calc.calculate(rangeStd20, index), doc::setMicRangeVol20, tasks);
        ifNull("micRangeCompressionW20", doc::getMicRangeCompressionW20, () -> RangeCompressionW20Calc.calculate(rangeStd20, rangeSma20, index), doc::setMicRangeCompressionW20, tasks);
        ifNull("micCandleBrr", doc::getMicCandleBrr, () -> RangeBrrCalc.calculate(series, index), doc::setMicCandleBrr, tasks);
        ifNull("micCandleRange", doc::getMicCandleRange, () -> RangeCandleRangeCalc.calculate(series, index), doc::setMicCandleRange, tasks);
        ifNull("micCandleVolatilityInside", doc::getMicCandleVolatilityInside, () -> RangeVolatilityInsideCalc.calculate(rangeExt, rangeLaggedMean20, index), doc::setMicCandleVolatilityInside, tasks);
        ifNull("micCandleSpreadRatio", doc::getMicCandleSpreadRatio, () -> RangeSpreadRatioCalc.calculate(rangeExt, series, index), doc::setMicCandleSpreadRatio, tasks);
        ifNull("micCandleLmr", doc::getMicCandleLmr, () -> RangeLmrCalc.calculate(series, index), doc::setMicCandleLmr, tasks);
        ifNull("micRangeReturn", doc::getMicRangeReturn, () -> RangeReturnCalc.calculate(rangeExt, series, index), doc::setMicRangeReturn, tasks);
        ifNull("micHighReturn", doc::getMicHighReturn, () -> RangeHighReturnCalc.calculate(series, index), doc::setMicHighReturn, tasks);
        ifNull("micLowReturn", doc::getMicLowReturn, () -> RangeLowReturnCalc.calculate(series, index), doc::setMicLowReturn, tasks);
        ifNull("micExtremeRangeReturn", doc::getMicExtremeRangeReturn, () -> RangeExtremeReturnCalc.calculate(series, index), doc::setMicExtremeRangeReturn, tasks);
        ifNull("micRangeAtrn", doc::getMicRangeAtrn, () -> RangeAtrnCalc.calculate(rangeExt, atr14, index), doc::setMicRangeAtrn, tasks);
        ifNull("micTrAtrn", doc::getMicTrAtrn, () -> RangeTrAtrnCalc.calculate(trueRangeExt, atr14, index), doc::setMicTrAtrn, tasks);
        ifNull("micRangeStdn", doc::getMicRangeStdn, () -> RangeStdnCalc.calculate(rangeExt, rangeStd20, index), doc::setMicRangeStdn, tasks);
        ifNull("micRangeAtrRatio", doc::getMicRangeAtrRatio, () -> RangeAtrRatioCalc.calculate(rangeExt, atr14, index), doc::setMicRangeAtrRatio, tasks);
        ifNull("micRangeAsymmetry", doc::getMicRangeAsymmetry, () -> RangeAsymmetryCalc.calculate(series, index), doc::setMicRangeAsymmetry, tasks);
        ifNull("micRangeHeadroomAtr", doc::getMicRangeHeadroomAtr, () -> RangeHeadroomAtrCalc.calculate(rangeExt, atr14, index), doc::setMicRangeHeadroomAtr, tasks);
        // fora da prod mask: ifNull("micHlc3Ma10", doc::getMicHlc3Ma10, () -> RangeHlc3Ma10Calc.calculate(hlc3Sma10, index), doc::setMicHlc3Ma10, tasks);
        // fora da prod mask: ifNull("micHlc3Ma20", doc::getMicHlc3Ma20, () -> RangeHlc3Ma20Calc.calculate(hlc3Sma20, index), doc::setMicHlc3Ma20, tasks);
        // fora da prod mask: ifNull("micHlc3SlpW20", doc::getMicHlc3SlpW20, () -> RangeHlc3SlpW20Calc.calculate(hlc3Slp20, index), doc::setMicHlc3SlpW20, tasks);
        // fora da prod mask: ifNull("micHlc3Vol10", doc::getMicHlc3Vol10, () -> RangeHlc3Vol10Calc.calculate(hlc3Std10, index), doc::setMicHlc3Vol10, tasks);
        // fora da prod mask: ifNull("micLogRangeMa10", doc::getMicLogRangeMa10, () -> RangeLogRangeMa10Calc.calculate(logRangeSma10, index), doc::setMicLogRangeMa10, tasks);
        ifNull("micLogRangeSlpW20", doc::getMicLogRangeSlpW20, () -> RangeLogRangeSlpW20Calc.calculate(logRangeSlp20, index), doc::setMicLogRangeSlpW20, tasks);
        // fora da prod mask: ifNull("micLogRangeVol10", doc::getMicLogRangeVol10, () -> RangeLogRangeVol10Calc.calculate(logRangeStd10, index), doc::setMicLogRangeVol10, tasks);
        ifNull("micRangeSqueezeW20", doc::getMicRangeSqueezeW20, () -> RangeSqueezeW20Calc.calculate(rangeExt, rangeSma20, index), doc::setMicRangeSqueezeW20, tasks);
        ifNull("micGapRatio", doc::getMicGapRatio, () -> RangeGapRatioCalc.calculate(trueRangeExt, rangeExt, index), doc::setMicGapRatio, tasks);
        ifNull("micTrRangeRatio", doc::getMicTrRangeRatio, () -> RangeTrRangeRatioCalc.calculate(trueRangeExt, rangeExt, index), doc::setMicTrRangeRatio, tasks);
        // fora da prod mask: ifNull("micLogRangePctileW48", doc::getMicLogRangePctileW48, () -> RangeLogRangePctileW48Calc.calculate(logRangePctile48, index), doc::setMicLogRangePctileW48, tasks);
        // fora da prod mask: ifNull("micRangeRegimeState", doc::getMicRangeRegimeState, () -> RangeRegimeStateCalc.calculate(rangeExt, trueRangeExt, atr14, series, index), doc::setMicRangeRegimeState, tasks);

        // ── Return1C ──────────────────────────────────
        // fora da prod mask: ifNull("micReturn", doc::getMicReturn, () -> Ret1cReturnCalc.calculate(series, index), doc::setMicReturn, tasks);
        // fora da prod mask: ifNull("micReturnLog", doc::getMicReturnLog, () -> Ret1cReturnLogCalc.calculate(series, index), doc::setMicReturnLog, tasks);
        // fora da prod mask: ifNull("micReturnDirection", doc::getMicReturnDirection, () -> Ret1cDirectionCalc.calculate(series, index), doc::setMicReturnDirection, tasks);
        // fora da prod mask: ifNull("micReturnAbsoluteStrength", doc::getMicReturnAbsoluteStrength, () -> Ret1cAbsStrengthCalc.calculate(series, index), doc::setMicReturnAbsoluteStrength, tasks);
        // fora da prod mask: ifNull("micReturnAcceleration", doc::getMicReturnAcceleration, () -> Ret1cAccelerationCalc.calculate(series, index), doc::setMicReturnAcceleration, tasks);
        // fora da prod mask: ifNull("micReturnReversalForce", doc::getMicReturnReversalForce, () -> Ret1cReversalForceCalc.calculate(series, index), doc::setMicReturnReversalForce, tasks);
        // fora da prod mask: ifNull("micReturnDominanceRatio", doc::getMicReturnDominanceRatio, () -> Ret1cDominanceRatioCalc.calculate(series, index), doc::setMicReturnDominanceRatio, tasks);
        // fora da prod mask: ifNull("micRvr", doc::getMicRvr, () -> Ret1cRvrCalc.calculate(series, index), doc::setMicRvr, tasks);
        // fora da prod mask: ifNull("micLogReturnDominance", doc::getMicLogReturnDominance, () -> Ret1cLogReturnDominanceCalc.calculate(series, index), doc::setMicLogReturnDominance, tasks);
        // fora da prod mask: ifNull("micReturnTrDominance", doc::getMicReturnTrDominance, () -> Ret1cReturnTrDominanceCalc.calculate(series, index), doc::setMicReturnTrDominance, tasks);
        // fora da prod mask: ifNull("micReturnGapPressure", doc::getMicReturnGapPressure, () -> Ret1cGapPressureCalc.calculate(series, index), doc::setMicReturnGapPressure, tasks);
        // fora da prod mask: ifNull("micReturnSignPrstW20", doc::getMicReturnSignPrstW20, () -> Ret1cSignPrstW20Calc.calculate(series, index), doc::setMicReturnSignPrstW20, tasks);
        // fora da prod mask: ifNull("micReturnRunLen", doc::getMicReturnRunLen, () -> Ret1cRunLenCalc.calculate(series, index), doc::setMicReturnRunLen, tasks);
        // fora da prod mask: ifNull("micReturnAtrn", doc::getMicReturnAtrn, () -> Ret1cAtrnCalc.calculate(series, atr14, index), doc::setMicReturnAtrn, tasks);
        // fora da prod mask: ifNull("micReturnStdn", doc::getMicReturnStdn, () -> Ret1cStdnCalc.calculate(series, index), doc::setMicReturnStdn, tasks);

        // ── ReturnWindow ──────────────────────────────────
        // fora da prod mask: ifNull("micReturnZscore5", doc::getMicReturnZscore5, () -> RetWinZscore5Calc.calculate(series, index), doc::setMicReturnZscore5, tasks);
        // fora da prod mask: ifNull("micReturnZscore14", doc::getMicReturnZscore14, () -> RetWinZscore14Calc.calculate(series, index), doc::setMicReturnZscore14, tasks);
        // fora da prod mask: ifNull("micReturnStdnW96", doc::getMicReturnStdnW96, () -> RetWinStdnW96Calc.calculate(series, index), doc::setMicReturnStdnW96, tasks);
        // fora da prod mask: ifNull("micReturnStdnW48", doc::getMicReturnStdnW48, () -> RetWinStdnW48Calc.calculate(series, index), doc::setMicReturnStdnW48, tasks);
        // fora da prod mask: ifNull("micReturnStdnW288", doc::getMicReturnStdnW288, () -> RetWinStdnW288Calc.calculate(series, index), doc::setMicReturnStdnW288, tasks);
        // fora da prod mask: ifNull("micReturnPctl20", doc::getMicReturnPctl20, () -> RetWinPctl20Calc.calculate(series, index), doc::setMicReturnPctl20, tasks);
        // fora da prod mask: ifNull("micReturnPctl50", doc::getMicReturnPctl50, () -> RetWinPctl50Calc.calculate(series, index), doc::setMicReturnPctl50, tasks);
        // fora da prod mask: ifNull("micReturnSkew", doc::getMicReturnSkew, () -> RetWinSkewCalc.calculate(series, index), doc::setMicReturnSkew, tasks);
        // fora da prod mask: ifNull("micReturnKurtosis", doc::getMicReturnKurtosis, () -> RetWinKurtosisCalc.calculate(series, index), doc::setMicReturnKurtosis, tasks);
        // fora da prod mask: ifNull("micReturnStdRolling", doc::getMicReturnStdRolling, () -> RetWinStdRollingCalc.calculate(series, index), doc::setMicReturnStdRolling, tasks);
        // fora da prod mask: ifNull("micReturnSmoothness", doc::getMicReturnSmoothness, () -> RetWinSmoothnessCalc.calculate(series, index), doc::setMicReturnSmoothness, tasks);
        // fora da prod mask: ifNull("micReturnRsp", doc::getMicReturnRsp, () -> RetWinRspCalc.calculate(series, index), doc::setMicReturnRsp, tasks);
        // fora da prod mask: ifNull("micReturnRds", doc::getMicReturnRds, () -> RetWinRdsCalc.calculate(series, index), doc::setMicReturnRds, tasks);
        // fora da prod mask: ifNull("micReturnRnr", doc::getMicReturnRnr, () -> RetWinRnrCalc.calculate(series, index), doc::setMicReturnRnr, tasks);
        // fora da prod mask: ifNull("micReturnFlipRateW20", doc::getMicReturnFlipRateW20, () -> RetWinFlipRateW20Calc.calculate(series, index), doc::setMicReturnFlipRateW20, tasks);
        // fora da prod mask: ifNull("micReturnAutocorr1W20", doc::getMicReturnAutocorr1W20, () -> RetWinAutocorr1W20Calc.calculate(series, index), doc::setMicReturnAutocorr1W20, tasks);

        // ── Roll W16 ──────────────────────────────────
        // fora da prod mask: var rollCovW16 = rollCache.getCov(symbol, interval, series, 16);
        // fora da prod mask: var rollCovPctW16 = rollCache.getCovPct(symbol, interval, series, 16);
        var rollSpreadW16 = rollCache.getSpread(symbol, interval, series, 16);
        // fora da prod mask: var rollSpreadPctW16 = rollCache.getSpreadPct(symbol, interval, series, 16);
        // fora da prod mask: var rollCovZscW16 = rollCache.getCovZscore(symbol, interval, series, 16, 20);
        // fora da prod mask: var rollCovPctZscW16 = rollCache.getCovPctZscore(symbol, interval, series, 16, 20);
        // fora da prod mask: var rollSpreadZscW16 = rollCache.getZscore(symbol, interval, series, 16, 20);
        // fora da prod mask: var rollSpreadPctZscW16 = rollCache.getSpreadPctZscore(symbol, interval, series, 16, 20);
        // fora da prod mask: var rollSpreadSmaW16 = rollCache.getSma(symbol, interval, series, 16, 20);
        // fora da prod mask: var rollSpreadPctileW16 = rollCache.getPercentile(symbol, interval, series, 16, 20);
        var rollSpreadSlpW16 = rollCache.getSlope(symbol, interval, series, 16, 20);
        // fora da prod mask: var rollSpreadStdW16 = rollCache.getStd(symbol, interval, series, 16, 20);

        // fora da prod mask: ifNull("micRollCovW16", doc::getMicRollCovW16, () -> RollCovW16Calc.calculate(rollCovW16, index), doc::setMicRollCovW16, tasks);
        // fora da prod mask: ifNull("micRollCovPctW16", doc::getMicRollCovPctW16, () -> RollCovPctW16Calc.calculate(rollCovPctW16, index), doc::setMicRollCovPctW16, tasks);
        // fora da prod mask: ifNull("micRollCovZscW16", doc::getMicRollCovZscW16, () -> RollCovZscW16Calc.calculate(rollCovZscW16, index), doc::setMicRollCovZscW16, tasks);
        // fora da prod mask: ifNull("micRollCovPctZscW16", doc::getMicRollCovPctZscW16, () -> RollCovPctZscW16Calc.calculate(rollCovPctZscW16, index), doc::setMicRollCovPctZscW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadW16", doc::getMicRollSpreadW16, () -> RollSpreadW16Calc.calculate(rollSpreadW16, index), doc::setMicRollSpreadW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctW16", doc::getMicRollSpreadPctW16, () -> RollSpreadPctW16Calc.calculate(rollSpreadPctW16, index), doc::setMicRollSpreadPctW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadZscW16", doc::getMicRollSpreadZscW16, () -> RollSpreadZscW16Calc.calculate(rollSpreadZscW16, index), doc::setMicRollSpreadZscW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctZscW16", doc::getMicRollSpreadPctZscW16, () -> RollSpreadZscW16Calc.calculate(rollSpreadPctZscW16, index), doc::setMicRollSpreadPctZscW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadMaW16", doc::getMicRollSpreadMaW16, () -> RollSpreadMaW16Calc.calculate(rollSpreadSmaW16, index), doc::setMicRollSpreadMaW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadPrstW16", doc::getMicRollSpreadPrstW16, () -> RollSpreadPrstW16Calc.calculate(rollSpreadW16, index), doc::setMicRollSpreadPrstW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctileW16", doc::getMicRollSpreadPctileW16, () -> RollSpreadPctileW16Calc.calculate(rollSpreadPctileW16, index), doc::setMicRollSpreadPctileW16, tasks);
        ifNull("micRollSpreadAccW16", doc::getMicRollSpreadAccW16, () -> RollSpreadAccW16Calc.calculate(rollSpreadW16, index), doc::setMicRollSpreadAccW16, tasks);
        ifNull("micRollSpreadSlpW16", doc::getMicRollSpreadSlpW16, () -> RollSpreadSlpW16Calc.calculate(rollSpreadSlpW16, index), doc::setMicRollSpreadSlpW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadVolW16", doc::getMicRollSpreadVolW16, () -> RollSpreadVolW16Calc.calculate(rollSpreadStdW16, index), doc::setMicRollSpreadVolW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadDvgcW16", doc::getMicRollSpreadDvgcW16, () -> RollSpreadDvgcW16Calc.calculate(rollSpreadZscW16, rollSpreadPctZscW16, index), doc::setMicRollSpreadDvgcW16, tasks);
        // fora da prod mask: ifNull("micRollSpreadAtrn14W16", doc::getMicRollSpreadAtrn14W16, () -> RollSpreadAtrn14W16Calc.calculate(atr14, index, doc.getMicRollSpreadW16() != null ? doc.getMicRollSpreadW16() : RollSpreadW16Calc.calculate(rollSpreadW16, index)), doc::setMicRollSpreadAtrn14W16, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctAtrn14W16", doc::getMicRollSpreadPctAtrn14W16, () -> RollSpreadPctAtrn14W16Calc.calculate(atr14, index, doc.getMicRollSpreadPctW16() != null ? doc.getMicRollSpreadPctW16() : RollSpreadPctW16Calc.calculate(rollSpreadPctW16, index)), doc::setMicRollSpreadPctAtrn14W16, tasks);

        // ── Roll W32 ──────────────────────────────────
        // fora da prod mask: var rollCovW32 = rollCache.getCov(symbol, interval, series, 32);
        // fora da prod mask: var rollCovPctW32 = rollCache.getCovPct(symbol, interval, series, 32);
        var rollSpreadW32 = rollCache.getSpread(symbol, interval, series, 32);
        // fora da prod mask: var rollSpreadPctW32 = rollCache.getSpreadPct(symbol, interval, series, 32);
        // fora da prod mask: var rollCovZscW32 = rollCache.getCovZscore(symbol, interval, series, 32, 20);
        // fora da prod mask: var rollCovPctZscW32 = rollCache.getCovPctZscore(symbol, interval, series, 32, 20);
        // fora da prod mask: var rollSpreadZscW32 = rollCache.getZscore(symbol, interval, series, 32, 20);
        // fora da prod mask: var rollSpreadPctZscW32 = rollCache.getSpreadPctZscore(symbol, interval, series, 32, 20);
        // fora da prod mask: var rollSpreadSmaW32 = rollCache.getSma(symbol, interval, series, 32, 20);
        // fora da prod mask: var rollSpreadPctileW32 = rollCache.getPercentile(symbol, interval, series, 32, 20);
        var rollSpreadSlpW32 = rollCache.getSlope(symbol, interval, series, 32, 20);
        // fora da prod mask: var rollSpreadStdW32 = rollCache.getStd(symbol, interval, series, 32, 20);

        // fora da prod mask: ifNull("micRollCovW32", doc::getMicRollCovW32, () -> RollCovW32Calc.calculate(rollCovW32, index), doc::setMicRollCovW32, tasks);
        // fora da prod mask: ifNull("micRollCovPctW32", doc::getMicRollCovPctW32, () -> RollCovPctW32Calc.calculate(rollCovPctW32, index), doc::setMicRollCovPctW32, tasks);
        // fora da prod mask: ifNull("micRollCovZscW32", doc::getMicRollCovZscW32, () -> RollCovZscW32Calc.calculate(rollCovZscW32, index), doc::setMicRollCovZscW32, tasks);
        // fora da prod mask: ifNull("micRollCovPctZscW32", doc::getMicRollCovPctZscW32, () -> RollCovPctZscW32Calc.calculate(rollCovPctZscW32, index), doc::setMicRollCovPctZscW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadW32", doc::getMicRollSpreadW32, () -> RollSpreadW32Calc.calculate(rollSpreadW32, index), doc::setMicRollSpreadW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctW32", doc::getMicRollSpreadPctW32, () -> RollSpreadPctW32Calc.calculate(rollSpreadPctW32, index), doc::setMicRollSpreadPctW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadZscW32", doc::getMicRollSpreadZscW32, () -> RollSpreadZscW32Calc.calculate(rollSpreadZscW32, index), doc::setMicRollSpreadZscW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctZscW32", doc::getMicRollSpreadPctZscW32, () -> RollSpreadZscW32Calc.calculate(rollSpreadPctZscW32, index), doc::setMicRollSpreadPctZscW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadMaW32", doc::getMicRollSpreadMaW32, () -> RollSpreadMaW32Calc.calculate(rollSpreadSmaW32, index), doc::setMicRollSpreadMaW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadPrstW32", doc::getMicRollSpreadPrstW32, () -> RollSpreadPrstW32Calc.calculate(rollSpreadW32, index), doc::setMicRollSpreadPrstW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctileW32", doc::getMicRollSpreadPctileW32, () -> RollSpreadPctileW32Calc.calculate(rollSpreadPctileW32, index), doc::setMicRollSpreadPctileW32, tasks);
        ifNull("micRollSpreadAccW32", doc::getMicRollSpreadAccW32, () -> RollSpreadAccW32Calc.calculate(rollSpreadW32, index), doc::setMicRollSpreadAccW32, tasks);
        ifNull("micRollSpreadSlpW32", doc::getMicRollSpreadSlpW32, () -> RollSpreadSlpW32Calc.calculate(rollSpreadSlpW32, index), doc::setMicRollSpreadSlpW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadVolW32", doc::getMicRollSpreadVolW32, () -> RollSpreadVolW32Calc.calculate(rollSpreadStdW32, index), doc::setMicRollSpreadVolW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadDvgcW32", doc::getMicRollSpreadDvgcW32, () -> RollSpreadDvgcW32Calc.calculate(rollSpreadZscW32, rollSpreadPctZscW32, index), doc::setMicRollSpreadDvgcW32, tasks);
        // fora da prod mask: ifNull("micRollSpreadAtrn14W32", doc::getMicRollSpreadAtrn14W32, () -> RollSpreadAtrn14W32Calc.calculate(atr14, index, doc.getMicRollSpreadW32() != null ? doc.getMicRollSpreadW32() : RollSpreadW32Calc.calculate(rollSpreadW32, index)), doc::setMicRollSpreadAtrn14W32, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctAtrn14W32", doc::getMicRollSpreadPctAtrn14W32, () -> RollSpreadPctAtrn14W32Calc.calculate(atr14, index, doc.getMicRollSpreadPctW32() != null ? doc.getMicRollSpreadPctW32() : RollSpreadPctW32Calc.calculate(rollSpreadPctW32, index)), doc::setMicRollSpreadPctAtrn14W32, tasks);

        // ── Roll W48 ──────────────────────────────────
        // fora da prod mask: var rollCovW48 = rollCache.getCov(symbol, interval, series, 48);
        // fora da prod mask: var rollCovPctW48 = rollCache.getCovPct(symbol, interval, series, 48);
        var rollSpreadW48 = rollCache.getSpread(symbol, interval, series, 48);
        // fora da prod mask: var rollSpreadPctW48 = rollCache.getSpreadPct(symbol, interval, series, 48);
        // fora da prod mask: var rollCovZscW48 = rollCache.getCovZscore(symbol, interval, series, 48, 20);
        // fora da prod mask: var rollCovPctZscW48 = rollCache.getCovPctZscore(symbol, interval, series, 48, 20);
        // fora da prod mask: var rollSpreadZscW48 = rollCache.getZscore(symbol, interval, series, 48, 20);
        // fora da prod mask: var rollSpreadPctZscW48 = rollCache.getSpreadPctZscore(symbol, interval, series, 48, 20);
        // fora da prod mask: var rollSpreadSmaW48 = rollCache.getSma(symbol, interval, series, 48, 20);
        // fora da prod mask: var rollSpreadPctileW48 = rollCache.getPercentile(symbol, interval, series, 48, 20);
        var rollSpreadSlpW48 = rollCache.getSlope(symbol, interval, series, 48, 20);
        // fora da prod mask: var rollSpreadStdW48 = rollCache.getStd(symbol, interval, series, 48, 20);

        // fora da prod mask: ifNull("micRollCovW48", doc::getMicRollCovW48, () -> RollCovW48Calc.calculate(rollCovW48, index), doc::setMicRollCovW48, tasks);
        // fora da prod mask: ifNull("micRollCovPctW48", doc::getMicRollCovPctW48, () -> RollCovPctW48Calc.calculate(rollCovPctW48, index), doc::setMicRollCovPctW48, tasks);
        // fora da prod mask: ifNull("micRollCovZscW48", doc::getMicRollCovZscW48, () -> RollCovZscW48Calc.calculate(rollCovZscW48, index), doc::setMicRollCovZscW48, tasks);
        // fora da prod mask: ifNull("micRollCovPctZscW48", doc::getMicRollCovPctZscW48, () -> RollCovPctZscW48Calc.calculate(rollCovPctZscW48, index), doc::setMicRollCovPctZscW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadW48", doc::getMicRollSpreadW48, () -> RollSpreadW48Calc.calculate(rollSpreadW48, index), doc::setMicRollSpreadW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctW48", doc::getMicRollSpreadPctW48, () -> RollSpreadPctW48Calc.calculate(rollSpreadPctW48, index), doc::setMicRollSpreadPctW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadZscW48", doc::getMicRollSpreadZscW48, () -> RollSpreadZscW48Calc.calculate(rollSpreadZscW48, index), doc::setMicRollSpreadZscW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctZscW48", doc::getMicRollSpreadPctZscW48, () -> RollSpreadZscW48Calc.calculate(rollSpreadPctZscW48, index), doc::setMicRollSpreadPctZscW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadMaW48", doc::getMicRollSpreadMaW48, () -> RollSpreadMaW48Calc.calculate(rollSpreadSmaW48, index), doc::setMicRollSpreadMaW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadPrstW48", doc::getMicRollSpreadPrstW48, () -> RollSpreadPrstW48Calc.calculate(rollSpreadW48, index), doc::setMicRollSpreadPrstW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctileW48", doc::getMicRollSpreadPctileW48, () -> RollSpreadPctileW48Calc.calculate(rollSpreadPctileW48, index), doc::setMicRollSpreadPctileW48, tasks);
        ifNull("micRollSpreadAccW48", doc::getMicRollSpreadAccW48, () -> RollSpreadAccW48Calc.calculate(rollSpreadW48, index), doc::setMicRollSpreadAccW48, tasks);
        ifNull("micRollSpreadSlpW48", doc::getMicRollSpreadSlpW48, () -> RollSpreadSlpW48Calc.calculate(rollSpreadSlpW48, index), doc::setMicRollSpreadSlpW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadVolW48", doc::getMicRollSpreadVolW48, () -> RollSpreadVolW48Calc.calculate(rollSpreadStdW48, index), doc::setMicRollSpreadVolW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadDvgcW48", doc::getMicRollSpreadDvgcW48, () -> RollSpreadDvgcW48Calc.calculate(rollSpreadZscW48, rollSpreadPctZscW48, index), doc::setMicRollSpreadDvgcW48, tasks);
        // fora da prod mask: ifNull("micRollSpreadAtrn14W48", doc::getMicRollSpreadAtrn14W48, () -> RollSpreadAtrn14W48Calc.calculate(atr14, index, doc.getMicRollSpreadW48() != null ? doc.getMicRollSpreadW48() : RollSpreadW48Calc.calculate(rollSpreadW48, index)), doc::setMicRollSpreadAtrn14W48, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctAtrn14W48", doc::getMicRollSpreadPctAtrn14W48, () -> RollSpreadPctAtrn14W48Calc.calculate(atr14, index, doc.getMicRollSpreadPctW48() != null ? doc.getMicRollSpreadPctW48() : RollSpreadPctW48Calc.calculate(rollSpreadPctW48, index)), doc::setMicRollSpreadPctAtrn14W48, tasks);

        // ── Roll W96 ──────────────────────────────────
        // fora da prod mask: var rollCovW96 = rollCache.getCov(symbol, interval, series, 96);
        // fora da prod mask: var rollCovPctW96 = rollCache.getCovPct(symbol, interval, series, 96);
        // fora da prod mask: var rollSpreadW96 = rollCache.getSpread(symbol, interval, series, 96);
        // fora da prod mask: var rollSpreadPctW96 = rollCache.getSpreadPct(symbol, interval, series, 96);
        // fora da prod mask: var rollCovZscW96 = rollCache.getCovZscore(symbol, interval, series, 96, 20);
        // fora da prod mask: var rollCovPctZscW96 = rollCache.getCovPctZscore(symbol, interval, series, 96, 20);
        // fora da prod mask: var rollSpreadZscW96 = rollCache.getZscore(symbol, interval, series, 96, 20);
        // fora da prod mask: var rollSpreadPctZscW96 = rollCache.getSpreadPctZscore(symbol, interval, series, 96, 20);
        // fora da prod mask: var rollSpreadSmaW96 = rollCache.getSma(symbol, interval, series, 96, 20);
        // fora da prod mask: var rollSpreadPctileW96 = rollCache.getPercentile(symbol, interval, series, 96, 20);
        // fora da prod mask: var rollSpreadSlpW96 = rollCache.getSlope(symbol, interval, series, 96, 20);
        // fora da prod mask: var rollSpreadStdW96 = rollCache.getStd(symbol, interval, series, 96, 20);

        // fora da prod mask: ifNull("micRollCovW96", doc::getMicRollCovW96, () -> RollCovW96Calc.calculate(rollCovW96, index), doc::setMicRollCovW96, tasks);
        // fora da prod mask: ifNull("micRollCovPctW96", doc::getMicRollCovPctW96, () -> RollCovPctW96Calc.calculate(rollCovPctW96, index), doc::setMicRollCovPctW96, tasks);
        // fora da prod mask: ifNull("micRollCovZscW96", doc::getMicRollCovZscW96, () -> RollCovZscW96Calc.calculate(rollCovZscW96, index), doc::setMicRollCovZscW96, tasks);
        // fora da prod mask: ifNull("micRollCovPctZscW96", doc::getMicRollCovPctZscW96, () -> RollCovPctZscW96Calc.calculate(rollCovPctZscW96, index), doc::setMicRollCovPctZscW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadW96", doc::getMicRollSpreadW96, () -> RollSpreadW96Calc.calculate(rollSpreadW96, index), doc::setMicRollSpreadW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctW96", doc::getMicRollSpreadPctW96, () -> RollSpreadPctW96Calc.calculate(rollSpreadPctW96, index), doc::setMicRollSpreadPctW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadZscW96", doc::getMicRollSpreadZscW96, () -> RollSpreadZscW96Calc.calculate(rollSpreadZscW96, index), doc::setMicRollSpreadZscW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctZscW96", doc::getMicRollSpreadPctZscW96, () -> RollSpreadZscW96Calc.calculate(rollSpreadPctZscW96, index), doc::setMicRollSpreadPctZscW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadMaW96", doc::getMicRollSpreadMaW96, () -> RollSpreadMaW96Calc.calculate(rollSpreadSmaW96, index), doc::setMicRollSpreadMaW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadPrstW96", doc::getMicRollSpreadPrstW96, () -> RollSpreadPrstW96Calc.calculate(rollSpreadW96, index), doc::setMicRollSpreadPrstW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctileW96", doc::getMicRollSpreadPctileW96, () -> RollSpreadPctileW96Calc.calculate(rollSpreadPctileW96, index), doc::setMicRollSpreadPctileW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadAccW96", doc::getMicRollSpreadAccW96, () -> RollSpreadAccW96Calc.calculate(rollSpreadW96, index), doc::setMicRollSpreadAccW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadSlpW96", doc::getMicRollSpreadSlpW96, () -> RollSpreadSlpW96Calc.calculate(rollSpreadSlpW96, index), doc::setMicRollSpreadSlpW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadVolW96", doc::getMicRollSpreadVolW96, () -> RollSpreadVolW96Calc.calculate(rollSpreadStdW96, index), doc::setMicRollSpreadVolW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadDvgcW96", doc::getMicRollSpreadDvgcW96, () -> RollSpreadDvgcW96Calc.calculate(rollSpreadZscW96, rollSpreadPctZscW96, index), doc::setMicRollSpreadDvgcW96, tasks);
        // fora da prod mask: ifNull("micRollSpreadAtrn14W96", doc::getMicRollSpreadAtrn14W96, () -> RollSpreadAtrn14W96Calc.calculate(atr14, index, doc.getMicRollSpreadW96() != null ? doc.getMicRollSpreadW96() : RollSpreadW96Calc.calculate(rollSpreadW96, index)), doc::setMicRollSpreadAtrn14W96, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctAtrn14W96", doc::getMicRollSpreadPctAtrn14W96, () -> RollSpreadPctAtrn14W96Calc.calculate(atr14, index, doc.getMicRollSpreadPctW96() != null ? doc.getMicRollSpreadPctW96() : RollSpreadPctW96Calc.calculate(rollSpreadPctW96, index)), doc::setMicRollSpreadPctAtrn14W96, tasks);

        // ── Roll W336 ──────────────────────────────────
        // fora da prod mask: var rollCovW336 = rollCache.getCov(symbol, interval, series, 336);
        // fora da prod mask: var rollCovPctW336 = rollCache.getCovPct(symbol, interval, series, 336);
        // fora da prod mask: var rollSpreadW336 = rollCache.getSpread(symbol, interval, series, 336);
        // fora da prod mask: var rollSpreadPctW336 = rollCache.getSpreadPct(symbol, interval, series, 336);
        // fora da prod mask: var rollCovZscW336 = rollCache.getCovZscore(symbol, interval, series, 336, 20);
        // fora da prod mask: var rollCovPctZscW336 = rollCache.getCovPctZscore(symbol, interval, series, 336, 20);
        // fora da prod mask: var rollSpreadZscW336 = rollCache.getZscore(symbol, interval, series, 336, 20);
        // fora da prod mask: var rollSpreadPctZscW336 = rollCache.getSpreadPctZscore(symbol, interval, series, 336, 20);
        // fora da prod mask: var rollSpreadSmaW336 = rollCache.getSma(symbol, interval, series, 336, 20);
        // fora da prod mask: var rollSpreadPctileW336 = rollCache.getPercentile(symbol, interval, series, 336, 20);
        // fora da prod mask: var rollSpreadSlpW336 = rollCache.getSlope(symbol, interval, series, 336, 20);
        // fora da prod mask: var rollSpreadStdW336 = rollCache.getStd(symbol, interval, series, 336, 20);

        // fora da prod mask: ifNull("micRollCovW336", doc::getMicRollCovW336, () -> RollCovW336Calc.calculate(rollCovW336, index), doc::setMicRollCovW336, tasks);
        // fora da prod mask: ifNull("micRollCovPctW336", doc::getMicRollCovPctW336, () -> RollCovPctW336Calc.calculate(rollCovPctW336, index), doc::setMicRollCovPctW336, tasks);
        // fora da prod mask: ifNull("micRollCovZscW336", doc::getMicRollCovZscW336, () -> RollCovZscW336Calc.calculate(rollCovZscW336, index), doc::setMicRollCovZscW336, tasks);
        // fora da prod mask: ifNull("micRollCovPctZscW336", doc::getMicRollCovPctZscW336, () -> RollCovPctZscW336Calc.calculate(rollCovPctZscW336, index), doc::setMicRollCovPctZscW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadW336", doc::getMicRollSpreadW336, () -> RollSpreadW336Calc.calculate(rollSpreadW336, index), doc::setMicRollSpreadW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctW336", doc::getMicRollSpreadPctW336, () -> RollSpreadPctW336Calc.calculate(rollSpreadPctW336, index), doc::setMicRollSpreadPctW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadZscW336", doc::getMicRollSpreadZscW336, () -> RollSpreadZscW336Calc.calculate(rollSpreadZscW336, index), doc::setMicRollSpreadZscW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctZscW336", doc::getMicRollSpreadPctZscW336, () -> RollSpreadZscW336Calc.calculate(rollSpreadPctZscW336, index), doc::setMicRollSpreadPctZscW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadMaW336", doc::getMicRollSpreadMaW336, () -> RollSpreadMaW336Calc.calculate(rollSpreadSmaW336, index), doc::setMicRollSpreadMaW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadPrstW336", doc::getMicRollSpreadPrstW336, () -> RollSpreadPrstW336Calc.calculate(rollSpreadW336, index), doc::setMicRollSpreadPrstW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctileW336", doc::getMicRollSpreadPctileW336, () -> RollSpreadPctileW336Calc.calculate(rollSpreadPctileW336, index), doc::setMicRollSpreadPctileW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadAccW336", doc::getMicRollSpreadAccW336, () -> RollSpreadAccW336Calc.calculate(rollSpreadW336, index), doc::setMicRollSpreadAccW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadSlpW336", doc::getMicRollSpreadSlpW336, () -> RollSpreadSlpW336Calc.calculate(rollSpreadSlpW336, index), doc::setMicRollSpreadSlpW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadVolW336", doc::getMicRollSpreadVolW336, () -> RollSpreadVolW336Calc.calculate(rollSpreadStdW336, index), doc::setMicRollSpreadVolW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadDvgcW336", doc::getMicRollSpreadDvgcW336, () -> RollSpreadDvgcW336Calc.calculate(rollSpreadZscW336, rollSpreadPctZscW336, index), doc::setMicRollSpreadDvgcW336, tasks);
        // fora da prod mask: ifNull("micRollSpreadAtrn14W336", doc::getMicRollSpreadAtrn14W336, () -> RollSpreadAtrn14W336Calc.calculate(atr14, index, doc.getMicRollSpreadW336() != null ? doc.getMicRollSpreadW336() : RollSpreadW336Calc.calculate(rollSpreadW336, index)), doc::setMicRollSpreadAtrn14W336, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctAtrn14W336", doc::getMicRollSpreadPctAtrn14W336, () -> RollSpreadPctAtrn14W336Calc.calculate(atr14, index, doc.getMicRollSpreadPctW336() != null ? doc.getMicRollSpreadPctW336() : RollSpreadPctW336Calc.calculate(rollSpreadPctW336, index)), doc::setMicRollSpreadPctAtrn14W336, tasks);

        // ── Roll W512 ──────────────────────────────────
        // fora da prod mask: var rollCovW512 = rollCache.getCov(symbol, interval, series, 512);
        // fora da prod mask: var rollCovPctW512 = rollCache.getCovPct(symbol, interval, series, 512);
        // fora da prod mask: var rollSpreadW512 = rollCache.getSpread(symbol, interval, series, 512);
        // fora da prod mask: var rollSpreadPctW512 = rollCache.getSpreadPct(symbol, interval, series, 512);
        // fora da prod mask: var rollCovZscW512 = rollCache.getCovZscore(symbol, interval, series, 512, 20);
        // fora da prod mask: var rollCovPctZscW512 = rollCache.getCovPctZscore(symbol, interval, series, 512, 20);
        // fora da prod mask: var rollSpreadZscW512 = rollCache.getZscore(symbol, interval, series, 512, 20);
        // fora da prod mask: var rollSpreadPctZscW512 = rollCache.getSpreadPctZscore(symbol, interval, series, 512, 20);
        // fora da prod mask: var rollSpreadSmaW512 = rollCache.getSma(symbol, interval, series, 512, 20);
        // fora da prod mask: var rollSpreadPctileW512 = rollCache.getPercentile(symbol, interval, series, 512, 20);
        // fora da prod mask: var rollSpreadSlpW512 = rollCache.getSlope(symbol, interval, series, 512, 20);
        // fora da prod mask: var rollSpreadStdW512 = rollCache.getStd(symbol, interval, series, 512, 20);

        // fora da prod mask: ifNull("micRollCovW512", doc::getMicRollCovW512, () -> RollCovW512Calc.calculate(rollCovW512, index), doc::setMicRollCovW512, tasks);
        // fora da prod mask: ifNull("micRollCovPctW512", doc::getMicRollCovPctW512, () -> RollCovPctW512Calc.calculate(rollCovPctW512, index), doc::setMicRollCovPctW512, tasks);
        // fora da prod mask: ifNull("micRollCovZscW512", doc::getMicRollCovZscW512, () -> RollCovZscW512Calc.calculate(rollCovZscW512, index), doc::setMicRollCovZscW512, tasks);
        // fora da prod mask: ifNull("micRollCovPctZscW512", doc::getMicRollCovPctZscW512, () -> RollCovPctZscW512Calc.calculate(rollCovPctZscW512, index), doc::setMicRollCovPctZscW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadW512", doc::getMicRollSpreadW512, () -> RollSpreadW512Calc.calculate(rollSpreadW512, index), doc::setMicRollSpreadW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctW512", doc::getMicRollSpreadPctW512, () -> RollSpreadPctW512Calc.calculate(rollSpreadPctW512, index), doc::setMicRollSpreadPctW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadZscW512", doc::getMicRollSpreadZscW512, () -> RollSpreadZscW512Calc.calculate(rollSpreadZscW512, index), doc::setMicRollSpreadZscW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctZscW512", doc::getMicRollSpreadPctZscW512, () -> RollSpreadZscW512Calc.calculate(rollSpreadPctZscW512, index), doc::setMicRollSpreadPctZscW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadMaW512", doc::getMicRollSpreadMaW512, () -> RollSpreadMaW512Calc.calculate(rollSpreadSmaW512, index), doc::setMicRollSpreadMaW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadPrstW512", doc::getMicRollSpreadPrstW512, () -> RollSpreadPrstW512Calc.calculate(rollSpreadW512, index), doc::setMicRollSpreadPrstW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctileW512", doc::getMicRollSpreadPctileW512, () -> RollSpreadPctileW512Calc.calculate(rollSpreadPctileW512, index), doc::setMicRollSpreadPctileW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadAccW512", doc::getMicRollSpreadAccW512, () -> RollSpreadAccW512Calc.calculate(rollSpreadW512, index), doc::setMicRollSpreadAccW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadSlpW512", doc::getMicRollSpreadSlpW512, () -> RollSpreadSlpW512Calc.calculate(rollSpreadSlpW512, index), doc::setMicRollSpreadSlpW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadVolW512", doc::getMicRollSpreadVolW512, () -> RollSpreadVolW512Calc.calculate(rollSpreadStdW512, index), doc::setMicRollSpreadVolW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadDvgcW512", doc::getMicRollSpreadDvgcW512, () -> RollSpreadDvgcW512Calc.calculate(rollSpreadZscW512, rollSpreadPctZscW512, index), doc::setMicRollSpreadDvgcW512, tasks);
        // fora da prod mask: ifNull("micRollSpreadAtrn14W512", doc::getMicRollSpreadAtrn14W512, () -> RollSpreadAtrn14W512Calc.calculate(atr14, index, doc.getMicRollSpreadW512() != null ? doc.getMicRollSpreadW512() : RollSpreadW512Calc.calculate(rollSpreadW512, index)), doc::setMicRollSpreadAtrn14W512, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctAtrn14W512", doc::getMicRollSpreadPctAtrn14W512, () -> RollSpreadPctAtrn14W512Calc.calculate(atr14, index, doc.getMicRollSpreadPctW512() != null ? doc.getMicRollSpreadPctW512() : RollSpreadPctW512Calc.calculate(rollSpreadPctW512, index)), doc::setMicRollSpreadPctAtrn14W512, tasks);

        // ── Roll W672 ──────────────────────────────────
        // fora da prod mask: var rollCovW672 = rollCache.getCov(symbol, interval, series, 672);
        // fora da prod mask: var rollCovPctW672 = rollCache.getCovPct(symbol, interval, series, 672);
        // fora da prod mask: var rollSpreadW672 = rollCache.getSpread(symbol, interval, series, 672);
        // fora da prod mask: var rollSpreadPctW672 = rollCache.getSpreadPct(symbol, interval, series, 672);
        // fora da prod mask: var rollCovZscW672 = rollCache.getCovZscore(symbol, interval, series, 672, 20);
        // fora da prod mask: var rollCovPctZscW672 = rollCache.getCovPctZscore(symbol, interval, series, 672, 20);
        // fora da prod mask: var rollSpreadZscW672 = rollCache.getZscore(symbol, interval, series, 672, 20);
        // fora da prod mask: var rollSpreadPctZscW672 = rollCache.getSpreadPctZscore(symbol, interval, series, 672, 20);
        // fora da prod mask: var rollSpreadSmaW672 = rollCache.getSma(symbol, interval, series, 672, 20);
        // fora da prod mask: var rollSpreadPctileW672 = rollCache.getPercentile(symbol, interval, series, 672, 20);
        // fora da prod mask: var rollSpreadSlpW672 = rollCache.getSlope(symbol, interval, series, 672, 20);
        // fora da prod mask: var rollSpreadStdW672 = rollCache.getStd(symbol, interval, series, 672, 20);

        // fora da prod mask: ifNull("micRollCovW672", doc::getMicRollCovW672, () -> RollCovW672Calc.calculate(rollCovW672, index), doc::setMicRollCovW672, tasks);
        // fora da prod mask: ifNull("micRollCovPctW672", doc::getMicRollCovPctW672, () -> RollCovPctW672Calc.calculate(rollCovPctW672, index), doc::setMicRollCovPctW672, tasks);
        // fora da prod mask: ifNull("micRollCovZscW672", doc::getMicRollCovZscW672, () -> RollCovZscW672Calc.calculate(rollCovZscW672, index), doc::setMicRollCovZscW672, tasks);
        // fora da prod mask: ifNull("micRollCovPctZscW672", doc::getMicRollCovPctZscW672, () -> RollCovPctZscW672Calc.calculate(rollCovPctZscW672, index), doc::setMicRollCovPctZscW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadW672", doc::getMicRollSpreadW672, () -> RollSpreadW672Calc.calculate(rollSpreadW672, index), doc::setMicRollSpreadW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctW672", doc::getMicRollSpreadPctW672, () -> RollSpreadPctW672Calc.calculate(rollSpreadPctW672, index), doc::setMicRollSpreadPctW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadZscW672", doc::getMicRollSpreadZscW672, () -> RollSpreadZscW672Calc.calculate(rollSpreadZscW672, index), doc::setMicRollSpreadZscW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctZscW672", doc::getMicRollSpreadPctZscW672, () -> RollSpreadZscW672Calc.calculate(rollSpreadPctZscW672, index), doc::setMicRollSpreadPctZscW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadMaW672", doc::getMicRollSpreadMaW672, () -> RollSpreadMaW672Calc.calculate(rollSpreadSmaW672, index), doc::setMicRollSpreadMaW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadPrstW672", doc::getMicRollSpreadPrstW672, () -> RollSpreadPrstW672Calc.calculate(rollSpreadW672, index), doc::setMicRollSpreadPrstW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctileW672", doc::getMicRollSpreadPctileW672, () -> RollSpreadPctileW672Calc.calculate(rollSpreadPctileW672, index), doc::setMicRollSpreadPctileW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadAccW672", doc::getMicRollSpreadAccW672, () -> RollSpreadAccW672Calc.calculate(rollSpreadW672, index), doc::setMicRollSpreadAccW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadSlpW672", doc::getMicRollSpreadSlpW672, () -> RollSpreadSlpW672Calc.calculate(rollSpreadSlpW672, index), doc::setMicRollSpreadSlpW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadVolW672", doc::getMicRollSpreadVolW672, () -> RollSpreadVolW672Calc.calculate(rollSpreadStdW672, index), doc::setMicRollSpreadVolW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadDvgcW672", doc::getMicRollSpreadDvgcW672, () -> RollSpreadDvgcW672Calc.calculate(rollSpreadZscW672, rollSpreadPctZscW672, index), doc::setMicRollSpreadDvgcW672, tasks);
        // fora da prod mask: ifNull("micRollSpreadAtrn14W672", doc::getMicRollSpreadAtrn14W672, () -> RollSpreadAtrn14W672Calc.calculate(atr14, index, doc.getMicRollSpreadW672() != null ? doc.getMicRollSpreadW672() : RollSpreadW672Calc.calculate(rollSpreadW672, index)), doc::setMicRollSpreadAtrn14W672, tasks);
        // fora da prod mask: ifNull("micRollSpreadPctAtrn14W672", doc::getMicRollSpreadPctAtrn14W672, () -> RollSpreadPctAtrn14W672Calc.calculate(atr14, index, doc.getMicRollSpreadPctW672() != null ? doc.getMicRollSpreadPctW672() : RollSpreadPctW672Calc.calculate(rollSpreadPctW672, index)), doc::setMicRollSpreadPctAtrn14W672, tasks);

        // ── Wick ──────────────────────────────────
        var upperWickExt = wickCache.getUpperWick(symbol, interval, series);
        var lowerWickExt = wickCache.getLowerWick(symbol, interval, series);
        var wickImbExt = wickCache.getWickImbalance(symbol, interval, series);
        // fora da prod mask: var closePosNormExt = wickCache.getClosePosNorm(symbol, interval, series);
        // fora da prod mask: var upperWickSma10 = wickCache.getUpperWickSma(symbol, interval, series, 10);
        // fora da prod mask: var lowerWickSma10 = wickCache.getLowerWickSma(symbol, interval, series, 10);
        var wickImbSlp10 = wickCache.getWickImbalanceSlope(symbol, interval, series, 10);
        // fora da prod mask: var wickImbStd10 = wickCache.getWickImbalanceStd(symbol, interval, series, 10);
        var closePosSlp20 = wickCache.getClosePosNormSlope(symbol, interval, series, 20);

        // fora da prod mask: ifNull("micCandleUpperWick", doc::getMicCandleUpperWick, () -> WickUpperCalc.calculate(upperWickExt, index), doc::setMicCandleUpperWick, tasks);
        // fora da prod mask: ifNull("micCandleLowerWick", doc::getMicCandleLowerWick, () -> WickLowerCalc.calculate(lowerWickExt, index), doc::setMicCandleLowerWick, tasks);
        ifNull("micCandleUpperWickPct", doc::getMicCandleUpperWickPct, () -> WickUpperPctCalc.calculate(upperWickExt, series, index), doc::setMicCandleUpperWickPct, tasks);
        ifNull("micCandleLowerWickPct", doc::getMicCandleLowerWickPct, () -> WickLowerPctCalc.calculate(lowerWickExt, series, index), doc::setMicCandleLowerWickPct, tasks);
        ifNull("micWickPercUp", doc::getMicWickPercUp, () -> WickPercUpCalc.calculate(upperWickExt, series, index), doc::setMicWickPercUp, tasks);
        ifNull("micWickPercDown", doc::getMicWickPercDown, () -> WickPercDownCalc.calculate(lowerWickExt, series, index), doc::setMicWickPercDown, tasks);
        ifNull("micCandleWickImbalance", doc::getMicCandleWickImbalance, () -> WickImbalanceCalc.calculate(wickImbExt, index), doc::setMicCandleWickImbalance, tasks);
        ifNull("micWickImbalance", doc::getMicWickImbalance, () -> WickImbalanceAltCalc.calculate(wickImbExt, index), doc::setMicWickImbalance, tasks);
        ifNull("micCandleWickPressureScore", doc::getMicCandleWickPressureScore, () -> WickPressureScoreCalc.calculate(wickImbExt, index), doc::setMicCandleWickPressureScore, tasks);
        ifNull("micCandleShadowRatio", doc::getMicCandleShadowRatio, () -> WickShadowRatioCalc.calculate(upperWickExt, lowerWickExt, series, index), doc::setMicCandleShadowRatio, tasks);
        ifNull("micCandleWickBodyAlignment", doc::getMicCandleWickBodyAlignment, () -> WickBodyAlignmentCalc.calculate(wickImbExt, series, index), doc::setMicCandleWickBodyAlignment, tasks);
        ifNull("micShadowImbalanceScore", doc::getMicShadowImbalanceScore, () -> WickShadowImbalanceScoreCalc.calculate(upperWickExt, lowerWickExt, index), doc::setMicShadowImbalanceScore, tasks);
        ifNull("micCandleWickDominance", doc::getMicCandleWickDominance, () -> WickDominanceCalc.calculate(upperWickExt, lowerWickExt, index), doc::setMicCandleWickDominance, tasks);
        ifNull("micCandleWickExhaustion", doc::getMicCandleWickExhaustion, () -> WickExhaustionCalc.calculate(wickImbExt, series, index), doc::setMicCandleWickExhaustion, tasks);
        // fora da prod mask: ifNull("micCandleTotalWick", doc::getMicCandleTotalWick, () -> WickTotalCalc.calculate(upperWickExt, lowerWickExt, index), doc::setMicCandleTotalWick, tasks);
        ifNull("micCandleTotalWickPct", doc::getMicCandleTotalWickPct, () -> WickTotalPctCalc.calculate(upperWickExt, lowerWickExt, series, index), doc::setMicCandleTotalWickPct, tasks);
        ifNull("micCandleTotalWickAtrn", doc::getMicCandleTotalWickAtrn, () -> WickTotalAtrnCalc.calculate(upperWickExt, lowerWickExt, atr14, index), doc::setMicCandleTotalWickAtrn, tasks);
        ifNull("micCandleWickImbalanceNorm", doc::getMicCandleWickImbalanceNorm, () -> WickImbalanceNormCalc.calculate(upperWickExt, lowerWickExt, index), doc::setMicCandleWickImbalanceNorm, tasks);
        ifNull("micCandleWickImbalanceSlpW10", doc::getMicCandleWickImbalanceSlpW10, () -> WickImbalanceSlpW10Calc.calculate(wickImbSlp10, index), doc::setMicCandleWickImbalanceSlpW10, tasks);
        // fora da prod mask: ifNull("micCandleWickImbalanceVol10", doc::getMicCandleWickImbalanceVol10, () -> WickImbalanceVol10Calc.calculate(wickImbStd10, index), doc::setMicCandleWickImbalanceVol10, tasks);
        ifNull("micClosePosSlpW20", doc::getMicClosePosSlpW20, () -> WickClosePosSlpW20Calc.calculate(closePosSlp20, index), doc::setMicClosePosSlpW20, tasks);
        // fora da prod mask: ifNull("micCandleUpperWickMa10", doc::getMicCandleUpperWickMa10, () -> WickUpperMa10Calc.calculate(upperWickSma10, index), doc::setMicCandleUpperWickMa10, tasks);
        // fora da prod mask: ifNull("micCandleLowerWickMa10", doc::getMicCandleLowerWickMa10, () -> WickLowerMa10Calc.calculate(lowerWickSma10, index), doc::setMicCandleLowerWickMa10, tasks);
        ifNull("micUpperWickReturn", doc::getMicUpperWickReturn, () -> WickUpperReturnCalc.calculate(series, index), doc::setMicUpperWickReturn, tasks);
        ifNull("micLowerWickReturn", doc::getMicLowerWickReturn, () -> WickLowerReturnCalc.calculate(series, index), doc::setMicLowerWickReturn, tasks);

        // ── ShapePattern ──────────────────────────────────
        // fora da prod mask: var geoScoreExt = shapeCache.getGeometryScore(symbol, interval, series);
        // fora da prod mask: var compIdxExt = shapeCache.getCompressionIndex(symbol, interval, series);
        // fora da prod mask: var shapeIdxExt = shapeCache.getShapeIndex(symbol, interval, series);
        // fora da prod mask: var geoSma10 = shapeCache.getGeometrySma(symbol, interval, series, 10);
        // fora da prod mask: var geoSlp20 = shapeCache.getGeometrySlope(symbol, interval, series, 20);
        // fora da prod mask: var geoStd10 = shapeCache.getGeometryStd(symbol, interval, series, 10);
        // fora da prod mask: var geoSma48 = shapeCache.getGeometrySma(symbol, interval, series, 48);
        // fora da prod mask: var shapeIdxSma20 = shapeCache.getShapeIndexSma(symbol, interval, series, 20);
        // fora da prod mask: var shapeIdxStd20 = shapeCache.getShapeIndexStd(symbol, interval, series, 20);
        // fora da prod mask: var compSma20 = shapeCache.getCompressionSma(symbol, interval, series, 20);
        // fora da prod mask: var compSma48 = shapeCache.getCompressionSma(symbol, interval, series, 48);
        // fora da prod mask: var compStd48 = shapeCache.getCompressionStd(symbol, interval, series, 48);
        // fora da prod mask: var compZsc20 = shapeCache.getCompressionZscore(symbol, interval, series, 20);

        // fora da prod mask: ifNull("micCandleDirection", doc::getMicCandleDirection, () -> ShapeDirectionCalc.calculate(series, index), doc::setMicCandleDirection, tasks);
        // fora da prod mask: ifNull("micCandleType", doc::getMicCandleType, () -> ShapeTypeCalc.calculate(series, index), doc::setMicCandleType, tasks);
        // fora da prod mask: ifNull("micCandleShapeIndex", doc::getMicCandleShapeIndex, () -> ShapeIndexCalc.calculate(shapeIdxExt, index), doc::setMicCandleShapeIndex, tasks);
        // fora da prod mask: ifNull("micCandleSymmetryScore", doc::getMicCandleSymmetryScore, () -> ShapeSymmetryScoreCalc.calculate(series, index), doc::setMicCandleSymmetryScore, tasks);
        // fora da prod mask: ifNull("micCandleTriangleScore", doc::getMicCandleTriangleScore, () -> ShapeTriangleScoreCalc.calculate(series, index), doc::setMicCandleTriangleScore, tasks);
        // fora da prod mask: ifNull("micCandleGeometryScore", doc::getMicCandleGeometryScore, () -> ShapeGeometryScoreCalc.calculate(geoScoreExt, index), doc::setMicCandleGeometryScore, tasks);
        // fora da prod mask: ifNull("micCandleEntropy", doc::getMicCandleEntropy, () -> ShapeEntropyCalc.calculate(series, index), doc::setMicCandleEntropy, tasks);
        // fora da prod mask: ifNull("micCandleDojiScore", doc::getMicCandleDojiScore, () -> ShapeDojiScoreCalc.calculate(series, index), doc::setMicCandleDojiScore, tasks);
        // fora da prod mask: ifNull("micCandleImpulseScore", doc::getMicCandleImpulseScore, () -> ShapeImpulseScoreCalc.calculate(series, index), doc::setMicCandleImpulseScore, tasks);
        // fora da prod mask: ifNull("micCandleCompressionIndex", doc::getMicCandleCompressionIndex, () -> ShapeCompressionIndexCalc.calculate(compIdxExt, index), doc::setMicCandleCompressionIndex, tasks);
        // fora da prod mask: ifNull("micCandleDirectionPrstW10", doc::getMicCandleDirectionPrstW10, () -> ShapeDirectionPrstW10Calc.calculate(series, index), doc::setMicCandleDirectionPrstW10, tasks);
        // fora da prod mask: ifNull("micCandleDirectionPrstW20", doc::getMicCandleDirectionPrstW20, () -> ShapeDirectionPrstW20Calc.calculate(series, index), doc::setMicCandleDirectionPrstW20, tasks);
        // fora da prod mask: ifNull("micCandleGeometryMa10", doc::getMicCandleGeometryMa10, () -> ShapeGeometryMa10Calc.calculate(geoSma10, index), doc::setMicCandleGeometryMa10, tasks);
        // fora da prod mask: ifNull("micCandleGeometrySlpW20", doc::getMicCandleGeometrySlpW20, () -> ShapeGeometrySlpW20Calc.calculate(geoSlp20, index), doc::setMicCandleGeometrySlpW20, tasks);
        // fora da prod mask: ifNull("micCandleGeometryVol10", doc::getMicCandleGeometryVol10, () -> ShapeGeometryVol10Calc.calculate(geoStd10, index), doc::setMicCandleGeometryVol10, tasks);
        // fora da prod mask: ifNull("micCandleShapeIndexMa20", doc::getMicCandleShapeIndexMa20, () -> ShapeShapeIndexMa20Calc.calculate(shapeIdxSma20, index), doc::setMicCandleShapeIndexMa20, tasks);
        // fora da prod mask: ifNull("micCandleShapeIndexVol20", doc::getMicCandleShapeIndexVol20, () -> ShapeShapeIndexVol20Calc.calculate(shapeIdxStd20, index), doc::setMicCandleShapeIndexVol20, tasks);
        // fora da prod mask: ifNull("micCandleCompressionMa20", doc::getMicCandleCompressionMa20, () -> ShapeCompressionMa20Calc.calculate(compSma20, index), doc::setMicCandleCompressionMa20, tasks);
        // fora da prod mask: ifNull("micCandleCompressionMa48", doc::getMicCandleCompressionMa48, () -> ShapeCompressionMa48Calc.calculate(compSma48, index), doc::setMicCandleCompressionMa48, tasks);
        // fora da prod mask: ifNull("micCandleCompressionVol48", doc::getMicCandleCompressionVol48, () -> ShapeCompressionVol48Calc.calculate(compStd48, index), doc::setMicCandleCompressionVol48, tasks);
        // fora da prod mask: ifNull("micCandleCompressionZscore20", doc::getMicCandleCompressionZscore20, () -> ShapeCompressionZscore20Calc.calculate(compZsc20, index), doc::setMicCandleCompressionZscore20, tasks);
        // fora da prod mask: ifNull("micCandleShapeRegimeState", doc::getMicCandleShapeRegimeState, () -> ShapeRegimeStateCalc.calculate(geoScoreExt, compIdxExt, shapeIdxExt, series, index), doc::setMicCandleShapeRegimeState, tasks);
        // fora da prod mask: ifNull("micCandleTypeFlipRateW20", doc::getMicCandleTypeFlipRateW20, () -> ShapeTypeFlipRateW20Calc.calculate(series, index), doc::setMicCandleTypeFlipRateW20, tasks);
        // fora da prod mask: ifNull("micCandleDirectionFlipRateW20", doc::getMicCandleDirectionFlipRateW20, () -> ShapeDirectionFlipRateW20Calc.calculate(series, index), doc::setMicCandleDirectionFlipRateW20, tasks);
        // fora da prod mask: ifNull("micCandleGeometryMa48", doc::getMicCandleGeometryMa48, () -> ShapeGeometryMa48Calc.calculate(geoSma48, index), doc::setMicCandleGeometryMa48, tasks);

        if (!tasks.isEmpty()) {
            execute(tasks);
        }

        return doc;
    }

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
            catch (Exception e) { throw new RuntimeException("[MIC] erro", e); }
        }
    }
}
