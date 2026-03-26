package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import br.com.yacamin.rafael.application.service.indicator.derivate.volume.MicroburstDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.volume.PressureDerivation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class SlopeDerivation {

    private final PressureDerivation pressure;
    private final MicroburstDerivation microburst;

    public double pressure(int index, BarSeries s) {
        // =========================================================================
        // PRESSURE SLOPE (10)
        // =========================================================================
        double[] p = new double[10];
        int end = index;

        for (int i = 0; i < 10; i++) {
            int idx = end - (9 - i);
            p[i] = pressure.sustainedPressureAt(s, idx, 10, PressureDerivation.TRADES);
        }

        return slope(p);
    }

    public double microburst(int index, BarSeries s) {
        // =========================================================================
        // PRESSURE SLOPE (10)
        // =========================================================================
        double[] mb = new double[10];
        int end = index;

        for (int i = 0; i < 10; i++) {
            int idx = end - (9 - i);
            mb[i] = microburst.intensityAt(s, idx, MicroburstDerivation.VOLUME, 10);
        }

        return slope(mb);
    }

    /**
     * Regressão linear simples sobre y[].
     * x = 0, 1, 2, ..., n-1
     *
     * slope = Cov(x,y) / Var(x)
     */
    public double slope(double[] y) {
        int n = y.length;

        if (n < 2) {
            throw new IllegalStateException("Slope requer ao menos 2 pontos");
        }

        // Média de x e y
        double meanX = (n - 1) / 2.0;

        double sumY = 0.0;
        for (double v : y) sumY += v;
        double meanY = sumY / n;

        // Covariância e variância
        double cov = 0.0;
        double var = 0.0;

        for (int i = 0; i < n; i++) {
            double dx = i - meanX;
            double dy = y[i] - meanY;

            cov += dx * dy;
            var += dx * dx;
        }

        return cov / var;  // se var==0 → explode → Regra de Ouro
    }
}
