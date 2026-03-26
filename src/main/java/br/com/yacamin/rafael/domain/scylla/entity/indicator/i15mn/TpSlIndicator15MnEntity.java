package br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;

import jakarta.persistence.*;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.TpSlIndicatorEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "tpsl_indicator_15_mn")
@IdClass(IndicatorKey.class)
public class TpSlIndicator15MnEntity implements TpSlIndicatorEntity {

    @Id
    private String symbol;

    @Id
    @Column(name = "open_time")
    private Instant openTime;

    // =========================================================================
    // TPSL FEATURES (prefixo oficial: tpsl_)
    // Setup fixo: TP=1% e SL=2% (por candle)
    // =========================================================================

    // =========================================================================
    // 1) SETUP PRICES (per candle)
    // =========================================================================
    @Column(name = "tpsl_tp_price_long")   private double tpsl_tp_price_long;    // close * 1.01
    @Column(name = "tpsl_sl_price_long")   private double tpsl_sl_price_long;    // close * 0.98

    @Column(name = "tpsl_tp_price_short")  private double tpsl_tp_price_short;   // close * 0.99
    @Column(name = "tpsl_sl_price_short")  private double tpsl_sl_price_short;   // close * 1.02

    // =========================================================================
    // 2) DISTANCES (absolute price distance)
    // =========================================================================
    @Column(name = "tpsl_tp_dist_long")    private double tpsl_tp_dist_long;     // tp_price_long  - close
    @Column(name = "tpsl_tp_dist_short")   private double tpsl_tp_dist_short;    // close - tp_price_short

    @Column(name = "tpsl_sl_dist_long")    private double tpsl_sl_dist_long;     // close - sl_price_long
    @Column(name = "tpsl_sl_dist_short")   private double tpsl_sl_dist_short;    // sl_price_short - close

    // =========================================================================
    // 3) NORMALIZED DISTANCES — ATR14 (ATR em preço)
    // =========================================================================
    @Column(name = "tpsl_tp_dist_atr14_long")   private double tpsl_tp_dist_atr14_long;
    @Column(name = "tpsl_tp_dist_atr14_short")  private double tpsl_tp_dist_atr14_short;

    @Column(name = "tpsl_sl_dist_atr14_long")   private double tpsl_sl_dist_atr14_long;
    @Column(name = "tpsl_sl_dist_atr14_short")  private double tpsl_sl_dist_atr14_short;

    // =========================================================================
    // 4) NORMALIZED DISTANCES — RV48 / EWMA48 (vol em retorno => preço = close*vol)
    // =========================================================================
    @Column(name = "tpsl_tp_dist_rv48_long")    private double tpsl_tp_dist_rv48_long;
    @Column(name = "tpsl_tp_dist_rv48_short")   private double tpsl_tp_dist_rv48_short;

    @Column(name = "tpsl_tp_dist_ewma48_long")  private double tpsl_tp_dist_ewma48_long;
    @Column(name = "tpsl_tp_dist_ewma48_short") private double tpsl_tp_dist_ewma48_short;

    @Column(name = "tpsl_sl_dist_rv48_long")    private double tpsl_sl_dist_rv48_long;
    @Column(name = "tpsl_sl_dist_rv48_short")   private double tpsl_sl_dist_rv48_short;

    @Column(name = "tpsl_sl_dist_ewma48_long")  private double tpsl_sl_dist_ewma48_long;
    @Column(name = "tpsl_sl_dist_ewma48_short") private double tpsl_sl_dist_ewma48_short;

    // =========================================================================
    // 5) RISK/REWARD
    // =========================================================================
    @Column(name = "tpsl_rr_ratio_long")   private double tpsl_rr_ratio_long;
    @Column(name = "tpsl_rr_ratio_short")  private double tpsl_rr_ratio_short;

    // =========================================================================
    // 6) TP in units of spread / fee (final ratios)
    // =========================================================================
    /* Para o Futuro
    @Column(name = "tpsl_tp_dist_spread_long")   private double tpsl_tp_dist_spread_long;
    @Column(name = "tpsl_tp_dist_spread_short")  private double tpsl_tp_dist_spread_short;

    @Column(name = "tpsl_tp_dist_fee_long")      private double tpsl_tp_dist_fee_long;
    @Column(name = "tpsl_tp_dist_fee_short")     private double tpsl_tp_dist_fee_short;*/

    // =========================================================================
    // 7) HEADROOM / ROOM (semantic distances)
    // =========================================================================
    @Column(name = "tpsl_headroom_to_tp_atr14_long")   private double tpsl_headroom_to_tp_atr14_long;
    @Column(name = "tpsl_headroom_to_tp_atr14_short")  private double tpsl_headroom_to_tp_atr14_short;

    @Column(name = "tpsl_room_to_sl_atr14_long")       private double tpsl_room_to_sl_atr14_long;
    @Column(name = "tpsl_room_to_sl_atr14_short")      private double tpsl_room_to_sl_atr14_short;

    // =========================================================================
    // 8) HEADROOM/ROOM using candle extremes (HIGH/LOW aware)
    // =========================================================================
    @Column(name = "tpsl_headroom_high_to_tp_atr14_long")   private double tpsl_headroom_high_to_tp_atr14_long;
    @Column(name = "tpsl_headroom_low_to_tp_atr14_short")   private double tpsl_headroom_low_to_tp_atr14_short;

    @Column(name = "tpsl_room_low_to_sl_atr14_long")        private double tpsl_room_low_to_sl_atr14_long;
    @Column(name = "tpsl_room_high_to_sl_atr14_short")      private double tpsl_room_high_to_sl_atr14_short;

