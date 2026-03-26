package br.com.yacamin.rafael.adapter.in.event.dto;

import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.BookTickerUpdateResponse;
import lombok.Data;

@Data
public class BookTickerUpdateSocketEvent {

    private BookTickerUpdateResponse response;

}
