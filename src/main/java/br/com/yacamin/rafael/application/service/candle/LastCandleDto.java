package br.com.yacamin.rafael.application.service.candle;

import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.Data;

import java.time.Instant;

@Data
public class LastCandleDto {
    private String symbol;
    private Instant open;
    private CandleIntervals interval;
}
