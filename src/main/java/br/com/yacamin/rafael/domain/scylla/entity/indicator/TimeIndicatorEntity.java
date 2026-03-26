package br.com.yacamin.rafael.domain.scylla.entity.indicator;

import java.time.Instant;

public interface TimeIndicatorEntity {

    Instant getOpenTime();
    void setOpenTime(Instant openTime);

    // =========================================================================
    // TIME FEATURES (prefixo oficial: tim_)
    // =========================================================================
    double getTim_minute_of_day();
    void setTim_minute_of_day(double value);

    double getTim_day_of_week();
    void setTim_day_of_week(double value);

    double getTim_session_asia();
    void setTim_session_asia(double value);

    double getTim_session_europe();
    void setTim_session_europe(double value);

    double getTim_session_ny();
    void setTim_session_ny(double value);

    double getTim_sin_time();
    void setTim_sin_time(double value);

    double getTim_cos_time();
    void setTim_cos_time(double value);

    // =========================================================================
    // V3 ADDITIONS — TIME FEATURES (tim_)
    // =========================================================================
    double getTim_day_of_month();
    void setTim_day_of_month(double value);

    double getTim_sin_day_of_week();
    void setTim_sin_day_of_week(double value);

    double getTim_cos_day_of_week();
    void setTim_cos_day_of_week(double value);

    double getTim_overlap_asia_eur();
    void setTim_overlap_asia_eur(double value);

    double getTim_overlap_eur_ny();
    void setTim_overlap_eur_ny(double value);

    double getTim_candle_in_h1();
    void setTim_candle_in_h1(double value);


}
