package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.indicator.microstructure.RangeAmplitudeDerivation;
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
 * Calcula os 22 campos Range usados na máscara.
 * Todos calculam direto do BarSeries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RangeWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(16);
    private final RangeAmplitudeDerivation rangeDerivation;
    private final AtrCacheService atrCacheService;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {
        var index = series.getEndIndex();
        var atr14 = atrCacheService.getAtr14(candle.getSymbol(), candle.getInterval(), series);

        List<Callable<Void>> tasks = List.of(
                calc("mic_true_range", entity::getMic_true_range,
                        () -> rangeDerivation.calculateTrueRange(candle, series, index), entity::setMic_true_range),
                calc("mic_tr_atrn", entity::getMic_tr_atrn,
                        () -> rangeDerivation.calculateTrueRangeAtrn(candle, atr14, series, index), entity::setMic_tr_atrn),
                calc("mic_tr_range_ratio", entity::getMic_tr_range_ratio,
                        () -> rangeDerivation.calculateTrRangeRatio(candle, series, index), entity::setMic_tr_range_ratio),
                calc("mic_range_atrn", entity::getMic_range_atrn,
                        () -> rangeDerivation.calculateRangeAtrn(candle, atr14, series, index), entity::setMic_range_atrn),
                calc("mic_range_stdn", entity::getMic_range_stdn,
                        () -> rangeDerivation.calculateRangeStdn(candle, series, index, 20), entity::setMic_range_stdn),
                calc("mic_range_atr_ratio", entity::getMic_range_atr_ratio,
                        () -> rangeDerivation.calculateRangeAtrRatio(candle, atr14, series, index), entity::setMic_range_atr_ratio),
                calc("mic_range_slp_w10", entity::getMic_range_slp_w10,
                        () -> rangeDerivation.calculateRangeSlope(candle, series, index, 10), entity::setMic_range_slp_w10),
                calc("mic_range_slp_w20", entity::getMic_range_slp_w20,
                        () -> rangeDerivation.calculateRangeSlope(candle, series, index, 20), entity::setMic_range_slp_w20),
                calc("mic_range_acc_w5", entity::getMic_range_acc_w5,
                        () -> rangeDerivation.calculateRangeAcceleration(candle, series, index, 5), entity::setMic_range_acc_w5),
                calc("mic_range_acc_w10", entity::getMic_range_acc_w10,
                        () -> rangeDerivation.calculateRangeAcceleration(candle, series, index, 10), entity::setMic_range_acc_w10),
                calc("mic_range_compression_w20", entity::getMic_range_compression_w20,
                        () -> rangeDerivation.calculateRangeCompressionW20(candle, series, index), entity::setMic_range_compression_w20),
                calc("mic_range_squeeze_w20", entity::getMic_range_squeeze_w20,
                        () -> rangeDerivation.calculateRangeSqueeze(candle, series, index), entity::setMic_range_squeeze_w20),
                calc("mic_range_asymmetry", entity::getMic_range_asymmetry,
                        () -> rangeDerivation.calculateRangeAsymmetry(series, index), entity::setMic_range_asymmetry),
                calc("mic_range_headroom_atr", entity::getMic_range_headroom_atr,
                        () -> rangeDerivation.calculateRangeHeadroom(candle, atr14, series, index), entity::setMic_range_headroom_atr),
                calc("mic_range_return", entity::getMic_range_return,
                        () -> rangeDerivation.micRangeReturn(candle, series, index), entity::setMic_range_return),
                calc("mic_gap_ratio", entity::getMic_gap_ratio,
                        () -> rangeDerivation.calculateGapRatio(candle, series, index), entity::setMic_gap_ratio),
                calc("mic_log_range_slp_w20", entity::getMic_log_range_slp_w20,
                        () -> rangeDerivation.calculateLogRangeSlopeW20(candle, series, index), entity::setMic_log_range_slp_w20),
                calc("mic_candle_brr", entity::getMic_candle_brr,
                        () -> rangeDerivation.micCandleBrr(candle), entity::setMic_candle_brr),
                calc("mic_candle_range", entity::getMic_candle_range,
                        () -> rangeDerivation.micCandleRange(candle), entity::setMic_candle_range),
                calc("mic_candle_volatility_inside", entity::getMic_candle_volatility_inside,
                        () -> rangeDerivation.micCandleVolatilityInside(candle, series, index), entity::setMic_candle_volatility_inside),
                calc("mic_candle_spread_ratio", entity::getMic_candle_spread_ratio,
                        () -> rangeDerivation.micCandleSpreadRatio(candle, series, index), entity::setMic_candle_spread_ratio),
                calc("mic_candle_lmr", entity::getMic_candle_lmr,
                        () -> rangeDerivation.micCandleLmr(candle), entity::setMic_candle_lmr),
                calc("mic_high_return", entity::getMic_high_return,
                        () -> rangeDerivation.micHighReturn(series, index), entity::setMic_high_return),
                calc("mic_low_return", entity::getMic_low_return,
                        () -> rangeDerivation.micLowReturn(series, index), entity::setMic_low_return),
                calc("mic_extreme_range_return", entity::getMic_extreme_range_return,
                        () -> rangeDerivation.micExtremeRangeReturn(series, index), entity::setMic_extreme_range_return)
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
