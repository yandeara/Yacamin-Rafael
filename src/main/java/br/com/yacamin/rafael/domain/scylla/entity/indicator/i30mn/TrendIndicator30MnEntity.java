package br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;

import jakarta.persistence.*;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.TrendIndicatorEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "trend_indicator_30_mn")
@IdClass(IndicatorKey.class)
public class TrendIndicator30MnEntity implements TrendIndicatorEntity {

    @Id
    private String symbol;

    @Id
    @Column(name = "open_time")
    private Instant openTime;

    // =========================================================================
    // 1) EMA — RAW (Ribbon / Regime State)
    // =========================================================================
    @Column(name = "trd_ema_8")   private double trd_ema_8;
    @Column(name = "trd_ema_12")  private double trd_ema_12;

    @Column(name = "trd_ema_16")  private double trd_ema_16;  // V3 anchor (H4)
    @Column(name = "trd_ema_20")  private double trd_ema_20;
    @Column(name = "trd_ema_21")  private double trd_ema_21;

    @Column(name = "trd_ema_32")  private double trd_ema_32;  // V3 anchor (H8)
    @Column(name = "trd_ema_34")  private double trd_ema_34;

    @Column(name = "trd_ema_50")  private double trd_ema_50;
    @Column(name = "trd_ema_55")  private double trd_ema_55;

    @Column(name = "trd_ema_100") private double trd_ema_100;
    @Column(name = "trd_ema_144") private double trd_ema_144;
    @Column(name = "trd_ema_200") private double trd_ema_200;
    @Column(name = "trd_ema_233") private double trd_ema_233;

    // =========================================================================
    // 2) EMA — SLOPE (Trend Direction / Strength)
    // =========================================================================
    @Column(name = "trd_ema_8_slp")   private double trd_ema_8_slp;
    @Column(name = "trd_ema_20_slp")  private double trd_ema_20_slp;
    @Column(name = "trd_ema_50_slp")  private double trd_ema_50_slp;

    @Column(name = "trd_ema_16_slp")  private double trd_ema_16_slp; // V3
    @Column(name = "trd_ema_32_slp")  private double trd_ema_32_slp; // V3

    // =========================================================================
    // 3) EMA — SLOPE (ATR-N)
    // =========================================================================
    @Column(name = "trd_ema_8_slp_atrn")   private double trd_ema_8_slp_atrn;
    @Column(name = "trd_ema_20_slp_atrn")  private double trd_ema_20_slp_atrn;
    @Column(name = "trd_ema_50_slp_atrn")  private double trd_ema_50_slp_atrn;

    @Column(name = "trd_ema_16_slp_atrn")  private double trd_ema_16_slp_atrn; // V3
    @Column(name = "trd_ema_32_slp_atrn")  private double trd_ema_32_slp_atrn; // V3

    // =========================================================================
    // 4) EMA — SLOPE ACC (Acceleration)
    // =========================================================================
    @Column(name = "trd_ema_8_slp_acc")   private double trd_ema_8_slp_acc;
    @Column(name = "trd_ema_20_slp_acc")  private double trd_ema_20_slp_acc;
    @Column(name = "trd_ema_50_slp_acc")  private double trd_ema_50_slp_acc;

    // =========================================================================
    // 5) EMA — SLOPE ACC (ATR-N)
    // =========================================================================
    @Column(name = "trd_ema_8_slp_acc_atrn")   private double trd_ema_8_slp_acc_atrn;
    @Column(name = "trd_ema_20_slp_acc_atrn")  private double trd_ema_20_slp_acc_atrn;
    @Column(name = "trd_ema_50_slp_acc_atrn")  private double trd_ema_50_slp_acc_atrn;

    // =========================================================================
    // 6) EMA — TDS / TVR (mantidos)
    // =========================================================================
    @Column(name = "trd_ema_8_slp_tds")   private double trd_ema_8_slp_tds;
    @Column(name = "trd_ema_20_slp_tds")  private double trd_ema_20_slp_tds;
    @Column(name = "trd_ema_50_slp_tds")  private double trd_ema_50_slp_tds;

    @Column(name = "trd_ema_8_slp_tvr")   private double trd_ema_8_slp_tvr;
    @Column(name = "trd_ema_20_slp_tvr")  private double trd_ema_20_slp_tvr;
    @Column(name = "trd_ema_50_slp_tvr")  private double trd_ema_50_slp_tvr;

    // =========================================================================
    // 7) DISTANCE — ATR-N ONLY (remove dist_raw + stdn)
    // =========================================================================
    @Column(name = "trd_dist_close_ema_8_atrn")   private double trd_dist_close_ema_8_atrn;
    @Column(name = "trd_dist_close_ema_20_atrn")  private double trd_dist_close_ema_20_atrn;
    @Column(name = "trd_dist_close_ema_50_atrn")  private double trd_dist_close_ema_50_atrn;

    @Column(name = "trd_dist_close_ema_16_atrn")  private double trd_dist_close_ema_16_atrn; // V3
    @Column(name = "trd_dist_close_ema_32_atrn")  private double trd_dist_close_ema_32_atrn; // V3

