package br.com.yacamin.rafael.adapter.out.websocket.binance.dto.request;

import lombok.Getter;

public enum StreamRequestMethod {

    SUBSCRIBE("SUBSCRIBE"),
    UNSUBSCRIBE("UNSUBSCRIBE");

    @Getter
    private final String value;

    StreamRequestMethod(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
