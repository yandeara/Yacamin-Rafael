package br.com.yacamin.rafael.application.service.indicator.derivate.volatility;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

@Service
@RequiredArgsConstructor
public class RealizedVolDerivation {

    // =============================================================================================
    // RV level (from cached indicator)
    // =============================================================================================
    public double rv(Indicator<Num> rv, int index) {
        return rv.getValue(index).doubleValue();
    }

    // =============================================================================================
    // Z-Score rolling (on cached indicator values)
    // =============================================================================================
    public double zScore(Indicator<Num> ind, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = ind.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double std = Math.sqrt(variance);

        double last = ind.getValue(index).doubleValue();
        return (last - mean) / std; // std==0 -> explode (seu safe resolve)
    }

    // =============================================================================================
    // Percentile rank (0..1): % de valores <= current na janela
    // =============================================================================================
    public double pctileRank(Indicator<Num> ind, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double cur = ind.getValue(index).doubleValue();
        int le = 0;

        for (int i = start; i <= index; i++) {
            double v = ind.getValue(i).doubleValue();
            if (v <= cur) le++;
        }

        return ((double) le) / ((double) n);
    }

    // =============================================================================================
    // Ratio = A/B (at index)
    // =============================================================================================
    public double ratio(Indicator<Num> a, Indicator<Num> b, int index) {
        double x = a.getValue(index).doubleValue();
        double y = b.getValue(index).doubleValue();
        return x / y; // y==0 -> explode (safe resolve)
    }

    // =============================================================================================
    // Slope (linear regression) over last window points of indicator
    // x = 0..n-1, y = ind(t-window+1..t)
    // =============================================================================================
    public double slope(Indicator<Num> ind, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        int k = 0;
        for (int i = start; i <= index; i++) {
            double x = k++;
            double y = ind.getValue(i).doubleValue();

            sumX  += x;
            sumY  += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (denom == 0.0) {
            return 0.0;
        }

        return (n * sumXY - sumX * sumY) / denom;
    }

    // =============================================================================================
    // Vol-of-Vol (std dev of indicator values over window)
    // =============================================================================================
    public double volOfVol(Indicator<Num> ind, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = ind.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        return Math.sqrt(variance);
    }
}
