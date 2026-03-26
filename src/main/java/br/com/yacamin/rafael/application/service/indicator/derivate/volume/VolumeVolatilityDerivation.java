package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
public class VolumeVolatilityDerivation {

    /**
     * VoV(t) = std(volume_t ... volume_{t-window+1})
     * Nenhuma proteção/safe: se window for inválido ou faltar candle → explode (Regra de Ouro).
     */
    public double vov(BarSeries series, int index, int window) {

        int start = index - window + 1;

        double[] vols = new double[window];
        int idx = 0;

        for (int i = start; i <= index; i++) {
            RafaelBar bar = (RafaelBar) series.getBar(i);
            vols[idx++] = bar.getVolume().doubleValue();
        }

        return std(vols, mean(vols));
    }


    /**
     * Z-score dos últimos zWindow valores de VOV(window)
     */
    public double vovZscore(BarSeries series,
                            int index,
                            int window,
                            int zWindow) {

        double[] vals = new double[zWindow];

        for (int offset = 0; offset < zWindow; offset++) {
            vals[offset] = vov(series, index - offset, window);
        }

        double mean = mean(vals);
        double sd   = std(vals, mean);

        return (vals[0] - mean) / sd;   // se sd==0 → explode (Regra de Ouro)
    }


    /**
     * Versão fixa usada em outras partes do sistema
     */
    public double vovZscore20(BarSeries series, int index) {
        return vovZscore(series, index, 20, 20);
    }


    // =============================================================================================
    // Helpers: média e std REAL — float64 only
    // =============================================================================================

    private double mean(double[] x) {
        double sum = 0.0;
        for (double v : x) sum += v;
        return sum / x.length;
    }

    private double std(double[] x, double mean) {
        double sumSq = 0.0;
        for (double v : x) {
            double d = v - mean;
            sumSq += d * d;
        }
        return Math.sqrt(sumSq / x.length);
    }
}
