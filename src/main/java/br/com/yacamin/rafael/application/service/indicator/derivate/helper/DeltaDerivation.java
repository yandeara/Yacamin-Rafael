package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import br.com.yacamin.rafael.domain.RafaelBar;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
public class DeltaDerivation {

    private final double EPS = 1e-9;

    // =============================================================================================
    // Delta Generic for All (D = X0 - X1)
    // =============================================================================================
    public double delta(double actual, double prev) {
        return actual - prev;
    }

    // =============================================================================================
    // Delta Pct Generic for All (D = X0 - X1) / prev
    // =============================================================================================
    public double deltaPct(double actual, double prev) {
        // SEGURANÇA: Evita divisão por zero.
        if (Math.abs(prev) < EPS) { // 1e-9 é um EPS (Epsilon) para quase-zero
            return 0.0;
        }

        // Chama o Delta Bruto e divide
        double deltaRaw = delta(actual, prev);
        return deltaRaw / prev;
    }





    // =============================================================================================
    // DELTA DE VOLUME
    // delta = (now - prev) / |prev|
    // Se prev == 0 → explode (Regra de Ouro)
    // =============================================================================================
    public double volumeDelta(BarSeries series, int k) {

        int end = series.getEndIndex();
        int prevIndex = end - k;

        // Regra de Ouro: não esconder janelas insuficientes
        if (prevIndex < 0) {
            throw new IllegalStateException("volumeDelta(): janela insuficiente para k=" + k);
        }

        double now  = series.getBar(end).getVolume().doubleValue();
        double prev = series.getBar(prevIndex).getVolume().doubleValue();

        // Regra de Ouro: se prev == 0 → deixa explodir (Infinity ou NaN)
        return (now - prev) / Math.abs(prev);
    }

    // =============================================================================================
    // DELTA DE TRADES
    // =============================================================================================
    public double tradesDelta(BarSeries series, int k) {

        int end = series.getEndIndex();
        int prevIndex = end - k;

        if (prevIndex < 0) {
            throw new IllegalStateException("tradesDelta(): janela insuficiente para k=" + k);
        }

        double now  = series.getBar(end).getTrades();
        double prev = series.getBar(prevIndex).getTrades();

        return (now - prev) / Math.abs(prev);
    }

    // =============================================================================================
    // DELTA DE QUOTE VOLUME
    // =============================================================================================
    public double quoteVolumeDelta(BarSeries series, int k) {

        int end = series.getEndIndex();
        int prevIndex = end - k;

        if (prevIndex < 0) {
            throw new IllegalStateException("quoteVolumeDelta(): janela insuficiente para k=" + k);
        }

        RafaelBar nowBar  = (RafaelBar) series.getBar(end);
        RafaelBar prevBar = (RafaelBar) series.getBar(prevIndex);

        double now  = nowBar.getQuoteVolume().doubleValue();
        double prev = prevBar.getQuoteVolume().doubleValue();

        return (now - prev) / Math.abs(prev);
    }
}
