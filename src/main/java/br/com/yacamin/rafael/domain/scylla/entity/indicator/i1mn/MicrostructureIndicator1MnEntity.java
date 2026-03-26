package br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;

import jakarta.persistence.*;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.MicrostructureIndicatorEntity;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "microstructure_indicator_1_mn")
@IdClass(IndicatorKey.class)
public class MicrostructureIndicator1MnEntity implements MicrostructureIndicatorEntity {
    @Id
    private String symbol;
    @Id
    @Column(name = "open_time")
    private Instant openTime;
    @Column(name = "mic_amihud_acc_w10") private double mic_amihud_acc_w10;
    @Column(name = "mic_amihud_acc_w16") private double mic_amihud_acc_w16;
    @Column(name = "mic_amihud_acc_w4") private double mic_amihud_acc_w4;
    @Column(name = "mic_amihud_acc_w5") private double mic_amihud_acc_w5;
    @Column(name = "mic_amihud_slp_w20") private double mic_amihud_slp_w20;
    @Column(name = "mic_amihud_slp_w4") private double mic_amihud_slp_w4;
    @Column(name = "mic_body_atr_ratio") private double mic_body_atr_ratio;
    @Column(name = "mic_body_perc") private double mic_body_perc;
    @Column(name = "mic_body_ratio") private double mic_body_ratio;
    @Column(name = "mic_body_ratio_slp_w10") private double mic_body_ratio_slp_w10;
    @Column(name = "mic_body_return") private double mic_body_return;
    @Column(name = "mic_body_run_len") private double mic_body_run_len;
    @Column(name = "mic_body_shock_atrn") private double mic_body_shock_atrn;
    @Column(name = "mic_body_sign_prst_w20") private double mic_body_sign_prst_w20;
    @Column(name = "mic_candle_balance_score") private double mic_candle_balance_score;
    @Column(name = "mic_candle_body_center_position") private double mic_candle_body_center_position;
    @Column(name = "mic_candle_body_pct") private double mic_candle_body_pct;
    @Column(name = "mic_candle_body_ratio") private double mic_candle_body_ratio;
    @Column(name = "mic_candle_body_slp_w10") private double mic_candle_body_slp_w10;
    @Column(name = "mic_candle_body_slp_w20") private double mic_candle_body_slp_w20;
    @Column(name = "mic_candle_body_strength_score") private double mic_candle_body_strength_score;
    @Column(name = "mic_candle_brr") private double mic_candle_brr;
    @Column(name = "mic_candle_close_pos_norm") private double mic_candle_close_pos_norm;
    @Column(name = "mic_candle_energy_atrn") private double mic_candle_energy_atrn;
    @Column(name = "mic_candle_energy_raw") private double mic_candle_energy_raw;
    @Column(name = "mic_candle_lmr") private double mic_candle_lmr;
    @Column(name = "mic_candle_lower_wick_pct") private double mic_candle_lower_wick_pct;
    @Column(name = "mic_candle_pressure_raw") private double mic_candle_pressure_raw;
    @Column(name = "mic_candle_range") private double mic_candle_range;
    @Column(name = "mic_candle_shadow_ratio") private double mic_candle_shadow_ratio;
    @Column(name = "mic_candle_spread_ratio") private double mic_candle_spread_ratio;
    @Column(name = "mic_candle_strength") private double mic_candle_strength;
    @Column(name = "mic_candle_total_wick_atrn") private double mic_candle_total_wick_atrn;
    @Column(name = "mic_candle_total_wick_pct") private double mic_candle_total_wick_pct;
    @Column(name = "mic_candle_upper_wick_pct") private double mic_candle_upper_wick_pct;
    @Column(name = "mic_candle_volatility_inside") private double mic_candle_volatility_inside;
    @Column(name = "mic_candle_wick_body_alignment") private double mic_candle_wick_body_alignment;
    @Column(name = "mic_candle_wick_dominance") private double mic_candle_wick_dominance;
    @Column(name = "mic_candle_wick_exhaustion") private double mic_candle_wick_exhaustion;
    @Column(name = "mic_candle_wick_imbalance") private double mic_candle_wick_imbalance;
    @Column(name = "mic_candle_wick_imbalance_norm") private double mic_candle_wick_imbalance_norm;
    @Column(name = "mic_candle_wick_imbalance_slp_w10") private double mic_candle_wick_imbalance_slp_w10;
    @Column(name = "mic_candle_wick_pressure_score") private double mic_candle_wick_pressure_score;
    @Column(name = "mic_close_hlc3_atrn") private double mic_close_hlc3_atrn;
    @Column(name = "mic_close_open_norm") private double mic_close_open_norm;
    @Column(name = "mic_close_open_ratio") private double mic_close_open_ratio;
    @Column(name = "mic_close_pos_norm") private double mic_close_pos_norm;
    @Column(name = "mic_close_pos_slp_w20") private double mic_close_pos_slp_w20;
    @Column(name = "mic_close_to_high_norm") private double mic_close_to_high_norm;
    @Column(name = "mic_close_to_low_norm") private double mic_close_to_low_norm;
    @Column(name = "mic_close_triangle_score_atrn") private double mic_close_triangle_score_atrn;
    @Column(name = "mic_close_vwap_atrn") private double mic_close_vwap_atrn;
    @Column(name = "mic_extreme_range_return") private double mic_extreme_range_return;
    @Column(name = "mic_gap_ratio") private double mic_gap_ratio;
    @Column(name = "mic_hasb_lambda_w16_slp_w20") private double mic_hasb_lambda_w16_slp_w20;
    @Column(name = "mic_hasb_lambda_w48_slp_w20") private double mic_hasb_lambda_w48_slp_w20;
    @Column(name = "mic_high_return") private double mic_high_return;
    @Column(name = "mic_kyle_lambda_w16_acc_w10") private double mic_kyle_lambda_w16_acc_w10;
    @Column(name = "mic_kyle_lambda_w16_acc_w16") private double mic_kyle_lambda_w16_acc_w16;
    @Column(name = "mic_kyle_lambda_w16_acc_w4") private double mic_kyle_lambda_w16_acc_w4;
    @Column(name = "mic_kyle_lambda_w16_acc_w5") private double mic_kyle_lambda_w16_acc_w5;
    @Column(name = "mic_kyle_lambda_w16_slp_w20") private double mic_kyle_lambda_w16_slp_w20;
    @Column(name = "mic_kyle_lambda_w16_slp_w4") private double mic_kyle_lambda_w16_slp_w4;
    @Column(name = "mic_kyle_lambda_w4_acc_w10") private double mic_kyle_lambda_w4_acc_w10;
    @Column(name = "mic_kyle_lambda_w4_acc_w16") private double mic_kyle_lambda_w4_acc_w16;
    @Column(name = "mic_kyle_lambda_w4_acc_w4") private double mic_kyle_lambda_w4_acc_w4;
    @Column(name = "mic_kyle_lambda_w4_acc_w5") private double mic_kyle_lambda_w4_acc_w5;
    @Column(name = "mic_kyle_lambda_w4_slp_w20") private double mic_kyle_lambda_w4_slp_w20;
    @Column(name = "mic_kyle_lambda_w4_slp_w4") private double mic_kyle_lambda_w4_slp_w4;
    @Column(name = "mic_log_range_slp_w20") private double mic_log_range_slp_w20;
    @Column(name = "mic_low_return") private double mic_low_return;
    @Column(name = "mic_lower_wick_return") private double mic_lower_wick_return;
    @Column(name = "mic_range_acc_w10") private double mic_range_acc_w10;
    @Column(name = "mic_range_acc_w5") private double mic_range_acc_w5;
    @Column(name = "mic_range_asymmetry") private double mic_range_asymmetry;
    @Column(name = "mic_range_atr_ratio") private double mic_range_atr_ratio;
    @Column(name = "mic_range_atrn") private double mic_range_atrn;
    @Column(name = "mic_range_compression_w20") private double mic_range_compression_w20;
    @Column(name = "mic_range_headroom_atr") private double mic_range_headroom_atr;
    @Column(name = "mic_range_return") private double mic_range_return;
    @Column(name = "mic_range_slp_w10") private double mic_range_slp_w10;
    @Column(name = "mic_range_slp_w20") private double mic_range_slp_w20;
    @Column(name = "mic_range_squeeze_w20") private double mic_range_squeeze_w20;
    @Column(name = "mic_range_stdn") private double mic_range_stdn;
    @Column(name = "mic_roll_spread_acc_w16") private double mic_roll_spread_acc_w16;
    @Column(name = "mic_roll_spread_acc_w32") private double mic_roll_spread_acc_w32;
    @Column(name = "mic_roll_spread_acc_w48") private double mic_roll_spread_acc_w48;
    @Column(name = "mic_roll_spread_slp_w16") private double mic_roll_spread_slp_w16;
    @Column(name = "mic_roll_spread_slp_w32") private double mic_roll_spread_slp_w32;
    @Column(name = "mic_roll_spread_slp_w48") private double mic_roll_spread_slp_w48;
    @Column(name = "mic_shadow_imbalance_score") private double mic_shadow_imbalance_score;
    @Column(name = "mic_tr_atrn") private double mic_tr_atrn;
    @Column(name = "mic_tr_range_ratio") private double mic_tr_range_ratio;
    @Column(name = "mic_true_range") private double mic_true_range;
    @Column(name = "mic_upper_wick_return") private double mic_upper_wick_return;
    @Column(name = "mic_wick_imbalance") private double mic_wick_imbalance;
    @Column(name = "mic_wick_perc_down") private double mic_wick_perc_down;
    @Column(name = "mic_wick_perc_up") private double mic_wick_perc_up;
}
