package br.com.yacamin.rafael.adapter.in.event.dto;

import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.TradeUpdateResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TradeUpdateSocketEvent {

    private LocalDateTime dateTime;
    private TradeUpdateResponse response;

}
