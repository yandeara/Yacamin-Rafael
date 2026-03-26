package br.com.yacamin.rafael.domain.scylla.entity.indicator;

import java.time.Instant;

public interface MicrostructureIndicatorEntity {

    Instant getOpenTime();
    void setOpenTime(Instant openTime);

    double getMic_amihud_acc_w10();
    void setMic_amihud_acc_w10(double value);

    double getMic_amihud_acc_w16();
    void setMic_amihud_acc_w16(double value);

    double getMic_amihud_acc_w4();
    void setMic_amihud_acc_w4(double value);

    double getMic_amihud_acc_w5();
    void setMic_amihud_acc_w5(double value);

    double getMic_amihud_slp_w20();
    void setMic_amihud_slp_w20(double value);

    double getMic_amihud_slp_w4();
    void setMic_amihud_slp_w4(double value);

    double getMic_body_atr_ratio();
    void setMic_body_atr_ratio(double value);

    double getMic_body_perc();
    void setMic_body_perc(double value);

    double getMic_body_ratio();
    void setMic_body_ratio(double value);

    double getMic_body_ratio_slp_w10();
    void setMic_body_ratio_slp_w10(double value);

    double getMic_body_return();
    void setMic_body_return(double value);

    double getMic_body_run_len();
    void setMic_body_run_len(double value);

    double getMic_body_shock_atrn();
    void setMic_body_shock_atrn(double value);

    double getMic_body_sign_prst_w20();
    void setMic_body_sign_prst_w20(double value);

    double getMic_candle_balance_score();
    void setMic_candle_balance_score(double value);

    double getMic_candle_body_center_position();
    void setMic_candle_body_center_position(double value);

    double getMic_candle_body_pct();
    void setMic_candle_body_pct(double value);

    double getMic_candle_body_ratio();
    void setMic_candle_body_ratio(double value);

    double getMic_candle_body_slp_w10();
    void setMic_candle_body_slp_w10(double value);

    double getMic_candle_body_slp_w20();
    void setMic_candle_body_slp_w20(double value);

    double getMic_candle_body_strength_score();
    void setMic_candle_body_strength_score(double value);

    double getMic_candle_brr();
    void setMic_candle_brr(double value);

    double getMic_candle_close_pos_norm();
    void setMic_candle_close_pos_norm(double value);

    double getMic_candle_energy_atrn();
    void setMic_candle_energy_atrn(double value);

    double getMic_candle_energy_raw();
    void setMic_candle_energy_raw(double value);

    double getMic_candle_lmr();
    void setMic_candle_lmr(double value);

    double getMic_candle_lower_wick_pct();
    void setMic_candle_lower_wick_pct(double value);

    double getMic_candle_pressure_raw();
    void setMic_candle_pressure_raw(double value);

    double getMic_candle_range();
    void setMic_candle_range(double value);

    double getMic_candle_shadow_ratio();
    void setMic_candle_shadow_ratio(double value);

    double getMic_candle_spread_ratio();
    void setMic_candle_spread_ratio(double value);

    double getMic_candle_strength();
    void setMic_candle_strength(double value);

    double getMic_candle_total_wick_atrn();
    void setMic_candle_total_wick_atrn(double value);

    double getMic_candle_total_wick_pct();
    void setMic_candle_total_wick_pct(double value);

    double getMic_candle_upper_wick_pct();
    void setMic_candle_upper_wick_pct(double value);

    double getMic_candle_volatility_inside();
    void setMic_candle_volatility_inside(double value);

    double getMic_candle_wick_body_alignment();
    void setMic_candle_wick_body_alignment(double value);

    double getMic_candle_wick_dominance();
    void setMic_candle_wick_dominance(double value);

    double getMic_candle_wick_exhaustion();
    void setMic_candle_wick_exhaustion(double value);

    double getMic_candle_wick_imbalance();
    void setMic_candle_wick_imbalance(double value);

    double getMic_candle_wick_imbalance_norm();
    void setMic_candle_wick_imbalance_norm(double value);

    double getMic_candle_wick_imbalance_slp_w10();
    void setMic_candle_wick_imbalance_slp_w10(double value);

    double getMic_candle_wick_pressure_score();
    void setMic_candle_wick_pressure_score(double value);

    double getMic_close_hlc3_atrn();
    void setMic_close_hlc3_atrn(double value);

    double getMic_close_open_norm();
    void setMic_close_open_norm(double value);

    double getMic_close_open_ratio();
    void setMic_close_open_ratio(double value);

    double getMic_close_pos_norm();
    void setMic_close_pos_norm(double value);

    double getMic_close_pos_slp_w20();
    void setMic_close_pos_slp_w20(double value);

    double getMic_close_to_high_norm();
    void setMic_close_to_high_norm(double value);

