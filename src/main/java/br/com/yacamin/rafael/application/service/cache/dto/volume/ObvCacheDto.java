package br.com.yacamin.rafael.application.service.cache.dto.volume;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

@Data
@Builder
public class ObvCacheDto implements CacheDto {
    private OnBalanceVolumeIndicator indicator;
}
