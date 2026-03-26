package br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookTickerUpdateResponse {

    @JsonProperty("b")
    private double bestBid;

    @JsonProperty("a")
    private double bestAsk;

    @JsonProperty("s")
    private String symbol;

}
