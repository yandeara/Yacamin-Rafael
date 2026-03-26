package br.com.yacamin.rafael.application.service.cache.dto.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.BollingerWidthIndicator;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;

@Data
@Builder
public class BollingerCacheDto implements CacheDto {

    BollingerBandsUpperIndicator indicatorUp;
    BollingerBandsMiddleIndicator indicatorMid;
    BollingerBandsLowerIndicator indicatorLow;
    BollingerWidthIndicator indicatorWidth;

}
