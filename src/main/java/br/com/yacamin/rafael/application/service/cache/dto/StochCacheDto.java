package br.com.yacamin.rafael.application.service.cache.dto;

import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

@Data
@Builder
public class StochCacheDto implements CacheDto {
    private StochasticOscillatorKIndicator k;
    private StochasticOscillatorDIndicator d;
}