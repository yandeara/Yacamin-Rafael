package br.com.yacamin.rafael.adapter.out.websocket.binance.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MarketDataRequest {

    private Long id;
    private String method;
    private List<String> params;

}
