package br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response;

import br.com.yacamin.rafael.application.configuration.JsonTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MarkPriceUpdateResponse {

    @JsonProperty("e")
    private String eventType;

    @JsonProperty("E")
    @JsonDeserialize(using = JsonTimeDeserializer.class)
    private LocalDateTime eventTime;

    @JsonProperty("s")
    private String Symbol;

    @JsonProperty("p")
    private BigDecimal markPrice;

    @JsonProperty("i")
    private BigDecimal indexPrice;

    @JsonProperty("P")
    private BigDecimal settlePrice;

    @JsonProperty("r")
    private BigDecimal fundingRate;

    @JsonProperty("T")
    @JsonDeserialize(using = JsonTimeDeserializer.class)
    private LocalDateTime nextFundingTime;

}
