package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class StructuralVolIndicatorService {

    private static final int WINDOW_HURST_100      = 100;
    private static final int WINDOW_HURST_200      = 200;
    private static final int WINDOW_ENTROPY_RET_50 = 50;
    private static final int ENTROPY_BINS          = 10;

    // =============================================================================================
    // Helpers
    // =============================================================================================

    private double close(BarSeries s, int i) {
        return s.getBar(i).getClosePrice().doubleValue();
    }

    // Retornos log de 1 passo
    private double[] getLogReturnsWindow(BarSeries series, int lastIndex, int window) {

        int start = Math.max(1, lastIndex - window + 1);
        int n = lastIndex - start + 1;

        double[] r = new double[n];
        int idx = 0;

        for (int i = start; i <= lastIndex; i++) {
            double c = close(series, i);
            double p = close(series, i - 1);
            r[idx++] = Math.log(c / p);  // se p=0 → explode → correto (Regra de Ouro)
        }

        return r;
    }

    // =============================================================================================
    // Hurst exponent (R/S)
    // =============================================================================================

    private double computeHurst(double[] r) {

        int n = r.length;

        // Média
        double mean = 0.0;
        for (double v : r) mean += v;
        mean /= n;

        // Série cumulativa de desvios
        double[] cum = new double[n];
        double acc = 0.0;
        double minCum = Double.POSITIVE_INFINITY;
        double maxCum = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < n; i++) {
            acc += (r[i] - mean);
            cum[i] = acc;

            if (acc < minCum) minCum = acc;
            if (acc > maxCum) maxCum = acc;
        }

        double R = maxCum - minCum;  // se R=0 → explode no final → correto
        // STD dos retornos
        double sum2 = 0.0;
        for (double v : r) {
            double d = v - mean;
            sum2 += d * d;
        }
        double S = Math.sqrt(sum2 / n); // se S=0 → explode → correto

        double RS = R / S;
        return Math.log(RS) / Math.log(n);  // se n<2 → explode → correto
    }

    // =============================================================================================
    // Entropia (histogram entropy)
    // =============================================================================================

    private double computeEntropy(double[] r, int bins) {

        int n = r.length;

        // Min / max
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (double v : r) {
            if (v < min) min = v;
            if (v > max) max = v;
        }

        double width = (max - min) / bins;   // se width=0 → explode → correto

        int[] counts = new int[bins];

        for (double v : r) {
            int bin = (int) ((v - min) / width);
            if (bin < 0) bin = 0;
            if (bin >= bins) bin = bins - 1;
            counts[bin]++;
        }

        double entropy = 0.0;
        for (int c : counts) {
            if (c == 0) continue;
            double p = (double) c / n;
            entropy -= p * Math.log(p);      // igual ao Python
        }

        return entropy;
    }

    // =============================================================================================
    // Dispatcher
    // =============================================================================================

    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        int last = series.getEndIndex();

        return switch (frame) {

            case HURST_100 -> {
                double[] r = getLogReturnsWindow(series, last, WINDOW_HURST_100);
                yield computeHurst(r);
            }

            case HURST_200 -> {
                double[] r = getLogReturnsWindow(series, last, WINDOW_HURST_200);
                yield computeHurst(r);
            }

            case ENTROPY_RET_50 -> {
                double[] r = getLogReturnsWindow(series, last, WINDOW_ENTROPY_RET_50);
                yield computeEntropy(r, ENTROPY_BINS);
            }

            default -> throw new IllegalArgumentException("Frame StructuralVol não suportado: " + frame);
        };
    }
}
