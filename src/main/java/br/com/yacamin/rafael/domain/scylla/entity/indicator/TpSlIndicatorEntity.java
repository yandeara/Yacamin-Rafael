package br.com.yacamin.rafael.domain.scylla.entity.indicator;

import java.time.Instant;

public interface TpSlIndicatorEntity {

    Instant getOpenTime();
    void setOpenTime(Instant openTime);

    // =========================================================================
    // TPSL FEATURES (prefixo oficial: tpsl_)
    // =========================================================================

    // 1) SETUP PRICES
    double getTpsl_tp_price_long();   void setTpsl_tp_price_long(double value);
    double getTpsl_sl_price_long();   void setTpsl_sl_price_long(double value);

    double getTpsl_tp_price_short();  void setTpsl_tp_price_short(double value);
    double getTpsl_sl_price_short();  void setTpsl_sl_price_short(double value);

    // 2) DISTANCES (absolute price distance)
    double getTpsl_tp_dist_long();    void setTpsl_tp_dist_long(double value);
    double getTpsl_tp_dist_short();   void setTpsl_tp_dist_short(double value);

    double getTpsl_sl_dist_long();    void setTpsl_sl_dist_long(double value);
    double getTpsl_sl_dist_short();   void setTpsl_sl_dist_short(double value);

    // 3) NORMALIZED DISTANCES — ATR14
    double getTpsl_tp_dist_atr14_long();   void setTpsl_tp_dist_atr14_long(double value);
    double getTpsl_tp_dist_atr14_short();  void setTpsl_tp_dist_atr14_short(double value);

    double getTpsl_sl_dist_atr14_long();   void setTpsl_sl_dist_atr14_long(double value);
    double getTpsl_sl_dist_atr14_short();  void setTpsl_sl_dist_atr14_short(double value);

    // 4) NORMALIZED DISTANCES — RV48 / EWMA48
    double getTpsl_tp_dist_rv48_long();     void setTpsl_tp_dist_rv48_long(double value);
    double getTpsl_tp_dist_rv48_short();    void setTpsl_tp_dist_rv48_short(double value);

    double getTpsl_tp_dist_ewma48_long();   void setTpsl_tp_dist_ewma48_long(double value);
    double getTpsl_tp_dist_ewma48_short();  void setTpsl_tp_dist_ewma48_short(double value);

    double getTpsl_sl_dist_rv48_long();     void setTpsl_sl_dist_rv48_long(double value);
    double getTpsl_sl_dist_rv48_short();    void setTpsl_sl_dist_rv48_short(double value);

    double getTpsl_sl_dist_ewma48_long();   void setTpsl_sl_dist_ewma48_long(double value);
    double getTpsl_sl_dist_ewma48_short();  void setTpsl_sl_dist_ewma48_short(double value);

    // 5) RISK/REWARD
    double getTpsl_rr_ratio_long();   void setTpsl_rr_ratio_long(double value);
    double getTpsl_rr_ratio_short();  void setTpsl_rr_ratio_short(double value);

    // 6) TP in units of spread / fee
    /*
    double getTpsl_tp_dist_spread_long();   void setTpsl_tp_dist_spread_long(double value);
    double getTpsl_tp_dist_spread_short();  void setTpsl_tp_dist_spread_short(double value);

    double getTpsl_tp_dist_fee_long();      void setTpsl_tp_dist_fee_long(double value);
    double getTpsl_tp_dist_fee_short();     void setTpsl_tp_dist_fee_short(double value);*/

    // 7) HEADROOM / ROOM
    double getTpsl_headroom_to_tp_atr14_long();   void setTpsl_headroom_to_tp_atr14_long(double value);
    double getTpsl_headroom_to_tp_atr14_short();  void setTpsl_headroom_to_tp_atr14_short(double value);

    double getTpsl_room_to_sl_atr14_long();       void setTpsl_room_to_sl_atr14_long(double value);
    double getTpsl_room_to_sl_atr14_short();      void setTpsl_room_to_sl_atr14_short(double value);

    // 8) HEADROOM/ROOM using candle extremes
    double getTpsl_headroom_high_to_tp_atr14_long();  void setTpsl_headroom_high_to_tp_atr14_long(double value);
    double getTpsl_headroom_low_to_tp_atr14_short();  void setTpsl_headroom_low_to_tp_atr14_short(double value);

    double getTpsl_room_low_to_sl_atr14_long();       void setTpsl_room_low_to_sl_atr14_long(double value);
    double getTpsl_room_high_to_sl_atr14_short();     void setTpsl_room_high_to_sl_atr14_short(double value);

