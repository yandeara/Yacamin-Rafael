package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.dto.momentum.PpoCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.momentum.PpoCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class PpoIndicatorService {

    private final PpoCacheService ppoCacheService;

    // =============================================================================================
    // PPO puro
    // =============================================================================================
    private double compute(PPOIndicator ind, int last) {
        return ind.getValue(last).doubleValue();
    }

    private double compute(EMAIndicator ind, int last) {
        return ind.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Histograma = PPO - Signal
    // =============================================================================================
    private double computeHist(PPOIndicator ppo, EMAIndicator signal, int last) {
        double p = ppo.getValue(last).doubleValue();
        double s = signal.getValue(last).doubleValue();
        return p - s;
    }

    // =============================================================================================
    // Dispatcher
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        // Regra de Ouro: se last < 0, TA4J vai quebrar naturalmente

        PpoCacheDto dto = ppoCacheService.getPpoDefault(symbol, interval, series);
        PPOIndicator ppo = dto.getPpo();
        EMAIndicator signal = dto.getSignal();

        return switch (frame) {

            case PPO_12_26 ->
                    compute(ppo, last);

            case PPO_SIG_12_26_9 ->
                    compute(signal, last);

            case PPO_HIST_12_26_9 ->
                    computeHist(ppo, signal, last);

            default ->
                    throw new IllegalArgumentException("Frame PPO não suportado: " + frame);
        };
    }
}
