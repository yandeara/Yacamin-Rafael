package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.indicator.microstructure.WickDerivation;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MicrostructureIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Calcula os 19 campos Wick usados na máscara.
 * Todos calculam direto do BarSeries/candle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WickWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(16);
    private final WickDerivation wickDerivation;
    private final AtrCacheService atrCacheService;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {
        var index = series.getEndIndex();
        var atr14 = atrCacheService.getAtr14(candle.getSymbol(), candle.getInterval(), series);

        List<Callable<Void>> tasks = List.of(
                calc("mic_candle_upper_wick_pct", entity::getMic_candle_upper_wick_pct,
                        () -> wickDerivation.calculateUpperWickPct(candle, series, index), entity::setMic_candle_upper_wick_pct),
                calc("mic_candle_lower_wick_pct", entity::getMic_candle_lower_wick_pct,
                        () -> wickDerivation.calculateLowerWickPct(candle, series, index), entity::setMic_candle_lower_wick_pct),
                calc("mic_wick_perc_up", entity::getMic_wick_perc_up,
                        () -> wickDerivation.calculateUpperWickPct(candle, series, index), entity::setMic_wick_perc_up),
                calc("mic_wick_perc_down", entity::getMic_wick_perc_down,
                        () -> wickDerivation.calculateLowerWickPct(candle, series, index), entity::setMic_wick_perc_down),
                calc("mic_candle_total_wick_pct", entity::getMic_candle_total_wick_pct,
                        () -> wickDerivation.calculateTotalWickPct(candle, series, index), entity::setMic_candle_total_wick_pct),
                calc("mic_candle_total_wick_atrn", entity::getMic_candle_total_wick_atrn,
                        () -> wickDerivation.calculateTotalWickAtrn(atr14, candle, series, index), entity::setMic_candle_total_wick_atrn),
                calc("mic_upper_wick_return", entity::getMic_upper_wick_return,
                        () -> wickDerivation.calculateUpperWickReturn(series, index), entity::setMic_upper_wick_return),
                calc("mic_lower_wick_return", entity::getMic_lower_wick_return,
                        () -> wickDerivation.calculateLowerWickReturn(series, index), entity::setMic_lower_wick_return),
                calc("mic_candle_wick_imbalance", entity::getMic_candle_wick_imbalance,
                        () -> wickDerivation.calculateWickImbalance(candle, series, index), entity::setMic_candle_wick_imbalance),
                calc("mic_wick_imbalance", entity::getMic_wick_imbalance,
                        () -> wickDerivation.calculateWickImbalance(candle, series, index), entity::setMic_wick_imbalance),
                calc("mic_candle_wick_imbalance_norm", entity::getMic_candle_wick_imbalance_norm,
                        () -> wickDerivation.calculateWickImbalanceNorm(candle, series, index), entity::setMic_candle_wick_imbalance_norm),
                calc("mic_candle_wick_imbalance_slp_w10", entity::getMic_candle_wick_imbalance_slp_w10,
                        () -> wickDerivation.calculateWickImbalanceSlope(candle, series, index, 10), entity::setMic_candle_wick_imbalance_slp_w10),
                calc("mic_shadow_imbalance_score", entity::getMic_shadow_imbalance_score,
                        () -> wickDerivation.calculateShadowImbalanceScore(candle, series, index), entity::setMic_shadow_imbalance_score),
                calc("mic_candle_wick_pressure_score", entity::getMic_candle_wick_pressure_score,
                        () -> wickDerivation.calculateWickPressureScore(candle, series, index), entity::setMic_candle_wick_pressure_score),
                calc("mic_candle_shadow_ratio", entity::getMic_candle_shadow_ratio,
                        () -> wickDerivation.calculateShadowRatio(candle, series, index), entity::setMic_candle_shadow_ratio),
                calc("mic_candle_wick_body_alignment", entity::getMic_candle_wick_body_alignment,
                        () -> wickDerivation.calculateWickBodyAlignment(candle, series, index), entity::setMic_candle_wick_body_alignment),
                calc("mic_candle_wick_dominance", entity::getMic_candle_wick_dominance,
                        () -> wickDerivation.calculateWickDominance(candle, series, index), entity::setMic_candle_wick_dominance),
                calc("mic_candle_wick_exhaustion", entity::getMic_candle_wick_exhaustion,
                        () -> wickDerivation.calculateWickExhaustion(candle, series, index), entity::setMic_candle_wick_exhaustion),
                calc("mic_close_pos_slp_w20", entity::getMic_close_pos_slp_w20,
                        () -> wickDerivation.calculateClosePosSlopeW20(candle, series, index), entity::setMic_close_pos_slp_w20)
        );

        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) { try { f.get(); } catch (ExecutionException ee) { throw new RuntimeException(ee.getCause()); } }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
    }

    private Callable<Void> calc(String name, Supplier<Double> getter, Supplier<Double> calculator, Consumer<Double> setter) {
        return () -> { Double c = getter.get(); if (c == null || c == 0) setter.accept(DoubleValidator.validate(calculator.get(), name)); return null; };
    }
}
