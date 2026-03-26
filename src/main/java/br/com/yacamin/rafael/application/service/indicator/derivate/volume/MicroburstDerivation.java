package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
public class MicroburstDerivation {

    // =====================================================================
    // Z-SCORE — matematicamente puro (se std==0 → explode)
    // =====================================================================
    private double zscore(double now, double mean, double std) {
        return (now - mean) / std;
    }

    // =====================================================================
    // MEAN + STD — usando índices corretos do TA4J
    // =====================================================================
    private MeanStd meanStd(BarSeries s, int period, ValueExtractor extractor) {

        int end   = s.getEndIndex();
        int start = end - period + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= end; i++) {
            double x = extractor.get(s, i);
            sum += x;
            sumSq += x * x;
        }

        double mean = sum / period;
        double variance = (sumSq / period) - (mean * mean);

        // Correção IEEE-754 (permitida)
        if (variance < 0 && variance > -1e-9) {
            variance = 0;
        }

        double std = Math.sqrt(variance); // std==0 -> explode depois (Regra de Ouro)
        return new MeanStd(mean, std);
    }

    private record MeanStd(double mean, double std) {}

    // =====================================================================
    // Extractor Interface
    // =====================================================================
    @FunctionalInterface
    public interface ValueExtractor {
        double get(BarSeries s, int idx);
    }

    // =====================================================================
    // Extractors prontos
    // =====================================================================
    public static final ValueExtractor VOLUME =
            (s, i) -> s.getBar(i).getVolume().doubleValue();

    public static final ValueExtractor TRADES =
            (s, i) -> s.getBar(i).getTrades();

    public static final ValueExtractor QUOTE =
            (s, i) -> ((RafaelBar) s.getBar(i)).getQuoteVolume().doubleValue();

    // =====================================================================
    // SPIKE SCORE — Z-score do candle atual
    // =====================================================================
    public double spikeScore(BarSeries s, int period, ValueExtractor extractor) {
        int idxNow = s.getEndIndex();
        double now = extractor.get(s, idxNow);
        MeanStd ms = meanStd(s, period, extractor);
        return zscore(now, ms.mean, ms.std);
    }

    // =====================================================================
    // INTENSITY — razão now / mean (se mean==0 explode → correto)
    // =====================================================================
    public double intensity(BarSeries s, int period, ValueExtractor extractor) {
        int idxNow = s.getEndIndex();
        double now = extractor.get(s, idxNow);
        MeanStd ms = meanStd(s, period, extractor);
        return now / ms.mean;
    }

    // =====================================================================
    // COMBO — mantém fórmula original
    // =====================================================================
    public double combo(BarSeries s, int period) {

        double sv = spikeScore(s, period, VOLUME);
        double st = spikeScore(s, period, TRADES);

        double iv = intensity(s, period, VOLUME);
        double it = intensity(s, period, TRADES);

        double ivAdj = iv - 1.0;
        double itAdj = it - 1.0;

        return sv + st + ivAdj * 0.5 + itAdj * 0.5;
    }

    // =====================================================================
    // INTENSITY AT INDEX — usado pelo slope
    // =====================================================================
    public double intensityAt(BarSeries s, int index, ValueExtractor extractor, int period) {

        double now = extractor.get(s, index);

        int start = index - period + 1;
        double sum = 0.0;

        for (int i = start; i <= index; i++) {
            sum += extractor.get(s, i);
        }

        double mean = sum / period;
        return now / mean;
    }
}
