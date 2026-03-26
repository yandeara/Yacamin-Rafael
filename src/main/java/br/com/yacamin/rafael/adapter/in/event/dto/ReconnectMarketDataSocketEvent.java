package br.com.yacamin.rafael.adapter.in.event.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReconnectMarketDataSocketEvent {

    private LocalDateTime dateTime;
    private String message;
    private String type;

}
