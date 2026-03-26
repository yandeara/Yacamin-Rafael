package br.com.yacamin.rafael.application.service.cache.indicator.volume;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.volume.CmfCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.volume.ObvCacheDto;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

@Service
@RequiredArgsConstructor
public class CmfCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private ChaikinMoneyFlowIndicator get(String symbol,
                                          CandleIntervals interval,
                                          BarSeries series,
                                          Frame label,
                                          int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (CmfCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        ChaikinMoneyFlowIndicator cmf = create(series, period);

        cacheService.put(key, CmfCacheDto.builder()
                .indicator(cmf)
                .build());

        return cmf;
    }

    private ChaikinMoneyFlowIndicator create(BarSeries series, int period) {
        return new ChaikinMoneyFlowIndicator(series, period);
    }

    public ChaikinMoneyFlowIndicator getCmf20(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CMF_20, 20);
    }

}
