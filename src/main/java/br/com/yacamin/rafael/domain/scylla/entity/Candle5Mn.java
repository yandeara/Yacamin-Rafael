package br.com.yacamin.rafael.domain.scylla.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Data
@Table("candle_5_mn")
public class Candle5Mn implements NewCandleEntity {

    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED)
    private String symbol;

    @PrimaryKeyColumn(name = "open_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    @Column("open_time")
    private Instant openTime;

    @Column("open")
    private double open;

    @Column("high")
    private double high;

    @Column("low")
    private double low;

    @Column("close")
    private double close;

    @Column("volume")
    private double volume;

    @Column("quote_volume")
    private double quoteVolume;

    @Column("number_of_trades")
    private double numberOfTrades;

    @Column("taker_buy_base_volume")
    private double takerBuyBaseVolume;

    @Column("taker_buy_quote_volume")
    private double takerBuyQuoteVolume;

    @Column("taker_sell_base_volume")
    private double takerSellBaseVolume;

    @Column("taker_sell_quote_volume")
    private double takerSellQuoteVolume;
}
