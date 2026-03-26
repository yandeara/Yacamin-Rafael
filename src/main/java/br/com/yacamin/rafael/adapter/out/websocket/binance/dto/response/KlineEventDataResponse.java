package br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

public record KlineEventDataResponse(

    @JsonProperty("t")
    Instant openTime,

    @JsonProperty("T")
    Instant closeTime,

    @JsonProperty("s")
    String symbol,

    @JsonProperty("i")
    String interval,

    @JsonProperty("f")
    Long firstTradeId,

    @JsonProperty("L")
    Long lastTradeId,

    @JsonProperty("o")
    BigDecimal open,

    @JsonProperty("c")
    BigDecimal close,

    @JsonProperty("h")
    BigDecimal high,

    @JsonProperty("l")
    BigDecimal low,

    @JsonProperty("v")
    BigDecimal volume,

    @JsonProperty("n")
    Long numberOfTrades,

    @JsonProperty("x")
    boolean closed,

    @JsonProperty("q")
    BigDecimal quoteVolume,

    @JsonProperty("V")
    BigDecimal takerBuyBaseVolume,

    @JsonProperty("Q")
    BigDecimal takerBuyQuoteVolume,

    @JsonProperty("B")
    String ignore

) {}
