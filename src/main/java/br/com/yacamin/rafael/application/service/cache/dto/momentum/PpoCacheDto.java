package br.com.yacamin.rafael.application.service.cache.dto.momentum;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Data
@Builder
public class PpoCacheDto implements CacheDto {

    private PPOIndicator ppo;
    private EMAIndicator signal;
}
