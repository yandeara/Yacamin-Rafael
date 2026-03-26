package br.com.yacamin.rafael.application.service.indicator.derivate.volatility;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class EwmaVolDerivation {

    // lambda de decaimento (ajustável). 0.97 funciona bem em 15m.
    private static final double LAMBDA = 0.97;

    private double close(BarSeries s, int i) {
        return s.getBar(i).getClosePrice().doubleValue();
    }

    private double logReturn(BarSeries s, int i) {
        double c = close(s, i);
        double p = close(s, i - 1);
        return Math.log(c / p); // p==0 -> explode (safe)
    }

    // =============================================================================================
    // EWMA VOL = sqrt( weighted mean of r^2 ), weights = lambda^k (k=0 is most recent)
    // =============================================================================================
    public double ewmaVol(BarSeries series, int last, int window, double lambda) {

        int start = Math.max(1, last - window + 1);

        double sumW = 0.0;
        double sumWR2 = 0.0;

        // k=0 (mais recente), k cresce indo para o passado
        int k = 0;
        for (int i = last; i >= start; i--) {
            double r = logReturn(series, i);
            double w = Math.pow(lambda, k++);
            sumW += w;
            sumWR2 += w * (r * r);
        }

        double var = sumWR2 / sumW;
        return Math.sqrt(var);
    }

    // =============================================================================================
    // Convenience levels (base + sniper multi-scale)
    // =============================================================================================
    public double ewmaVol20(BarSeries series, int last)  { return ewmaVol(series, last, 20,  LAMBDA); }
    public double ewmaVol32(BarSeries series, int last)  { return ewmaVol(series, last, 32,  LAMBDA); }
    public double ewmaVol48(BarSeries series, int last)  { return ewmaVol(series, last, 48,  LAMBDA); }
    public double ewmaVol96(BarSeries series, int last)  { return ewmaVol(series, last, 96,  LAMBDA); }
    public double ewmaVol288(BarSeries series, int last) { return ewmaVol(series, last, 288, LAMBDA); }

    // =============================================================================================
    // Z-Score rolling (recomputing ewmaVol over a rolling window)
    // =============================================================================================
    public double ewmaVolZScore(BarSeries series, int lastIndex, int ewmaWindow, int zWindow) {

        int start = Math.max(1, lastIndex - zWindow + 1);
        int n = lastIndex - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;
        double lastV = 0.0;

        for (int i = start; i <= lastIndex; i++) {
            double v = ewmaVol(series, i, ewmaWindow, LAMBDA);
            sum += v;
            sumSq += v * v;
            if (i == lastIndex) lastV = v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double sd = Math.sqrt(variance);

        return (lastV - mean) / sd;
    }

    // =============================================================================================
    // Slope (linear regression) over last slopeWindow points of ewmaVol(ewmaWindow)
    // =============================================================================================
    public double ewmaVolSlope(BarSeries series, int lastIndex, int ewmaWindow, int slopeWindow) {

        int start = Math.max(1, lastIndex - slopeWindow + 1);
        int n = lastIndex - start + 1;

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;

        int k = 0;
        for (int i = start; i <= lastIndex; i++) {
            double x = k++;
            double y = ewmaVol(series, i, ewmaWindow, LAMBDA);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (denom == 0.0) return 0.0;

        return (n * sumXY - sumX * sumY) / denom;
    }

    // =============================================================================================
    // Ratio = ewma(short) / ewma(long)
    // =============================================================================================
    public double ewmaVolRatio(BarSeries series, int lastIndex, int shortWindow, int longWindow) {
        double a = ewmaVol(series, lastIndex, shortWindow, LAMBDA);
        double b = ewmaVol(series, lastIndex, longWindow, LAMBDA);
        return a / b;
    }
}
