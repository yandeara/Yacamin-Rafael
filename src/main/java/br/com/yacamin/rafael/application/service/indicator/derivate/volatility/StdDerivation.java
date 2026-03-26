package br.com.yacamin.rafael.application.service.indicator.derivate.volatility;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;

@Service
@RequiredArgsConstructor
public class StdDerivation {

    // =============================================================================================
    // STD level
    // =============================================================================================
    public double std(StandardDeviationIndicator std, int index) {
        return std.getValue(index).doubleValue();
    }

    // =============================================================================================
    // STD change rate: (std_t / std_{t-1}) - 1
    // =============================================================================================
    public double stdChange(StandardDeviationIndicator std, int index) {
        double curr = std.getValue(index).doubleValue();
        double prev = std.getValue(index - 1).doubleValue();
        return (curr / prev) - 1.0; // prev==0 -> explode (Regra de Ouro)
    }

    // =============================================================================================
    // STD 14/50 regime
    // =============================================================================================
    public double stdRatio(StandardDeviationIndicator a, StandardDeviationIndicator b, int index) {
        double x = a.getValue(index).doubleValue();
        double y = b.getValue(index).doubleValue();
        return x / y;
    }

    public double expansionFromRatio(double ratio) {
        return Math.max(ratio - 1.0, 0.0);
    }

    public double compressionFromRatio(double ratio) {
        return Math.max(1.0 - ratio, 0.0);
    }

    // =============================================================================================
    // Z-Score of STD (rolling)
    // =============================================================================================
    public double stdZScore(StandardDeviationIndicator std, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = std.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double sd = Math.sqrt(variance);

        double last = std.getValue(index).doubleValue();
        return (last - mean) / sd; // sd==0 -> explode
    }

    // =============================================================================================
    // Vol-of-vol (std of STD values)
    // =============================================================================================
    public double stdVolOfVol(StandardDeviationIndicator std, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = std.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        return Math.sqrt(variance); // variance==0 -> 0 natural
    }

    // =============================================================================================
    // STD slope (via cached LinearRegressionSlopeIndicator)
    // =============================================================================================
    public double stdSlope(LinearRegressionSlopeIndicator slp, int index) {
        return slp.getValue(index).doubleValue();
    }
}
