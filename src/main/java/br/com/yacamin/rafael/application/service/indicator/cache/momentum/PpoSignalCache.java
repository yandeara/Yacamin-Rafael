package br.com.yacamin.rafael.application.service.indicator.cache.momentum;

import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PpoSignalCache implements IndicatorCache {

    private final Map<String, EMAIndicator> cache = new ConcurrentHashMap<>();

    public EMAIndicator getSignal(String symbol, CandleIntervals interval, PPOIndicator ppo,
                                   int fast, int slow, int signalPeriod) {
        String key = symbol + "_" + interval.name() + "_ppo" + fast + "_" + slow + "_sig" + signalPeriod;
        return cache.computeIfAbsent(key, k -> new EMAIndicator(ppo, signalPeriod));
    }

    public void clear() {
        cache.clear();
    }
}
