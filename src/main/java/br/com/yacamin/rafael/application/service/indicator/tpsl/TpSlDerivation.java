package br.com.yacamin.rafael.application.service.indicator.tpsl;

import br.com.yacamin.rafael.domain.RafaelBar;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Service
@RequiredArgsConstructor
public class TpSlDerivation {

    private static final double EPS = 1e-12;

    // Setup fixo (30m)
    private static final double TP_PCT = 0.01; // 1%
    private static final double SL_PCT = 0.02; // 2%

    // =========================================================================
    // HELPERS — OHLC (via RafaelBar)
    // =========================================================================
    private RafaelBar bar(BarSeries series, int index) {
        return (RafaelBar) series.getBar(index);
    }

    private double high(BarSeries series, int index) {
        return bar(series, index).getHighPrice().doubleValue();
    }

    private double low(BarSeries series, int index) {
        return bar(series, index).getLowPrice().doubleValue();
    }

    private double close(BarSeries series, int index) {
        return bar(series, index).getClosePrice().doubleValue();
    }

    // =========================================================================
    // 1) SETUP PRICES (per candle) — FIXED TP/SL
    // =========================================================================

    public double calculateTpPriceLong(SymbolCandle c) {
        double p = c.getClose();
        if (Math.abs(p) < EPS) return 0.0;
        return p * (1.0 + TP_PCT);
    }

    public double calculateSlPriceLong(SymbolCandle c) {
        double p = c.getClose();
        if (Math.abs(p) < EPS) return 0.0;
        return p * (1.0 - SL_PCT);
    }

    public double calculateTpPriceShort(SymbolCandle c) {
        double p = c.getClose();
        if (Math.abs(p) < EPS) return 0.0;
        return p * (1.0 - TP_PCT);
    }

    public double calculateSlPriceShort(SymbolCandle c) {
        double p = c.getClose();
        if (Math.abs(p) < EPS) return 0.0;
        return p * (1.0 + SL_PCT);
    }

    // =========================================================================
    // 2) DISTANCES (absolute price distances)
    // =========================================================================

    public double calculateTpDistLong(SymbolCandle c, double tpPriceLong) {
        double p = c.getClose();
        double d = tpPriceLong - p;
        return (d > 0.0) ? d : 0.0;
    }

    public double calculateTpDistShort(SymbolCandle c, double tpPriceShort) {
        double p = c.getClose();
        double d = p - tpPriceShort;
        return (d > 0.0) ? d : 0.0;
    }

    public double calculateSlDistLong(SymbolCandle c, double slPriceLong) {
        double p = c.getClose();
        double d = p - slPriceLong;
        return (d > 0.0) ? d : 0.0;
    }

    public double calculateSlDistShort(SymbolCandle c, double slPriceShort) {
        double p = c.getClose();
        double d = slPriceShort - p;
        return (d > 0.0) ? d : 0.0;
    }

    // =========================================================================
    // 3) NORMALIZATION — ATR14 (ATR in price)
    // =========================================================================

    public double normalizeByAtr(ATRIndicator atr14, int index, double value) {
        double a = atr14.getValue(index).doubleValue();
        if (a < EPS) return 0.0;
        return value / (a + EPS);
    }

    // =========================================================================
    // 4) REALIZED VOL (RV) / EWMA VOL (both in return units)
    // =========================================================================

    public double calculateRvVol(BarSeries series, int index, int window) {
        if (index <= 1) return 0.0;

        int nBars = Math.min(window, index + 1);
        if (nBars < 3) return 0.0;

        int start = index - nBars + 1;
        int nRet = nBars - 1;

        double[] r = new double[nRet];

        double prev = close(series, start);
        for (int i = 0; i < nRet; i++) {
            double cur = close(series, start + 1 + i);
            if (prev < EPS || cur < EPS) r[i] = 0.0;
            else r[i] = Math.log(cur / prev);
            prev = cur;
        }

        return std(r);
    }

    public double calculateEwmaVol(BarSeries series, int index, int window) {
        if (index <= 1) return 0.0;

        int nBars = Math.min(window, index + 1);
        if (nBars < 3) return 0.0;

        int start = index - nBars + 1;
        int nRet = nBars - 1;

        double alpha = 2.0 / (window + 1.0);
        double var = 0.0;

        double prev = close(series, start);
        for (int i = 0; i < nRet; i++) {
            double cur = close(series, start + 1 + i);

            double ret;
            if (prev < EPS || cur < EPS) ret = 0.0;
            else ret = Math.log(cur / prev);

            double r2 = ret * ret;

            if (i == 0) var = r2;
            else var = alpha * r2 + (1.0 - alpha) * var;

            prev = cur;
        }

        return Math.sqrt(Math.max(0.0, var));
    }

    // vol em retorno -> vol em preço ~ close*vol
    public double normalizeByReturnVol(SymbolCandle c, double valueInPrice, double volInReturn) {
        double p = c.getClose();
        if (p < EPS || volInReturn < EPS) return 0.0;
        return valueInPrice / (p * volInReturn + EPS);
    }

