package br.com.yacamin.rafael.application.service.indicator;

import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiCandleReturnIndicatorService {

    private static final double EPS = 1e-9;

    // =========================================================================
    // BASE
    // =========================================================================

    private double[] computeReturns(BarSeries series, int last, int window) {

        if (last - window < 0) {
            throw new IllegalStateException("BarSeries insuficiente para window = " + window);
        }

        double[] arr = new double[window];

        for (int i = 0; i < window; i++) {

            double c  = series.getBar(last - i).getClosePrice().doubleValue();
            double pc = series.getBar(last - i - 1).getClosePrice().doubleValue();

            if (Math.abs(pc) < EPS) {
                throw new IllegalStateException("Divisor zero em computeReturns");
            }

            arr[i] = (c - pc) / pc;
        }

        return arr;
    }

    // =========================================================================
    // Z-SCORE
    // =========================================================================

    private double computeZscore(double[] arr) {

        double mean = Arrays.stream(arr).average().orElseThrow();

        double var = 0.0;
        for (double d : arr) {
            double diff = d - mean;
            var += diff * diff;
        }

        double sd = Math.sqrt(var / arr.length);

        //E plausivel dentro de 5 candles o preço fechar coincidentemente 5 vezes no mesmo valor.
        if (sd < EPS) {
            return 0;
        }

        double curr = arr[0];

        return (curr - mean) / sd;
    }

    // =========================================================================
    // PERCENTILE
    // =========================================================================

    private double computePercentile(double[] arr, double pct) {

        double[] copy = Arrays.copyOf(arr, arr.length);
        Arrays.sort(copy);

        int idx = (int) Math.floor((pct / 100.0) * (copy.length - 1));

        return copy[idx];
    }

    // =========================================================================
    // SKEW
    // =========================================================================

    private double computeSkew(double[] arr) {

        int n = arr.length;

        double mean = Arrays.stream(arr).average().orElseThrow();

        double var = Arrays.stream(arr)
                .map(x -> {
                    double d = x - mean;
                    return d * d;
                })
                .sum() / n;

        double sd = Math.sqrt(var);

        if (sd < EPS) {
            throw new IllegalStateException("Desvio padrão zero em computeSkew");
        }

        return Arrays.stream(arr)
                .map(x -> Math.pow((x - mean) / sd, 3.0))
                .sum() / n;
    }

    // =========================================================================
    // KURTOSIS
    // =========================================================================

    private double computeKurtosis(double[] arr) {

        int n = arr.length;

        double mean = Arrays.stream(arr).average().orElseThrow();

        double var = Arrays.stream(arr)
                .map(x -> {
                    double d = x - mean;
                    return d * d;
                })
                .sum() / n;

        double sd = Math.sqrt(var);

        if (sd < EPS) {
            throw new IllegalStateException("Desvio padrão zero em computeKurtosis");
        }

        double k = Arrays.stream(arr)
                .map(x -> Math.pow((x - mean) / sd, 4.0))
                .sum() / n;

        return k - 3.0; // excess kurtosis
    }

    // =========================================================================
    // ROLLING STD
    // =========================================================================

    private double computeRollingStd(double[] arr) {

        double mean = Arrays.stream(arr).average().orElseThrow();

        double var = Arrays.stream(arr)
                .map(x -> {
                    double d = x - mean;
                    return d * d;
                })
                .sum() / arr.length;

        return Math.sqrt(var);
    }

    // =========================================================================
    // SMOOTHNESS
    // =========================================================================

    private double computeSmoothness(double[] arr) {

        double sum = 0.0;
        for (int i = 1; i < arr.length; i++) {
            sum += Math.abs(arr[i] - arr[i - 1]);
        }

        return sum;
    }

    // =========================================================================
    // RSP — RUNNING SAME-POSITION RATIO
    // =========================================================================

    private double computeRsp(double[] arr) {

        double last = Math.signum(arr[0]);
        int count = 0;

        for (int i = 1; i < arr.length; i++) {

            double s = Math.signum(arr[i]);

            if (s == last) {
                count++;
            }

            last = s;
        }

        return (double) count / (arr.length - 1);
    }

    // =========================================================================
    // RDS — ABSOLUTE DEVIATION MEAN
    // =========================================================================

    private double computeRds(double[] arr) {

        double mean = Arrays.stream(arr).average().orElseThrow();

        double sum = Arrays.stream(arr)
                .map(x -> Math.abs(x - mean))
                .sum();

        return sum / arr.length;
    }

    // =========================================================================
    // RNR — REVERSAL RATIO
    // =========================================================================

    private double computeRnr(double[] arr) {

        int flips = 0;

        for (int i = 1; i < arr.length; i++) {
            if (Math.signum(arr[i]) != Math.signum(arr[i - 1])) {
                flips++;
            }
        }

        return (double) flips / (arr.length - 1);
    }

    // =========================================================================
    // DISPATCHER — NOVO PADRÃO
    // =========================================================================

    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        int last = series.getEndIndex();

        return switch (frame) {

            case RETURN_ZSCORE_5 ->
                    computeZscore(computeReturns(series, last, 5));

            case RETURN_ZSCORE_14 ->
                    computeZscore(computeReturns(series, last, 14));

            case RETURN_PCTL_20 ->
                    computePercentile(computeReturns(series, last, 20), 20.0);

            case RETURN_PCTL_50 ->
                    computePercentile(computeReturns(series, last, 50), 50.0);

            case RETURN_SKEW ->
                    computeSkew(computeReturns(series, last, 14));

            case RETURN_KURTOSIS ->
                    computeKurtosis(computeReturns(series, last, 14));

            case RETURN_STD_ROLLING ->
                    computeRollingStd(computeReturns(series, last, 14));

            case RETURN_SMOOTHNESS ->
                    computeSmoothness(computeReturns(series, last, 14));

            case RSP ->
                    computeRsp(computeReturns(series, last, 14));

            case RDS ->
                    computeRds(computeReturns(series, last, 14));

            case RNR ->
                    computeRnr(computeReturns(series, last, 14));

            default ->
                    throw new IllegalArgumentException("Frame MultiCandleReturn não suportado: " + frame);
        };
    }
}
