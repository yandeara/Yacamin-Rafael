package br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.TimeIndicatorEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Setter
@Getter
@Table("time_indicator_15_mn")
public class TimeIndicator15MnEntity implements TimeIndicatorEntity {

    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED)
    private String symbol;

    @PrimaryKeyColumn(
            name = "open_time",
            type = PrimaryKeyType.CLUSTERED,
            ordering = Ordering.ASCENDING
    )
    @Column("open_time")
    private Instant openTime;


    // =========================================================================
    // TIME FEATURES (prefixo oficial: tim_)
    // =========================================================================
    @Column("tim_minute_of_day")
    private double tim_minute_of_day;

    @Column("tim_day_of_week")
    private double tim_day_of_week;

    @Column("tim_session_asia")
    private double tim_session_asia;

    @Column("tim_session_europe")
    private double tim_session_europe;

    @Column("tim_session_ny")
    private double tim_session_ny;

    @Column("tim_sin_time")
    private double tim_sin_time;

    @Column("tim_cos_time")
    private double tim_cos_time;

    // =========================================================================
    // V3 ADDITIONS — TIME FEATURES (tim_)
    // =========================================================================

    @Column("tim_day_of_month")
    private double tim_day_of_month;

    @Column("tim_sin_day_of_week")
    private double tim_sin_day_of_week;

    @Column("tim_cos_day_of_week")
    private double tim_cos_day_of_week;

    @Column("tim_overlap_asia_eur")
    private double tim_overlap_asia_eur;

    @Column("tim_overlap_eur_ny")
    private double tim_overlap_eur_ny;

    @Column("tim_candle_in_h1")
    private double tim_candle_in_h1;


}
