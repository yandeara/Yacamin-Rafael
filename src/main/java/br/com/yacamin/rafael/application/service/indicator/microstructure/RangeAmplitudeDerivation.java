package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.microstructure.RangeCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.AtrNormalizeDerivation;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Service
@RequiredArgsConstructor
public class RangeAmplitudeDerivation {

    private static final double EPS = 1e-12;

    private final RangeCacheService rangeCacheService;
    private final AtrNormalizeDerivation atrNormalizeDerivation;

    // =========================================================================
    // 1) RANGE RAW
    // =========================================================================
    public double calculateRange(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getRange(candle.getSymbol(), candle.getInterval(), series)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 9) CANDLE RANGE MICROSTRUCTURE
    // =========================================================================

    /** mic_candle_range = high - low */
    public double micCandleRange(SymbolCandle c) {
        return c.getHigh() - c.getLow();
    }

    /** range / média(range últimos 20 candles EXCLUINDO o atual) */
    public double micCandleVolatilityInside(SymbolCandle candle, BarSeries series, int index) {
        double rangeNow = calculateRange(candle, series, index);

        double avgPrev20 = rangeCacheService
                .getRangeMaLag(candle.getSymbol(), candle.getInterval(), series, 20, 1) // lag=1 => exclui atual
                .getValue(index).doubleValue();

        if (avgPrev20 < EPS) return 0.0;
        return rangeNow / (avgPrev20 + EPS);
    }

    /** range / close */
    public double micCandleSpreadRatio(SymbolCandle candle, BarSeries series, int index) {
        double r = calculateRange(candle, series, index);
        double c = series.getBar(index).getClosePrice().doubleValue();
        if (Math.abs(c) < EPS) return 0.0;
        return r / (c + EPS);
    }

    /** |body| / range */
    public double micCandleBrr(SymbolCandle c) {
        double body = Math.abs(c.getClose() - c.getOpen());
        double range = c.getHigh() - c.getLow();
        if (range < EPS) return 0.0;
        return body / (range + EPS);
    }

    /** (close - mid_range) / range */
    public double micCandleLmr(SymbolCandle c) {
        double mid = (c.getHigh() + c.getLow()) / 2.0;
        double range = c.getHigh() - c.getLow();
        if (range < EPS) return 0.0;
        return (c.getClose() - mid) / (range + EPS);
    }

    // =========================================================================
    // 10) RANGE-BASED RETURNS
    // =========================================================================

    /** (high - low) / close_{t-1} */
    public double micRangeReturn(SymbolCandle candle, BarSeries series, int index) {
        if (index <= 0) return 0.0;
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0.0;
        double range = calculateRange(candle, series, index);
        return range / (prevClose + EPS);
    }

    /** (high - close_{t-1}) / close_{t-1} */
    public double micHighReturn(BarSeries series, int index) {
        if (index <= 0) return 0.0;
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0.0;
        return (series.getBar(index).getHighPrice().doubleValue() - prevClose) / (prevClose + EPS);
    }

    /** (low - close_{t-1}) / close_{t-1} */
    public double micLowReturn(BarSeries series, int index) {
        if (index <= 0) return 0.0;
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0.0;
        return (series.getBar(index).getLowPrice().doubleValue() - prevClose) / (prevClose + EPS);
    }

    /** max(|high-prevClose|, |low-prevClose|) / prevClose */
    public double micExtremeRangeReturn(BarSeries series, int index) {
        if (index <= 0) return 0.0;

        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0.0;

        double h = series.getBar(index).getHighPrice().doubleValue();
        double l = series.getBar(index).getLowPrice().doubleValue();

        double up = Math.abs(h - prevClose);
        double down = Math.abs(l - prevClose);

        return Math.max(up, down) / (prevClose + EPS);
    }

