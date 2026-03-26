package br.com.yacamin.rafael.application.service.indicator.volatility.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BollingerDto {
    private BigDecimal u;
    private BigDecimal m;
    private BigDecimal l;
    private BigDecimal p;
    private BigDecimal w;
}