package br.com.yacamin.rafael.application.service.indicator.derivate;

import org.springframework.stereotype.Service;

@Service
public class CrossoverDerivation {

    /**
     * Detecta cruzamento:
     * +1 = bullish crossover (fast cruza de baixo para cima)
     * -1 = bearish crossover (fast cruza de cima para baixo)
     *  0 = nenhum cruzamento
     */
    public double binary(double fastPrev,
                         double slowPrev,
                         double fastNow,
                         double slowNow) {

        if (Double.isNaN(fastPrev) || Double.isNaN(slowPrev) ||
                Double.isNaN(fastNow)  || Double.isNaN(slowNow)) {

            throw new IllegalStateException(
                    "Valores inválidos em CrossoverDerivation.binary"
            );
        }

        boolean wasBelow = fastPrev < slowPrev;
        boolean nowAbove = fastNow  > slowNow;

        boolean wasAbove = fastPrev > slowPrev;
        boolean nowBelow = fastNow  < slowNow;

        if (wasBelow && nowAbove) return  1.0;  // bullish
        if (wasAbove && nowBelow) return -1.0;  // bearish

        return 0.0;
    }

    /**
     * delta = (fast_now - slow_now) / close
     */
    public double delta(double fastNow,
                        double slowNow,
                        double close) {

        if (Double.isNaN(fastNow) || Double.isNaN(slowNow) || Double.isNaN(close)) {
            throw new IllegalStateException(
                    "Valores inválidos em CrossoverDerivation.delta"
            );
        }

        if (close == 0.0) {
            throw new IllegalStateException("Divisão por zero em delta(): close == 0");
        }

        return (fastNow - slowNow) / close;
    }
}
