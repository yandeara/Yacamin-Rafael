package br.com.yacamin.rafael.application.service.cache.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.volatility.DonchianCacheDto;
import br.com.yacamin.rafael.application.service.indicator.volatility.extension.DonchianLowerIndicator;
import br.com.yacamin.rafael.application.service.indicator.volatility.extension.DonchianMiddleIndicator;
import br.com.yacamin.rafael.application.service.indicator.volatility.extension.DonchianUpperIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class DonchianCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private DonchianCacheDto get(String symbol,
                                 CandleIntervals interval,
                                 BarSeries series,
                                 Frame label) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (DonchianCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing;
        }

        DonchianCacheDto dto = create(series, label);

        cacheService.put(key, dto);

        return dto;
    }

    private DonchianCacheDto create(BarSeries series, Frame frame) {

        return switch (frame) {

            case DNCH10  -> build(series, 10);
            case DNCH20  -> build(series, 20);
            case DNCH50  -> build(series, 50);
            case DNCH55  -> build(series, 55);
            case DNCH100 -> build(series, 100);

            default -> throw new IllegalStateException("Unexpected Donchian Frame: " + frame);
        };
    }

    private DonchianCacheDto build(BarSeries series, int period) {

        DonchianUpperIndicator upper   = new DonchianUpperIndicator(series, period);
        DonchianLowerIndicator lower   = new DonchianLowerIndicator(series, period);
        DonchianMiddleIndicator middle = new DonchianMiddleIndicator(series, period);

        return DonchianCacheDto.builder()
                .indicatorUp(upper)
                .indicatorLow(lower)
                .indicatorMid(middle)
                .build();
    }

    public DonchianCacheDto getDnch10(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.DNCH10);
    }

    public DonchianCacheDto getDnch20(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.DNCH20);
    }

    public DonchianCacheDto getDnch50(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.DNCH50);
    }

    public DonchianCacheDto getDnch55(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.DNCH55);
    }

    public DonchianCacheDto getDnch100(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.DNCH100);
    }
}
