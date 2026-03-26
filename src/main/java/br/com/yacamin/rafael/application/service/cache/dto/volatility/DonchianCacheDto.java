package br.com.yacamin.rafael.application.service.cache.dto.volatility;


import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import br.com.yacamin.rafael.application.service.indicator.volatility.extension.DonchianLowerIndicator;
import br.com.yacamin.rafael.application.service.indicator.volatility.extension.DonchianMiddleIndicator;
import br.com.yacamin.rafael.application.service.indicator.volatility.extension.DonchianUpperIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DonchianCacheDto implements CacheDto {

    private DonchianUpperIndicator indicatorUp;
    private DonchianMiddleIndicator indicatorMid;
    private DonchianLowerIndicator indicatorLow;

}
