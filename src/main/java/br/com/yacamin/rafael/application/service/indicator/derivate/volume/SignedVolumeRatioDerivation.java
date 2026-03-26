package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class SignedVolumeRatioDerivation {

    private static final double EPS = 1e-12;

    // ============================================================================
    // SVR raw (pure)
    // SVR(t) = (buy - sell) / (buy + sell)
    // ============================================================================
    public double svrAt(BarSeries series, int index) {
        RafaelBar bar = (RafaelBar) series.getBar(index);

        double buy  = bar.getTakerBuyBaseVolume().doubleValue();
        double sell = bar.getTakerSellBaseVolume().doubleValue();

        double total = buy + sell; // total==0 -> explode (ok)
        return (buy - sell) / total;
    }

    // mantém compat (se outros lugares usam)
    public double svr(SymbolCandle candle, BarSeries series, int index) {
        return svrAt(series, index);
    }

    // ============================================================================
    // REL: svr_now / mean(svr) na janela
    // ============================================================================
    public double svrRel(SymbolCandle candle, BarSeries series, int index, int window) {
        return svrRelAt(series, index, window);
    }

    public double svrRelAt(BarSeries series, int index, int window) {
        int start = index - window + 1;
        if (start < 0) throw new IllegalStateException("Index insuficiente para SVR-Rel");

        double sum = 0.0;
        for (int i = start; i <= index; i++) {
            sum += svrAt(series, i);
        }

        double avg = sum / window;
        return svrAt(series, index) / avg; // avg==0 explode (ok)
    }

    // ============================================================================
    // ZSCORE: window
    // ============================================================================
    public double svrZscore(SymbolCandle candle, BarSeries series, int index, int window) {
        return svrZscoreAt(series, index, window);
    }

    public double svrZscoreAt(BarSeries series, int index, int window) {
        int start = index - window + 1;
        if (start < 0) throw new IllegalStateException("Index insuficiente para SVR-Zscore");

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = svrAt(series, i);
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / window;
        double var = (sumSq / window) - (mean * mean);
        if (var < 0 && var > -1e-9) var = 0.0;

        double sd = Math.sqrt(var);

        var r = (svrAt(series, index) - mean) / sd;

        if(Double.isNaN(r))
            return 0;

        return r;// sd==0 explode (ok)
    }

    // ============================================================================
    // ACC: now - past
    // ============================================================================
    public double svrAcc(SymbolCandle candle, BarSeries series, int index, int lookback) {
        if (index < lookback) throw new IllegalStateException("Index insuficiente para SVR-Acc");
        return svrAt(series, index) - svrAt(series, index - lookback);
    }

    // ============================================================================
    // SLOPE (w20)
    // ============================================================================
    public double svrSlopeW20(BarSeries series, int index) {
        return slope(series, index, 20);
    }

    private double slope(BarSeries series, int index, int window) {
        int start = index - window + 1;
        if (start < 0) return 0.0;

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        int k = 0;

        for (int i = start; i <= index; i++) {
            double x = k++;
            double y = svrAt(series, i);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        int n = window;
        double denom = n * sumX2 - (sumX * sumX);
        if (Math.abs(denom) < EPS) return 0.0;

        return (n * sumXY - sumX * sumY) / denom;
    }

    // ============================================================================
    // Flip rate / persistence (w20) — baseado no sign(svr)
    // ============================================================================
    private double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    public double svrFlipRateW20(BarSeries series, int index) {
        int window = 20;
        int start = index - window + 1;
        if (start < 1) return 0.0;

        int flips = 0;
        double prev = sign(svrAt(series, start - 1));

        for (int i = start; i <= index; i++) {
            double cur = sign(svrAt(series, i));
            if (cur != 0.0 && prev != 0.0 && cur != prev) flips++;
            if (cur != 0.0) prev = cur;
        }

        return (double) flips / (double) window;
    }

    public double svrPrstW20(BarSeries series, int index) {
        int window = 20;
        int start = index - window + 1;
        if (start < 0) return 0.0;

        double now = sign(svrAt(series, index));
        if (now == 0.0) return 0.0;

        int same = 0;
        for (int i = start; i <= index; i++) {
            if (sign(svrAt(series, i)) == now) same++;
        }

        return (double) same / (double) window;
    }

    // ============================================================================
    // Vol-of-vol (std of SVR values) — w20
    // ============================================================================
    public double svrVolOfVol(BarSeries series, int index, int window) {
        int start = index - window + 1;
        if (start < 0) return 0.0;

        double sum = 0.0, sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = svrAt(series, i);
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / window;
        double var = (sumSq / window) - (mean * mean);
        if (var < 0 && var > -1e-9) var = 0.0;

        return Math.sqrt(var);
    }
}
