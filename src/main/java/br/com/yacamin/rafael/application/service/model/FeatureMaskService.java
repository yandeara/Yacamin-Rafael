package br.com.yacamin.rafael.application.service.model;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureMaskService {

    public List<String> getProdMask() {
        return List.of(
                "mic_roll_spread_acc_w16",
                "mic_roll_spread_acc_w32",
                "mic_roll_spread_acc_w48",

                "mic_roll_spread_slp_w16",
                "mic_roll_spread_slp_w32",
                "mic_roll_spread_slp_w48",

                "mic_amihud_slp_w4",
                "mic_amihud_slp_w20",

                "mic_amihud_acc_w4",
                "mic_amihud_acc_w5",
                "mic_amihud_acc_w10",
                "mic_amihud_acc_w16",

                "mic_kyle_lambda_w4_slp_w4",
                "mic_kyle_lambda_w4_slp_w20",

                "mic_kyle_lambda_w16_slp_w4",
                "mic_kyle_lambda_w16_slp_w20",

                "mic_kyle_lambda_w4_acc_w4",
                "mic_kyle_lambda_w4_acc_w5",
                "mic_kyle_lambda_w4_acc_w10",
                "mic_kyle_lambda_w4_acc_w16",

                "mic_kyle_lambda_w16_acc_w4",
                "mic_kyle_lambda_w16_acc_w5",
                "mic_kyle_lambda_w16_acc_w10",
                "mic_kyle_lambda_w16_acc_w16",

                "mic_hasb_lambda_w16_slp_w20",
                "mic_hasb_lambda_w48_slp_w20",

                "mic_range_atrn",
                "mic_range_stdn",
                "mic_range_atr_ratio",

                "mic_range_slp_w10",
                "mic_range_slp_w20",

                "mic_range_acc_w5",
                "mic_range_acc_w10",

                "mic_range_compression_w20",
                "mic_log_range_slp_w20",

                "mic_range_return",
                "mic_high_return",
                "mic_low_return",
                "mic_extreme_range_return",

                "mic_candle_range",
                "mic_candle_volatility_inside",
                "mic_candle_spread_ratio",
                "mic_candle_brr",
                "mic_candle_lmr",

                "mic_true_range",
                "mic_tr_atrn",
                "mic_tr_range_ratio",

                "mic_range_squeeze_w20",
                "mic_range_asymmetry",
                "mic_range_headroom_atr",
                "mic_gap_ratio",

                "mic_candle_body_pct",
                "mic_body_perc",
                "mic_body_ratio",
                "mic_body_atr_ratio",
                "mic_candle_body_ratio",
                "mic_candle_body_center_position",

                "mic_candle_pressure_raw",
                "mic_candle_strength",
                "mic_candle_body_strength_score",

                "mic_candle_energy_atrn",
                "mic_body_ratio_slp_w10",
                "mic_candle_body_slp_w10",
                "mic_candle_body_slp_w20",

                "mic_body_return",
                "mic_body_shock_atrn",

                "mic_body_sign_prst_w20",
                "mic_body_run_len",

                "mic_candle_upper_wick_pct",
                "mic_candle_lower_wick_pct",

                "mic_wick_perc_up",
                "mic_wick_perc_down",

                "mic_candle_total_wick_pct",
                "mic_candle_total_wick_atrn",

                "mic_upper_wick_return",
                "mic_lower_wick_return",

                "mic_candle_wick_imbalance",
                "mic_wick_imbalance",
                "mic_candle_wick_imbalance_norm",
                "mic_candle_wick_imbalance_slp_w10",
                "mic_shadow_imbalance_score",

                "mic_candle_wick_pressure_score",
                "mic_candle_shadow_ratio",
                "mic_candle_wick_body_alignment",

                "mic_candle_wick_dominance",
                "mic_candle_wick_exhaustion",

                "mic_close_open_ratio",
                "mic_close_open_norm",

                "mic_close_hlc3_atrn",
                "mic_close_vwap_atrn",

                "mic_close_triangle_score_atrn",

                "mic_candle_balance_score",
                "mic_close_pos_slp_w20",
                "mic_close_pos_norm",
                "mic_candle_close_pos_norm",
                "mic_close_to_high_norm",
                "mic_close_to_low_norm",

                "trd_ema_8_slp_atrn",
                "trd_ema_20_slp_atrn",
                "trd_ema_50_slp_atrn",

                "trd_ema_8_slp_acc_atrn",
                "trd_ema_20_slp_acc_atrn",
                "trd_ema_50_slp_acc_atrn",

                "trd_dist_close_ema_8_atrn",
                "trd_dist_close_ema_20_atrn",
                "trd_dist_close_ema_50_atrn",

                "trd_dist_ema_8_20_atrn",
                "trd_dist_ema_20_50_atrn",
                "trd_dist_ema_8_50_atrn",

                "trd_ratio_ema_8_20",
                "trd_ratio_ema_20_50",
                "trd_ratio_ema_8_50",

                "trd_alignment_ema_8_20_50_score",
                "trd_alignment_ema_8_20_50_normalized",
                "trd_aligment_ema_8_20_50_delta",

                "trd_cross_ema_8_20_delta_atrn",
                "trd_cross_ema_20_50_delta_atrn",

                "trd_delta_close_ema_8_atrn",
                "trd_delta_close_ema_20_atrn",
                "trd_delta_close_ema_50_atrn",

                "trd_delta_ema_8_20_atrn",
                "trd_delta_ema_20_50_atrn",

                "trd_di_diff_14",

                "mom_close_ret_1_atrn",
                "mom_close_ret_2_atrn",
                "mom_close_ret_3_atrn",
                "mom_close_ret_4_atrn",
                "mom_close_ret_5_atrn",
                "mom_close_ret_6_atrn",
                "mom_close_ret_8_atrn",
                "mom_close_ret_10_atrn",
                "mom_close_ret_12_atrn",
                "mom_close_ret_16_atrn",

                "mom_burst_10",
                "mom_burst_16",
                "mom_burst_32",

                "mom_cntrate_10",
                "mom_cntrate_16",
                "mom_cntrate_32",

                "mom_decay_10",
                "mom_decay_16",
                "mom_decay_32",

                "mom_impls_10",
                "mom_impls_16",
                "mom_impls_32",

                "mom_chprt_10",
                "mom_chprt_16",
                "mom_chprt_32",

                "mom_rsi_2_dlt",
                "mom_rsi_3_dlt",
                "mom_rsi_5_dlt",
                "mom_rsi_7_dlt",
                "mom_rsi_14_dlt",

                "mom_rsi_2_roc",
                "mom_rsi_3_roc",
                "mom_rsi_5_roc",
                "mom_rsi_7_roc",
                "mom_rsi_14_roc",

                "mom_rsi_7_slp",

                "mom_rsi_14_slp",
                "mom_rsi_14_acc",
                "mom_rsi_14_atrn",

                "mom_rsi_7_dst_mid",
                "mom_rsi_14_dst_mid",

                "mom_rsi_7_tail_up",
                "mom_rsi_7_tail_dw",
                "mom_rsi_14_tail_up",
                "mom_rsi_14_tail_dw",

                "mom_cmo_14_dlt",
                "mom_cmo_20_dlt",

                "mom_cmo_14_dst_mid",
                "mom_cmo_20_dst_mid",

                "mom_wpr_14_dlt",
                "mom_wpr_28_dlt",
                "mom_wpr_42_dlt",

                "mom_wpr_14_dst_mid",
                "mom_wpr_28_dst_mid",
                "mom_wpr_48_dst_mid",

                "mom_stoch_14_k_dlt",
                "mom_stoch_14_d_dlt",
                "mom_stoch_14_spread",
                "mom_stoch_14_k_dst_mid",

                "mom_trix_9_dlt",
                "mom_trix_9_hist",

                "mom_tsi_25_13_dlt",
                "mom_tsi_25_13_hist",
                "mom_tsi_25_13_dst_mid",

                "mom_ppo_hist_12_26_9",
                "mom_ppo_12_26_dlt",
                "mom_ppo_hist_12_26_9_dlt",

                "mom_close_3_slp",
                "mom_close_8_slp",
                "mom_close_14_slp",

                "mom_close_3_slp_atrn",
                "mom_close_8_slp_atrn",
                "mom_close_14_slp_atrn",

                "mom_close_3_slp_acc",
                "mom_close_8_slp_acc",
                "mom_close_14_slp_acc",

                "mom_close_3_slp_acc_atrn",
                "mom_close_8_slp_acc_atrn",
                "mom_close_14_slp_acc_atrn",

                "mom_cci_14_dlt",
                "mom_cci_20_dlt",

                "mom_cci_14_dst_mid",
                "mom_cci_20_dst_mid",

                "mom_roc_1",
                "mom_roc_2",
                "mom_roc_3",
                "mom_roc_5",

                "mom_ppo_hist_12_26_9_slp_w20",
                "mom_trix_hist_9_slp_w20",
                "mom_tsi_hist_25_13_slp_w20",

                "vol_taker_buy_ratio",
                "vol_taker_buy_sell_imbalance",

                "vol_taker_buy_ratio_rel_16",
                "vol_taker_buy_ratio_slp_w20",
                "vol_taker_buy_sell_imbalance_slp_w20",

                "vol_volume_delta_1",
                "vol_volume_delta_3",

                "vol_trades_delta_1",
                "vol_trades_delta_3",

                "vol_quote_volume_delta_1",
                "vol_quote_volume_delta_3",

                "vol_act_trades_sp_16",
                "vol_act_trades_sp_32",

                "vol_act_trades_acc_16",
                "vol_act_trades_acc_32",

                "vol_act_quote_sp_16",
                "vol_act_quote_sp_32",

                "vol_act_quote_acc_16",
                "vol_act_quote_acc_32",

                "vol_volume_spike_score_16",
                "vol_trades_spike_score_16",

                "vol_microburst_volume_intensity_16",
                "vol_microburst_trades_intensity_16",

                "vol_microburst_combo_16",

                "vol_microburst_slope_16",
                "vol_pressure_slope_16",

                "vol_vwap_distance",

                "vol_ofi",
                "vol_ofi_rel_16",
                "vol_ofi_slp_w20",

                "vol_bap",
                "vol_bap_slope_16",
                "vol_bap_acc_16",

                "vol_svr",
                "vol_svr_slp_w20",
                "vol_svr_acc_5",
                "vol_svr_acc_10",

                "vlt_atr_7_chg",
                "vlt_atr_14_chg",
                "vlt_atr_21_chg",

                "vlt_range_atr_14_loc",
                "vlt_range_atr_14_loc_chg",

                "vlt_atr_7_21_ratio",
                "vlt_atr_7_21_expn",
                "vlt_atr_7_21_cmpr",

                "vlt_atr_7_slp",
                "vlt_atr_14_slp",
                "vlt_atr_21_slp",

                "vlt_std_14_chg",
                "vlt_std_20_chg",
                "vlt_std_50_chg",

                "vlt_std_14_50_ratio",
                "vlt_std_14_50_expn",
                "vlt_std_14_50_cmpr",

                "vlt_std_14_48_ratio",
                "vlt_std_14_48_expn",
                "vlt_std_14_48_cmpr",

                "vlt_std_14_slp",
                "vlt_std_20_slp",
                "vlt_std_50_slp",

                "vlt_boll_20_width_chg",
                "vlt_vol_sqz_bb_kelt_chg",
                "vlt_vol_sqz_bb_kelt_20_chg",

                "vlt_vol_gk_16_slp",
                "vlt_vol_gk_32_slp",
                "vlt_vol_park_16_slp",
                "vlt_vol_park_32_slp",
                "vlt_vol_rs_16_slp",
                "vlt_vol_rs_32_slp",

                "vlt_vol_gk_16_48_ratio",
                "vlt_vol_gk_32_48_ratio",
                "vlt_vol_park_16_48_ratio",
                "vlt_vol_park_32_48_ratio",
                "vlt_vol_rs_16_48_ratio",
                "vlt_vol_rs_32_48_ratio",

                "vlt_vol_rv_10_slp",
                "vlt_vol_rv_30_slp",

                "vlt_vol_rv_10_30_ratio",
                "vlt_vol_rv_10_48_ratio",

                "vlt_ewma_vol_20_slp",
                "vlt_ewma_vol_32_slp",

                "vlt_ewma_vol_20_48_ratio",
                "vlt_ewma_vol_32_48_ratio",

                "tim_minute_of_day",
                "tim_day_of_week",
                "tim_session_asia",
                "tim_session_europe",
                "tim_session_ny",
                "tim_sin_time",
                "tim_cos_time",
                "tim_day_of_month",
                "tim_sin_day_of_week",
                "tim_cos_day_of_week",
                "tim_overlap_asia_eur",
                "tim_overlap_eur_ny",
                "tim_candle_in_h1"
        );
    }

}
