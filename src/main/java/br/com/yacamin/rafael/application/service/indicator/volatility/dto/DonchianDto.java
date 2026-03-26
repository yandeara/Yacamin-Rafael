package br.com.yacamin.rafael.application.service.indicator.volatility.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DonchianDto {

    private BigDecimal h;    // high
    private BigDecimal l;    // low
    private BigDecimal r;    // range
    private BigDecimal p;    // percent position (close dentro do canal)
    private BigDecimal pos;  // sinônimo do percent (pode usar para expansão futura)

}
