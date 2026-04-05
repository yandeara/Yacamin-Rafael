package br.com.yacamin.rafael.application.service.indicator.extension;

import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import lombok.Data;
import org.ta4j.core.Bar;
import org.ta4j.core.num.Num;

/**
 * MikhaelBar implementation of a {@link Bar}.
 */
@Data
public class MikhaelBar implements Bar {

    @Serial
    private static final long serialVersionUID = 8038383777467488147L;

    /** The time period (e.g. 1 day, 15 min, etc.) of the bar. */
    private final Duration timePeriod;

    /** The begin time of the bar period (in UTC). */
    private final Instant beginTime;

    /** The end time of the bar period (in UTC). */
    private final Instant endTime;

    /** The open price of the bar period. */
    private Num openPrice;

    /** The high price of the bar period. */
    private Num highPrice;

    /** The low price of the bar period. */
    private Num lowPrice;

    /** The close price of the bar period. */
    private Num closePrice;

    /** The total traded volume of the bar period. */
    private Num volume;

    /** The total traded amount of the bar period. */
    private Num amount;

    private Num quoteVolume;

    private Num takerBuyBaseVolume;
    private Num takerSellBaseVolume;

    private Num takerBuyQuoteVolume;
    private Num takerSellQuoteVolume;

    /** The number of trades of the bar period. */
    private long trades;

    /**
     * Constructor.
     *
     * <p>
     * The {@link #beginTime} will be calculated by {@link #endTime} -
     * {@link #timePeriod}.
     *
     * @param timePeriod the time period
     * @param endTime    the end time of the bar period (in UTC)
     * @param openPrice  the open price of the bar period
     * @param highPrice  the highest price of the bar period
     * @param lowPrice   the lowest price of the bar period
     * @param closePrice the close price of the bar period
     * @param volume     the total traded volume of the bar period
     * @param amount     the total traded amount of the bar period
     * @param trades     the number of trades of the bar period
     * @throws NullPointerException if {@link #endTime} or {@link #timePeriod} is
     *                              {@code null}
     */
    public MikhaelBar(
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
        this.timePeriod = Objects.requireNonNull(timePeriod, "Time period cannot be null");
        this.endTime = Objects.requireNonNull(endTime, "End time cannot be null");
        this.beginTime = endTime.minus(timePeriod);

        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;

        this.volume = volume;
        this.quoteVolume = quoteVolume;
        this.amount = amount;
        this.trades = trades;

        this.takerBuyBaseVolume  = takerBuyBaseVolume;
        this.takerSellBaseVolume = takerSellBaseVolume;
        this.takerBuyQuoteVolume = takerBuyQuoteVolume;
        this.takerSellQuoteVolume = takerSellQuoteVolume;
    }

    @Override
    public Duration getTimePeriod() {
        return timePeriod;
    }

    @Override
    public Instant getBeginTime() {
        return beginTime;
    }

    @Override
    public Instant getEndTime() {
        return endTime;
    }

    @Override
    public Num getOpenPrice() {
        return openPrice;
    }

    @Override
    public Num getHighPrice() {
        return highPrice;
    }

    @Override
    public Num getLowPrice() {
        return lowPrice;
    }

    @Override
    public Num getClosePrice() {
        return closePrice;
    }

    @Override
    public Num getVolume() {
        return volume;
    }

    @Override
    public Num getAmount() {
        return amount;
    }

    @Override
    public long getTrades() {
        return trades;
    }

    @Override
    public void addTrade(Num tradeVolume, Num tradePrice) {
        addPrice(tradePrice);

        volume = volume.plus(tradeVolume);
        amount = amount.plus(tradeVolume.multipliedBy(tradePrice));
        trades++;
    }

    @Override
    public void addPrice(Num price) {
        if (openPrice == null) {
            openPrice = price;
        }
        closePrice = price;
        if (highPrice == null || highPrice.isLessThan(price)) {
            highPrice = price;
        }
        if (lowPrice == null || lowPrice.isGreaterThan(price)) {
            lowPrice = price;
        }
    }

    /**
     * @return {end time, close price, open price, low price, high price, volume}
     */
    @Override
    public String toString() {
        return String.format(
                "{end time: %1s, close price: %2s, open price: %3s, low price: %4s high price: %5s, volume: %6s}",
                endTime, closePrice, openPrice, lowPrice, highPrice, volume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginTime, endTime, timePeriod, openPrice, highPrice, lowPrice, closePrice, volume, amount,
                trades);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof MikhaelBar other))
            return false;
        return Objects.equals(beginTime, other.beginTime) && Objects.equals(endTime, other.endTime)
                && Objects.equals(timePeriod, other.timePeriod) && Objects.equals(openPrice, other.openPrice)
                && Objects.equals(highPrice, other.highPrice) && Objects.equals(lowPrice, other.lowPrice)
                && Objects.equals(closePrice, other.closePrice) && Objects.equals(volume, other.volume)
                && Objects.equals(amount, other.amount) && trades == other.trades;
    }
}