    double getMic_close_to_low_norm();
    void setMic_close_to_low_norm(double value);

    double getMic_close_triangle_score_atrn();
    void setMic_close_triangle_score_atrn(double value);

    double getMic_close_vwap_atrn();
    void setMic_close_vwap_atrn(double value);

    double getMic_extreme_range_return();
    void setMic_extreme_range_return(double value);

    double getMic_gap_ratio();
    void setMic_gap_ratio(double value);

    double getMic_hasb_lambda_w16_slp_w20();
    void setMic_hasb_lambda_w16_slp_w20(double value);

    double getMic_hasb_lambda_w48_slp_w20();
    void setMic_hasb_lambda_w48_slp_w20(double value);

    double getMic_high_return();
    void setMic_high_return(double value);

    double getMic_kyle_lambda_w16_acc_w10();
    void setMic_kyle_lambda_w16_acc_w10(double value);

    double getMic_kyle_lambda_w16_acc_w16();
    void setMic_kyle_lambda_w16_acc_w16(double value);

    double getMic_kyle_lambda_w16_acc_w4();
    void setMic_kyle_lambda_w16_acc_w4(double value);

    double getMic_kyle_lambda_w16_acc_w5();
    void setMic_kyle_lambda_w16_acc_w5(double value);

    double getMic_kyle_lambda_w16_slp_w20();
    void setMic_kyle_lambda_w16_slp_w20(double value);

    double getMic_kyle_lambda_w16_slp_w4();
    void setMic_kyle_lambda_w16_slp_w4(double value);

    double getMic_kyle_lambda_w4_acc_w10();
    void setMic_kyle_lambda_w4_acc_w10(double value);

    double getMic_kyle_lambda_w4_acc_w16();
    void setMic_kyle_lambda_w4_acc_w16(double value);

    double getMic_kyle_lambda_w4_acc_w4();
    void setMic_kyle_lambda_w4_acc_w4(double value);

    double getMic_kyle_lambda_w4_acc_w5();
    void setMic_kyle_lambda_w4_acc_w5(double value);

    double getMic_kyle_lambda_w4_slp_w20();
    void setMic_kyle_lambda_w4_slp_w20(double value);

    double getMic_kyle_lambda_w4_slp_w4();
    void setMic_kyle_lambda_w4_slp_w4(double value);

    double getMic_log_range_slp_w20();
    void setMic_log_range_slp_w20(double value);

    double getMic_low_return();
    void setMic_low_return(double value);

    double getMic_lower_wick_return();
    void setMic_lower_wick_return(double value);

    double getMic_range_acc_w10();
    void setMic_range_acc_w10(double value);

    double getMic_range_acc_w5();
    void setMic_range_acc_w5(double value);

    double getMic_range_asymmetry();
    void setMic_range_asymmetry(double value);

    double getMic_range_atr_ratio();
    void setMic_range_atr_ratio(double value);

    double getMic_range_atrn();
    void setMic_range_atrn(double value);

    double getMic_range_compression_w20();
    void setMic_range_compression_w20(double value);

    double getMic_range_headroom_atr();
    void setMic_range_headroom_atr(double value);

    double getMic_range_return();
    void setMic_range_return(double value);

    double getMic_range_slp_w10();
    void setMic_range_slp_w10(double value);

    double getMic_range_slp_w20();
    void setMic_range_slp_w20(double value);

    double getMic_range_squeeze_w20();
    void setMic_range_squeeze_w20(double value);

    double getMic_range_stdn();
    void setMic_range_stdn(double value);

    double getMic_roll_spread_acc_w16();
    void setMic_roll_spread_acc_w16(double value);

    double getMic_roll_spread_acc_w32();
    void setMic_roll_spread_acc_w32(double value);

    double getMic_roll_spread_acc_w48();
    void setMic_roll_spread_acc_w48(double value);

    double getMic_roll_spread_slp_w16();
    void setMic_roll_spread_slp_w16(double value);

    double getMic_roll_spread_slp_w32();
    void setMic_roll_spread_slp_w32(double value);

    double getMic_roll_spread_slp_w48();
    void setMic_roll_spread_slp_w48(double value);

    double getMic_shadow_imbalance_score();
    void setMic_shadow_imbalance_score(double value);

    double getMic_tr_atrn();
    void setMic_tr_atrn(double value);

    double getMic_tr_range_ratio();
    void setMic_tr_range_ratio(double value);

    double getMic_true_range();
    void setMic_true_range(double value);

    double getMic_upper_wick_return();
    void setMic_upper_wick_return(double value);

    double getMic_wick_imbalance();
    void setMic_wick_imbalance(double value);

    double getMic_wick_perc_down();
    void setMic_wick_perc_down(double value);

    double getMic_wick_perc_up();
    void setMic_wick_perc_up(double value);
}
