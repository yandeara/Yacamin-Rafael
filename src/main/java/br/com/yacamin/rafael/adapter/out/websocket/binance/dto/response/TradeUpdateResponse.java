package br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response;

import br.com.yacamin.rafael.application.configuration.JsonTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeUpdateResponse {

    @JsonProperty("e")
    private String eventType;

    @JsonProperty("E")
    @JsonDeserialize(using = JsonTimeDeserializer.class)
    private LocalDateTime eventTime;

    @JsonProperty("T")
    @JsonDeserialize(using = JsonTimeDeserializer.class)
    private LocalDateTime tradeTime;

    @JsonProperty("s")
    private String Symbol;

    @JsonProperty("t")
    private Long tradeId;

    @JsonProperty("p")
    private BigDecimal price;

    @JsonProperty("q")
    private BigDecimal quantity;

    @JsonProperty("X")
    private String orderType;

    @JsonProperty("m")
    private boolean maker;

}
