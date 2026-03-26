package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import org.springframework.stereotype.Service;

@Service
public class StdHelperDerivation {

    private static final double EPS = 1e-12;

    /**
     * Desvio padrão simples de um vetor de valores:
     * std = sqrt( sum((x_i - mean)^2) / n )
     */
    public double std(double[] values) {

        int n = values.length;
        if (n < 2) {
            return 0.0;  // sem variância possível
        }

        // média
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        double mean = sum / n;

        // variância populacional
        double sumSq = 0.0;
        for (double v : values) {
            double d = v - mean;
            sumSq += d * d;
        }

        double variance = sumSq / n;
        double std = Math.sqrt(variance);

        return (std < EPS) ? 0.0 : std;
    }
}
