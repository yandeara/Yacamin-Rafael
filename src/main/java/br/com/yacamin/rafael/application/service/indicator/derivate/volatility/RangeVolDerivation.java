package br.com.yacamin.rafael.application.service.indicator.derivate.volatility;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class RangeVolDerivation {

    private double open(BarSeries s, int i)  { return s.getBar(i).getOpenPrice().doubleValue(); }
    private double high(BarSeries s, int i)  { return s.getBar(i).getHighPrice().doubleValue(); }
    private double low(BarSeries s, int i)   { return s.getBar(i).getLowPrice().doubleValue(); }
    private double close(BarSeries s, int i) { return s.getBar(i).getClosePrice().doubleValue(); }

    // =============================================================================================
    // 1) Estimators
    // =============================================================================================
    // Garman–Klass
    public double garmanKlass(BarSeries series, int lastIndex, int period) {
        int start = Math.max(0, lastIndex - period + 1);
        int n = lastIndex - start + 1;

        double sum = 0.0;
        double k = 2.0 * Math.log(2.0) - 1.0;

        for (int i = start; i <= lastIndex; i++) {
            double o = open(series, i);
            double h = high(series, i);
            double l = low(series, i);
            double c = close(series, i);

            double logHL = Math.log(h / l);
            double logCO = Math.log(c / o);

            double term = 0.5 * logHL * logHL - k * logCO * logCO;
            sum += term;
        }

        double variance = sum / n;
        return Math.sqrt(variance);
    }

    // Parkinson
    public double parkinson(BarSeries series, int lastIndex, int period) {
        int start = Math.max(0, lastIndex - period + 1);
        int n = lastIndex - start + 1;

        double sumSq = 0.0;
        for (int i = start; i <= lastIndex; i++) {
            double h = high(series, i);
            double l = low(series, i);
            double logHL = Math.log(h / l);
            sumSq += logHL * logHL;
        }

        double denom = 4.0 * n * Math.log(2.0);
        double variance = sumSq / denom;
        return Math.sqrt(variance);
    }

    // Rogers–Satchell
    public double rogersSatchell(BarSeries series, int lastIndex, int period) {
        int start = Math.max(0, lastIndex - period + 1);
        int n = lastIndex - start + 1;

        double sum = 0.0;
        for (int i = start; i <= lastIndex; i++) {
            double o = open(series, i);
            double h = high(series, i);
            double l = low(series, i);
            double c = close(series, i);

            double logHO = Math.log(h / o);
            double logLO = Math.log(l / o);
            double logCO = Math.log(c / o);

            sum += logHO * (logHO - logCO)
                    + logLO * (logLO - logCO);
        }

        double variance = sum / n;
        return Math.sqrt(variance);
    }

    // =============================================================================================
    // 2) Helpers (ZSC / SLP / RATIO) — sem lambdas no Warmup
    // =============================================================================================
    public double gkZScore(BarSeries series, int index, int period, int window) {
        return zScore(series, index, window, period, Estimator.GK);
    }

    public double parkZScore(BarSeries series, int index, int period, int window) {
        return zScore(series, index, window, period, Estimator.PARK);
    }

    public double rsZScore(BarSeries series, int index, int period, int window) {
        return zScore(series, index, window, period, Estimator.RS);
    }

    public double gkSlope(BarSeries series, int index, int period, int window) {
        return slope(series, index, window, period, Estimator.GK);
    }

    public double parkSlope(BarSeries series, int index, int period, int window) {
        return slope(series, index, window, period, Estimator.PARK);
    }

    public double rsSlope(BarSeries series, int index, int period, int window) {
        return slope(series, index, window, period, Estimator.RS);
    }

    public double gkRatio(BarSeries series, int index, int shortPeriod, int longPeriod) {
        double a = garmanKlass(series, index, shortPeriod);
        double b = garmanKlass(series, index, longPeriod);
        return a / b;
    }

    public double parkRatio(BarSeries series, int index, int shortPeriod, int longPeriod) {
        double a = parkinson(series, index, shortPeriod);
        double b = parkinson(series, index, longPeriod);
        return a / b;
    }

    public double rsRatio(BarSeries series, int index, int shortPeriod, int longPeriod) {
        double a = rogersSatchell(series, index, shortPeriod);
        double b = rogersSatchell(series, index, longPeriod);
        return a / b;
    }

    // -------------------------
    // Internals
    // -------------------------
    private enum Estimator { GK, PARK, RS }

    private double value(BarSeries series, int index, int period, Estimator est) {
        return switch (est) {
            case GK   -> garmanKlass(series, index, period);
            case PARK -> parkinson(series, index, period);
            case RS   -> rogersSatchell(series, index, period);
        };
    }

    private double zScore(BarSeries series, int index, int window, int period, Estimator est) {
        int start = Math.max(1, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;
        double lastV = 0.0;

        for (int i = start; i <= index; i++) {
            double v = value(series, i, period, est);
            sum += v;
            sumSq += v * v;
            if (i == index) lastV = v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double sd = Math.sqrt(variance);

        return (lastV - mean) / sd;
    }

    private double slope(BarSeries series, int index, int window, int period, Estimator est) {
        int start = Math.max(1, index - window + 1);
        int n = index - start + 1;

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;

        int k = 0;
        for (int i = start; i <= index; i++) {
            double x = k++;
            double y = value(series, i, period, est);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (denom == 0.0) return 0.0;

        return (n * sumXY - sumX * sumY) / denom;
    }
}
