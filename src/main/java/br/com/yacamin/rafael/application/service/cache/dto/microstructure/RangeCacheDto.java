package br.com.yacamin.rafael.application.service.cache.dto.microstructure;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

@Data
@Builder
public class RangeCacheDto implements CacheDto {
    private Indicator<Num> indicator;
}
