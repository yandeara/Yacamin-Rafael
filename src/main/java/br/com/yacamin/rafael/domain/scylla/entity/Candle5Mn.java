package br.com.yacamin.rafael.domain.scylla.entity;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;

import jakarta.persistence.*;

import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "candle_5_mn")
@IdClass(IndicatorKey.class)
public class Candle5Mn implements NewCandleEntity {

    @Id
    private String symbol;

    @Id
    @Column(name = "open_time")
    private Instant openTime;

    @Column(name = "open")
    private double open;

    @Column(name = "high")
    private double high;

    @Column(name = "low")
    private double low;

    @Column(name = "close")
    private double close;

    @Column(name = "volume")
    private double volume;

    @Column(name = "quote_volume")
    private double quoteVolume;

    @Column(name = "number_of_trades")
    private double numberOfTrades;

    @Column(name = "taker_buy_base_volume")
    private double takerBuyBaseVolume;

    @Column(name = "taker_buy_quote_volume")
    private double takerBuyQuoteVolume;

    @Column(name = "taker_sell_base_volume")
    private double takerSellBaseVolume;

    @Column(name = "taker_sell_quote_volume")
    private double takerSellQuoteVolume;
}
