package br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;

import jakarta.persistence.*;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.TimeIndicatorEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "time_indicator_30_mn")
@IdClass(IndicatorKey.class)
public class TimeIndicator30MnEntity implements TimeIndicatorEntity {

    @Id
    private String symbol;

    
    @Id
    @Column(name = "open_time")
    private Instant openTime;
    // =========================================================================
    // TIME FEATURES (prefixo oficial: tim_)
    // =========================================================================
    @Column(name = "tim_minute_of_day")
    private double tim_minute_of_day;

    @Column(name = "tim_day_of_week")
    private double tim_day_of_week;

    @Column(name = "tim_session_asia")
    private double tim_session_asia;

    @Column(name = "tim_session_europe")
    private double tim_session_europe;

    @Column(name = "tim_session_ny")
    private double tim_session_ny;

    @Column(name = "tim_sin_time")
    private double tim_sin_time;

    @Column(name = "tim_cos_time")
    private double tim_cos_time;

    // =========================================================================
    // V3 ADDITIONS — TIME FEATURES (tim_)
    // =========================================================================

    @Column(name = "tim_day_of_month")
    private double tim_day_of_month;

    @Column(name = "tim_sin_day_of_week")
    private double tim_sin_day_of_week;

    @Column(name = "tim_cos_day_of_week")
    private double tim_cos_day_of_week;

    @Column(name = "tim_overlap_asia_eur")
    private double tim_overlap_asia_eur;

    @Column(name = "tim_overlap_eur_ny")
    private double tim_overlap_eur_ny;

    @Column(name = "tim_candle_in_h1")
    private double tim_candle_in_h1;
}
