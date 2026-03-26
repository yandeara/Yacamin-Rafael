package br.com.yacamin.rafael.domain.scylla.entity.indicator;

import java.time.Instant;

public interface VolumeIndicatorEntity {

    Instant getOpenTime();
    void setOpenTime(Instant openTime);

    // =========================================================================
    // 1) RAW MICROSTRUCTURE (mask)
    // =========================================================================
    double getVol_taker_buy_ratio(); void setVol_taker_buy_ratio(double value);
    double getVol_taker_buy_sell_imbalance(); void setVol_taker_buy_sell_imbalance(double value);

    // =========================================================================
    // 1.1) TAKER PRESSURE DYNAMICS (mask)
    // =========================================================================
    double getVol_taker_buy_ratio_rel_16(); void setVol_taker_buy_ratio_rel_16(double value);
    double getVol_taker_buy_ratio_slp_w20(); void setVol_taker_buy_ratio_slp_w20(double value);
    double getVol_taker_buy_sell_imbalance_slp_w20(); void setVol_taker_buy_sell_imbalance_slp_w20(double value);

    // =========================================================================
    // 2) DELTAS (mask)
    // =========================================================================
    double getVol_volume_delta_1(); void setVol_volume_delta_1(double value);
    double getVol_volume_delta_3(); void setVol_volume_delta_3(double value);

    double getVol_trades_delta_1(); void setVol_trades_delta_1(double value);
    double getVol_trades_delta_3(); void setVol_trades_delta_3(double value);

    double getVol_quote_volume_delta_1(); void setVol_quote_volume_delta_1(double value);
    double getVol_quote_volume_delta_3(); void setVol_quote_volume_delta_3(double value);

    // =========================================================================
    // 3) ACTIVITY PRESSURE (mask: sp/acc 16/32 for TRADES and QUOTE)
    // =========================================================================
    double getVol_act_trades_sp_16(); void setVol_act_trades_sp_16(double value);
    double getVol_act_trades_sp_32(); void setVol_act_trades_sp_32(double value);

    double getVol_act_trades_acc_16(); void setVol_act_trades_acc_16(double value);
    double getVol_act_trades_acc_32(); void setVol_act_trades_acc_32(double value);

    double getVol_act_quote_sp_16(); void setVol_act_quote_sp_16(double value);
    double getVol_act_quote_sp_32(); void setVol_act_quote_sp_32(double value);

    double getVol_act_quote_acc_16(); void setVol_act_quote_acc_16(double value);
    double getVol_act_quote_acc_32(); void setVol_act_quote_acc_32(double value);

    // =========================================================================
    // 4) SPIKE SCORE (mask: 16 only)
    // =========================================================================
    double getVol_volume_spike_score_16(); void setVol_volume_spike_score_16(double value);
    double getVol_trades_spike_score_16(); void setVol_trades_spike_score_16(double value);

    // =========================================================================
    // 5) MICROBURST (mask: 16 only)
    // =========================================================================
    double getVol_microburst_volume_intensity_16(); void setVol_microburst_volume_intensity_16(double value);
    double getVol_microburst_trades_intensity_16(); void setVol_microburst_trades_intensity_16(double value);
    double getVol_microburst_combo_16(); void setVol_microburst_combo_16(double value);
    double getVol_microburst_slope_16(); void setVol_microburst_slope_16(double value);
    double getVol_pressure_slope_16(); void setVol_pressure_slope_16(double value);

    // =========================================================================
    // 6) VWAP (mask: vol_vwap_distance; intermediary: vol_vwap)
    // =========================================================================
    double getVol_vwap(); void setVol_vwap(double value);
    double getVol_vwap_distance(); void setVol_vwap_distance(double value);

    // =========================================================================
    // 7) ORDER FLOW IMBALANCE (mask: ofi, ofi_rel_16, ofi_slp_w20)
    // =========================================================================
    double getVol_ofi(); void setVol_ofi(double value);
    double getVol_ofi_rel_16(); void setVol_ofi_rel_16(double value);
    double getVol_ofi_slp_w20(); void setVol_ofi_slp_w20(double value);

    // =========================================================================
    // 8) BID/ASK PRESSURE PROXY (mask: bap, bap_slope_16, bap_acc_16)
    // =========================================================================
    double getVol_bap(); void setVol_bap(double value);
    double getVol_bap_slope_16(); void setVol_bap_slope_16(double value);
    double getVol_bap_acc_16(); void setVol_bap_acc_16(double value);

    // =========================================================================
    // 9) SIGNED VOLUME RATIO (mask: svr, svr_slp_w20, svr_acc_5, svr_acc_10)
    // =========================================================================
    double getVol_svr(); void setVol_svr(double value);
    double getVol_svr_slp_w20(); void setVol_svr_slp_w20(double value);
    double getVol_svr_acc_5(); void setVol_svr_acc_5(double value);
    double getVol_svr_acc_10(); void setVol_svr_acc_10(double value);
}
