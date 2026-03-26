package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.volatility.DonchianCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.DonchianCacheService;
import br.com.yacamin.rafael.application.service.indicator.volatility.dto.DonchianDto;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonchianIndicatorService {

    private final CloseCacheService closeCacheService;
    private final DonchianCacheService donchianCacheService;

    private DonchianDto calculate(String symbol,
                                  CandleIntervals interval,
                                  BarSeries series,
                                  DonchianCacheDto dto) {

        int last = series.getEndIndex();

        Num upper = dto.getIndicatorUp().getValue(last);
        Num lower = dto.getIndicatorLow().getValue(last);
        Num middle = dto.getIndicatorMid().getValue(last);

        Num range = upper.minus(lower);

        ClosePriceIndicator closeIndicator = closeCacheService.getClosePrice(symbol, interval, series);
        Num close = closeIndicator.getValue(last);

        // 0..1: onde o close está entre o low e o high
        Num percent = close.minus(lower).dividedBy(range);

        // posição relativa em torno do middle (aprox. -0.5..+0.5)
        Num position = close.minus(middle).dividedBy(range);

        return DonchianDto.builder()
                .h(upper.bigDecimalValue())
                .l(lower.bigDecimalValue())
                .r(range.bigDecimalValue())
                .p(percent.bigDecimalValue())
                .pos(position.bigDecimalValue())
                .build();
    }

    public DonchianDto calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();

        return switch (frame) {

            case DNCH10  -> calculate(symbol, interval, series, donchianCacheService.getDnch10(symbol, interval, series));
            case DNCH20  -> calculate(symbol, interval, series, donchianCacheService.getDnch20(symbol, interval, series));
            case DNCH50  -> calculate(symbol, interval, series, donchianCacheService.getDnch50(symbol, interval, series));
            case DNCH55  -> calculate(symbol, interval, series, donchianCacheService.getDnch55(symbol, interval, series));
            case DNCH100 -> calculate(symbol, interval, series, donchianCacheService.getDnch100(symbol, interval, series));

            default -> throw new IllegalArgumentException("Frame Donchian não suportado: " + frame);
        };
    }
}
