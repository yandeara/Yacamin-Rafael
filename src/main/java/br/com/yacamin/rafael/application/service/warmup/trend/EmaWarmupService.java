package br.com.yacamin.rafael.application.service.warmup.trend;

import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.EmaCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.AtrNormalizeDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.trend.EmaDerivation;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.TrendIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Calcula APENAS os 25 campos EMA usados na máscara getProdMask().
 * Todos calculam a partir de caches (EMA, close, ATR), sem intermediários na entity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmaWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(16);

    private final EmaCacheService emaCache;
    private final CloseCacheService closeCache;
    private final AtrCacheService atrCache;
    private final AtrNormalizeDerivation atrNormalize;
    private final EmaDerivation emaDerivation;

    public void analyse(TrendIndicatorEntity entity, SymbolCandle candle, BarSeries series) {

        var symbol   = candle.getSymbol();
        var interval = candle.getInterval();
        int last     = series.getEndIndex();

        var closeInd = closeCache.getClosePrice(symbol, interval, series);
        var atr14    = atrCache.getAtr14(symbol, interval, series);

        // Apenas os EMAs usados na máscara: e8, e20, e50
        var e8  = emaCache.getEma8(symbol, interval, series);
        var e20 = emaCache.getEma20(symbol, interval, series);
        var e50 = emaCache.getEma50(symbol, interval, series);

        // Slopes usados na máscara: s8, s20, s50
        var s8  = emaCache.getEma8Slp(symbol, interval, series);
        var s20 = emaCache.getEma20Slp(symbol, interval, series);
        var s50 = emaCache.getEma50Slp(symbol, interval, series);

        // Slope accs usados na máscara: a8, a20, a50
        var a8  = emaCache.getEma8SlpAcc(symbol, interval, series);
        var a20 = emaCache.getEma20SlpAcc(symbol, interval, series);
        var a50 = emaCache.getEma50SlpAcc(symbol, interval, series);

        double closeNow = candle.getClose();

        List<Callable<Void>> tasks = List.of(
                // Slope ATR-N
                calc("trd_ema_8_slp_atrn", entity::getTrd_ema_8_slp_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.slope(s8, last)), entity::setTrd_ema_8_slp_atrn),
                calc("trd_ema_20_slp_atrn", entity::getTrd_ema_20_slp_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.slope(s20, last)), entity::setTrd_ema_20_slp_atrn),
                calc("trd_ema_50_slp_atrn", entity::getTrd_ema_50_slp_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.slope(s50, last)), entity::setTrd_ema_50_slp_atrn),

                // Slope Acc ATR-N
                calc("trd_ema_8_slp_acc_atrn", entity::getTrd_ema_8_slp_acc_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.slopeAcc(a8, last)), entity::setTrd_ema_8_slp_acc_atrn),
                calc("trd_ema_20_slp_acc_atrn", entity::getTrd_ema_20_slp_acc_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.slopeAcc(a20, last)), entity::setTrd_ema_20_slp_acc_atrn),
                calc("trd_ema_50_slp_acc_atrn", entity::getTrd_ema_50_slp_acc_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.slopeAcc(a50, last)), entity::setTrd_ema_50_slp_acc_atrn),

                // Distance ATR-N (close to EMA)
                calc("trd_dist_close_ema_8_atrn", entity::getTrd_dist_close_ema_8_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.absDistance(closeInd, e8, last)), entity::setTrd_dist_close_ema_8_atrn),
                calc("trd_dist_close_ema_20_atrn", entity::getTrd_dist_close_ema_20_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.absDistance(closeInd, e20, last)), entity::setTrd_dist_close_ema_20_atrn),
                calc("trd_dist_close_ema_50_atrn", entity::getTrd_dist_close_ema_50_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.absDistance(closeInd, e50, last)), entity::setTrd_dist_close_ema_50_atrn),

                // Distance ATR-N (EMA to EMA)
                calc("trd_dist_ema_8_20_atrn", entity::getTrd_dist_ema_8_20_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.absDistance(e8, e20, last)), entity::setTrd_dist_ema_8_20_atrn),
                calc("trd_dist_ema_20_50_atrn", entity::getTrd_dist_ema_20_50_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.absDistance(e20, e50, last)), entity::setTrd_dist_ema_20_50_atrn),
                calc("trd_dist_ema_8_50_atrn", entity::getTrd_dist_ema_8_50_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.absDistance(e8, e50, last)), entity::setTrd_dist_ema_8_50_atrn),

                // Ratios
                calc("trd_ratio_ema_8_20", entity::getTrd_ratio_ema_8_20,
                        () -> emaDerivation.ratio(e8, e20, last), entity::setTrd_ratio_ema_8_20),
                calc("trd_ratio_ema_20_50", entity::getTrd_ratio_ema_20_50,
                        () -> emaDerivation.ratio(e20, e50, last), entity::setTrd_ratio_ema_20_50),
                calc("trd_ratio_ema_8_50", entity::getTrd_ratio_ema_8_50,
                        () -> emaDerivation.ratio(e8, e50, last), entity::setTrd_ratio_ema_8_50),

                // Alignment
                calc("trd_alignment_ema_8_20_50_score", entity::getTrd_alignment_ema_8_20_50_score,
                        () -> (double) emaDerivation.alignmentScore(e8, e20, e50, last), entity::setTrd_alignment_ema_8_20_50_score),
                calc("trd_alignment_ema_8_20_50_normalized", entity::getTrd_alignment_ema_8_20_50_normalized,
                        () -> emaDerivation.alignmentNormalized(emaDerivation.alignmentScore(e8, e20, e50, last)), entity::setTrd_alignment_ema_8_20_50_normalized),
                calc("trd_aligment_ema_8_20_50_delta", entity::getTrd_aligment_ema_8_20_50_delta,
                        () -> emaDerivation.alignmentDelta(e20, e50, closeNow, last), entity::setTrd_aligment_ema_8_20_50_delta),

                // Cross Delta ATR-N
                calc("trd_cross_ema_8_20_delta_atrn", entity::getTrd_cross_ema_8_20_delta_atrn,
                        () -> emaDerivation.crossDeltaAtrn(e8, e20, closeNow, atr14, last), entity::setTrd_cross_ema_8_20_delta_atrn),
                calc("trd_cross_ema_20_50_delta_atrn", entity::getTrd_cross_ema_20_50_delta_atrn,
                        () -> emaDerivation.crossDeltaAtrn(e20, e50, closeNow, atr14, last), entity::setTrd_cross_ema_20_50_delta_atrn),

                // Delta ATR-N (close to EMA)
                calc("trd_delta_close_ema_8_atrn", entity::getTrd_delta_close_ema_8_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.delta(closeInd, e8, last)), entity::setTrd_delta_close_ema_8_atrn),
                calc("trd_delta_close_ema_20_atrn", entity::getTrd_delta_close_ema_20_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.delta(closeInd, e20, last)), entity::setTrd_delta_close_ema_20_atrn),
                calc("trd_delta_close_ema_50_atrn", entity::getTrd_delta_close_ema_50_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.delta(closeInd, e50, last)), entity::setTrd_delta_close_ema_50_atrn),

                // Delta ATR-N (EMA to EMA)
                calc("trd_delta_ema_8_20_atrn", entity::getTrd_delta_ema_8_20_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.delta(e8, e20, last)), entity::setTrd_delta_ema_8_20_atrn),
                calc("trd_delta_ema_20_50_atrn", entity::getTrd_delta_ema_20_50_atrn,
                        () -> atrNormalize.normalize(atr14, last, emaDerivation.delta(e20, e50, last)), entity::setTrd_delta_ema_20_50_atrn)
        );

        execute(tasks);
    }

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) { try { f.get(); } catch (ExecutionException ee) { throw new RuntimeException(ee.getCause()); } }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
    }

    private Callable<Void> calc(String name, Supplier<Double> getter, Supplier<Double> calculator, Consumer<Double> setter) {
        return () -> { Double c = getter.get(); if (c == null || c == 0) setter.accept(DoubleValidator.validate(calculator.get(), name)); return null; };
    }
}
