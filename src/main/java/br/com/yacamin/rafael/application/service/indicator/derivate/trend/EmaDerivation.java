package br.com.yacamin.rafael.application.service.indicator.derivate.trend;

import br.com.yacamin.rafael.application.service.indicator.extension.DeviationIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

@Service
@RequiredArgsConstructor
public class EmaDerivation {

    private static final double EPS = 1e-12;

    // =============================================================================================
    // EMA RAW
    // =============================================================================================
    public double ema(EMAIndicator ema, int last) {
        return ema.getValue(last).doubleValue();
    }

    // =============================================================================================
    // EMA SLOPE / ACC
    // =============================================================================================
    public double slope(LinearRegressionSlopeIndicator slp, int last) {
        return slp.getValue(last).doubleValue();
    }

    public double slopeAcc(DifferenceIndicator acc, int last) {
        return acc.getValue(last).doubleValue();
    }

    // =============================================================================================
    // ZSCORE (EMA vs SMA, usando STD)
    // =============================================================================================
    public double zscore(EMAIndicator ema, SMAIndicator sma, StandardDeviationIndicator std, int last) {
        double e = ema.getValue(last).doubleValue();
        double m = sma.getValue(last).doubleValue();
        double sd = std.getValue(last).doubleValue();
        if (sd == 0.0) return 0.0;
        return (e - m) / sd;
    }

    // =============================================================================================
    // DISTANCE (abs) + DELTA (signed)
    // =============================================================================================
    public double absDistance(Indicator<Num> a, Indicator<Num> b, int last) {
        return Math.abs(a.getValue(last).doubleValue() - b.getValue(last).doubleValue());
    }

    public double delta(Indicator<Num> a, Indicator<Num> b, int last) {
        return a.getValue(last).doubleValue() - b.getValue(last).doubleValue();
    }

    // =============================================================================================
    // RATIO
    // =============================================================================================
    public double ratio(EMAIndicator a, EMAIndicator b, int last) {
        double av = a.getValue(last).doubleValue();
        double bv = b.getValue(last).doubleValue();
        if (Math.abs(bv) < EPS) return 0.0;
        return av / bv;
    }

    // =============================================================================================
    // TDS (DeviationIndicator cacheado)
    // =============================================================================================
    public double tds(DeviationIndicator ind, int last) {
        return ind.getValue(last).doubleValue();
    }

    // =============================================================================================
    // TVR = ATR / |slope|
    // =============================================================================================
    public double tvr(ATRIndicator atr14, LinearRegressionSlopeIndicator slp, int last) {
        double atr = atr14.getValue(last).doubleValue();
        double s = slp.getValue(last).doubleValue();
        double abs = Math.abs(s);
        if (abs < EPS) return 0.0;
        return atr / abs;
    }

    // =============================================================================================
    // CTS close > EMA(window)
    // =============================================================================================
    public double ctsCloseAboveEma(BarSeries series, EMAIndicator ema, int window, int last) {
        int up = 0;
        int start = last - window + 1;
        for (int i = start; i <= last; i++) {
            double close = series.getBar(i).getClosePrice().doubleValue();
            double e = ema.getValue(i).doubleValue();
            if (close > e) up++;
        }
        return (double) up / (double) window;
    }

    // =============================================================================================
    // CTS EMA 8/20/50 ordering score/3
    // =============================================================================================
    public double ctsEmaRibbon(EMAIndicator e8, EMAIndicator e20, EMAIndicator e50, int last) {
        double a = e8.getValue(last).doubleValue();
        double b = e20.getValue(last).doubleValue();
        double c = e50.getValue(last).doubleValue();

        int score = 0;
        if (a > b) score++;
        if (b > c) score++;
        if (a > c) score++;
        return score / 3.0;
    }

    // =============================================================================================
    // ALIGNMENT 8/20/50 (score, normalized, binary, delta)
    // =============================================================================================
    public int alignmentScore(EMAIndicator fast, EMAIndicator mid, EMAIndicator slow, int last) {
        double f = fast.getValue(last).doubleValue();
        double m = mid.getValue(last).doubleValue();
        double s = slow.getValue(last).doubleValue();
        int score = 0;
        if (f > m) score++;
        if (m > s) score++;
        if (f > s) score++;
        return score;
    }

    public double alignmentNormalized(int score) {
        return score / 3.0;
    }

    public double alignmentBinary(EMAIndicator mid, EMAIndicator slow, int last) {
        return mid.getValue(last).doubleValue() > slow.getValue(last).doubleValue() ? 1.0 : 0.0;
    }

