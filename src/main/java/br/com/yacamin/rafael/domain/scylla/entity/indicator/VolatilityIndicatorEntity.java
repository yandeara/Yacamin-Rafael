package br.com.yacamin.rafael.domain.scylla.entity.indicator;

import java.time.Instant;

public interface VolatilityIndicatorEntity {

    Instant getOpenTime();
    void setOpenTime(Instant openTime);

    // =========================================================================
    // ATR — CHANGE (3)
    // =========================================================================
    double getVlt_atr_7_chg();   void setVlt_atr_7_chg(double value);
    double getVlt_atr_14_chg();  void setVlt_atr_14_chg(double value);
    double getVlt_atr_21_chg();  void setVlt_atr_21_chg(double value);

    // =========================================================================
    // ATR — LOCAL (2)
    // =========================================================================
    double getVlt_range_atr_14_loc();     void setVlt_range_atr_14_loc(double value);
    double getVlt_range_atr_14_loc_chg(); void setVlt_range_atr_14_loc_chg(double value);

    // =========================================================================
    // ATR — REGIME 7/21 (3)
    // =========================================================================
    double getVlt_atr_7_21_ratio(); void setVlt_atr_7_21_ratio(double value);
    double getVlt_atr_7_21_expn();  void setVlt_atr_7_21_expn(double value);
    double getVlt_atr_7_21_cmpr();  void setVlt_atr_7_21_cmpr(double value);

    // =========================================================================
    // ATR — SLOPE (3)
    // =========================================================================
    double getVlt_atr_7_slp();  void setVlt_atr_7_slp(double value);
    double getVlt_atr_14_slp(); void setVlt_atr_14_slp(double value);
    double getVlt_atr_21_slp(); void setVlt_atr_21_slp(double value);

    // =========================================================================
    // STD — CHANGE (3)
    // =========================================================================
    double getVlt_std_14_chg(); void setVlt_std_14_chg(double value);
    double getVlt_std_20_chg(); void setVlt_std_20_chg(double value);
    double getVlt_std_50_chg(); void setVlt_std_50_chg(double value);

    // =========================================================================
    // STD — REGIME 14/50 (3)
    // =========================================================================
    double getVlt_std_14_50_ratio(); void setVlt_std_14_50_ratio(double value);
    double getVlt_std_14_50_expn();  void setVlt_std_14_50_expn(double value);
    double getVlt_std_14_50_cmpr();  void setVlt_std_14_50_cmpr(double value);

    // =========================================================================
    // STD — REGIME 14/48 (3)
    // =========================================================================
    double getVlt_std_14_48_ratio(); void setVlt_std_14_48_ratio(double value);
    double getVlt_std_14_48_expn();  void setVlt_std_14_48_expn(double value);
    double getVlt_std_14_48_cmpr();  void setVlt_std_14_48_cmpr(double value);

    // =========================================================================
    // STD — SLOPE (3)
    // =========================================================================
    double getVlt_std_14_slp(); void setVlt_std_14_slp(double value);
    double getVlt_std_20_slp(); void setVlt_std_20_slp(double value);
    double getVlt_std_50_slp(); void setVlt_std_50_slp(double value);

    // =========================================================================
    // BOLLINGER — WIDTH CHANGE (1)
    // =========================================================================
    double getVlt_boll_20_width_chg(); void setVlt_boll_20_width_chg(double value);

    // =========================================================================
    // SQUEEZE BB/KELT (2)
    // =========================================================================
    double getVlt_vol_sqz_bb_kelt_chg();    void setVlt_vol_sqz_bb_kelt_chg(double value);
    double getVlt_vol_sqz_bb_kelt_20_chg(); void setVlt_vol_sqz_bb_kelt_20_chg(double value);

    // =========================================================================
    // RANGE VOL — GK SLOPE (2)
    // =========================================================================
    double getVlt_vol_gk_16_slp(); void setVlt_vol_gk_16_slp(double value);
    double getVlt_vol_gk_32_slp(); void setVlt_vol_gk_32_slp(double value);

    // =========================================================================
    // RANGE VOL — PARK SLOPE (2)
    // =========================================================================
    double getVlt_vol_park_16_slp(); void setVlt_vol_park_16_slp(double value);
    double getVlt_vol_park_32_slp(); void setVlt_vol_park_32_slp(double value);

    // =========================================================================
    // RANGE VOL — RS SLOPE (2)
    // =========================================================================
    double getVlt_vol_rs_16_slp(); void setVlt_vol_rs_16_slp(double value);
    double getVlt_vol_rs_32_slp(); void setVlt_vol_rs_32_slp(double value);

    // =========================================================================
    // RANGE VOL — GK RATIO (2)
    // =========================================================================
    double getVlt_vol_gk_16_48_ratio(); void setVlt_vol_gk_16_48_ratio(double value);
    double getVlt_vol_gk_32_48_ratio(); void setVlt_vol_gk_32_48_ratio(double value);

    // =========================================================================
    // RANGE VOL — PARK RATIO (2)
    // =========================================================================
    double getVlt_vol_park_16_48_ratio(); void setVlt_vol_park_16_48_ratio(double value);
    double getVlt_vol_park_32_48_ratio(); void setVlt_vol_park_32_48_ratio(double value);

    // =========================================================================
    // RANGE VOL — RS RATIO (2)
    // =========================================================================
    double getVlt_vol_rs_16_48_ratio(); void setVlt_vol_rs_16_48_ratio(double value);
    double getVlt_vol_rs_32_48_ratio(); void setVlt_vol_rs_32_48_ratio(double value);

    // =========================================================================
    // REALIZED VOL — SLOPE (2)
    // =========================================================================
    double getVlt_vol_rv_10_slp(); void setVlt_vol_rv_10_slp(double value);
    double getVlt_vol_rv_30_slp(); void setVlt_vol_rv_30_slp(double value);

    // =========================================================================
    // REALIZED VOL — RATIO (2)
    // =========================================================================
    double getVlt_vol_rv_10_30_ratio(); void setVlt_vol_rv_10_30_ratio(double value);
    double getVlt_vol_rv_10_48_ratio(); void setVlt_vol_rv_10_48_ratio(double value);

    // =========================================================================
    // EWMA VOL — SLOPE (2)
    // =========================================================================
    double getVlt_ewma_vol_20_slp(); void setVlt_ewma_vol_20_slp(double value);
    double getVlt_ewma_vol_32_slp(); void setVlt_ewma_vol_32_slp(double value);

    // =========================================================================
    // EWMA VOL — RATIO (2)
    // =========================================================================
    double getVlt_ewma_vol_20_48_ratio(); void setVlt_ewma_vol_20_48_ratio(double value);
    double getVlt_ewma_vol_32_48_ratio(); void setVlt_ewma_vol_32_48_ratio(double value);
}
