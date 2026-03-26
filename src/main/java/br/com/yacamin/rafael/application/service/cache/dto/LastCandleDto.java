package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.domain.CandleIntervals;
import java.time.Instant;
import lombok.Data;

@Data
public class LastCandleDto {

    private String symbol;
    private Instant open;
    private CandleIntervals interval;

}
