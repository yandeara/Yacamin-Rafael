package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class VolumeSlopeDerivation {

    private static final int WINDOW = 16;

    private final PressureDerivation pressure;
    private final MicroburstDerivation microburst;

    // =========================================================================
    // PRESSURE SLOPE (16) — slope da sustainedPressureAt(..., 16, TRADES) ao longo do tempo
    // =========================================================================
    public double pressureSlope16(int index, BarSeries s) {

        double[] p = new double[WINDOW];
        int end = index;

        for (int i = 0; i < WINDOW; i++) {
            int idx = end - ((WINDOW - 1) - i); // end-15 .. end
            p[i] = pressure.sustainedPressureAt(s, idx, WINDOW, PressureDerivation.TRADES);
        }

        return slope(p);
    }

    // =========================================================================
    // MICROBURST SLOPE (16) — slope da intensityAt(..., VOLUME, 16) ao longo do tempo
    // =========================================================================
    public double microburstSlope16(int index, BarSeries s) {

        double[] mb = new double[WINDOW];
        int end = index;

        for (int i = 0; i < WINDOW; i++) {
            int idx = end - ((WINDOW - 1) - i);
            mb[i] = microburst.intensityAt(s, idx, MicroburstDerivation.VOLUME, WINDOW);
        }

        return slope(mb);
    }

    /**
     * Regressão linear simples sobre y[].
     * x = 0, 1, 2, ..., n-1
     *
     * slope = Cov(x,y) / Var(x)
     */


    private static final double EPS = 1e-12;

    private final MicroburstDerivation microburstDerivation;
    private final PressureDerivation pressureDerivation;

    // slope linear simples em y[t-window+1..t]
    private double slope(double[] y) {
        int n = y.length;
        if (n < 2) return 0.0;

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;

        for (int i = 0; i < n; i++) {
            double x = i;
            sumX += x;
            sumY += y[i];
            sumXY += x * y[i];
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (Math.abs(denom) < EPS) return 0.0;

        return (n * sumXY - sumX * sumY) / denom;
    }

    // PRESSURE slope: usa sustainedPressureAt ao longo da janela
    public double pressureSlope(int end, BarSeries series, int window) {
        int start = end - window + 1;
        if (start < 0) return 0.0;

        double[] sp = new double[window];

        // sp[t] = sustainedPressureAt(index=t, k=window, extractor=TRADES)
        // Para evitar "janela dentro de janela", usamos k=window e percorremos os últimos window pontos
        // (é o mais coerente com seu desenho de pressure)
        for (int i = 0; i < window; i++) {
            int idx = start + i;
            sp[i] = pressureDerivation.sustainedPressureAt(series, idx, window, PressureDerivation.TRADES);
        }

        return slope(sp);
    }

    // MICROBURST slope: slope do intensityAt (VOLUME+TRADES) agregado (combo de intensidades)
    public double microburstSlope(int end, BarSeries series, int window) {
        int start = end - window + 1;
        if (start < 0) return 0.0;

        double[] mb = new double[window];

        for (int i = 0; i < window; i++) {
            int idx = start + i;

            double iv = microburstDerivation.intensityAt(series, idx, MicroburstDerivation.VOLUME, window);
            double it = microburstDerivation.intensityAt(series, idx, MicroburstDerivation.TRADES, window);

            // mesma ideia do combo: (iv-1) e (it-1) somam "intensidade acima do normal"
            mb[i] = (iv - 1.0) + (it - 1.0);
        }

        return slope(mb);
    }

}
