package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
public class PressureDerivation {

    // -----------------------------------------------
    // SIGN
    // -----------------------------------------------
    private int sign(double v) {
        return v > 0 ? 1 : (v < 0 ? -1 : 0);
    }

    // -----------------------------------------------
    // DELTA
    // -----------------------------------------------
    private double delta(double now, double prev) {
        return now - prev;
    }

    // -----------------------------------------------
    // Sustained Pressure (puro)
    // -----------------------------------------------
    public double sustainedPressure(BarSeries series, int k, ValueExtractor extractor) {

        int end   = series.getEndIndex();
        int start = end - k + 1;   // janela correta

        int score = 0;

        for (int i = start + 1; i <= end; i++) {
            double now  = extractor.get(series, i);
            double prev = extractor.get(series, i - 1);
            score += sign(delta(now, prev));
        }

        return (double) score / (double) k;
    }

    // -----------------------------------------------
    // Sustained Pressure normalizado pelo STD
    // (Regra de Ouro → SEM fallback)
    // -----------------------------------------------
    public double sustainedPressureVol(BarSeries series, int k, ValueExtractor extractor) {

        double sp = sustainedPressure(series, k, extractor);

        int end   = series.getEndIndex();
        int start = end - k + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= end; i++) {
            double x = extractor.get(series, i);
            sum += x;
            sumSq += x * x;
        }

        double mean = sum / k;
        double variance = (sumSq / k) - (mean * mean);

        // Correção IEEE-754 — INOFENSIVA E PERMITIDA
        if (variance < 0 && variance > -1e-9) {
            variance = 0.0;
        }

        double std = Math.sqrt(variance);  // se std==0 → explode (correto pela Regra de Ouro)

        return sp / std;   // se std=0 → NaN → correto
    }

    // -----------------------------------------------
    // Pressure Accumulation
    // -----------------------------------------------
    public double pressureAcc(BarSeries series, int k, ValueExtractor extractor) {

        int end   = series.getEndIndex();
        int start = end - k + 1;

        double acc = 0.0;

        for (int i = start + 1; i <= end; i++) {
            double now  = extractor.get(series, i);
            double prev = extractor.get(series, i - 1);
            acc += delta(now, prev);
        }

        return acc;
    }

    // -----------------------------------------------
    // Pressure Chop
    // -----------------------------------------------
    public double pressureChop(BarSeries series, int k, ValueExtractor extractor) {
        double sp = sustainedPressure(series, k, extractor);
        return 1.0 - Math.abs(sp);
    }

    // -----------------------------------------------
    // SustainedPressureAt (estava correto)
    // -----------------------------------------------
    public double sustainedPressureAt(BarSeries series, int index, int k, ValueExtractor extractor) {

        int start = index - k + 1;
        int end   = index;

        int score = 0;

        for (int i = start + 1; i <= end; i++) {
            double now  = extractor.get(series, i);
            double prev = extractor.get(series, i - 1);
            score += sign(delta(now, prev));
        }

        return (double) score / (double) k;
    }

    // -----------------------------------------------
    // Extractors DOUBLE
    // -----------------------------------------------
    @FunctionalInterface
    public interface ValueExtractor {
        double get(BarSeries series, int idx);
    }

    public static final ValueExtractor VOLUME =
            (series, idx) -> series.getBar(idx).getVolume().doubleValue();

    public static final ValueExtractor TRADES =
            (series, idx) -> series.getBar(idx).getTrades();

    public static final ValueExtractor QUOTE =
            (series, idx) -> ((RafaelBar) series.getBar(idx)).getQuoteVolume().doubleValue();
}
