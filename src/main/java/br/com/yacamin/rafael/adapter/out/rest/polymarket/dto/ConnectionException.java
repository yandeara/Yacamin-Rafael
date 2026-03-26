package br.com.yacamin.rafael.adapter.out.rest.polymarket.dto;

import lombok.Getter;

@Getter
public class ConnectionException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Connection failed.";

    private int erroCode;
    private String erroMessage;

    public ConnectionException() {
        super(DEFAULT_MESSAGE);
    }

    public ConnectionException(String message) {
        super(message != null ? message : DEFAULT_MESSAGE);
    }

    public ConnectionException(String message, int erroCode, String erroMessage) {
        super(message != null ? message : DEFAULT_MESSAGE);
        this.erroCode = erroCode;
        this.erroMessage = erroMessage;
    }

}