    // 9) DONCHIAN RANGE POSITION (w48/w96/w288)
    double getTpsl_donch_high_w48();              void setTpsl_donch_high_w48(double value);
    double getTpsl_donch_low_w48();               void setTpsl_donch_low_w48(double value);
    double getTpsl_donch_pos_w48();               void setTpsl_donch_pos_w48(double value);
    double getTpsl_dist_to_donch_high_w48_atrn(); void setTpsl_dist_to_donch_high_w48_atrn(double value);
    double getTpsl_dist_to_donch_low_w48_atrn();  void setTpsl_dist_to_donch_low_w48_atrn(double value);

    double getTpsl_donch_high_w96();              void setTpsl_donch_high_w96(double value);
    double getTpsl_donch_low_w96();               void setTpsl_donch_low_w96(double value);
    double getTpsl_donch_pos_w96();               void setTpsl_donch_pos_w96(double value);
    double getTpsl_dist_to_donch_high_w96_atrn(); void setTpsl_dist_to_donch_high_w96_atrn(double value);
    double getTpsl_dist_to_donch_low_w96_atrn();  void setTpsl_dist_to_donch_low_w96_atrn(double value);

    double getTpsl_donch_high_w288();              void setTpsl_donch_high_w288(double value);
    double getTpsl_donch_low_w288();               void setTpsl_donch_low_w288(double value);
    double getTpsl_donch_pos_w288();               void setTpsl_donch_pos_w288(double value);
    double getTpsl_dist_to_donch_high_w288_atrn(); void setTpsl_dist_to_donch_high_w288_atrn(double value);
    double getTpsl_dist_to_donch_low_w288_atrn();  void setTpsl_dist_to_donch_low_w288_atrn(double value);

    // 10) BAND POSITION — Bollinger / Keltner
    double getTpsl_bb_percentb_20();  void setTpsl_bb_percentb_20(double value);
    double getTpsl_bb_percentb_48();  void setTpsl_bb_percentb_48(double value);
    double getTpsl_bb_percentb_96();  void setTpsl_bb_percentb_96(double value);

    double getTpsl_bb_z_20();         void setTpsl_bb_z_20(double value);
    double getTpsl_bb_z_48();         void setTpsl_bb_z_48(double value);
    double getTpsl_bb_z_96();         void setTpsl_bb_z_96(double value);

    double getTpsl_kelt_pos_20();     void setTpsl_kelt_pos_20(double value);
    double getTpsl_kelt_pos_48();     void setTpsl_kelt_pos_48(double value);
    double getTpsl_kelt_pos_96();     void setTpsl_kelt_pos_96(double value);

    // 11) MARKET EFFICIENCY
    double getTpsl_eff_ratio_w48();   void setTpsl_eff_ratio_w48(double value);
    double getTpsl_chop_eff_w48();    void setTpsl_chop_eff_w48(double value);

    /*
    // 12) DERIVATIVES — Funding
    double getTpsl_funding_rate();        void setTpsl_funding_rate(double value);
    double getTpsl_funding_ema_16();      void setTpsl_funding_ema_16(double value);
    double getTpsl_funding_ema_48();      void setTpsl_funding_ema_48(double value);
    double getTpsl_funding_zscore_96();   void setTpsl_funding_zscore_96(double value);
    double getTpsl_funding_zscore_288();  void setTpsl_funding_zscore_288(double value);

    // 13) DERIVATIVES — Open Interest
    double getTpsl_open_interest();           void setTpsl_open_interest(double value);

    double getTpsl_oi_delta_1();              void setTpsl_oi_delta_1(double value);
    double getTpsl_oi_pct_1();                void setTpsl_oi_pct_1(double value);

    double getTpsl_oi_ema_48();               void setTpsl_oi_ema_48(double value);

    double getTpsl_oi_zscore_96();            void setTpsl_oi_zscore_96(double value);
    double getTpsl_oi_zscore_288();           void setTpsl_oi_zscore_288(double value);

    double getTpsl_oi_delta_zscore_96();      void setTpsl_oi_delta_zscore_96(double value);

    // 14) DERIVATIVES — Basis
    double getTpsl_basis_mark_index();            void setTpsl_basis_mark_index(double value);
    double getTpsl_basis_mark_index_ema_48();     void setTpsl_basis_mark_index_ema_48(double value);
    double getTpsl_basis_mark_index_zscore_96();  void setTpsl_basis_mark_index_zscore_96(double value);

    double getTpsl_basis_perp_spot();             void setTpsl_basis_perp_spot(double value);
    double getTpsl_basis_perp_spot_ema_48();      void setTpsl_basis_perp_spot_ema_48(double value);
    double getTpsl_basis_perp_spot_zscore_96();   void setTpsl_basis_perp_spot_zscore_96(double value);*/


}
