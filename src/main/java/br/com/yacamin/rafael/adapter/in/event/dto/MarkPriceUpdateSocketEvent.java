package br.com.yacamin.rafael.adapter.in.event.dto;

import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.MarkPriceUpdateResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MarkPriceUpdateSocketEvent {

    private LocalDateTime dateTime;
    private MarkPriceUpdateResponse response;

}
