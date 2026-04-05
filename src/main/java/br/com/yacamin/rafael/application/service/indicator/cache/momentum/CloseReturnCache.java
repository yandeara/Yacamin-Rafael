package br.com.yacamin.rafael.application.service.indicator.cache.momentum;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.BurstStrengthExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.ChopRatioExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.CloseReturnExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.ContinuationRateExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.DecayRateExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.ImpulseExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CloseReturnCache implements IndicatorCache {

    private final Map<String, CloseReturnExtension> returnCache = new ConcurrentHashMap<>();
    private final Map<String, BurstStrengthExtension> burstCache = new ConcurrentHashMap<>();
    private final Map<String, ContinuationRateExtension> contCache = new ConcurrentHashMap<>();
    private final Map<String, DecayRateExtension> decayCache = new ConcurrentHashMap<>();
    private final Map<String, ImpulseExtension> impulseCache = new ConcurrentHashMap<>();
    private final Map<String, ChopRatioExtension> chopCache = new ConcurrentHashMap<>();

    private final CloseCache closeCache;

    public CloseReturnCache(CloseCache closeCache) {
        this.closeCache = closeCache;
    }

    public CloseReturnExtension getCloseReturn(String symbol, CandleIntervals interval,
                                                BarSeries series, int window) {
        return returnCache.computeIfAbsent(symbol + "_" + interval.name() + "_" + window, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new CloseReturnExtension(close, window);
        });
    }

    public BurstStrengthExtension getBurstStrength(String symbol, CandleIntervals interval,
                                                    BarSeries series, int window) {
        return burstCache.computeIfAbsent(symbol + "_" + interval.name() + "_" + window, k -> {
            var ret1 = getCloseReturn(symbol, interval, series, 1);
            return new BurstStrengthExtension(ret1, window);
        });
    }

    public ContinuationRateExtension getContinuationRate(String symbol, CandleIntervals interval,
                                                          BarSeries series, int window) {
        return contCache.computeIfAbsent(symbol + "_" + interval.name() + "_" + window, k -> {
            var ret1 = getCloseReturn(symbol, interval, series, 1);
            return new ContinuationRateExtension(ret1, window);
        });
    }

    public DecayRateExtension getDecayRate(String symbol, CandleIntervals interval,
                                            BarSeries series, int window) {
        return decayCache.computeIfAbsent(symbol + "_" + interval.name() + "_" + window, k -> {
            var ret1 = getCloseReturn(symbol, interval, series, 1);
            return new DecayRateExtension(ret1, window);
        });
    }

    public ImpulseExtension getImpulse(String symbol, CandleIntervals interval,
                                        BarSeries series, int window) {
        return impulseCache.computeIfAbsent(symbol + "_" + interval.name() + "_" + window, k -> {
            var ret1 = getCloseReturn(symbol, interval, series, 1);
            return new ImpulseExtension(ret1, window);
        });
    }

    public ChopRatioExtension getChopRatio(String symbol, CandleIntervals interval,
                                            BarSeries series, int window) {
        return chopCache.computeIfAbsent(symbol + "_" + interval.name() + "_" + window, k -> {
            var ret1 = getCloseReturn(symbol, interval, series, 1);
            return new ChopRatioExtension(ret1, window);
        });
    }

    public void clear() {
        returnCache.clear();
        burstCache.clear();
        contCache.clear();
        decayCache.clear();
        impulseCache.clear();
        chopCache.clear();
    }
}
