package br.com.yacamin.rafael.domain.scylla.entity.indicator;

import java.time.Instant;

public interface MomentumIndicatorEntity {

    Instant getOpenTime();
    void setOpenTime(Instant openTime);

    // ================================================================================================================
    // CLOSE RETURNS — RAW (intermediaries for _atrn)
    // ================================================================================================================
    double getMom_close_ret_1();
    void setMom_close_ret_1(double value);
    double getMom_close_ret_2();
    void setMom_close_ret_2(double value);
    double getMom_close_ret_3();
    void setMom_close_ret_3(double value);
    double getMom_close_ret_4();
    void setMom_close_ret_4(double value);
    double getMom_close_ret_5();
    void setMom_close_ret_5(double value);
    double getMom_close_ret_6();
    void setMom_close_ret_6(double value);
    double getMom_close_ret_8();
    void setMom_close_ret_8(double value);
    double getMom_close_ret_10();
    void setMom_close_ret_10(double value);
    double getMom_close_ret_12();
    void setMom_close_ret_12(double value);
    double getMom_close_ret_16();
    void setMom_close_ret_16(double value);

    // ================================================================================================================
    // CLOSE RETURNS — ATR-N (mask)
    // ================================================================================================================
    double getMom_close_ret_1_atrn();
    void setMom_close_ret_1_atrn(double value);
    double getMom_close_ret_2_atrn();
    void setMom_close_ret_2_atrn(double value);
    double getMom_close_ret_3_atrn();
    void setMom_close_ret_3_atrn(double value);
    double getMom_close_ret_4_atrn();
    void setMom_close_ret_4_atrn(double value);
    double getMom_close_ret_5_atrn();
    void setMom_close_ret_5_atrn(double value);
    double getMom_close_ret_6_atrn();
    void setMom_close_ret_6_atrn(double value);
    double getMom_close_ret_8_atrn();
    void setMom_close_ret_8_atrn(double value);
    double getMom_close_ret_10_atrn();
    void setMom_close_ret_10_atrn(double value);
    double getMom_close_ret_12_atrn();
    void setMom_close_ret_12_atrn(double value);
    double getMom_close_ret_16_atrn();
    void setMom_close_ret_16_atrn(double value);

    // ================================================================================================================
    // BURST (mask)
    // ================================================================================================================
    double getMom_burst_10();
    void setMom_burst_10(double value);
    double getMom_burst_16();
    void setMom_burst_16(double value);
    double getMom_burst_32();
    void setMom_burst_32(double value);

    // ================================================================================================================
    // CONTINUATION RATE (mask)
    // ================================================================================================================
    double getMom_cntrate_10();
    void setMom_cntrate_10(double value);
    double getMom_cntrate_16();
    void setMom_cntrate_16(double value);
    double getMom_cntrate_32();
    void setMom_cntrate_32(double value);

    // ================================================================================================================
    // DECAY (mask)
    // ================================================================================================================
    double getMom_decay_10();
    void setMom_decay_10(double value);
    double getMom_decay_16();
    void setMom_decay_16(double value);
    double getMom_decay_32();
    void setMom_decay_32(double value);

    // ================================================================================================================
    // IMPULSE (mask)
    // ================================================================================================================
    double getMom_impls_10();
    void setMom_impls_10(double value);
    double getMom_impls_16();
    void setMom_impls_16(double value);
    double getMom_impls_32();
    void setMom_impls_32(double value);

    // ================================================================================================================
    // CHOP RATIO (mask)
    // ================================================================================================================
    double getMom_chprt_10();
    void setMom_chprt_10(double value);
    double getMom_chprt_16();
    void setMom_chprt_16(double value);
    double getMom_chprt_32();
    void setMom_chprt_32(double value);

