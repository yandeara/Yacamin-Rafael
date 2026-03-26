package br.com.yacamin.rafael.application.service.cache;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import org.springframework.stereotype.Service;

@Service
public class GetCacheKeyService {
    public String getCacheKey(String symbol, CandleIntervals interval) {
        return symbol + "-" + interval.name();
    }

    public String getCacheKeyNew(String symbol, CandleIntervals interval, Frame label) {
        return symbol + "-" + interval.name() + "-" + label.name();
    }
}
