package br.com.yacamin.rafael.adapter.in.event.dto;

import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.KlineEventResponse;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record KlineUpdateSocketEvent (

    LocalDateTime dateTime,
    KlineEventResponse response

){}