    // ================================================================================================================
    // RSI DELTA (mask)
    // ================================================================================================================
    double getMom_rsi_2_dlt();
    void setMom_rsi_2_dlt(double value);
    double getMom_rsi_3_dlt();
    void setMom_rsi_3_dlt(double value);
    double getMom_rsi_5_dlt();
    void setMom_rsi_5_dlt(double value);
    double getMom_rsi_7_dlt();
    void setMom_rsi_7_dlt(double value);
    double getMom_rsi_14_dlt();
    void setMom_rsi_14_dlt(double value);

    // ================================================================================================================
    // RSI ROC (mask)
    // ================================================================================================================
    double getMom_rsi_2_roc();
    void setMom_rsi_2_roc(double value);
    double getMom_rsi_3_roc();
    void setMom_rsi_3_roc(double value);
    double getMom_rsi_5_roc();
    void setMom_rsi_5_roc(double value);
    double getMom_rsi_7_roc();
    void setMom_rsi_7_roc(double value);
    double getMom_rsi_14_roc();
    void setMom_rsi_14_roc(double value);

    // ================================================================================================================
    // RSI SLOPE (mask)
    // ================================================================================================================
    double getMom_rsi_7_slp();
    void setMom_rsi_7_slp(double value);
    double getMom_rsi_14_slp();
    void setMom_rsi_14_slp(double value);

    // ================================================================================================================
    // RSI ACC / ATRN (mask)
    // ================================================================================================================
    double getMom_rsi_14_acc();
    void setMom_rsi_14_acc(double value);
    double getMom_rsi_14_atrn();
    void setMom_rsi_14_atrn(double value);

    // ================================================================================================================
    // RSI DIST MID (mask)
    // ================================================================================================================
    double getMom_rsi_7_dst_mid();
    void setMom_rsi_7_dst_mid(double value);
    double getMom_rsi_14_dst_mid();
    void setMom_rsi_14_dst_mid(double value);

    // ================================================================================================================
    // RSI TAIL (mask)
    // ================================================================================================================
    double getMom_rsi_7_tail_up();
    void setMom_rsi_7_tail_up(double value);
    double getMom_rsi_7_tail_dw();
    void setMom_rsi_7_tail_dw(double value);
    double getMom_rsi_14_tail_up();
    void setMom_rsi_14_tail_up(double value);
    double getMom_rsi_14_tail_dw();
    void setMom_rsi_14_tail_dw(double value);

    // ================================================================================================================
    // CMO DELTA (mask)
    // ================================================================================================================
    double getMom_cmo_14_dlt();
    void setMom_cmo_14_dlt(double value);
    double getMom_cmo_20_dlt();
    void setMom_cmo_20_dlt(double value);

    // ================================================================================================================
    // CMO DIST MID (mask)
    // ================================================================================================================
    double getMom_cmo_14_dst_mid();
    void setMom_cmo_14_dst_mid(double value);
    double getMom_cmo_20_dst_mid();
    void setMom_cmo_20_dst_mid(double value);

    // ================================================================================================================
    // WPR DELTA (mask)
    // ================================================================================================================
    double getMom_wpr_14_dlt();
    void setMom_wpr_14_dlt(double value);
    double getMom_wpr_28_dlt();
    void setMom_wpr_28_dlt(double value);
    double getMom_wpr_42_dlt();
    void setMom_wpr_42_dlt(double value);

    // ================================================================================================================
    // WPR DIST MID (mask)
    // ================================================================================================================
    double getMom_wpr_14_dst_mid();
    void setMom_wpr_14_dst_mid(double value);
    double getMom_wpr_28_dst_mid();
    void setMom_wpr_28_dst_mid(double value);
    double getMom_wpr_48_dst_mid();
    void setMom_wpr_48_dst_mid(double value);

    // ================================================================================================================
    // STOCH (mask)
    // ================================================================================================================
    double getMom_stoch_14_k_dlt();
    void setMom_stoch_14_k_dlt(double value);
    double getMom_stoch_14_d_dlt();
    void setMom_stoch_14_d_dlt(double value);
    double getMom_stoch_14_spread();
    void setMom_stoch_14_spread(double value);
    double getMom_stoch_14_k_dst_mid();
    void setMom_stoch_14_k_dst_mid(double value);

