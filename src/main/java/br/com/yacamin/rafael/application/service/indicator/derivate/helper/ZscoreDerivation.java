package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import br.com.yacamin.rafael.domain.RafaelBar;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
public class ZscoreDerivation {

    private static final double EPS = 1e-12;

    // =============================================================================================
    // ZScore Generic for All
    // =============================================================================================
    public double zscore(double[] values) {
        int length = values.length;

        // Janela muito pequena -> não dá pra ter desvio padrão decente, retorna 0.0
        if (length < 2) {
            return 0.0;
        }

        double sum = 0.0;

        for (double v : values) sum += v;

        double mean = sum / length;

        double sumSq = 0.0;

        for (double v : values) {
            double d = v - mean;
            sumSq += d * d;
        }

        double variance = sumSq / length;

        double std = Math.sqrt(variance);

        if (std < EPS) {
            // interpretação: último está exatamente na média → zscore = 0
            return 0.0;
        }

        double last = values[length - 1];
        return (last - mean) / std;
    }




    // =============================================================================================
    // Construção de janelas usando getEndIndex()
    // =============================================================================================
    private double[] getVolumeWindow(BarSeries series, int period) {

        int end   = series.getEndIndex();
        int start = end - period + 1;

        double[] vals = new double[period];
        int idx = 0;

        for (int i = start; i <= end; i++) {
            vals[idx++] = series.getBar(i).getVolume().doubleValue();
        }

        return vals;
    }

    private double[] getTradesWindow(BarSeries series, int period) {

        int end   = series.getEndIndex();
        int start = end - period + 1;

        double[] vals = new double[period];
        int idx = 0;

        for (int i = start; i <= end; i++) {
            vals[idx++] = series.getBar(i).getTrades();
        }

        return vals;
    }

    private double[] getQuoteVolumeWindow(BarSeries series, int period) {

        int end   = series.getEndIndex();
        int start = end - period + 1;

        double[] vals = new double[period];
        int idx = 0;

        for (int i = start; i <= end; i++) {
            RafaelBar bar = (RafaelBar) series.getBar(i);
            vals[idx++] = bar.getQuoteVolume().doubleValue();
        }

        return vals;
    }

    // =============================================================================================
    // Z-score wrappers — Regra de Ouro (sem proteções)
    // =============================================================================================
    public double zscoreVolume(BarSeries series, int period) {
        return zscore(getVolumeWindow(series, period));
    }

    public double zscoreTrades(BarSeries series, int period) {
        return zscore(getTradesWindow(series, period));
    }

    public double zscoreQuoteVolume(BarSeries series, int period) {
        return zscore(getQuoteVolumeWindow(series, period));
    }
}