    // =========================================================================
    // 2) TRUE RANGE RAW
    // =========================================================================
    public double calculateTrueRange(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getTrueRange(candle.getSymbol(), candle.getInterval(), series)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 3) RANGE NORMALIZED
    // =========================================================================
    public double calculateRangeAtrn(SymbolCandle candle, ATRIndicator atr, BarSeries series, int index) {
        double range = calculateRange(candle, series, index);
        return atrNormalizeDerivation.normalize(atr, index, range);
    }

    public double calculateTrueRangeAtrn(SymbolCandle candle, ATRIndicator atr, BarSeries series, int index) {
        double tr = calculateTrueRange(candle, series, index);
        return atrNormalizeDerivation.normalize(atr, index, tr);
    }

    public double calculateRangeStdn(SymbolCandle candle, BarSeries series, int index, int window) {
        double r = calculateRange(candle, series, index);
        double sd = rangeCacheService
                .getRangeVol(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();

        if (sd < EPS) return 0.0;
        return r / (sd + EPS);
    }

    // =========================================================================
    // HLC3 MOVING AVERAGES
    // =========================================================================
    public double calculateHlc3Ma10(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getHlc3Ma(candle.getSymbol(), candle.getInterval(), series, 10)
                .getValue(index).doubleValue();
    }

    public double calculateHlc3Ma20(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getHlc3Ma(candle.getSymbol(), candle.getInterval(), series, 20)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // HLC3 SLOPE / VOL
    // =========================================================================
    public double calculateHlc3SlopeW20(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getHlc3Slope(candle.getSymbol(), candle.getInterval(), series, 20)
                .getValue(index).doubleValue();
    }

    public double calculateHlc3Vol10(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getHlc3Vol(candle.getSymbol(), candle.getInterval(), series, 10)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // LOG RANGE MA / SLOPE / VOL
    // =========================================================================
    public double calculateLogRangeMa10(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getLogRangeMaIgnoreZero(candle.getSymbol(), candle.getInterval(), series, 10)
                .getValue(index).doubleValue();
    }

    public double calculateLogRangeSlopeW20(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getLogRangeSlope(candle.getSymbol(), candle.getInterval(), series, 20)
                .getValue(index).doubleValue();
    }

    public double calculateLogRangeVol10(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getLogRangeVol(candle.getSymbol(), candle.getInterval(), series, 10)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 4) RANGE DYNAMICS
    // =========================================================================
    public double calculateRangeSlope(SymbolCandle candle, BarSeries series, int index, int window) {
        return rangeCacheService
                .getRangeSlope(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateRangeAcceleration(SymbolCandle candle, BarSeries series, int index, int lookback) {
        int past = index - lookback;
        if (past < 0) return 0.0;

        var range = rangeCacheService.getRange(candle.getSymbol(), candle.getInterval(), series);

        double nowV = range.getValue(index).doubleValue();
        double pastV = range.getValue(past).doubleValue();

        return nowV - pastV;
    }

    // =========================================================================
    // 5) RANGE SMOOTH & VOL
    // =========================================================================
    public double calculateRangeSmooth(SymbolCandle candle, BarSeries series, int index, int window) {
        return rangeCacheService
                .getRangeMa(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateRangeVolatility(SymbolCandle candle, BarSeries series, int index, int window) {
        return rangeCacheService
                .getRangeVol(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 6) RANGE COMPRESSION / SQUEEZE
    // =========================================================================
    public double calculateRangeSqueeze(SymbolCandle candle, BarSeries series, int index) {
        double ma20 = calculateRangeSmooth(candle, series, index, 20);
        if (ma20 < EPS) return 0.0;

        double r = calculateRange(candle, series, index);
        return r / (ma20 + EPS);
    }

    public double calculateRangeCompressionW20(SymbolCandle candle, BarSeries series, int index) {
        double ma20 = calculateRangeSmooth(candle, series, index, 20);
        if (Math.abs(ma20) < EPS) return 0.0;

        double vol20 = calculateRangeVolatility(candle, series, index, 20);
        return vol20 / (ma20 + EPS);
    }

    // =========================================================================
    // 7) RANGE ASYMMETRY
    // =========================================================================
    public double calculateRangeAsymmetry(BarSeries series, int index) {
        var bar = series.getBar(index);

        double h = bar.getHighPrice().doubleValue();
        double l = bar.getLowPrice().doubleValue();
        double c = bar.getClosePrice().doubleValue();

        double up = h - c;
        double down = c - l;

        if (Math.abs(down) < EPS) return 0.0;
        return up / (down + EPS);
    }

    // =========================================================================
    // 8) RANGE HEADROOM
    // =========================================================================
    public double calculateRangeHeadroom(SymbolCandle candle, ATRIndicator atr, BarSeries series, int index) {
        double atrV = atr.getValue(index).doubleValue();
        if (atrV < EPS) return 0.0;

        double range = calculateRange(candle, series, index);
        return (atrV - range) / (atrV + EPS);
    }

    // =========================================================================
    // RANGE / ATR RATIO
    // =========================================================================
    public double calculateRangeAtrRatio(SymbolCandle candle, ATRIndicator atr, BarSeries series, int index) {
        double atrV = atr.getValue(index).doubleValue();
        if (atrV < EPS) return 0.0;

        double range = calculateRange(candle, series, index);
        return range / (atrV + EPS);
    }

    // =========================================================================
    // GAPINESS / SHOCK SCORE
    // =========================================================================
    public double calculateGapRatio(SymbolCandle candle, BarSeries series, int index) {
        double tr = calculateTrueRange(candle, series, index);
        double r  = calculateRange(candle, series, index);

        if (Math.abs(tr) < EPS) return 0.0;
        return (tr - r) / (tr + EPS);
    }

    public double calculateTrRangeRatio(SymbolCandle candle, BarSeries series, int index) {
        double tr = calculateTrueRange(candle, series, index);
        double r  = calculateRange(candle, series, index);

        if (Math.abs(r) < EPS) return 0.0;
        return tr / (r + EPS);
    }

    // =========================================================================
    // LOG RANGE PERCENTILE (w48)
    // =========================================================================
    public double calculateLogRangePercentileW48(SymbolCandle candle, BarSeries series, int index) {
        return rangeCacheService
                .getLogRangePercentile(candle.getSymbol(), candle.getInterval(), series, 48)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // RANGE REGIME STATE (0..3)
    // =========================================================================
    public double calculateRangeRegimeState(SymbolCandle candle, ATRIndicator atr, BarSeries series, int index) {

        double range = calculateRange(candle, series, index);
        double tr    = calculateTrueRange(candle, series, index);

        double squeeze = calculateRangeSqueeze(candle, series, index);        // range/ma20(range)
        double comp    = calculateRangeCompressionW20(candle, series, index); // vol20/ma20
        double gapR    = (Math.abs(tr) < EPS) ? 0.0 : (tr - range) / (tr + EPS);
        double trRR    = (Math.abs(range) < EPS) ? 0.0 : tr / (range + EPS);

        double atrn = atrNormalizeDerivation.normalize(atr, index, range);

        boolean shock =
                gapR > 0.35 ||
                        trRR > 1.25 ||
                        atrn > 2.5;

        boolean compressed =
                squeeze > 0.0 && squeeze < 0.75 &&
                        comp > 0.0 && comp < 0.60 &&
                        atrn > 0.0 && atrn < 1.0;

        boolean expanded =
                squeeze > 1.25 ||
                        comp > 1.20 ||
                        atrn > 1.60;

        if (shock) return 3;
        if (compressed) return 0;
        if (expanded) return 2;
        return 1;
    }

    // =========================================================================
    // RAW helpers (mantidos)
    // =========================================================================
    public double calculateHlc3(SymbolCandle candle) {
        return (candle.getHigh() + candle.getLow() + candle.getClose()) / 3.0;
    }

    public double calculateLogRange(SymbolCandle candle) {
        double range = candle.getHigh() - candle.getLow();
        if (range < EPS) return 0.0;
        return Math.log(range);
    }
}
