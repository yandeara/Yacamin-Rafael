package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.VwapCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.microstructure.PositionBalanceDerivation;
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
 * Calcula os 10 campos PositionBalance na máscara (mic_close_pos_slp_w20 está no WickWarmup):
 * mic_candle_balance_score, mic_candle_close_pos_norm, mic_close_hlc3_atrn,
 * mic_close_open_norm, mic_close_open_ratio, mic_close_pos_norm,
 * mic_close_to_high_norm, mic_close_to_low_norm, mic_close_triangle_score_atrn, mic_close_vwap_atrn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionBalanceWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final PositionBalanceDerivation positionDerivation;
    private final AtrCacheService atrCacheService;
    private final VwapCacheService vwapCacheService;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {
        var index = series.getEndIndex();
        var atr14 = atrCacheService.getAtr14(candle.getSymbol(), candle.getInterval(), series);
        double vwap = vwapCacheService.getVwap(candle.getSymbol(), candle.getInterval(), series).getValue(index).doubleValue();

        List<Callable<Void>> tasks = List.of(
                calc("mic_close_open_ratio", entity::getMic_close_open_ratio,
                        () -> positionDerivation.calculateCloseOpenRatio(candle), entity::setMic_close_open_ratio),
                calc("mic_close_open_norm", entity::getMic_close_open_norm,
                        () -> positionDerivation.calculateCloseOpenNorm(candle), entity::setMic_close_open_norm),
                calc("mic_close_pos_norm", entity::getMic_close_pos_norm,
                        () -> positionDerivation.calculateClosePosNorm(candle), entity::setMic_close_pos_norm),
                calc("mic_candle_close_pos_norm", entity::getMic_candle_close_pos_norm,
                        () -> positionDerivation.calculateClosePosNorm(candle), entity::setMic_candle_close_pos_norm),
                calc("mic_close_to_high_norm", entity::getMic_close_to_high_norm,
                        () -> positionDerivation.calculateCloseToHighNorm(candle), entity::setMic_close_to_high_norm),
                calc("mic_close_to_low_norm", entity::getMic_close_to_low_norm,
                        () -> positionDerivation.calculateCloseToLowNorm(candle), entity::setMic_close_to_low_norm),
                calc("mic_candle_balance_score", entity::getMic_candle_balance_score,
                        () -> positionDerivation.calculateCandleBalanceScore(candle), entity::setMic_candle_balance_score),
                calc("mic_close_hlc3_atrn", entity::getMic_close_hlc3_atrn,
                        () -> positionDerivation.calculateCloseHlc3AtrN(atr14, candle, index), entity::setMic_close_hlc3_atrn),
                calc("mic_close_triangle_score_atrn", entity::getMic_close_triangle_score_atrn,
                        () -> positionDerivation.calculateCloseTriangleScoreAtrn(atr14, candle, index), entity::setMic_close_triangle_score_atrn),
                calc("mic_close_vwap_atrn", entity::getMic_close_vwap_atrn,
                        () -> positionDerivation.calculateCloseVwapAtrN(atr14, candle, vwap, index), entity::setMic_close_vwap_atrn)
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
