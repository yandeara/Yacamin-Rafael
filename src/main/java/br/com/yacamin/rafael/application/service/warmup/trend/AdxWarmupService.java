package br.com.yacamin.rafael.application.service.warmup.trend;

import br.com.yacamin.rafael.application.service.cache.indicator.trend.AdxCacheService;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.trend.AdxDerivation;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.TrendIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

/**
 * Calcula APENAS trd_di_diff_14 (único campo ADX na máscara).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdxWarmupService {

    private final AdxCacheService adxCacheService;
    private final AdxDerivation adxDerivation;

    public void analyse(TrendIndicatorEntity entity, SymbolCandle candle, BarSeries series) {
        int last = series.getEndIndex();
        var dto = adxCacheService.getAdx14(candle.getSymbol(), candle.getInterval(), series);

        Double cur = entity.getTrd_di_diff_14();
        if (cur == 0) {
            entity.setTrd_di_diff_14(DoubleValidator.validate(
                    adxDerivation.diDiff(dto.getPdi(), dto.getMdi(), last), "trd_di_diff_14"));
        }
    }
}