    // =========================================================================
    // 9) DONCHIAN RANGE POSITION (w48/w96/w288)
    // =========================================================================
    @Column(name = "tpsl_donch_high_w48")                  private double tpsl_donch_high_w48;
    @Column(name = "tpsl_donch_low_w48")                   private double tpsl_donch_low_w48;
    @Column(name = "tpsl_donch_pos_w48")                   private double tpsl_donch_pos_w48;
    @Column(name = "tpsl_dist_to_donch_high_w48_atrn")     private double tpsl_dist_to_donch_high_w48_atrn;
    @Column(name = "tpsl_dist_to_donch_low_w48_atrn")      private double tpsl_dist_to_donch_low_w48_atrn;

    @Column(name = "tpsl_donch_high_w96")                  private double tpsl_donch_high_w96;
    @Column(name = "tpsl_donch_low_w96")                   private double tpsl_donch_low_w96;
    @Column(name = "tpsl_donch_pos_w96")                   private double tpsl_donch_pos_w96;
    @Column(name = "tpsl_dist_to_donch_high_w96_atrn")     private double tpsl_dist_to_donch_high_w96_atrn;
    @Column(name = "tpsl_dist_to_donch_low_w96_atrn")      private double tpsl_dist_to_donch_low_w96_atrn;

    @Column(name = "tpsl_donch_high_w288")                 private double tpsl_donch_high_w288;
    @Column(name = "tpsl_donch_low_w288")                  private double tpsl_donch_low_w288;
    @Column(name = "tpsl_donch_pos_w288")                  private double tpsl_donch_pos_w288;
    @Column(name = "tpsl_dist_to_donch_high_w288_atrn")    private double tpsl_dist_to_donch_high_w288_atrn;
    @Column(name = "tpsl_dist_to_donch_low_w288_atrn")     private double tpsl_dist_to_donch_low_w288_atrn;

    // =========================================================================
    // 10) BAND POSITION — Bollinger / Keltner
    // =========================================================================
    @Column(name = "tpsl_bb_percentb_20")   private double tpsl_bb_percentb_20;
    @Column(name = "tpsl_bb_percentb_48")   private double tpsl_bb_percentb_48;
    @Column(name = "tpsl_bb_percentb_96")   private double tpsl_bb_percentb_96;

    @Column(name = "tpsl_bb_z_20")          private double tpsl_bb_z_20;
    @Column(name = "tpsl_bb_z_48")          private double tpsl_bb_z_48;
    @Column(name = "tpsl_bb_z_96")          private double tpsl_bb_z_96;

    @Column(name = "tpsl_kelt_pos_20")      private double tpsl_kelt_pos_20;
    @Column(name = "tpsl_kelt_pos_48")      private double tpsl_kelt_pos_48;
    @Column(name = "tpsl_kelt_pos_96")      private double tpsl_kelt_pos_96;

    // =========================================================================
    // 11) MARKET EFFICIENCY
    // =========================================================================
    @Column(name = "tpsl_eff_ratio_w48")    private double tpsl_eff_ratio_w48;
    @Column(name = "tpsl_chop_eff_w48")     private double tpsl_chop_eff_w48;

    // =========================================================================
    // 12) DERIVATIVES — Funding (from Binance)
    // =========================================================================
    /* Para o Futuro
    @Column(name = "tpsl_funding_rate")         private double tpsl_funding_rate;

    @Column(name = "tpsl_funding_ema_16")       private double tpsl_funding_ema_16;     // ~8h em candles 30m
    @Column(name = "tpsl_funding_ema_48")       private double tpsl_funding_ema_48;     // ~24h

    @Column(name = "tpsl_funding_zscore_96")    private double tpsl_funding_zscore_96;  // ~2d
    @Column(name = "tpsl_funding_zscore_288")   private double tpsl_funding_zscore_288; // ~6d*/

    // =========================================================================
    // 13) DERIVATIVES — Open Interest (from Binance)
    // =========================================================================
    /* Para o Futuro
    @Column(name = "tpsl_open_interest")            private double tpsl_open_interest;

    @Column(name = "tpsl_oi_delta_1")               private double tpsl_oi_delta_1;
    @Column(name = "tpsl_oi_pct_1")                 private double tpsl_oi_pct_1;

    @Column(name = "tpsl_oi_ema_48")                private double tpsl_oi_ema_48;

    @Column(name = "tpsl_oi_zscore_96")             private double tpsl_oi_zscore_96;
    @Column(name = "tpsl_oi_zscore_288")            private double tpsl_oi_zscore_288;

    @Column(name = "tpsl_oi_delta_zscore_96")       private double tpsl_oi_delta_zscore_96; */

    // =========================================================================
    // 14) DERIVATIVES — Basis (perp vs index / perp vs spot)
    // =========================================================================
    /* Para o Futuro
    @Column(name = "tpsl_basis_mark_index")            private double tpsl_basis_mark_index;
    @Column(name = "tpsl_basis_mark_index_ema_48")     private double tpsl_basis_mark_index_ema_48;
    @Column(name = "tpsl_basis_mark_index_zscore_96")  private double tpsl_basis_mark_index_zscore_96;

    @Column(name = "tpsl_basis_perp_spot")             private double tpsl_basis_perp_spot;
    @Column(name = "tpsl_basis_perp_spot_ema_48")      private double tpsl_basis_perp_spot_ema_48;
    @Column(name = "tpsl_basis_perp_spot_zscore_96")   private double tpsl_basis_perp_spot_zscore_96; */

}
