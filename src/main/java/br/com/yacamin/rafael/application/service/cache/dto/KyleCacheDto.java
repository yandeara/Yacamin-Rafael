package br.com.yacamin.rafael.application.service.cache.dto;

import lombok.Builder;
import lombok.Data;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

@Data
@Builder
public class KyleCacheDto implements CacheDto {
    private Indicator<Num> indicator;
}
