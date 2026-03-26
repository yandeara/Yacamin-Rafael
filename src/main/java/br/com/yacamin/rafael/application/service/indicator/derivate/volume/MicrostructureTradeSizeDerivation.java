package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
public class MicrostructureTradeSizeDerivation {

    // ============================================================================
    // TRADE SIZE BÁSICO (Regra de Ouro)
    // ============================================================================
    public double avgTradeSize(RafaelBar bar) {
        double volume = bar.getVolume().doubleValue();
        double trades = bar.getTrades();  // se trades == 0 → explode → correto
        return volume / trades;
    }

    public double avgQuotePerTrade(RafaelBar bar) {
        double qv = bar.getQuoteVolume().doubleValue();
        double trades = bar.getTrades();  // se trades == 0 → explode → correto
        return qv / trades;
    }

    // ============================================================================
    // MEAN & STD — Regra de Ouro
    // ============================================================================
    private double mean(double[] arr, int n) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) sum += arr[i];
        return sum / n;
    }

    private double std(double[] arr, int n, double mean) {
        if (n < 2) {
            throw new IllegalStateException("std(): janela insuficiente (n < 2)");
        }

        double sumSq = 0.0;
        for (int i = 0; i < n; i++) {
            double d = arr[i] - mean;
            sumSq += d * d;
        }

        double variance = sumSq / (n - 1);  // amostral
        return Math.sqrt(variance);          // se std==0 → explode depois no cálculo → OK
    }

    // ============================================================================
    // RELATIVE (Regra de Ouro — explode se janela insuficiente)
    // ============================================================================
    public double avgTradeSizeRel(BarSeries s, int period) {

        int end = s.getEndIndex();
        int start = end - period + 1;

        if (start < 0) {
            throw new IllegalStateException("avgTradeSizeRel(): janela insuficiente");
        }

        double now = avgTradeSize((RafaelBar) s.getBar(end));

        double[] hist = new double[period];
        for (int i = 0; i < period; i++) {
            hist[i] = avgTradeSize((RafaelBar) s.getBar(start + i));
        }

        double m = mean(hist, period);
        return now / m;  // pode explodir → correto
    }

    public double avgQuotePerTradeRel(BarSeries s, int period) {

        int end = s.getEndIndex();
        int start = end - period + 1;

        if (start < 0) {
            throw new IllegalStateException("avgQuotePerTradeRel(): janela insuficiente");
        }

        double now = avgQuotePerTrade((RafaelBar) s.getBar(end));

        double[] hist = new double[period];
        for (int i = 0; i < period; i++) {
            hist[i] = avgQuotePerTrade((RafaelBar) s.getBar(start + i));
        }

        double m = mean(hist, period);
        return now / m;
    }

    // ============================================================================
    // Z-SCORE (Regra de Ouro — sem suavização)
    // ============================================================================
    public double avgTradeSizeZscore(BarSeries s, int period) {

        int end = s.getEndIndex();
        int start = end - period + 1;

        if (start < 0) {
            throw new IllegalStateException("avgTradeSizeZscore(): janela insuficiente");
        }

        double now = avgTradeSize((RafaelBar) s.getBar(end));

        double[] hist = new double[period];
        for (int i = 0; i < period; i++) {
            hist[i] = avgTradeSize((RafaelBar) s.getBar(start + i));
        }

        double m = mean(hist, period);
        double sd = std(hist, period, m);   // pode ser zero → explode no próximo passo

        return (now - m) / sd;
    }

    public double avgQuotePerTradeZscore(BarSeries s, int period) {

        int end = s.getEndIndex();
        int start = end - period + 1;

        if (start < 0) {
            throw new IllegalStateException("avgQuotePerTradeZscore(): janela insuficiente");
        }

        double now = avgQuotePerTrade((RafaelBar) s.getBar(end));

        double[] hist = new double[period];
        for (int i = 0; i < period; i++) {
            hist[i] = avgQuotePerTrade((RafaelBar) s.getBar(start + i));
        }

        double m = mean(hist, period);
        double sd = std(hist, period, m);

        return (now - m) / sd;
    }
}