    // ================================================================================================================
    // TRIX (mask)
    // ================================================================================================================
    double getMom_trix_9_dlt();
    void setMom_trix_9_dlt(double value);
    double getMom_trix_9_hist();
    void setMom_trix_9_hist(double value);

    // ================================================================================================================
    // TSI (mask)
    // ================================================================================================================
    double getMom_tsi_25_13_dlt();
    void setMom_tsi_25_13_dlt(double value);
    double getMom_tsi_25_13_hist();
    void setMom_tsi_25_13_hist(double value);
    double getMom_tsi_25_13_dst_mid();
    void setMom_tsi_25_13_dst_mid(double value);

    // ================================================================================================================
    // PPO (mask)
    // ================================================================================================================
    double getMom_ppo_hist_12_26_9();
    void setMom_ppo_hist_12_26_9(double value);
    double getMom_ppo_12_26_dlt();
    void setMom_ppo_12_26_dlt(double value);
    double getMom_ppo_hist_12_26_9_dlt();
    void setMom_ppo_hist_12_26_9_dlt(double value);

    // ================================================================================================================
    // CLOSE PRICE SLOPE (mask)
    // ================================================================================================================
    double getMom_close_3_slp();
    void setMom_close_3_slp(double value);
    double getMom_close_8_slp();
    void setMom_close_8_slp(double value);
    double getMom_close_14_slp();
    void setMom_close_14_slp(double value);

    // ================================================================================================================
    // CLOSE PRICE SLOPE ATR-N (mask)
    // ================================================================================================================
    double getMom_close_3_slp_atrn();
    void setMom_close_3_slp_atrn(double value);
    double getMom_close_8_slp_atrn();
    void setMom_close_8_slp_atrn(double value);
    double getMom_close_14_slp_atrn();
    void setMom_close_14_slp_atrn(double value);

    // ================================================================================================================
    // CLOSE PRICE SLOPE ACC (mask)
    // ================================================================================================================
    double getMom_close_3_slp_acc();
    void setMom_close_3_slp_acc(double value);
    double getMom_close_8_slp_acc();
    void setMom_close_8_slp_acc(double value);
    double getMom_close_14_slp_acc();
    void setMom_close_14_slp_acc(double value);

    // ================================================================================================================
    // CLOSE PRICE SLOPE ACC ATR-N (mask)
    // ================================================================================================================
    double getMom_close_3_slp_acc_atrn();
    void setMom_close_3_slp_acc_atrn(double value);
    double getMom_close_8_slp_acc_atrn();
    void setMom_close_8_slp_acc_atrn(double value);
    double getMom_close_14_slp_acc_atrn();
    void setMom_close_14_slp_acc_atrn(double value);

    // ================================================================================================================
    // CCI (mask)
    // ================================================================================================================
    double getMom_cci_14_dlt();
    void setMom_cci_14_dlt(double value);
    double getMom_cci_20_dlt();
    void setMom_cci_20_dlt(double value);
    double getMom_cci_14_dst_mid();
    void setMom_cci_14_dst_mid(double value);
    double getMom_cci_20_dst_mid();
    void setMom_cci_20_dst_mid(double value);

    // ================================================================================================================
    // ROC (mask)
    // ================================================================================================================
    double getMom_roc_1();
    void setMom_roc_1(double value);
    double getMom_roc_2();
    void setMom_roc_2(double value);
    double getMom_roc_3();
    void setMom_roc_3(double value);
    double getMom_roc_5();
    void setMom_roc_5(double value);

    // ================================================================================================================
    // STABILITY slp_w20 (mask)
    // ================================================================================================================
    double getMom_ppo_hist_12_26_9_slp_w20();
    void setMom_ppo_hist_12_26_9_slp_w20(double value);
    double getMom_trix_hist_9_slp_w20();
    void setMom_trix_hist_9_slp_w20(double value);
    double getMom_tsi_hist_25_13_slp_w20();
    void setMom_tsi_hist_25_13_slp_w20(double value);
}
