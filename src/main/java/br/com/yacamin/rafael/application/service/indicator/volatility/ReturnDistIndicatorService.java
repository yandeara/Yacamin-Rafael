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
public class ReturnDistIndicatorService {

    private static final int WINDOW_RET10_DIST = 50;

    // =============================================================================================
    // Helpers: acesso aos preços
    // =============================================================================================

    private double close(BarSeries s, int i) {
        return s.getBar(i).getClosePrice().doubleValue();
    }

    // =============================================================================================
    // Janela de retornos log de 10 períodos
    // =============================================================================================

    private double[] getRet10Window(BarSeries series, int lastIndex, int window) {

        int start = Math.max(10, lastIndex - window + 1);
        int n = lastIndex - start + 1;

        double[] values = new double[n];
        int idx = 0;

        for (int i = start; i <= lastIndex; i++) {
            double c = close(series, i);
            double p = close(series, i - 10);

            double r = Math.log(c / p);  // se p==0 → explode (Regra de Ouro)
            values[idx++] = r;
        }

        return values;
    }

    // =============================================================================================
    // Skewness (moment 3) — idêntico ao NumPy (sem correção de bias)
    // =============================================================================================

    private double computeSkew(double[] x) {

        int n = x.length;
        double sum = 0.0;
        for (double v : x) sum += v;

        double mean = sum / n;

        double m2 = 0.0;
        double m3 = 0.0;

        for (double v : x) {
            double d = v - mean;
            double d2 = d * d;
            m2 += d2;
            m3 += d2 * d;
        }

        m2 /= n;
        m3 /= n;

        double std = Math.sqrt(m2);    // se m2 == 0 → explode (NaN ou division) → correto

        return m3 / (std * std * std);
    }

    // =============================================================================================
    // Kurtosis (moment 4) — "raw kurtosis", equivalente ao NumPy (sem -3)
    // =============================================================================================

    private double computeKurt(double[] x) {

        int n = x.length;

        double sum = 0.0;
        for (double v : x) sum += v;

        double mean = sum / n;

        double m2 = 0.0;
        double m4 = 0.0;

        for (double v : x) {
            double d = v - mean;
            double d2 = d * d;
            m2 += d2;
            m4 += d2 * d2;
        }

        m2 /= n;
        m4 /= n;

        return m4 / (m2 * m2); // se m2 == 0 → explode → Regra de Ouro
    }

    // =============================================================================================
    // Dispatcher
    // =============================================================================================

    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        int last = series.getEndIndex();
        double[] ret10Window = getRet10Window(series, last, WINDOW_RET10_DIST);

        return switch (frame) {

            case RET_10_SKEW -> computeSkew(ret10Window);

            case RET_10_KURT -> computeKurt(ret10Window);

            default -> throw new IllegalArgumentException("Frame ReturnDist não suportado: " + frame);
        };
    }
}
