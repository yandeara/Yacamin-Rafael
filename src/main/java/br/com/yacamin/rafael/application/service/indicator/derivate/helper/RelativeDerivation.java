package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import org.springframework.stereotype.Service;

@Service
public class RelativeDerivation {

    private static final double EPS = 1e-12;

    /**
     * Relative Level calculation:
     * rel = last_value / mean(values)
     *
     * - values[] MUST be ordered from oldest → newest
     *   exactly like the z-score helper.
     *
     * - Handles mean≈0 safely.
     */
    public double relative(double[] values) {

        int length = values.length;
        if (length == 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }

        double mean = sum / length;
        if (Math.abs(mean) < EPS) {
            return 0.0;
        }

        double last = values[length - 1];
        return last / mean;
    }

}
