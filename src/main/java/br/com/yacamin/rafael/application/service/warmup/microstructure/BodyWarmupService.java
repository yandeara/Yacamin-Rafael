package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.AtrNormalizeDerivation;
import br.com.yacamin.rafael.application.service.indicator.microstructure.BodyDerivation;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MicrostructureIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Calcula os 22 campos Body usados na máscara getProdMask() + 1 intermediário (energy_raw).
 *
 * Removidos: mic_candle_body, mic_candle_body_abs, mic_candle_body_ma_10/20,
 *            mic_candle_body_vol_10/20, mic_body_ratio_vol_10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BodyWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(16);

    private final BodyDerivation bodyDerivation;
    private final AtrCacheService atrCacheService;
    private final AtrNormalizeDerivation atrNormalizeDerivation;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {

        var index = series.getEndIndex();
        ATRIndicator atr14 = atrCacheService.getAtr14(candle.getSymbol(), candle.getInterval(), series);

        // Priority: intermediários necessários para subDerivative
        List<Callable<Void>> priority = List.of(
                timedIfZero("mic_body_ratio", entity::getMic_body_ratio,
                        () -> bodyDerivation.calculateBodyRatio(candle, series, index),
                        entity::setMic_body_ratio),
                timedIfZero("mic_candle_energy_raw", entity::getMic_candle_energy_raw,
                        () -> bodyDerivation.calculateBodyEnergyRaw(candle, series, index),
                        entity::setMic_candle_energy_raw)
        );
        execute(priority);

        // Tudo o resto calcula direto do BarSeries, sem dependências entre si
        List<Callable<Void>> main = List.of(
                timedIfZero("mic_candle_body_slp_w10", entity::getMic_candle_body_slp_w10,
                        () -> bodyDerivation.calculateBodySlope(candle, series, index, 10),
                        entity::setMic_candle_body_slp_w10),
                timedIfZero("mic_candle_body_slp_w20", entity::getMic_candle_body_slp_w20,
                        () -> bodyDerivation.calculateBodySlope(candle, series, index, 20),
                        entity::setMic_candle_body_slp_w20),
                timedIfZero("mic_body_ratio_slp_w10", entity::getMic_body_ratio_slp_w10,
                        () -> bodyDerivation.calculateBodyRatioSlope(candle, series, index, 10),
                        entity::setMic_body_ratio_slp_w10),
                timedIfZero("mic_body_atr_ratio", entity::getMic_body_atr_ratio,
                        () -> bodyDerivation.calculateBodyAtrRatio(atr14, candle, series, index),
                        entity::setMic_body_atr_ratio),
                timedIfZero("mic_candle_energy_atrn", entity::getMic_candle_energy_atrn,
                        () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMic_candle_energy_raw()),
                        entity::setMic_candle_energy_atrn),
                timedIfZero("mic_candle_body_center_position", entity::getMic_candle_body_center_position,
                        () -> bodyDerivation.calculateBodyCenterPosition(candle),
                        entity::setMic_candle_body_center_position),
                timedIfZero("mic_candle_pressure_raw", entity::getMic_candle_pressure_raw,
                        () -> bodyDerivation.calculateBodyPressureRaw(candle, series, index),
                        entity::setMic_candle_pressure_raw),
                timedIfZero("mic_candle_strength", entity::getMic_candle_strength,
                        () -> bodyDerivation.calculateBodyStrength(candle, series, index),
                        entity::setMic_candle_strength),
                timedIfZero("mic_candle_body_strength_score", entity::getMic_candle_body_strength_score,
                        () -> bodyDerivation.calculateBodyStrengthScore(candle, series, index),
                        entity::setMic_candle_body_strength_score),
                timedIfZero("mic_candle_body_pct", entity::getMic_candle_body_pct,
                        () -> bodyDerivation.calculateBodyPct(candle, series, index),
                        entity::setMic_candle_body_pct),
                timedIfZero("mic_body_perc", entity::getMic_body_perc,
                        () -> bodyDerivation.calculateBodyPerc(candle, series, index),
                        entity::setMic_body_perc),
                timedIfZero("mic_candle_body_ratio", entity::getMic_candle_body_ratio,
                        () -> bodyDerivation.calculateBodyRatioAlt(candle, series, index),
                        entity::setMic_candle_body_ratio),
                timedIfZero("mic_body_return", entity::getMic_body_return,
                        () -> bodyDerivation.calculateBodyReturn(candle, series, index),
                        entity::setMic_body_return),
                timedIfZero("mic_body_shock_atrn", entity::getMic_body_shock_atrn,
                        () -> bodyDerivation.calculateBodyShockAtrn(atr14, candle, series, index),
                        entity::setMic_body_shock_atrn),
                timedIfZero("mic_body_sign_prst_w20", entity::getMic_body_sign_prst_w20,
                        () -> bodyDerivation.calculateBodySignPersistence(candle, series, index, 20),
                        entity::setMic_body_sign_prst_w20),
                timedIfZero("mic_body_run_len", entity::getMic_body_run_len,
                        () -> bodyDerivation.calculateBodyRunLen(candle, series, index, 256),
                        entity::setMic_body_run_len)
                // mic_candle_brr, mic_candle_lmr, mic_candle_spread_ratio,
                // mic_candle_range, mic_candle_volatility_inside
                // são calculados pelo RangeWarmupService
        );
        execute(main);
    }

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try { f.get(); }
                catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][MIC][BODY] erro", ee.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private Callable<Void> timedIfZero(String name, Supplier<Double> getter,
                                        Supplier<Double> calculator, Consumer<Double> setter) {
        return () -> {
            Double current = getter.get();
            if (current == null || current == 0d) {
                setter.accept(DoubleValidator.validate(calculator.get(), name));
            }
            return null;
        };
    }
}
