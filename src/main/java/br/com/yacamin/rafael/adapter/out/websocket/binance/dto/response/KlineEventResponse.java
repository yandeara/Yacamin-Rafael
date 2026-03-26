package br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record KlineEventResponse(

        @JsonProperty("e")
        String eventType,

        @JsonProperty("E")
        Instant eventTime,

        @JsonProperty("s")
        String symbol,

        @JsonProperty("k")
        KlineEventDataResponse kline

) {}
