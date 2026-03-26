package br.com.yacamin.rafael.domain;

import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.ta4j.core.Bar;
import org.ta4j.core.num.Num;

public class RafaelBar implements Bar {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Duration timePeriod;
    private final Instant beginTime;
    private final Instant endTime;

    private Num openPrice;
    private Num highPrice;
    private Num lowPrice;
    private Num closePrice;

    private Num volume;
    private Num amount;
    private Num quoteVolume;

    private Num takerBuyBaseVolume;
    private Num takerSellBaseVolume;
    private Num takerBuyQuoteVolume;
    private Num takerSellQuoteVolume;

    private long trades;

    public RafaelBar(
            Duration timePeriod,
            Instant endTime,
            Num openPrice,
            Num highPrice,
            Num lowPrice,
            Num closePrice,
            Num volume,
            Num quoteVolume,
            Num amount,
            long trades,
            Num takerBuyBaseVolume,
            Num takerSellBaseVolume,
            Num takerBuyQuoteVolume,
            Num takerSellQuoteVolume
    ) {
        this.timePeriod = Objects.requireNonNull(timePeriod);
        this.endTime = Objects.requireNonNull(endTime);
        this.beginTime = endTime.minus(timePeriod);

        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;

        this.volume = volume;
        this.quoteVolume = quoteVolume;
        this.amount = amount;
        this.trades = trades;

        this.takerBuyBaseVolume = takerBuyBaseVolume;
        this.takerSellBaseVolume = takerSellBaseVolume;
        this.takerBuyQuoteVolume = takerBuyQuoteVolume;
        this.takerSellQuoteVolume = takerSellQuoteVolume;
    }

    @Override public Duration getTimePeriod() { return timePeriod; }
    @Override public Instant getBeginTime() { return beginTime; }
    @Override public Instant getEndTime() { return endTime; }
    @Override public Num getOpenPrice() { return openPrice; }
    @Override public Num getHighPrice() { return highPrice; }
    @Override public Num getLowPrice() { return lowPrice; }
    @Override public Num getClosePrice() { return closePrice; }
    @Override public Num getVolume() { return volume; }
    @Override public Num getAmount() { return amount; }
    @Override public long getTrades() { return trades; }

    public Num getQuoteVolume() { return quoteVolume; }
    public Num getTakerBuyBaseVolume() { return takerBuyBaseVolume; }
    public Num getTakerSellBaseVolume() { return takerSellBaseVolume; }
    public Num getTakerBuyQuoteVolume() { return takerBuyQuoteVolume; }
    public Num getTakerSellQuoteVolume() { return takerSellQuoteVolume; }

    @Override
    public void addTrade(Num tradeVolume, Num tradePrice) {
        addPrice(tradePrice);
        volume = volume.plus(tradeVolume);
        amount = amount.plus(tradeVolume.multipliedBy(tradePrice));
        trades++;
    }

    @Override
    public void addPrice(Num price) {
        if (openPrice == null) openPrice = price;
        closePrice = price;
        if (highPrice == null || highPrice.isLessThan(price)) highPrice = price;
        if (lowPrice == null || lowPrice.isGreaterThan(price)) lowPrice = price;
    }

    @Override
    public String toString() {
        return String.format("{end: %s, O: %s, H: %s, L: %s, C: %s, V: %s}",
                endTime, openPrice, highPrice, lowPrice, closePrice, volume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginTime, endTime, timePeriod, openPrice, highPrice, lowPrice, closePrice, volume, amount, trades);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RafaelBar other)) return false;
        return Objects.equals(beginTime, other.beginTime) && Objects.equals(endTime, other.endTime)
                && Objects.equals(timePeriod, other.timePeriod) && Objects.equals(openPrice, other.openPrice)
                && Objects.equals(highPrice, other.highPrice) && Objects.equals(lowPrice, other.lowPrice)
                && Objects.equals(closePrice, other.closePrice) && Objects.equals(volume, other.volume)
                && Objects.equals(amount, other.amount) && trades == other.trades;
    }
}
