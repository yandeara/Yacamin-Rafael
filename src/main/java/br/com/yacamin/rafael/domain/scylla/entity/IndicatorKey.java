package br.com.yacamin.rafael.domain.scylla.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class IndicatorKey implements Serializable {
    private String symbol;
    private Instant openTime;

    public IndicatorKey() {}
    public IndicatorKey(String symbol, Instant openTime) {
        this.symbol = symbol;
        this.openTime = openTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndicatorKey that = (IndicatorKey) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(openTime, that.openTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, openTime);
    }
}
