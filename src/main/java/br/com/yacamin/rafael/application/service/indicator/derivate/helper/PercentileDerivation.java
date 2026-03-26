package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import org.springframework.stereotype.Service;

@Service
public class PercentileDerivation {

    public double percentileRank(double[] values, double target) {

        int n = values.length;
        if (n == 0) return 0.0;

        int count = 0;
        for (double v : values) {
            if (v <= target) count++;
        }

        return (double) count / n; // resultado entre 0 e 1
    }
}