    public double alignmentDelta(EMAIndicator mid, EMAIndicator slow, double close, int last) {
        double m = mid.getValue(last).doubleValue();
        double s = slow.getValue(last).doubleValue();
        if (Math.abs(close) < EPS) return 0.0;
        return (m - s) / close;
    }

    // =============================================================================================
    // CROSSOVER (binary + delta + delta_atrn)
    // =============================================================================================
    public int crossBinary(EMAIndicator fast, EMAIndicator slow, int last) {
        double fNow = fast.getValue(last).doubleValue();
        double sNow = slow.getValue(last).doubleValue();
        double fPrev = fast.getValue(last - 1).doubleValue();
        double sPrev = slow.getValue(last - 1).doubleValue();

        if (fPrev < sPrev && fNow > sNow) return 1;
        if (fPrev > sPrev && fNow < sNow) return -1;
        return 0;
    }

    public double crossDelta(EMAIndicator fast, EMAIndicator slow, double close, int last) {
        double fNow = fast.getValue(last).doubleValue();
        double sNow = slow.getValue(last).doubleValue();
        if (Math.abs(close) < EPS) return 0.0;
        return (fNow - sNow) / close;
    }

    public double crossDeltaAtrn(EMAIndicator fast, EMAIndicator slow, double close, ATRIndicator atr14, int last) {
        double d = crossDelta(fast, slow, close, last);
        double atr = atr14.getValue(last).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return d / atr;
    }

    // =============================================================================================
    // DURATION: candles since EMA_A > EMA_B
    // =============================================================================================
    public double durationAbove(EMAIndicator a, EMAIndicator b, int last) {
        double count = 0.0;
        for (int i = last; i >= 0; i--) {
            if (a.getValue(i).doubleValue() > b.getValue(i).doubleValue()) count++;
            else break;
        }
        return count;
    }

    // =============================================================================================
    // Trend Maturity (duration capped/window)
    // =============================================================================================
    public double trendMaturity(EMAIndicator a, EMAIndicator b, int window, int last) {
        double dur = durationAbove(a, b, last);
        double capped = Math.min(dur, (double) window);
        return capped / (double) window;
    }

    // =============================================================================================
    // TCS slope (mean of slopes 8/20/50)
    // =============================================================================================
    public double tcsSlope(LinearRegressionSlopeIndicator s8, LinearRegressionSlopeIndicator s20, LinearRegressionSlopeIndicator s50, int last) {
        double a = s8.getValue(last).doubleValue();
        double b = s20.getValue(last).doubleValue();
        double c = s50.getValue(last).doubleValue();
        return (a + b + c) / 3.0;
    }

    // =============================================================================================
    // EMA20 slope SNR (aligned mean / std of returns)
    // =============================================================================================
    public double slopeSnr(BarSeries series, LinearRegressionSlopeIndicator slope, int window, int last) {
        double slp = slope.getValue(last).doubleValue();
        int dir = (slp > 0 ? 1 : (slp < 0 ? -1 : 0));
        if (dir == 0) return 0.0;

        double[] rets = new double[window];
        double sumAligned = 0.0;

        for (int i = 0; i < window; i++) {
            int idx = last - i;
            int idxPrev = idx - 1;
            double closeNow = series.getBar(idx).getClosePrice().doubleValue();
            double closePrev = series.getBar(idxPrev).getClosePrice().doubleValue();
            double r = (closeNow / closePrev) - 1.0;
            rets[window - 1 - i] = r;
            sumAligned += dir * r;
        }

        int n = window;
        double meanAligned = sumAligned / n;

        double mean = 0.0;
        for (double r : rets) mean += r;
        mean /= n;

        double var = 0.0;
        for (double r : rets) {
            double diff = r - mean;
            var += diff * diff;
        }
        var /= n;

        double std = Math.sqrt(var);
        if (std == 0.0) return 0.0;

        return meanAligned / std;
    }

    // =============================================================================================
    // TCP — close channel position (window)
    // =============================================================================================
    public double closeTcp(BarSeries series, int window, int last) {
        int start = Math.max(0, last - window + 1);

        double minClose = Double.POSITIVE_INFINITY;
        double maxClose = Double.NEGATIVE_INFINITY;

        for (int i = start; i <= last; i++) {
            double c = series.getBar(i).getClosePrice().doubleValue();
            if (c < minClose) minClose = c;
            if (c > maxClose) maxClose = c;
        }

        double closeNow = series.getBar(last).getClosePrice().doubleValue();
        double range = maxClose - minClose;
        if (range == 0.0) return 0.0;

        return (closeNow - minClose) / range;
    }
}