    // =========================================================================
    // 5) RISK/REWARD
    // =========================================================================
    public double calculateRrRatio(double tpDist, double slDist) {
        if (slDist < EPS) return 0.0;
        return tpDist / (slDist + EPS);
    }

    // =========================================================================
    // 6) TP in units of spread / fee
    // =========================================================================

    public double calculateTpDistInSpread(double tpDistInPrice, double spreadAvgAbs) {
        if (spreadAvgAbs < EPS) return 0.0;
        return tpDistInPrice / (spreadAvgAbs + EPS);
    }

    public double calculateTpDistInFee(SymbolCandle c, double tpDistInPrice, double feeRoundtripPct) {
        double p = c.getClose();
        if (p < EPS || feeRoundtripPct < EPS) return 0.0;

        double tpRet = tpDistInPrice / (p + EPS);
        return tpRet / (feeRoundtripPct + EPS);
    }

    // =========================================================================
    // 7) HEADROOM / ROOM (semantic)
    // =========================================================================
    public double passthrough(double v) {
        return v;
    }

    // =========================================================================
    // 8) HEADROOM/ROOM using candle extremes (raw distances in price)
    // =========================================================================
    public double calculateHeadroomHighToTpLong(SymbolCandle c, double tpPriceLong) {
        double d = tpPriceLong - c.getHigh();
        return (d > 0.0) ? d : 0.0;
    }

    public double calculateHeadroomLowToTpShort(SymbolCandle c, double tpPriceShort) {
        double d = c.getLow() - tpPriceShort;
        return (d > 0.0) ? d : 0.0;
    }

    public double calculateRoomLowToSlLong(SymbolCandle c, double slPriceLong) {
        double d = c.getLow() - slPriceLong;
        return (d > 0.0) ? d : 0.0;
    }

    public double calculateRoomHighToSlShort(SymbolCandle c, double slPriceShort) {
        double d = slPriceShort - c.getHigh();
        return (d > 0.0) ? d : 0.0;
    }

    // =========================================================================
    // 9) DONCHIAN (isolado no TPSL — sem cache externo)
    // =========================================================================

    public double calculateDonchHigh(BarSeries series, int index, int window) {
        int start = Math.max(0, index - window + 1);
        double best = high(series, start);
        for (int i = start + 1; i <= index; i++) {
            double v = high(series, i);
            if (v > best) best = v;
        }
        return best;
    }

    public double calculateDonchLow(BarSeries series, int index, int window) {
        int start = Math.max(0, index - window + 1);
        double best = low(series, start);
        for (int i = start + 1; i <= index; i++) {
            double v = low(series, i);
            if (v < best) best = v;
        }
        return best;
    }

    /** (close - low) / (high - low) */
    public double calculateDonchPos(SymbolCandle c, double donchHigh, double donchLow) {
        double r = donchHigh - donchLow;
        if (r < EPS) return 0.0;
        return (c.getClose() - donchLow) / (r + EPS);
    }

    /** dist_to_donch_high = (donchHigh - close) */
    public double calculateDistToDonchHigh(SymbolCandle c, double donchHigh) {
        double d = donchHigh - c.getClose();
        return (d > 0.0) ? d : 0.0;
    }

    /** dist_to_donch_low = (close - donchLow) */
    public double calculateDistToDonchLow(SymbolCandle c, double donchLow) {
        double d = c.getClose() - donchLow;
        return (d > 0.0) ? d : 0.0;
    }

    // =========================================================================
    // INTERNAL: STD
    // =========================================================================
    private double std(double[] v) {
        int n = v.length;
        if (n < 2) return 0.0;

        double mean = 0.0;
        for (double x : v) mean += x;
        mean /= n;

        double var = 0.0;
        for (double x : v) {
            double d = x - mean;
            var += d * d;
        }
        var /= (n - 1.0);

        return Math.sqrt(Math.max(0.0, var));
    }

    // =========================================================================
    // 10) BAND POSITION — Bollinger (percentB / z) e Keltner pos
    // =========================================================================
    private static final double BOLL_K = 2.0;
    private static final double KELT_M = 1.5;

    /** SMA(close, window) */
    public double calculateSmaClose(BarSeries series, int index, int window) {
        int n = Math.min(window, index + 1);
        if (n <= 0) return 0.0;
        int start = index - n + 1;

        double sum = 0.0;
        for (int i = start; i <= index; i++) sum += close(series, i);
        return sum / (double) n;
    }

    /** STD(close, window) — sample std (n-1) */
    public double calculateStdClose(BarSeries series, int index, int window) {
        int n = Math.min(window, index + 1);
        if (n < 2) return 0.0;
        int start = index - n + 1;

        double[] v = new double[n];
        int k = 0;
        for (int i = start; i <= index; i++) v[k++] = close(series, i);

        return std(v);
    }