    @Column(name = "trd_dist_ema_8_20_atrn")      private double trd_dist_ema_8_20_atrn;
    @Column(name = "trd_dist_ema_20_50_atrn")     private double trd_dist_ema_20_50_atrn;
    @Column(name = "trd_dist_ema_8_50_atrn")      private double trd_dist_ema_8_50_atrn;

    // =========================================================================
    // 8) RATIOS — RAW ONLY (remove ratio_atrn + stdn)
    // =========================================================================
    @Column(name = "trd_ratio_ema_8_20")  private double trd_ratio_ema_8_20;
    @Column(name = "trd_ratio_ema_20_50") private double trd_ratio_ema_20_50;
    @Column(name = "trd_ratio_ema_8_50")  private double trd_ratio_ema_8_50;

    // =========================================================================
    // 9) ALIGNMENT / CROSS / DURATION (mantidos; remove *_stdn)
    // =========================================================================
    @Column(name = "trd_alignment_ema_8_20_50_score")       private double trd_alignment_ema_8_20_50_score;
    @Column(name = "trd_alignment_ema_8_20_50_normalized")  private double trd_alignment_ema_8_20_50_normalized;
    @Column(name = "trd_aligment_ema_8_20_50_delta")        private double trd_aligment_ema_8_20_50_delta;
    @Column(name = "trd_aligment_ema_8_20_50_binary")       private double trd_aligment_ema_8_20_50_binary;

    @Column(name = "trd_cross_ema_8_20_binary")     private double trd_cross_ema_8_20_binary;
    @Column(name = "trd_cross_ema_8_20_delta")      private double trd_cross_ema_8_20_delta;
    @Column(name = "trd_cross_ema_8_20_delta_atrn") private double trd_cross_ema_8_20_delta_atrn;

    @Column(name = "trd_cross_ema_20_50_binary")     private double trd_cross_ema_20_50_binary;
    @Column(name = "trd_cross_ema_20_50_delta")      private double trd_cross_ema_20_50_delta;
    @Column(name = "trd_cross_ema_20_50_delta_atrn") private double trd_cross_ema_20_50_delta_atrn;

    @Column(name = "trd_duration_ema_8_20") private double trd_duration_ema_8_20;
    @Column(name = "trd_duration_ema_20_50") private double trd_duration_ema_20_50;
    @Column(name = "trd_duration_ema_8_50") private double trd_duration_ema_8_50;

    // =========================================================================
    // 10) PUSH / GAP (mantidos; remove dist_raw; keep atrn)
    // =========================================================================
    @Column(name = "trd_delta_close_ema_8")      private double trd_delta_close_ema_8;
    @Column(name = "trd_delta_close_ema_20")     private double trd_delta_close_ema_20;
    @Column(name = "trd_delta_close_ema_50")     private double trd_delta_close_ema_50;

    @Column(name = "trd_delta_close_ema_8_atrn")  private double trd_delta_close_ema_8_atrn;
    @Column(name = "trd_delta_close_ema_20_atrn") private double trd_delta_close_ema_20_atrn;
    @Column(name = "trd_delta_close_ema_50_atrn") private double trd_delta_close_ema_50_atrn;

    @Column(name = "trd_delta_ema_8_20")      private double trd_delta_ema_8_20;
    @Column(name = "trd_delta_ema_20_50")     private double trd_delta_ema_20_50;
    @Column(name = "trd_delta_ema_8_20_atrn") private double trd_delta_ema_8_20_atrn;
    @Column(name = "trd_delta_ema_20_50_atrn") private double trd_delta_ema_20_50_atrn;

    // =========================================================================
    // 11) QUALITY / REGIME COMPOSITES (mantidos)
    // =========================================================================
    @Column(name = "trd_cts_close_ema_20_w50") private double trd_cts_close_ema_20_w50;
    @Column(name = "trd_cts_close_ema_20_w10") private double trd_cts_close_ema_20_w10;
    @Column(name = "trd_cts_ema_8_20_50")      private double trd_cts_ema_8_20_50;

    @Column(name = "trd_ema_8_20_50_slp_tcs")  private double trd_ema_8_20_50_slp_tcs;
    @Column(name = "trd_tm_ema_8_20_w10")      private double trd_tm_ema_8_20_w10;
    @Column(name = "trd_ema_20_slp_snr_w10")   private double trd_ema_20_slp_snr_w10;

    @Column(name = "trd_close_tcp_w50")        private double trd_close_tcp_w50;

    @Column(name = "trd_ema_8_zsc")            private double trd_ema_8_zsc;
    @Column(name = "trd_ema_20_zsc")           private double trd_ema_20_zsc;
    @Column(name = "trd_ema_50_zsc")           private double trd_ema_50_zsc;

    // =========================================================================
    // 12) ADX / DI — Trend Strength (V3) (novos)
    // =========================================================================
    @Column(name = "trd_adx_14")     private double trd_adx_14;
    @Column(name = "trd_pdi_14")     private double trd_pdi_14;
    @Column(name = "trd_mdi_14")     private double trd_mdi_14;
    @Column(name = "trd_di_diff_14") private double trd_di_diff_14; // pdi - mdi
}
