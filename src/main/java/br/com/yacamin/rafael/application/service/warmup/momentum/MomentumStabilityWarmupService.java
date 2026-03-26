package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.PpoCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.momentum.RsiCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.momentum.TrixCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.momentum.TsiCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.MomentumConsensusDerivation;
import br.com.yacamin.rafael.application.service.indicator.extension.TrixIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.TsiIndicator;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MomentumIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomentumStabilityWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    private final RsiCacheService rsiCacheService;
    private final PpoCacheService ppoCacheService;
    private final TrixCacheService trixCacheService;
    private final TsiCacheService tsiCacheService;

    private final MomentumConsensusDerivation consensusDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][MOM][MOM-STABILITY] erro interno no calculo", ee.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void analyse(MomentumIndicatorEntity entity, SymbolCandle candle, BarSeries series) {

        var symbol   = candle.getSymbol();
        var interval = candle.getInterval();
        var openTime = candle.getOpenTime();
        int index    = series.getEndIndex();

        log.info("[WARMUP][MOM][MOM-STABILITY] {} - {}", symbol, openTime);

        // Caches (TA4J)
        final RSIIndicator rsi14  = rsiCacheService.getRsi14(symbol, interval, series);
        final RSIIndicator rsi48  = rsiCacheService.getRsi48(symbol, interval, series);
        final RSIIndicator rsi288 = rsiCacheService.getRsi288(symbol, interval, series);

        var ppo12Dto  = ppoCacheService.getPpoDefault(symbol, interval, series);
        PPOIndicator ppo12 = ppo12Dto.getPpo();
        EMAIndicator sig12 = ppo12Dto.getSignal();

        var ppo48Dto  = ppoCacheService.getPpo48_104(symbol, interval, series);
        PPOIndicator ppo48 = ppo48Dto.getPpo();
        EMAIndicator sig48 = ppo48Dto.getSignal();

        var ppo288Dto = ppoCacheService.getPpo288_576(symbol, interval, series);
        PPOIndicator ppo288 = ppo288Dto.getPpo();
        EMAIndicator sig288 = ppo288Dto.getSignal();

        final TrixIndicator trix9   = trixCacheService.getTrix9(symbol, interval, series);
        final TrixIndicator trix48  = trixCacheService.getTrix48(symbol, interval, series);
        final TrixIndicator trix288 = trixCacheService.getTrix288(symbol, interval, series);

        final TsiIndicator tsi25_13   = tsiCacheService.getTsi25_13(symbol, interval, series);
        final TsiIndicator tsi48_25   = tsiCacheService.getTsi48_25(symbol, interval, series);
        final TsiIndicator tsi288_144 = tsiCacheService.getTsi288_144(symbol, interval, series);

        double chopScore = 0.0;

        var snap = consensusDerivation.compute(
                series, index,
                rsi14, rsi48, rsi288,
                ppo12, sig12,
                ppo48, sig48,
                ppo288, sig288,
                trix9, trix48, trix288,
                tsi25_13, tsi48_25, tsi288_144,
                chopScore
        );

        List<Callable<Void>> tasks = List.of(
                timedIfZero("mom_ppo_hist_12_26_9_slp_w20", entity::getMom_ppo_hist_12_26_9_slp_w20,
                        () -> snap.ppo_hist_12_slp_w20, entity::setMom_ppo_hist_12_26_9_slp_w20),

                timedIfZero("mom_trix_hist_9_slp_w20", entity::getMom_trix_hist_9_slp_w20,
                        () -> snap.trix_hist_9_slp_w20, entity::setMom_trix_hist_9_slp_w20),

                timedIfZero("mom_tsi_hist_25_13_slp_w20", entity::getMom_tsi_hist_25_13_slp_w20,
                        () -> snap.tsi_hist_25_slp_w20, entity::setMom_tsi_hist_25_13_slp_w20)
        );

        execute(tasks);
    }

    private Callable<Void> timedIfZero(String name,
                                       Supplier<Double> getter,
                                       Supplier<Double> calc,
                                       Consumer<Double> setter) {
        return () -> {
            Double cur = getter.get();
            if (cur == null || cur == 0d) {
                setter.accept(DoubleValidator.validate(calc.get(), name));
            }
            return null;
        };
    }
}