    /** bb_percentB = (close - (mid - k*std)) / ((mid+k*std)-(mid-k*std)) */
    public double calculateBbPercentB(BarSeries series, int index, int window) {
        double mid = calculateSmaClose(series, index, window);
        double sd  = calculateStdClose(series, index, window);
        if (sd < EPS) return 0.0;

        double up  = mid + BOLL_K * sd;
        double low = mid - BOLL_K * sd;
        double den = (up - low);
        if (Math.abs(den) < EPS) return 0.0;

        double c = close(series, index);
        return (c - low) / (den + EPS);
    }

    /** bb_z = (close - bb_mid) / bb_std */
    public double calculateBbZ(BarSeries series, int index, int window) {
        double mid = calculateSmaClose(series, index, window);
        double sd  = calculateStdClose(series, index, window);
        if (sd < EPS) return 0.0;

        double c = close(series, index);
        return (c - mid) / (sd + EPS);
    }

    /** typical = (H+L+C)/3 */
    private double typical(BarSeries series, int index) {
        return (high(series, index) + low(series, index) + close(series, index)) / 3.0;
    }

    /** EMA(typical, period) (seed = first value da janela) */
    public double calculateEmaTypical(BarSeries series, int index, int period) {
        int n = Math.min(period, index + 1);
        if (n <= 0) return 0.0;
        int start = index - n + 1;

        double alpha = 2.0 / (period + 1.0);

        double ema = typical(series, start); // seed simples (ok)
        for (int i = start + 1; i <= index; i++) {
            double x = typical(series, i);
            ema = alpha * x + (1.0 - alpha) * ema;
        }
        return ema;
    }

    /** kelt_pos = (close - kelt_mid) / (m * ATR(period)) */
    public double calculateKeltPos(BarSeries series, int index, int period, ATRIndicator atr) {
        double mid = calculateEmaTypical(series, index, period);
        double a = atr.getValue(index).doubleValue();
        double width = KELT_M * a;
        if (Math.abs(width) < EPS) return 0.0;

        double c = close(series, index);
        return (c - mid) / (width + EPS);
    }

    // =========================================================================
// 11) MARKET EFFICIENCY
// eff_ratio = abs(close[t]-close[t-w]) / sum(abs(delta close)) over w bars
// =========================================================================
    public double calculateEfficiencyRatio(BarSeries series, int index, int window) {
        if (index - window < 0) return 0.0;

        double cNow = close(series, index);
        double cOld = close(series, index - window);

        double net = Math.abs(cNow - cOld);

        double path = 0.0;
        for (int i = index - window + 1; i <= index; i++) {
            path += Math.abs(close(series, i) - close(series, i - 1));
        }

        if (path < EPS) return 0.0;
        return net / (path + EPS);
    }

    public double calculateChopEff(double effRatio) {
        return 1.0 - effRatio;
    }

// =========================================================================
// 12) HELPERS — EMA / ZSCORE / DELTAS for derivatives series
// =========================================================================

    /** EMA last value of series (len=n). alpha = 2/(n+1) */
    public double emaLast(double[] series) {
        int n = (series == null) ? 0 : series.length;
        if (n <= 0) return 0.0;
        if (n == 1) return series[0];

        double alpha = 2.0 / (n + 1.0);
        double ema = series[0];
        for (int i = 1; i < n; i++) {
            ema = alpha * series[i] + (1.0 - alpha) * ema;
        }
        return ema;
    }

    /** zscore of last element inside the window series */
    public double zscoreLast(double[] series) {
        int n = (series == null) ? 0 : series.length;
        if (n < 2) return 0.0;

        double mean = 0.0;
        for (double v : series) mean += v;
        mean /= n;

        double var = 0.0;
        for (double v : series) {
            double d = v - mean;
            var += d * d;
        }
        var /= (n - 1.0);

        double sd = Math.sqrt(Math.max(0.0, var));
        if (sd < EPS) return 0.0;

        double last = series[n - 1];
        return (last - mean) / (sd + EPS);
    }

    /** delta_1 = last - prev */
    public double delta1(double[] series2) {
        if (series2 == null || series2.length < 2) return 0.0;
        return series2[series2.length - 1] - series2[series2.length - 2];
    }

    /** pct_1 = last/prev - 1 */
    public double pct1(double[] series2) {
        if (series2 == null || series2.length < 2) return 0.0;
        double prev = series2[series2.length - 2];
        double last = series2[series2.length - 1];
        if (Math.abs(prev) < EPS) return 0.0;
        return (last / (prev + EPS)) - 1.0;
    }

    /** build delta series from a level series (len=n) -> deltas len=n-1 */
    public double[] deltaSeries(double[] levelSeries) {
        if (levelSeries == null || levelSeries.length < 2) return new double[0];
        int n = levelSeries.length - 1;
        double[] d = new double[n];
        for (int i = 1; i < levelSeries.length; i++) {
            d[i - 1] = levelSeries[i] - levelSeries[i - 1];
        }
        return d;
    }
}
