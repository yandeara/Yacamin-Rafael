package br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.MicrostructureIndicatorEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Setter
@Getter
@Table("microstructure_indicator_15_mn")
public class MicrostructureIndicator15MnEntity implements MicrostructureIndicatorEntity {

    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED)
    private String symbol;

    @PrimaryKeyColumn(name = "open_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    @Column("open_time")
    private Instant openTime;

    // =========================================================================
    // 1. ROLL
    // =========================================================================
    // =========================================================================
    // 1.1 ROLL COVARIANCE
    // =========================================================================
    // [Janelado (antigo): 16, 32, 96, 512] + [Novo 24h/7d/14d: 48, 336, 672]
    // =========================================================================

    // -------------------------------------------------------------------------
    // 1.1.0 ROLL COVARIANCE — RAW
    // -------------------------------------------------------------------------
    @Column("mic_roll_cov_w16")  private double mic_roll_cov_w16;
    @Column("mic_roll_cov_w32")  private double mic_roll_cov_w32;
    @Column("mic_roll_cov_w96")  private double mic_roll_cov_w96;
    @Column("mic_roll_cov_w512") private double mic_roll_cov_w512;

    @Column("mic_roll_cov_w48")  private double mic_roll_cov_w48;
    @Column("mic_roll_cov_w336") private double mic_roll_cov_w336;
    @Column("mic_roll_cov_w672") private double mic_roll_cov_w672;

    // -------------------------------------------------------------------------
    // 1.1.1 ROLL COVARIANCE — PERCENT
    // -------------------------------------------------------------------------
    @Column("mic_roll_cov_pct_w16")  private double mic_roll_cov_pct_w16;
    @Column("mic_roll_cov_pct_w32")  private double mic_roll_cov_pct_w32;
    @Column("mic_roll_cov_pct_w96")  private double mic_roll_cov_pct_w96;
    @Column("mic_roll_cov_pct_w512") private double mic_roll_cov_pct_w512;

    @Column("mic_roll_cov_pct_w48")  private double mic_roll_cov_pct_w48;
    @Column("mic_roll_cov_pct_w336") private double mic_roll_cov_pct_w336;
    @Column("mic_roll_cov_pct_w672") private double mic_roll_cov_pct_w672;

    // -------------------------------------------------------------------------
    // 1.1.2 ROLL COVARIANCE — PERCENT ZSCORE
    // -------------------------------------------------------------------------
    @Column("mic_roll_cov_pct_zsc_w16")  private double mic_roll_cov_pct_zsc_w16;
    @Column("mic_roll_cov_pct_zsc_w32")  private double mic_roll_cov_pct_zsc_w32;
    @Column("mic_roll_cov_pct_zsc_w96")  private double mic_roll_cov_pct_zsc_w96;
    @Column("mic_roll_cov_pct_zsc_w512") private double mic_roll_cov_pct_zsc_w512;

    @Column("mic_roll_cov_pct_zsc_w48")  private double mic_roll_cov_pct_zsc_w48;
    @Column("mic_roll_cov_pct_zsc_w336") private double mic_roll_cov_pct_zsc_w336;
    @Column("mic_roll_cov_pct_zsc_w672") private double mic_roll_cov_pct_zsc_w672;

    // -------------------------------------------------------------------------
    // 1.1.3 ROLL COVARIANCE — RAW ZSCORE
    // -------------------------------------------------------------------------
    @Column("mic_roll_cov_zsc_w16")  private double mic_roll_cov_zsc_w16;
    @Column("mic_roll_cov_zsc_w32")  private double mic_roll_cov_zsc_w32;
    @Column("mic_roll_cov_zsc_w96")  private double mic_roll_cov_zsc_w96;
    @Column("mic_roll_cov_zsc_w512") private double mic_roll_cov_zsc_w512;

    @Column("mic_roll_cov_zsc_w48")  private double mic_roll_cov_zsc_w48;
    @Column("mic_roll_cov_zsc_w336") private double mic_roll_cov_zsc_w336;
    @Column("mic_roll_cov_zsc_w672") private double mic_roll_cov_zsc_w672;

    // =========================================================================
    // 1.2 ROLL SPREAD [Janelado: 16, 32. 96 e 512]
    // =========================================================================
    @Column("mic_roll_spread_w16")
    private double mic_roll_spread_w16;

    @Column("mic_roll_spread_w32")
    private double mic_roll_spread_w32;

    @Column("mic_roll_spread_w96")
    private double mic_roll_spread_w96;

    @Column("mic_roll_spread_w512")
    private double mic_roll_spread_w512;

    @Column("mic_roll_spread_w48")
    private double mic_roll_spread_w48;

    @Column("mic_roll_spread_w336")
    private double mic_roll_spread_w336;

    @Column("mic_roll_spread_w672")
    private double mic_roll_spread_w672;

    // =========================================================================
    // 1.2.2 ROLL SPREAD ATR-N
    // =========================================================================
    @Column("mic_roll_spread_atrn14_w16")
    private double mic_roll_spread_atrn14_w16;

    @Column("mic_roll_spread_atrn14_w32")
    private double mic_roll_spread_atrn14_w32;

    @Column("mic_roll_spread_atrn14_w96")
    private double mic_roll_spread_atrn14_w96;

    @Column("mic_roll_spread_atrn14_w512")
    private double mic_roll_spread_atrn14_w512;

    @Column("mic_roll_spread_atrn14_w48")
    private double mic_roll_spread_atrn14_w48;

    @Column("mic_roll_spread_atrn14_w336")
    private double mic_roll_spread_atrn14_w336;

    @Column("mic_roll_spread_atrn14_w672")
    private double mic_roll_spread_atrn14_w672;

    // =========================================================================
    // 1.2.2 ROLL SPREAD PCT - PERCENT
    // =========================================================================
    @Column("mic_roll_spread_pct_w16")
    private double mic_roll_spread_pct_w16;

    @Column("mic_roll_spread_pct_w32")
    private double mic_roll_spread_pct_w32;

    @Column("mic_roll_spread_pct_w96")
    private double mic_roll_spread_pct_w96;

    @Column("mic_roll_spread_pct_w512")
    private double mic_roll_spread_pct_w512;

    @Column("mic_roll_spread_pct_w48")
    private double mic_roll_spread_pct_w48;

    @Column("mic_roll_spread_pct_w336")
    private double mic_roll_spread_pct_w336;

    @Column("mic_roll_spread_pct_w672")
    private double mic_roll_spread_pct_w672;

    // =========================================================================
    // 1.2.2.1 ROLL SPREAD PCT ATR-N
    // =========================================================================
    @Column("mic_roll_spread_pct_atrn14_w16")
    private double mic_roll_spread_pct_atrn14_w16;

    @Column("mic_roll_spread_pct_atrn14_w32")
    private double mic_roll_spread_pct_atrn14_w32;

    @Column("mic_roll_spread_pct_atrn14_w96")
    private double mic_roll_spread_pct_atrn14_w96;

    @Column("mic_roll_spread_pct_atrn14_w512")
    private double mic_roll_spread_pct_atrn14_w512;

    @Column("mic_roll_spread_pct_atrn14_w48")
    private double mic_roll_spread_pct_atrn14_w48;

    @Column("mic_roll_spread_pct_atrn14_w336")
    private double mic_roll_spread_pct_atrn14_w336;

    @Column("mic_roll_spread_pct_atrn14_w672")
    private double mic_roll_spread_pct_atrn14_w672;

    // =========================================================================
    // 1.2.2 ROLL SPREAD ACCELERATION
    // =========================================================================
    @Column("mic_roll_spread_acc_w16")
    private double mic_roll_spread_acc_w16;

    @Column("mic_roll_spread_acc_w32")
    private double mic_roll_spread_acc_w32;

    @Column("mic_roll_spread_acc_w96")
    private double mic_roll_spread_acc_w96;

    @Column("mic_roll_spread_acc_w512")
    private double mic_roll_spread_acc_w512;

    @Column("mic_roll_spread_acc_w48")
    private double mic_roll_spread_acc_w48;

    @Column("mic_roll_spread_acc_w336")
    private double mic_roll_spread_acc_w336;

    @Column("mic_roll_spread_acc_w672")
    private double mic_roll_spread_acc_w672;

    // =========================================================================
    // 1.2.3 ROLL SPREAD SLOPE
    // =========================================================================
    @Column("mic_roll_spread_slp_w16")
    private double mic_roll_spread_slp_w16;

    @Column("mic_roll_spread_slp_w32")
    private double mic_roll_spread_slp_w32;

    @Column("mic_roll_spread_slp_w96")
    private double mic_roll_spread_slp_w96;

    @Column("mic_roll_spread_slp_w512")
    private double mic_roll_spread_slp_w512;

    @Column("mic_roll_spread_slp_w48")
    private double mic_roll_spread_slp_w48;

    @Column("mic_roll_spread_slp_w336")
    private double mic_roll_spread_slp_w336;

    @Column("mic_roll_spread_slp_w672")
    private double mic_roll_spread_slp_w672;

    // =========================================================================
    // ROLL SPREAD SMOOTHING
    // =========================================================================
    @Column("mic_roll_spread_ma_w16")
    private double mic_roll_spread_ma_w16;

    @Column("mic_roll_spread_ma_w32")
    private double mic_roll_spread_ma_w32;

    @Column("mic_roll_spread_ma_w96")
    private double mic_roll_spread_ma_w96;

    @Column("mic_roll_spread_ma_w512")
    private double mic_roll_spread_ma_w512;

    @Column("mic_roll_spread_ma_w48")
    private double mic_roll_spread_ma_w48;

    @Column("mic_roll_spread_ma_w336")
    private double mic_roll_spread_ma_w336;

    @Column("mic_roll_spread_ma_w672")
    private double mic_roll_spread_ma_w672;

    // =========================================================================
    // ROLL SPREAD PERSISTENCE (proporção de spreads > 0)
    // =========================================================================
    @Column("mic_roll_spread_prst_w16")
    private double mic_roll_spread_prst_w16;

    @Column("mic_roll_spread_prst_w32")
    private double mic_roll_spread_prst_w32;

    @Column("mic_roll_spread_prst_w96")
    private double mic_roll_spread_prst_w96;

    @Column("mic_roll_spread_prst_w512")
    private double mic_roll_spread_prst_w512;

    @Column("mic_roll_spread_prst_w48")
    private double mic_roll_spread_prst_w48;

    @Column("mic_roll_spread_prst_w336")
    private double mic_roll_spread_prst_w336;

    @Column("mic_roll_spread_prst_w672")
    private double mic_roll_spread_prst_w672;

    // =========================================================================
    // ROLL SPREAD ZScore
    // =========================================================================
    @Column("mic_roll_spread_zsc_w16")
    private double mic_roll_spread_zsc_w16;

    @Column("mic_roll_spread_zsc_w32")
    private double mic_roll_spread_zsc_w32;

    @Column("mic_roll_spread_zsc_w96")
    private double mic_roll_spread_zsc_w96;

    @Column("mic_roll_spread_zsc_w512")
    private double mic_roll_spread_zsc_w512;

    @Column("mic_roll_spread_zsc_w48")
    private double mic_roll_spread_zsc_w48;

    @Column("mic_roll_spread_zsc_w336")
    private double mic_roll_spread_zsc_w336;

    @Column("mic_roll_spread_zsc_w672")
    private double mic_roll_spread_zsc_w672;

    // =========================================================================
    // ROLL SPREAD PCT ZScore
    // =========================================================================
    @Column("mic_roll_spread_pct_zsc_w16")
    private double mic_roll_spread_pct_zsc_w16;

    @Column("mic_roll_spread_pct_zsc_w32")
    private double mic_roll_spread_pct_zsc_w32;

    @Column("mic_roll_spread_pct_zsc_w96")
    private double mic_roll_spread_pct_zsc_w96;

    @Column("mic_roll_spread_pct_zsc_w512")
    private double mic_roll_spread_pct_zsc_w512;

    @Column("mic_roll_spread_pct_zsc_w48")
    private double mic_roll_spread_pct_zsc_w48;

    @Column("mic_roll_spread_pct_zsc_w336")
    private double mic_roll_spread_pct_zsc_w336;

    @Column("mic_roll_spread_pct_zsc_w672")
    private double mic_roll_spread_pct_zsc_w672;

    // =========================================================================
    // 1.2.6 ROLL SPREAD DIVERGENCE (Zraw - Zpct)
    // =========================================================================
    @Column("mic_roll_spread_dvgc_w16")
    private double mic_roll_spread_dvgc_w16;

    @Column("mic_roll_spread_dvgc_w32")
    private double mic_roll_spread_dvgc_w32;

    @Column("mic_roll_spread_dvgc_w96")
    private double mic_roll_spread_dvgc_w96;

    @Column("mic_roll_spread_dvgc_w512")
    private double mic_roll_spread_dvgc_w512;

    @Column("mic_roll_spread_dvgc_w48")
    private double mic_roll_spread_dvgc_w48;

    @Column("mic_roll_spread_dvgc_w336")
    private double mic_roll_spread_dvgc_w336;

    @Column("mic_roll_spread_dvgc_w672")
    private double mic_roll_spread_dvgc_w672;

    // =========================================================================
    // 1.2.6 ROLL SPREAD VOLATILITY
    // =========================================================================
    @Column("mic_roll_spread_vol_w16")
    private double mic_roll_spread_vol_w16;

    @Column("mic_roll_spread_vol_w32")
    private double mic_roll_spread_vol_w32;

    @Column("mic_roll_spread_vol_w96")
    private double mic_roll_spread_vol_w96;

    @Column("mic_roll_spread_vol_w512")
    private double mic_roll_spread_vol_w512;

    @Column("mic_roll_spread_vol_w48")
    private double mic_roll_spread_vol_w48;

    @Column("mic_roll_spread_vol_w336")
    private double mic_roll_spread_vol_w336;

    @Column("mic_roll_spread_vol_w672")
    private double mic_roll_spread_vol_w672;

    // =========================================================================
    // 1.2.6 ROLL SPREAD PERCENTILE
    // =========================================================================
    @Column("mic_roll_spread_pctile_w16")
    private double mic_roll_spread_pctile_w16;

    @Column("mic_roll_spread_pctile_w32")
    private double mic_roll_spread_pctile_w32;

    @Column("mic_roll_spread_pctile_w96")
    private double mic_roll_spread_pctile_w96;

    @Column("mic_roll_spread_pctile_w512")
    private double mic_roll_spread_pctile_w512;

    @Column("mic_roll_spread_pctile_w48")
    private double mic_roll_spread_pctile_w48;

    @Column("mic_roll_spread_pctile_w336")
    private double mic_roll_spread_pctile_w336;

    @Column("mic_roll_spread_pctile_w672")
    private double mic_roll_spread_pctile_w672;













    // ==========================================================================
    // 2. AMIHUD
    // ==========================================================================
    // ==========================================================================
    // AMIHUD RAW
    // ==========================================================================
    @Column("mic_amihud")
    private double mic_amihud;

    // ==========================================================================
    // AMIHUD Z-SCORE
    // ==========================================================================
    @Column("mic_amihud_zscore_20")
    private double mic_amihud_zscore_20;

    @Column("mic_amihud_zscore_80")
    private double mic_amihud_zscore_80;

    // ==========================================================================
    // AMIHUD Relative
    // ==========================================================================
    @Column("mic_amihud_rel_10")
    private double mic_amihud_rel_10;

    @Column("mic_amihud_rel_40")
    private double mic_amihud_rel_40;

    // ==========================================================================
    // AMIHUD SLOPE (TENDÊNCIA DA ILIQUIDEZ)
    // ==========================================================================
    @Column("mic_amihud_slp_w4")
    private double mic_amihud_slp_w4;

    @Column("mic_amihud_slp_w20")
    private double mic_amihud_slp_w20;

    @Column("mic_amihud_slp_w50")
    private double mic_amihud_slp_w50;

    // ==========================================================================
    // AMIHUD ACCELERATION
    // ==========================================================================
    @Column("mic_amihud_acc_w4")
    private double mic_amihud_acc_w4;

    @Column("mic_amihud_acc_w5")
    private double mic_amihud_acc_w5;

    @Column("mic_amihud_acc_w10")
    private double mic_amihud_acc_w10;

    @Column("mic_amihud_acc_w16")
    private double mic_amihud_acc_w16;

    // ==========================================================================
    // AMIHUD MOVING AVERAGES (SUAVIZAÇÃO / COMPRESSÃO)
    // ==========================================================================
    @Column("mic_amihud_ma_10")
    private double mic_amihud_ma_10;

    @Column("mic_amihud_ma_20")
    private double mic_amihud_ma_20;

    @Column("mic_amihud_ma_30")
    private double mic_amihud_ma_30;

    // ==========================================================================
    // AMIHUD VOLATILITY (INSTABILIDADE DE ILIQUIDEZ)
    // ==========================================================================
    @Column("mic_amihud_vol_10")
    private double mic_amihud_vol_10;

    @Column("mic_amihud_vol_20")
    private double mic_amihud_vol_20;

    @Column("mic_amihud_vol_40")
    private double mic_amihud_vol_40;

    // ==========================================================================
    // AMIHUD TURNOVER
    // ==========================================================================
    @Column("mic_amihud_turnover")
    private double mic_amihud_turnover;

    // ==========================================================================
    // AMIHUD PECENTILE
    // ==========================================================================
    @Column("mic_amihud_pctile_w20")
    private double mic_amihud_pctile_w20;

    // ==========================================================================
    // AMIHUD SIGNED
    // ==========================================================================
    @Column("mic_amihud_signed")
    private double mic_amihud_signed;

    // ==========================================================================
    // AMIHUD LRMR
    // ==========================================================================
    @Column("mic_amihud_lrmr_10_40")
    private double mic_amihud_lrmr_10_40;

    // ==========================================================================
    // AMIHUD STABILITY
    // ==========================================================================
    @Column("mic_amihud_stability_40")
    private double mic_amihud_stability_40;

    // ==========================================================================
    // AMIHUD VOLATILITY RELATIVE
    // ==========================================================================
    @Column("mic_amihud_vol_rel_40")
    private double mic_amihud_vol_rel_40;

    // ==========================================================================
    // AMIHUD ATR-N (REGIME AWARE)
    // ==========================================================================
    @Column("mic_amihud_atrn")
    private double mic_amihud_atrn;

    // ==========================================================================
    // AMIHUD PERSISTENCE (NÍVEL DE FRAGILIDADE CONTÍNUA)
    // ==========================================================================
    @Column("mic_amihud_prst_w10")
    private double mic_amihud_prst_w10;

    @Column("mic_amihud_prst_w20")
    private double mic_amihud_prst_w20;

    @Column("mic_amihud_prst_w40")
    private double mic_amihud_prst_w40;

    // ==========================================================================
    // AMIHUD DIVERGENCE (RAW - MEAN)
    // ==========================================================================
    @Column("mic_amihud_dvgc")
    private double mic_amihud_dvgc;

    // ==========================================================================
    // AMIHUD REGIME STATE
    // ==========================================================================
    @Column("mic_amihud_regime_state")
    private double mic_amihud_regime_state;

    // ==========================================================================
    // AMIHUD TREND ALIGNMENT
    // ==========================================================================
    @Column("mic_amihud_trend_alignment")
    private double mic_amihud_trend_alignment;

    // ==========================================================================
    // AMIHUD BREAKDOWN RISK
    // ==========================================================================
    @Column("mic_amihud_breakdown_risk")
    private double mic_amihud_breakdown_risk;

    // ==========================================================================
    // AMIHUD FRACTAL RATIO
    // ==========================================================================
    @Column("mic_amihud_fractal_5m_30m")
    private double mic_amihud_fractal_5m_30m;

    // ==========================================================================
    // AMIHUD REGIME CONFIDENCE
    // ==========================================================================
    @Column("mic_amihud_regime_conf")
    private double mic_amihud_regime_conf;

    // ==========================================================================
    // 3. KYLE LAMBDA
    // ==========================================================================
    @Column("mic_kyle_lambda_w4")
    private double mic_kyle_lambda_w4;

    @Column("mic_kyle_lambda_w16")
    private double mic_kyle_lambda_w16;

    @Column("mic_kyle_lambda_w96")
    private double mic_kyle_lambda_w96;

    @Column("mic_kyle_lambda_w200")
    private double mic_kyle_lambda_w200;

    // ==========================================================================
    // 3. KYLE LAMBDA Z-Score zW 20
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_zsc_20")
    private double mic_kyle_lambda_w4_zsc_20;

    @Column("mic_kyle_lambda_w16_zsc_20")
    private double mic_kyle_lambda_w16_zsc_20;

    @Column("mic_kyle_lambda_w96_zsc_20")
    private double mic_kyle_lambda_w96_zsc_20;

    @Column("mic_kyle_lambda_w200_zsc_20")
    private double mic_kyle_lambda_w200_zsc_20;

    // ==========================================================================
    // 3. KYLE Relative (10, 40)
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_rel_10")
    private double mic_kyle_lambda_w4_rel_10;

    @Column("mic_kyle_lambda_w4_rel_40")
    private double mic_kyle_lambda_w4_rel_40;

    @Column("mic_kyle_lambda_w16_rel_10")
    private double mic_kyle_lambda_w16_rel_10;

    @Column("mic_kyle_lambda_w16_rel_40")
    private double mic_kyle_lambda_w16_rel_40;

    @Column("mic_kyle_lambda_w96_rel_10")
    private double mic_kyle_lambda_w96_rel_10;

    @Column("mic_kyle_lambda_w96_rel_40")
    private double mic_kyle_lambda_w96_rel_40;

    @Column("mic_kyle_lambda_w200_rel_10")
    private double mic_kyle_lambda_w200_rel_10;

    @Column("mic_kyle_lambda_w200_rel_40")
    private double mic_kyle_lambda_w200_rel_40;

    // ==========================================================================
    // 3. KYLE SLOPE (w4, w20, w50)
    // ==========================================================================
    // w4
    @Column("mic_kyle_lambda_w4_slp_w4")
    private double mic_kyle_lambda_w4_slp_w4;

    @Column("mic_kyle_lambda_w4_slp_w20")
    private double mic_kyle_lambda_w4_slp_w20;

    @Column("mic_kyle_lambda_w4_slp_w50")
    private double mic_kyle_lambda_w4_slp_w50;

    // w16
    @Column("mic_kyle_lambda_w16_slp_w4")
    private double mic_kyle_lambda_w16_slp_w4;

    @Column("mic_kyle_lambda_w16_slp_w20")
    private double mic_kyle_lambda_w16_slp_w20;

    @Column("mic_kyle_lambda_w16_slp_w50")
    private double mic_kyle_lambda_w16_slp_w50;

    // w96
    @Column("mic_kyle_lambda_w96_slp_w4")
    private double mic_kyle_lambda_w96_slp_w4;

    @Column("mic_kyle_lambda_w96_slp_w20")
    private double mic_kyle_lambda_w96_slp_w20;

    @Column("mic_kyle_lambda_w96_slp_w50")
    private double mic_kyle_lambda_w96_slp_w50;

    // w200
    @Column("mic_kyle_lambda_w200_slp_w4")
    private double mic_kyle_lambda_w200_slp_w4;

    @Column("mic_kyle_lambda_w200_slp_w20")
    private double mic_kyle_lambda_w200_slp_w20;

    @Column("mic_kyle_lambda_w200_slp_w50")
    private double mic_kyle_lambda_w200_slp_w50;

    // ==========================================================================
    // 3. KYLE ACC
    // ==========================================================================
    // w4
    @Column("mic_kyle_lambda_w4_acc_w4")
    private double mic_kyle_lambda_w4_acc_w4;

    @Column("mic_kyle_lambda_w4_acc_w5")
    private double mic_kyle_lambda_w4_acc_w5;

    @Column("mic_kyle_lambda_w4_acc_w10")
    private double mic_kyle_lambda_w4_acc_w10;

    @Column("mic_kyle_lambda_w4_acc_w16")
    private double mic_kyle_lambda_w4_acc_w16;

    // w16
    @Column("mic_kyle_lambda_w16_acc_w4")
    private double mic_kyle_lambda_w16_acc_w4;

    @Column("mic_kyle_lambda_w16_acc_w5")
    private double mic_kyle_lambda_w16_acc_w5;

    @Column("mic_kyle_lambda_w16_acc_w10")
    private double mic_kyle_lambda_w16_acc_w10;

    @Column("mic_kyle_lambda_w16_acc_w16")
    private double mic_kyle_lambda_w16_acc_w16;

    // w96
    @Column("mic_kyle_lambda_w96_acc_w4")
    private double mic_kyle_lambda_w96_acc_w4;

    @Column("mic_kyle_lambda_w96_acc_w5")
    private double mic_kyle_lambda_w96_acc_w5;

    @Column("mic_kyle_lambda_w96_acc_w10")
    private double mic_kyle_lambda_w96_acc_w10;

    @Column("mic_kyle_lambda_w96_acc_w16")
    private double mic_kyle_lambda_w96_acc_w16;

    // w200
    @Column("mic_kyle_lambda_w200_acc_w4")
    private double mic_kyle_lambda_w200_acc_w4;

    @Column("mic_kyle_lambda_w200_acc_w5")
    private double mic_kyle_lambda_w200_acc_w5;

    @Column("mic_kyle_lambda_w200_acc_w10")
    private double mic_kyle_lambda_w200_acc_w10;

    @Column("mic_kyle_lambda_w200_acc_w16")
    private double mic_kyle_lambda_w200_acc_w16;

    // ==========================================================================
    // 3. KYLE MOVING AVERAGE (10, 20, 30)
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_ma_10")
    private double mic_kyle_lambda_w4_ma_10;

    @Column("mic_kyle_lambda_w4_ma_20")
    private double mic_kyle_lambda_w4_ma_20;

    @Column("mic_kyle_lambda_w4_ma_30")
    private double mic_kyle_lambda_w4_ma_30;

    // w16
    @Column("mic_kyle_lambda_w16_ma_10")
    private double mic_kyle_lambda_w16_ma_10;

    @Column("mic_kyle_lambda_w16_ma_20")
    private double mic_kyle_lambda_w16_ma_20;

    @Column("mic_kyle_lambda_w16_ma_30")
    private double mic_kyle_lambda_w16_ma_30;

    // w96
    @Column("mic_kyle_lambda_w96_ma_10")
    private double mic_kyle_lambda_w96_ma_10;

    @Column("mic_kyle_lambda_w96_ma_20")
    private double mic_kyle_lambda_w96_ma_20;

    @Column("mic_kyle_lambda_w96_ma_30")
    private double mic_kyle_lambda_w96_ma_30;

    // w200
    @Column("mic_kyle_lambda_w200_ma_10")
    private double mic_kyle_lambda_w200_ma_10;

    @Column("mic_kyle_lambda_w200_ma_20")
    private double mic_kyle_lambda_w200_ma_20;

    @Column("mic_kyle_lambda_w200_ma_30")
    private double mic_kyle_lambda_w200_ma_30;

    // ==========================================================================
    // 3. KYLE VOLATILITY (10, 20, 40)
    // ==========================================================================
    // w4
    @Column("mic_kyle_lambda_w4_vol_10")
    private double mic_kyle_lambda_w4_vol_10;

    @Column("mic_kyle_lambda_w4_vol_20")
    private double mic_kyle_lambda_w4_vol_20;

    @Column("mic_kyle_lambda_w4_vol_40")
    private double mic_kyle_lambda_w4_vol_40;

    // w16
    @Column("mic_kyle_lambda_w16_vol_10")
    private double mic_kyle_lambda_w16_vol_10;

    @Column("mic_kyle_lambda_w16_vol_20")
    private double mic_kyle_lambda_w16_vol_20;

    @Column("mic_kyle_lambda_w16_vol_40")
    private double mic_kyle_lambda_w16_vol_40;

    // w96
    @Column("mic_kyle_lambda_w96_vol_10")
    private double mic_kyle_lambda_w96_vol_10;

    @Column("mic_kyle_lambda_w96_vol_20")
    private double mic_kyle_lambda_w96_vol_20;

    @Column("mic_kyle_lambda_w96_vol_40")
    private double mic_kyle_lambda_w96_vol_40;

    // w200
    @Column("mic_kyle_lambda_w200_vol_10")
    private double mic_kyle_lambda_w200_vol_10;

    @Column("mic_kyle_lambda_w200_vol_20")
    private double mic_kyle_lambda_w200_vol_20;

    @Column("mic_kyle_lambda_w200_vol_40")
    private double mic_kyle_lambda_w200_vol_40;

    // ==========================================================================
    // 3. KYLE VOL RELATIVE (vol10 / vol40)
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_vol_rel_40")
    private double mic_kyle_lambda_w4_vol_rel_40;

    @Column("mic_kyle_lambda_w16_vol_rel_40")
    private double mic_kyle_lambda_w16_vol_rel_40;

    @Column("mic_kyle_lambda_w96_vol_rel_40")
    private double mic_kyle_lambda_w96_vol_rel_40;

    @Column("mic_kyle_lambda_w200_vol_rel_40")
    private double mic_kyle_lambda_w200_vol_rel_40;

    // ==========================================================================
    // 3. KYLE PERSISTENCE (10, 20, 40)
    // ==========================================================================
    // w4
    @Column("mic_kyle_lambda_w4_prst_w10")
    private double mic_kyle_lambda_w4_prst_w10;

    @Column("mic_kyle_lambda_w4_prst_w20")
    private double mic_kyle_lambda_w4_prst_w20;

    @Column("mic_kyle_lambda_w4_prst_w40")
    private double mic_kyle_lambda_w4_prst_w40;

    // w16
    @Column("mic_kyle_lambda_w16_prst_w10")
    private double mic_kyle_lambda_w16_prst_w10;

    @Column("mic_kyle_lambda_w16_prst_w20")
    private double mic_kyle_lambda_w16_prst_w20;

    @Column("mic_kyle_lambda_w16_prst_w40")
    private double mic_kyle_lambda_w16_prst_w40;

    // w96
    @Column("mic_kyle_lambda_w96_prst_w10")
    private double mic_kyle_lambda_w96_prst_w10;

    @Column("mic_kyle_lambda_w96_prst_w20")
    private double mic_kyle_lambda_w96_prst_w20;

    @Column("mic_kyle_lambda_w96_prst_w40")
    private double mic_kyle_lambda_w96_prst_w40;

    // w200
    @Column("mic_kyle_lambda_w200_prst_w10")
    private double mic_kyle_lambda_w200_prst_w10;

    @Column("mic_kyle_lambda_w200_prst_w20")
    private double mic_kyle_lambda_w200_prst_w20;

    @Column("mic_kyle_lambda_w200_prst_w40")
    private double mic_kyle_lambda_w200_prst_w40;

    // ==========================================================================
    // 3. KYLE DIVERGENCE (RAW − MA20)
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_dvgc")
    private double mic_kyle_lambda_w4_dvgc;

    @Column("mic_kyle_lambda_w16_dvgc")
    private double mic_kyle_lambda_w16_dvgc;

    @Column("mic_kyle_lambda_w96_dvgc")
    private double mic_kyle_lambda_w96_dvgc;

    @Column("mic_kyle_lambda_w200_dvgc")
    private double mic_kyle_lambda_w200_dvgc;

    // ==========================================================================
    // 3. KYLE PERCENTILE (20)
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_pctile_w20")
    private double mic_kyle_lambda_w4_pctile_w20;

    @Column("mic_kyle_lambda_w16_pctile_w20")
    private double mic_kyle_lambda_w16_pctile_w20;

    @Column("mic_kyle_lambda_w96_pctile_w20")
    private double mic_kyle_lambda_w96_pctile_w20;

    @Column("mic_kyle_lambda_w200_pctile_w20")
    private double mic_kyle_lambda_w200_pctile_w20;

    // ==========================================================================
    // 3. KYLE LRMR (MA10 – MA40)
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_lrmr_10_40")
    private double mic_kyle_lambda_w4_lrmr_10_40;

    @Column("mic_kyle_lambda_w16_lrmr_10_40")
    private double mic_kyle_lambda_w16_lrmr_10_40;

    @Column("mic_kyle_lambda_w96_lrmr_10_40")
    private double mic_kyle_lambda_w96_lrmr_10_40;

    @Column("mic_kyle_lambda_w200_lrmr_10_40")
    private double mic_kyle_lambda_w200_lrmr_10_40;

    // ==========================================================================
    // 3. KYLE STABILITY (Vol40 / MA40)
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_stability_40")
    private double mic_kyle_lambda_w4_stability_40;

    @Column("mic_kyle_lambda_w16_stability_40")
    private double mic_kyle_lambda_w16_stability_40;

    @Column("mic_kyle_lambda_w96_stability_40")
    private double mic_kyle_lambda_w96_stability_40;

    @Column("mic_kyle_lambda_w200_stability_40")
    private double mic_kyle_lambda_w200_stability_40;

    // ==========================================================================
    // 3. KYLE SIGNED
    // ==========================================================================
    @Column("mic_kyle_lambda_signed")
    private double mic_kyle_lambda_signed;

    // ==========================================================================
    // 3. KYLE LAMBDA ATR-N (Regime Aware)
    // ==========================================================================
    @Column("mic_kyle_lambda_w4_atrn")
    private double mic_kyle_lambda_w4_atrn;

    @Column("mic_kyle_lambda_w16_atrn")
    private double mic_kyle_lambda_w16_atrn;

    @Column("mic_kyle_lambda_w96_atrn")
    private double mic_kyle_lambda_w96_atrn;

    @Column("mic_kyle_lambda_w200_atrn")
    private double mic_kyle_lambda_w200_atrn;

    // ==========================================================================
    // 3. KYLE LAMBDA (NOVAS BASES 24H)
    // ==========================================================================

    // --------------------------------------------------------------------------
    // BASE RAW
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48")
    private double mic_kyle_lambda_w48;

    @Column("mic_kyle_lambda_w288")
    private double mic_kyle_lambda_w288;

    // --------------------------------------------------------------------------
    // Z-SCORE (zW 20)
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48_zsc_20")
    private double mic_kyle_lambda_w48_zsc_20;

    @Column("mic_kyle_lambda_w288_zsc_20")
    private double mic_kyle_lambda_w288_zsc_20;

    // --------------------------------------------------------------------------
    // RELATIVE (10, 40)
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48_rel_10")
    private double mic_kyle_lambda_w48_rel_10;

    @Column("mic_kyle_lambda_w48_rel_40")
    private double mic_kyle_lambda_w48_rel_40;

    @Column("mic_kyle_lambda_w288_rel_10")
    private double mic_kyle_lambda_w288_rel_10;

    @Column("mic_kyle_lambda_w288_rel_40")
    private double mic_kyle_lambda_w288_rel_40;

    // --------------------------------------------------------------------------
    // SLOPE (w4, w20, w50)
    // --------------------------------------------------------------------------
    // w48
    @Column("mic_kyle_lambda_w48_slp_w4")
    private double mic_kyle_lambda_w48_slp_w4;

    @Column("mic_kyle_lambda_w48_slp_w20")
    private double mic_kyle_lambda_w48_slp_w20;

    @Column("mic_kyle_lambda_w48_slp_w50")
    private double mic_kyle_lambda_w48_slp_w50;

    // w288
    @Column("mic_kyle_lambda_w288_slp_w4")
    private double mic_kyle_lambda_w288_slp_w4;

    @Column("mic_kyle_lambda_w288_slp_w20")
    private double mic_kyle_lambda_w288_slp_w20;

    @Column("mic_kyle_lambda_w288_slp_w50")
    private double mic_kyle_lambda_w288_slp_w50;

    // --------------------------------------------------------------------------
    // ACC (w4, w5, w10, w16)
    // --------------------------------------------------------------------------
    // w48
    @Column("mic_kyle_lambda_w48_acc_w4")
    private double mic_kyle_lambda_w48_acc_w4;

    @Column("mic_kyle_lambda_w48_acc_w5")
    private double mic_kyle_lambda_w48_acc_w5;

    @Column("mic_kyle_lambda_w48_acc_w10")
    private double mic_kyle_lambda_w48_acc_w10;

    @Column("mic_kyle_lambda_w48_acc_w16")
    private double mic_kyle_lambda_w48_acc_w16;

    // w288
    @Column("mic_kyle_lambda_w288_acc_w4")
    private double mic_kyle_lambda_w288_acc_w4;

    @Column("mic_kyle_lambda_w288_acc_w5")
    private double mic_kyle_lambda_w288_acc_w5;

    @Column("mic_kyle_lambda_w288_acc_w10")
    private double mic_kyle_lambda_w288_acc_w10;

    @Column("mic_kyle_lambda_w288_acc_w16")
    private double mic_kyle_lambda_w288_acc_w16;

    // --------------------------------------------------------------------------
    // MOVING AVERAGE (10, 20, 30)
    // --------------------------------------------------------------------------
    // w48
    @Column("mic_kyle_lambda_w48_ma_10")
    private double mic_kyle_lambda_w48_ma_10;

    @Column("mic_kyle_lambda_w48_ma_20")
    private double mic_kyle_lambda_w48_ma_20;

    @Column("mic_kyle_lambda_w48_ma_30")
    private double mic_kyle_lambda_w48_ma_30;

    // w288
    @Column("mic_kyle_lambda_w288_ma_10")
    private double mic_kyle_lambda_w288_ma_10;

    @Column("mic_kyle_lambda_w288_ma_20")
    private double mic_kyle_lambda_w288_ma_20;

    @Column("mic_kyle_lambda_w288_ma_30")
    private double mic_kyle_lambda_w288_ma_30;

    // --------------------------------------------------------------------------
    // VOLATILITY (10, 20, 40)
    // --------------------------------------------------------------------------
    // w48
    @Column("mic_kyle_lambda_w48_vol_10")
    private double mic_kyle_lambda_w48_vol_10;

    @Column("mic_kyle_lambda_w48_vol_20")
    private double mic_kyle_lambda_w48_vol_20;

    @Column("mic_kyle_lambda_w48_vol_40")
    private double mic_kyle_lambda_w48_vol_40;

    // w288
    @Column("mic_kyle_lambda_w288_vol_10")
    private double mic_kyle_lambda_w288_vol_10;

    @Column("mic_kyle_lambda_w288_vol_20")
    private double mic_kyle_lambda_w288_vol_20;

    @Column("mic_kyle_lambda_w288_vol_40")
    private double mic_kyle_lambda_w288_vol_40;

    // --------------------------------------------------------------------------
    // VOL RELATIVE (vol10 / vol40)
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48_vol_rel_40")
    private double mic_kyle_lambda_w48_vol_rel_40;

    @Column("mic_kyle_lambda_w288_vol_rel_40")
    private double mic_kyle_lambda_w288_vol_rel_40;

    // --------------------------------------------------------------------------
    // PERSISTENCE (10, 20, 40)
    // --------------------------------------------------------------------------
    // w48
    @Column("mic_kyle_lambda_w48_prst_w10")
    private double mic_kyle_lambda_w48_prst_w10;

    @Column("mic_kyle_lambda_w48_prst_w20")
    private double mic_kyle_lambda_w48_prst_w20;

    @Column("mic_kyle_lambda_w48_prst_w40")
    private double mic_kyle_lambda_w48_prst_w40;

    // w288
    @Column("mic_kyle_lambda_w288_prst_w10")
    private double mic_kyle_lambda_w288_prst_w10;

    @Column("mic_kyle_lambda_w288_prst_w20")
    private double mic_kyle_lambda_w288_prst_w20;

    @Column("mic_kyle_lambda_w288_prst_w40")
    private double mic_kyle_lambda_w288_prst_w40;

    // --------------------------------------------------------------------------
    // DIVERGENCE (RAW − MA20)
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48_dvgc")
    private double mic_kyle_lambda_w48_dvgc;

    @Column("mic_kyle_lambda_w288_dvgc")
    private double mic_kyle_lambda_w288_dvgc;

    // --------------------------------------------------------------------------
    // PERCENTILE (20)
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48_pctile_w20")
    private double mic_kyle_lambda_w48_pctile_w20;

    @Column("mic_kyle_lambda_w288_pctile_w20")
    private double mic_kyle_lambda_w288_pctile_w20;

    // --------------------------------------------------------------------------
    // LRMR (MA10 − MA40)
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48_lrmr_10_40")
    private double mic_kyle_lambda_w48_lrmr_10_40;

    @Column("mic_kyle_lambda_w288_lrmr_10_40")
    private double mic_kyle_lambda_w288_lrmr_10_40;

    // --------------------------------------------------------------------------
    // STABILITY (Vol40 / MA40)
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48_stability_40")
    private double mic_kyle_lambda_w48_stability_40;

    @Column("mic_kyle_lambda_w288_stability_40")
    private double mic_kyle_lambda_w288_stability_40;

    // --------------------------------------------------------------------------
    // ATR-N (Regime Aware)
    // --------------------------------------------------------------------------
    @Column("mic_kyle_lambda_w48_atrn")
    private double mic_kyle_lambda_w48_atrn;

    @Column("mic_kyle_lambda_w288_atrn")
    private double mic_kyle_lambda_w288_atrn;


    // ==========================================================================
    // 4. HASBROUCK LAMBDA — V3 (Price Discovery / Absorption Filter)
    // ==========================================================================
    // --------------------------------------------------------------------------
    // HASBROUCK RAW (Structural Price Discovery)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16")
    private double mic_hasb_lambda_w16;

    @Column("mic_hasb_lambda_w64")
    private double mic_hasb_lambda_w64;

    // --------------------------------------------------------------------------
    // HASBROUCK ATR-N (Regime Aware Price Discovery)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16_atrn")
    private double mic_hasb_lambda_w16_atrn;

    @Column("mic_hasb_lambda_w64_atrn")
    private double mic_hasb_lambda_w64_atrn;

    // --------------------------------------------------------------------------
    // HASBROUCK Z-SCORE (Structural Extremes)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16_zsc_40")
    private double mic_hasb_lambda_w16_zsc_40;

    @Column("mic_hasb_lambda_w64_zsc_40")
    private double mic_hasb_lambda_w64_zsc_40;

    // --------------------------------------------------------------------------
    // HASBROUCK MOVING AVERAGE (Structural Smoothing)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16_ma_20")
    private double mic_hasb_lambda_w16_ma_20;

    @Column("mic_hasb_lambda_w64_ma_20")
    private double mic_hasb_lambda_w64_ma_20;

    // --------------------------------------------------------------------------
    // HASBROUCK DIVERGENCE (Price vs Flow Mismatch)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16_dvgc")
    private double mic_hasb_lambda_w16_dvgc;

    @Column("mic_hasb_lambda_w64_dvgc")
    private double mic_hasb_lambda_w64_dvgc;

    // --------------------------------------------------------------------------
    // HASBROUCK RAW (Structural Price Discovery) — 24H BASES
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w48")
    private double mic_hasb_lambda_w48;

    @Column("mic_hasb_lambda_w288")
    private double mic_hasb_lambda_w288;

    // --------------------------------------------------------------------------
    // HASBROUCK ATR-N (Regime Aware Price Discovery)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w48_atrn")
    private double mic_hasb_lambda_w48_atrn;

    @Column("mic_hasb_lambda_w288_atrn")
    private double mic_hasb_lambda_w288_atrn;

    // --------------------------------------------------------------------------
    // HASBROUCK Z-SCORE (Structural Extremes)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w48_zsc_40")
    private double mic_hasb_lambda_w48_zsc_40;

    @Column("mic_hasb_lambda_w288_zsc_40")
    private double mic_hasb_lambda_w288_zsc_40;

    // --------------------------------------------------------------------------
    // HASBROUCK MOVING AVERAGE (Structural Smoothing)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w48_ma_20")
    private double mic_hasb_lambda_w48_ma_20;

    @Column("mic_hasb_lambda_w288_ma_20")
    private double mic_hasb_lambda_w288_ma_20;

    // --------------------------------------------------------------------------
    // HASBROUCK DIVERGENCE (Price vs Flow Mismatch)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w48_dvgc")
    private double mic_hasb_lambda_w48_dvgc;

    @Column("mic_hasb_lambda_w288_dvgc")
    private double mic_hasb_lambda_w288_dvgc;

    // ==========================================================================
    // 4. HASBROUCK — EXTENSÕES (V3)
    // ==========================================================================

    // --------------------------------------------------------------------------
    // HASBROUCK SLOPE (Trend of Price Discovery)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16_slp_w20")
    private double mic_hasb_lambda_w16_slp_w20;

    @Column("mic_hasb_lambda_w64_slp_w20")
    private double mic_hasb_lambda_w64_slp_w20;

    // --------------------------------------------------------------------------
    // HASBROUCK VOLATILITY (Stability of Discovery) — STD40
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16_vol_40")
    private double mic_hasb_lambda_w16_vol_40;

    @Column("mic_hasb_lambda_w64_vol_40")
    private double mic_hasb_lambda_w64_vol_40;

    // --------------------------------------------------------------------------
    // HASBROUCK STABILITY (Vol40 / MA40)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16_stability_40")
    private double mic_hasb_lambda_w16_stability_40;

    @Column("mic_hasb_lambda_w64_stability_40")
    private double mic_hasb_lambda_w64_stability_40;

    // --------------------------------------------------------------------------
    // HASBROUCK PERCENTILE (Structural Extremes) — PCTILE40
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w16_pctile_w40")
    private double mic_hasb_lambda_w16_pctile_w40;

    @Column("mic_hasb_lambda_w64_pctile_w40")
    private double mic_hasb_lambda_w64_pctile_w40;

    // --------------------------------------------------------------------------
    // HASBROUCK / KYLE RATIO (Permanent vs Transitory Impact)
    // --------------------------------------------------------------------------
    @Column("mic_hasb_to_kyle_ratio_w16")
    private double mic_hasb_to_kyle_ratio_w16;

    @Column("mic_hasb_to_kyle_ratio_w64")
    private double mic_hasb_to_kyle_ratio_w64;

    // --------------------------------------------------------------------------
    // HASBROUCK SLOPE — 24H BASES
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w48_slp_w20")
    private double mic_hasb_lambda_w48_slp_w20;

    @Column("mic_hasb_lambda_w288_slp_w20")
    private double mic_hasb_lambda_w288_slp_w20;

    // --------------------------------------------------------------------------
    // HASBROUCK VOL / STABILITY — 24H BASES
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w48_vol_40")
    private double mic_hasb_lambda_w48_vol_40;

    @Column("mic_hasb_lambda_w288_vol_40")
    private double mic_hasb_lambda_w288_vol_40;

    @Column("mic_hasb_lambda_w48_stability_40")
    private double mic_hasb_lambda_w48_stability_40;

    @Column("mic_hasb_lambda_w288_stability_40")
    private double mic_hasb_lambda_w288_stability_40;

    // --------------------------------------------------------------------------
    // HASBROUCK PERCENTILE — 24H BASES
    // --------------------------------------------------------------------------
    @Column("mic_hasb_lambda_w48_pctile_w40")
    private double mic_hasb_lambda_w48_pctile_w40;

    @Column("mic_hasb_lambda_w288_pctile_w40")
    private double mic_hasb_lambda_w288_pctile_w40;

    // --------------------------------------------------------------------------
    // HASBROUCK / KYLE RATIO — 24H BASES
    // --------------------------------------------------------------------------
    @Column("mic_hasb_to_kyle_ratio_w48")
    private double mic_hasb_to_kyle_ratio_w48;

    @Column("mic_hasb_to_kyle_ratio_w288")
    private double mic_hasb_to_kyle_ratio_w288;







    // ==========================================================================
    // 5. RANGE & AMPLITUDE (Microestrutura de Preço)
    // ==========================================================================
    // --------------------------------------------------------------------------
    // BASE RANGE & LEVEL
    // --------------------------------------------------------------------------
    @Column("mic_range")
    private double mic_range;

    @Column("mic_log_range")
    private double mic_log_range;

    @Column("mic_hlc3")
    private double mic_hlc3;

    // --------------------------------------------------------------------------
    // RANGE NORMALIZED (ATR / STD / RATIOS)
    // --------------------------------------------------------------------------
    @Column("mic_range_atrn")
    private double mic_range_atrn;

    @Column("mic_range_stdn")
    private double mic_range_stdn;

    @Column("mic_range_atr_ratio")
    private double mic_range_atr_ratio;

    // --------------------------------------------------------------------------
    // RANGE DYNAMICS (SLOPE / ACCELERATION)
    // --------------------------------------------------------------------------
    @Column("mic_range_slp_w10")
    private double mic_range_slp_w10;

    @Column("mic_range_slp_w20")
    private double mic_range_slp_w20;

    @Column("mic_range_acc_w5")
    private double mic_range_acc_w5;

    @Column("mic_range_acc_w10")
    private double mic_range_acc_w10;

    // --------------------------------------------------------------------------
    // RANGE MOVING AVERAGES (COMPRESSION / EXPANSION)
    // --------------------------------------------------------------------------
    @Column("mic_range_ma_10")
    private double mic_range_ma_10;

    @Column("mic_range_ma_20")
    private double mic_range_ma_20;

    @Column("mic_range_ma_30")
    private double mic_range_ma_30;

    // --------------------------------------------------------------------------
    // RANGE VOLATILITY & COMPRESSION
    // --------------------------------------------------------------------------
    @Column("mic_range_vol_10")
    private double mic_range_vol_10;

    @Column("mic_range_vol_20")
    private double mic_range_vol_20;

    @Column("mic_range_compression_w20")
    private double mic_range_compression_w20;

    // --------------------------------------------------------------------------
    // HLC3 & LOG RANGE DERIVATIVES
    // --------------------------------------------------------------------------
    @Column("mic_hlc3_slp_w20")
    private double mic_hlc3_slp_w20;

    @Column("mic_hlc3_ma_10")
    private double mic_hlc3_ma_10;

    @Column("mic_hlc3_ma_20")
    private double mic_hlc3_ma_20;

    @Column("mic_hlc3_vol_10")
    private double mic_hlc3_vol_10;

    @Column("mic_log_range_slp_w20")
    private double mic_log_range_slp_w20;

    @Column("mic_log_range_vol_10")
    private double mic_log_range_vol_10;

    @Column("mic_log_range_ma_10")
    private double mic_log_range_ma_10;

    // --------------------------------------------------------------------------
    // RANGE-BASED RETURNS (LAST CANDLE)
    // --------------------------------------------------------------------------
    @Column("mic_range_return")
    private double mic_range_return;

    @Column("mic_high_return")
    private double mic_high_return;

    @Column("mic_low_return")
    private double mic_low_return;

    @Column("mic_extreme_range_return")
    private double mic_extreme_range_return;

    // --------------------------------------------------------------------------
    // CANDLE RANGE MICROSTRUCTURE (GEOMETRY-ORIENTED)
    // --------------------------------------------------------------------------
    @Column("mic_candle_range")
    private double mic_candle_range;

    @Column("mic_candle_volatility_inside")
    private double mic_candle_volatility_inside;

    @Column("mic_candle_spread_ratio")
    private double mic_candle_spread_ratio;

    @Column("mic_candle_brr")
    private double mic_candle_brr;

    @Column("mic_candle_lmr")
    private double mic_candle_lmr;

    // --------------------------------------------------------------------------
    // TRUE RANGE (operational range, gap-aware)
    // --------------------------------------------------------------------------
    @Column("mic_true_range")
    private double mic_true_range;

    // --------------------------------------------------------------------------
    // TRUE RANGE NORMALIZED (ATR-N)
    // --------------------------------------------------------------------------
    @Column("mic_tr_atrn")
    private double mic_tr_atrn;

    // --------------------------------------------------------------------------
    // RANGE SQUEEZE / REGIME DE COMPRESSÃO
    // --------------------------------------------------------------------------
    // Mede compressão/expansão relativa do range atual vs regime recente
    // (range / MA(range, 20))
    @Column("mic_range_squeeze_w20")
    private double mic_range_squeeze_w20;

    // --------------------------------------------------------------------------
    // RANGE ASYMMETRY (direcionalidade geométrica do espaço)
    // --------------------------------------------------------------------------
    // Captura se o range foi consumido mais acima ou abaixo do close
    // Ex: (high - close) / (close - low)
    @Column("mic_range_asymmetry")
    private double mic_range_asymmetry;

    // --------------------------------------------------------------------------
    // RANGE HEADROOM (potencial restante vs ATR)
    // --------------------------------------------------------------------------
    // Mede quanto espaço ainda existe antes de “esgotar” o ATR
    // Ex: (ATR14 - range) / ATR14
    @Column("mic_range_headroom_atr")
    private double mic_range_headroom_atr;

    // --------------------------------------------------------------------------
    // RANGE GAPINESS / SHOCK SCORE (gap-aware shock)
    // (true_range - range) / (true_range + eps)
    // --------------------------------------------------------------------------
    @Column("mic_gap_ratio")
    private double mic_gap_ratio;

    // --------------------------------------------------------------------------
    // TRUE RANGE / RANGE RATIO (gap dominance)
    // true_range / (range + eps)
    // --------------------------------------------------------------------------
    @Column("mic_tr_range_ratio")
    private double mic_tr_range_ratio;

    // --------------------------------------------------------------------------
    // LOG RANGE PERCENTILE (structural extreme)
    // percentile_rank(log_range, window=48)  // default: w48 (ajustável por TF)
    // --------------------------------------------------------------------------
    @Column("mic_log_range_pctile_w48")
    private double mic_log_range_pctile_w48;

    // --------------------------------------------------------------------------
    // RANGE REGIME STATE (discrete regime classifier)
    // 0=COMPRESSED, 1=NORMAL, 2=EXPANDED, 3=SHOCK
    // derived from squeeze/compression/atrn/gap_ratio
    // --------------------------------------------------------------------------
    @Column("mic_range_regime_state")
    private double mic_range_regime_state;

    // ==========================================================================
    // 6. BODY MICROSTRUCTURE (corpo, força, energia)
    // ==========================================================================
    // --------------------------------------------------------------------------
    // BODY CORE (single candle)
    // --------------------------------------------------------------------------
    @Column("mic_candle_body")
    private double mic_candle_body;              // close - open

    @Column("mic_candle_body_abs")
    private double mic_candle_body_abs;          // |close - open|

    @Column("mic_candle_body_pct")
    private double mic_candle_body_pct;          // body / range

    @Column("mic_candle_body_ratio")
    private double mic_candle_body_ratio;        // variação alternativa de body vs range (mantido por compat.)

    @Column("mic_body_ratio")
    private double mic_body_ratio;               // versão "mic_" já existente (mantida para compat.)

    @Column("mic_body_perc")
    private double mic_body_perc;                // % do candle que é corpo (price-oscillatory)

    // --------------------------------------------------------------------------
    // BODY POSITION (onde o corpo está dentro do candle)
    // --------------------------------------------------------------------------
    @Column("mic_candle_body_center_position")
    private double mic_candle_body_center_position; // posição do centro do corpo em relação ao range

    // --------------------------------------------------------------------------
    // BODY PRESSURE & STRENGTH (força interna do candle)
    // --------------------------------------------------------------------------
    @Column("mic_candle_pressure_raw")
    private double mic_candle_pressure_raw;      // proxy de pressão direcional do corpo

    @Column("mic_candle_strength")
    private double mic_candle_strength;          // força agregada (body + posição + direção)

    @Column("mic_candle_body_strength_score")
    private double mic_candle_body_strength_score; // score numérico de força do corpo

    // --------------------------------------------------------------------------
    // BODY ENERGY (bruta e normalizada por ATR)
    // --------------------------------------------------------------------------
    @Column("mic_candle_energy_raw")
    private double mic_candle_energy_raw;        // energia geométrica do candle (corpo + range)

    @Column("mic_candle_energy_atrn")
    private double mic_candle_energy_atrn;       // energia normalizada por ATR

    // --------------------------------------------------------------------------
    // BODY NORMALIZED / RATIOS (regime-aware)
    // --------------------------------------------------------------------------
    @Column("mic_body_atr_ratio")
    private double mic_body_atr_ratio;           // body / ATR

    // --------------------------------------------------------------------------
    // BODY DYNAMICS (SLOPE / MA / VOL) – V2 em janela
    // --------------------------------------------------------------------------
    @Column("mic_candle_body_slp_w10")
    private double mic_candle_body_slp_w10;      // slope do corpo nos últimos 10 candles

    @Column("mic_candle_body_slp_w20")
    private double mic_candle_body_slp_w20;      // slope do corpo nos últimos 20 candles

    @Column("mic_candle_body_ma_10")
    private double mic_candle_body_ma_10;        // média do corpo em 10 candles

    @Column("mic_candle_body_ma_20")
    private double mic_candle_body_ma_20;        // média do corpo em 20 candles

    @Column("mic_candle_body_vol_10")
    private double mic_candle_body_vol_10;       // std do corpo em 10 candles

    @Column("mic_candle_body_vol_20")
    private double mic_candle_body_vol_20;       // std do corpo em 20 candles

    // --------------------------------------------------------------------------
    // BODY RATIO DYNAMICS (quão “cheio” o candle está ficando)
    // --------------------------------------------------------------------------
    @Column("mic_body_ratio_slp_w10")
    private double mic_body_ratio_slp_w10;       // slope de mic_body_ratio em 10 candles

    @Column("mic_body_ratio_vol_10")
    private double mic_body_ratio_vol_10;        // std de mic_body_ratio em 10 candles

    // --------------------------------------------------------------------------
    // BODY RETURNS (single-candle geometry return)
    // --------------------------------------------------------------------------
    @Column("mic_body_return")
    private double mic_body_return;              // retorno associado ao corpo do candle

    // ==========================================================================
// 6. BODY MICROSTRUCTURE — EXTENSÕES V3 (sem duplicar WICKS)
// ==========================================================================

    // --------------------------------------------------------------------------
// BODY SHOCK (extremo do corpo vs ATR)
// --------------------------------------------------------------------------
    @Column("mic_body_shock_atrn")
    private double mic_body_shock_atrn;             // |body| / (ATR14 + eps)

    // --------------------------------------------------------------------------
// BODY DIRECTION PERSISTENCE / RUN
// --------------------------------------------------------------------------
    @Column("mic_body_sign_prst_w20")
    private double mic_body_sign_prst_w20;          // % de candles com body>0 nos últimos 20

    @Column("mic_body_run_len")
    private double mic_body_run_len;                // comprimento da sequência atual (signed: +up / -down)








    // ==========================================================================
    // 3. WICKS & SHADOWS MICROSTRUCTURE (pavio, sombra, assimetria)
    // ==========================================================================

    // --------------------------------------------------------------------------
    // WICK CORE (single candle - tamanho absoluto e relativo)
    // --------------------------------------------------------------------------
    @Column("mic_candle_upper_wick")
    private double mic_candle_upper_wick;              // high - max(open, close)

    @Column("mic_candle_lower_wick")
    private double mic_candle_lower_wick;              // min(open, close) - low

    @Column("mic_candle_upper_wick_pct")
    private double mic_candle_upper_wick_pct;          // upper_wick / range

    @Column("mic_candle_lower_wick_pct")
    private double mic_candle_lower_wick_pct;          // lower_wick / range

    // Versões "mic_" oscilatórias mantidas para compatibilidade
    @Column("mic_wick_perc_up")
    private double mic_wick_perc_up;                   // % pavio superior

    @Column("mic_wick_perc_down")
    private double mic_wick_perc_down;                 // % pavio inferior

    // --------------------------------------------------------------------------
    // WICK IMBALANCE & SHADOW STRUCTURE
    // --------------------------------------------------------------------------
    @Column("mic_candle_wick_imbalance")
    private double mic_candle_wick_imbalance;          // (upper - lower) / range

    @Column("mic_wick_imbalance")
    private double mic_wick_imbalance;                 // versão "mic_" equivalente

    @Column("mic_candle_wick_pressure_score")
    private double mic_candle_wick_pressure_score;     // score de pressão via pavios

    @Column("mic_candle_shadow_ratio")
    private double mic_candle_shadow_ratio;            // sombras vs corpo/range

    @Column("mic_candle_wick_body_alignment")
    private double mic_candle_wick_body_alignment;     // alinhamento pavio ↔ corpo

    @Column("mic_shadow_imbalance_score")
    private double mic_shadow_imbalance_score;         // score agregado de sombras

    // --------------------------------------------------------------------------
    // WICK DYNAMICS (V2: regime em janela)
    // --------------------------------------------------------------------------
    @Column("mic_candle_wick_imbalance_slp_w10")
    private double mic_candle_wick_imbalance_slp_w10;  // slope da wick_imbalance em 10 candles

    @Column("mic_candle_wick_imbalance_vol_10")
    private double mic_candle_wick_imbalance_vol_10;   // std da wick_imbalance em 10 candles

    @Column("mic_candle_upper_wick_ma_10")
    private double mic_candle_upper_wick_ma_10;        // média do pavio superior em 10 candles

    @Column("mic_candle_lower_wick_ma_10")
    private double mic_candle_lower_wick_ma_10;        // média do pavio inferior em 10 candles

    // --------------------------------------------------------------------------
    // WICK-BASED RETURNS (single-candle geometry returns)
    // --------------------------------------------------------------------------
    @Column("mic_upper_wick_return")
    private double mic_upper_wick_return;              // retorno associado ao pavio superior

    @Column("mic_lower_wick_return")
    private double mic_lower_wick_return;              // retorno associado ao pavio inferior

    @Column("mic_candle_wick_dominance")
    private double mic_candle_wick_dominance;

    @Column("mic_candle_wick_exhaustion")
    private double mic_candle_wick_exhaustion;

    // ==========================================================================
    // 3. WICKS & SHADOWS — EXTENSÕES V3 (cirúrgicas)
    // ==========================================================================

    // --------------------------------------------------------------------------
    // TOTAL WICK (indecisão / absorção) — absoluto e relativo
    // --------------------------------------------------------------------------
    @Column("mic_candle_total_wick")
    private double mic_candle_total_wick;              // upper_wick + lower_wick

    @Column("mic_candle_total_wick_pct")
    private double mic_candle_total_wick_pct;          // (upper+lower) / range

    // --------------------------------------------------------------------------
    // TOTAL WICK ATR-N (regime aware)
    // --------------------------------------------------------------------------
    @Column("mic_candle_total_wick_atrn")
    private double mic_candle_total_wick_atrn;         // (upper+lower) / ATR14

    // --------------------------------------------------------------------------
    // WICK IMBALANCE NORMALIZED BY WICKS (directional purity)
    // --------------------------------------------------------------------------
    @Column("mic_candle_wick_imbalance_norm")
    private double mic_candle_wick_imbalance_norm;     // (upper-lower) / (upper+lower+eps)

    // --------------------------------------------------------------------------
    // CLOSE POSITION SLOPE (micro-momentum posicional)
    // --------------------------------------------------------------------------
    @Column("mic_close_pos_slp_w20")
    private double mic_close_pos_slp_w20;              // slope(close_pos_norm, 20)

    // ==========================================================================
    // 4. POSITION & BALANCE MICROSTRUCTURE
    // ==========================================================================
    // --------------------------------------------------------------------------
    // CORE POSITION (single candle)
    // --------------------------------------------------------------------------

    // relação entre close e open (nível bruto)
    @Column("mic_close_open_ratio")
    private double mic_close_open_ratio;              // close / open

    // versão normalizada / oscilatória
    @Column("mic_close_open_norm")
    private double mic_close_open_norm;               // (close - open) / open

    // posição do close dentro do range [low, high] (versão mic_)
    @Column("mic_close_pos_norm")
    private double mic_close_pos_norm;                // (close - low) / range

    // mesma ideia, mas no bloco candle geometry
    @Column("mic_candle_close_pos_norm")
    private double mic_candle_close_pos_norm;         // (close - low) / range (versão geometry)

    // --------------------------------------------------------------------------
    // POSITION VS REFERENCES (HLC3, VWAP, TRIANGLE)
    // --------------------------------------------------------------------------

    // desvio do close em relação ao HLC3
    @Column("mic_close_hlc3_delta")
    private double mic_close_hlc3_delta;              // close - HLC3

    // desvio do close em relação ao VWAP
    @Column("mic_close_vwap_delta")
    private double mic_close_vwap_delta;              // close - VWAP

    // score de "posição triangular" do close (geometricamente definido)
    @Column("mic_close_triangle_score")
    private double mic_close_triangle_score;          // score de posição do close em estrutura de triângulo

    // --------------------------------------------------------------------------
    // BALANCE SCORE (quanto o candle está equilibrado ou pendendo)
    // --------------------------------------------------------------------------

    // score de balanceamento entre corpo/pavios/posição
    @Column("mic_candle_balance_score")
    private double mic_candle_balance_score;

    // --------------------------------------------------------------------------
    // POSITION & BALANCE DYNAMICS (V2 - WINDOW FEATURES)
    // --------------------------------------------------------------------------

    // média da posição normalizada do close em 10 candles
    @Column("mic_close_pos_ma_10")
    private double mic_close_pos_ma_10;

    // volatilidade da posição normalizada do close em 10 candles
    @Column("mic_close_pos_vol_10")
    private double mic_close_pos_vol_10;

    // média do close_open_ratio em 10 candles
    @Column("mic_close_open_ratio_ma_10")
    private double mic_close_open_ratio_ma_10;

    // volatilidade do close_open_ratio em 10 candles
    @Column("mic_close_open_ratio_vol_10")
    private double mic_close_open_ratio_vol_10;

    // média do balance_score em 10 candles
    @Column("mic_candle_balance_ma_10")
    private double mic_candle_balance_ma_10;

    // volatilidade do balance_score em 10 candles
    @Column("mic_candle_balance_vol_10")
    private double mic_candle_balance_vol_10;

    @Column("mic_close_pos_zscore_20")
    private double mic_close_pos_zscore_20;

    @Column("mic_candle_balance_zscore_20")
    private double mic_candle_balance_zscore_20;

    @Column("mic_close_hlc3_atrn")
    private double mic_close_hlc3_atrn;

    @Column("mic_close_vwap_atrn")
    private double mic_close_vwap_atrn;

    // ==========================================================================
// 4. POSITION & BALANCE — EXTENSÕES V3 (cirúrgicas)
// ==========================================================================

    // --------------------------------------------------------------------------
// CLOSE TO EXTREMES (top/bottom pressure)
// --------------------------------------------------------------------------
    @Column("mic_close_to_high_norm")
    private double mic_close_to_high_norm;            // (high - close) / range

    @Column("mic_close_to_low_norm")
    private double mic_close_to_low_norm;             // (close - low) / range

    // --------------------------------------------------------------------------
// BALANCE STATE (discreto)
// 0=NEUTRAL, 1=BULL_CONTROL, 2=BEAR_CONTROL, 3=INDECISION/EXHAUSTION
// --------------------------------------------------------------------------
    @Column("mic_balance_state")
    private double mic_balance_state;

    // --------------------------------------------------------------------------
// TRIANGLE SCORE (regime-aware / ATR-N)
// --------------------------------------------------------------------------
    @Column("mic_close_triangle_score_atrn")
    private double mic_close_triangle_score_atrn;     // triangle_score / ATR (ou regra equivalente)





    // ==========================================================================
    // 5. SHAPE & PATTERN MICROSTRUCTURE
    // ==========================================================================

    // --------------------------------------------------------------------------
    // SHAPE & PATTERN CORE (single candle)
    // --------------------------------------------------------------------------

    // tipo de candle: ex. 0=neutro, 1=martelo, 2=shooting, etc. (codificado)
    @Column("mic_candle_type")
    private double mic_candle_type;

    // direção do candle: ex. +1=alta, -1=baixa, 0=doji/neutro
    @Column("mic_candle_direction")
    private double mic_candle_direction;

    // índice global de forma do candle (combina corpo/pavio/posição)
    @Column("mic_candle_shape_index")
    private double mic_candle_shape_index;

    // quão simétrico é o candle (corpo + pavios)
    @Column("mic_candle_symmetry_score")
    private double mic_candle_symmetry_score;

    // score geométrico baseado em “triângulo”/estrutura
    @Column("mic_candle_triangle_score")
    private double mic_candle_triangle_score;

    // score global de geometria (resumo de forma, corpo, pavios, posição)
    @Column("mic_candle_geometry_score")
    private double mic_candle_geometry_score;

    // entropia do candle (quão “informativo” ou “difuso” é o desenho)
    @Column("mic_candle_entropy")
    private double mic_candle_entropy;

    // índice de compressão da forma do candle (range interno, overlap, etc.)
    @Column("mic_candle_compression_index")
    private double mic_candle_compression_index;

    // --------------------------------------------------------------------------
    // SHAPE & PATTERN DYNAMICS (V2 — WINDOW FEATURES)
    // --------------------------------------------------------------------------

    // persistência da direção do candle (quantos candles seguidos na mesma direção)
    @Column("mic_candle_direction_prst_w10")
    private double mic_candle_direction_prst_w10;

    @Column("mic_candle_direction_prst_w20")
    private double mic_candle_direction_prst_w20;

    // média do geometry score em janela (regime de “candles bonitos/fortes”)
    @Column("mic_candle_geometry_ma_10")
    private double mic_candle_geometry_ma_10;

    // slope do geometry score em janela (forma ficando mais “tendencial” ou neutra)
    @Column("mic_candle_geometry_slp_w20")
    private double mic_candle_geometry_slp_w20;

    // volatilidade do geometry score (instabilidade de forma)
    @Column("mic_candle_geometry_vol_10")
    private double mic_candle_geometry_vol_10;

    // média do shape index em janela (regime de padrões específicos)
    @Column("mic_candle_shape_index_ma_20")
    private double mic_candle_shape_index_ma_20;

    // volatilidade do shape index (mudança frequente de padrão vs regime estável)
    @Column("mic_candle_shape_index_vol_20")
    private double mic_candle_shape_index_vol_20;

    // média do compression index em janela (períodos longos de compressão)
    @Column("mic_candle_compression_ma_20")
    private double mic_candle_compression_ma_20;

    // z-score do compression index (compressão atual vs regime recente)
    @Column("mic_candle_compression_zscore_20")
    private double mic_candle_compression_zscore_20;

    // ==========================================================================
    // 5. SHAPE & PATTERN — EXTENSÕES V3 (cirúrgicas)
    // ==========================================================================

    // --------------------------------------------------------------------------
    // SHAPE REGIME STATE (discreto)
    // 0=CHOP/INDECISION, 1=TREND_CLEAN, 2=REVERSAL_RISK, 3=SHOCK/IMPULSE
    // --------------------------------------------------------------------------
    @Column("mic_candle_shape_regime_state")
    private double mic_candle_shape_regime_state;

    // --------------------------------------------------------------------------
    // DOJI / INDECISION SCORE (0..1)
    // --------------------------------------------------------------------------
    @Column("mic_candle_doji_score")
    private double mic_candle_doji_score;

    // --------------------------------------------------------------------------
    // IMPULSE SCORE (0..1)
    // --------------------------------------------------------------------------
    @Column("mic_candle_impulse_score")
    private double mic_candle_impulse_score;

    // --------------------------------------------------------------------------
    // PATTERN TRANSITION RATE (regime stability)
    // --------------------------------------------------------------------------
    @Column("mic_candle_type_flip_rate_w20")
    private double mic_candle_type_flip_rate_w20;

    @Column("mic_candle_direction_flip_rate_w20")
    private double mic_candle_direction_flip_rate_w20;

    // --------------------------------------------------------------------------
    // 24H NATIVE WINDOWS (para 30m: w48)
    // --------------------------------------------------------------------------
    @Column("mic_candle_geometry_ma_48")
    private double mic_candle_geometry_ma_48;

    @Column("mic_candle_geometry_vol_48")
    private double mic_candle_geometry_vol_48;

    @Column("mic_candle_compression_ma_48")
    private double mic_candle_compression_ma_48;


    // ==========================================================================
    // 6. SINGLE-CANDLE RETURNS (Return 1C Microstructure)
    // ==========================================================================
    // --------------------------------------------------------------------------
    // CORE RETURNS (último candle)
    // --------------------------------------------------------------------------

    // retorno simples do close (close_t / close_{t-1} - 1)
    @Column("mic_return")
    private double mic_return;

    // log-return = log(close_t / close_{t-1})
    @Column("mic_return_log")
    private double mic_return_log;

    // retorno normalizado por ATR (regime-aware)
    @Column("mic_return_atrn")
    private double mic_return_atrn;

    // retorno normalizado por desvio padrão (regime-aware)
    @Column("mic_return_stdn")
    private double mic_return_stdn;

    // direção do retorno: +1, -1 ou 0
    @Column("mic_return_direction")
    private double mic_return_direction;

    @Column("mic_log_return_dominance")
    private double mic_log_return_dominance;

    // --------------------------------------------------------------------------
    // RETURN DYNAMICS (força, aceleração, reversão)
    // --------------------------------------------------------------------------

    // aceleração do retorno: retorno_t - retorno_{t-k} (k pequeno, ex. 1)
    @Column("mic_return_acceleration")
    private double mic_return_acceleration;

    // força de reversão: captura mudança de sinal ou reversão brusca do retorno
    @Column("mic_return_reversal_force")
    private double mic_return_reversal_force;

    // intensidade absoluta do retorno (|retorno| possivelmente transformado)
    @Column("mic_return_absolute_strength")
    private double mic_return_absolute_strength;

    // --------------------------------------------------------------------------
    // DOMINANCE & RVR (retorno vs range / corpo)
    // --------------------------------------------------------------------------

    // razão de dominância do retorno (ex: retorno / range, retorno / body, etc.)
    @Column("mic_return_dominance_ratio")
    private double mic_return_dominance_ratio;

    // mic_rvr: "Return vs Range" ou outro índice composto (mantido por compat.)
    @Column("mic_rvr")
    private double mic_rvr;

    // --------------------------------------------------------------------------
    // RETURN vs TRUE RANGE (gap-aware efficiency)
    // --------------------------------------------------------------------------
    @Column("mic_return_tr_dominance")
    private double mic_return_tr_dominance;          // (close - prevClose) / trueRange

    // --------------------------------------------------------------------------
    // GAP PRESSURE (directional gap component)
    // --------------------------------------------------------------------------
    @Column("mic_return_gap_pressure")
    private double mic_return_gap_pressure;          // sign(return) * (trueRange - range) / (trueRange + eps)

    // --------------------------------------------------------------------------
    // RETURN SIGN PERSISTENCE / RUN (regime / chop filter)
    // --------------------------------------------------------------------------
    @Column("mic_return_sign_prst_w20")
    private double mic_return_sign_prst_w20;         // % de returns > 0 nos últimos 20

    @Column("mic_return_run_len")
    private double mic_return_run_len;               // sequência atual (signed): +N / -N

    // ==========================================================================
    // 7. MULTI-CANDLE RETURN STATS (Return Window Microstructure)
    // ==========================================================================
    // --------------------------------------------------------------------------
    // Z-SCORES DE RETORNO (curto e médio prazo)
    // --------------------------------------------------------------------------

    // z-score do retorno em janela curta (5 candles)
    @Column("mic_return_zscore_5")
    private double mic_return_zscore_5;

    // z-score do retorno em janela média (14 candles)
    @Column("mic_return_zscore_14")
    private double mic_return_zscore_14;

    // --------------------------------------------------------------------------
    // PERCENTIS DE RETORNO (distribuição em janela)
    // --------------------------------------------------------------------------

    // percentil do retorno atual em relação aos últimos 20
    @Column("mic_return_pctl_20")
    private double mic_return_pctl_20;

    // percentil do retorno atual em relação aos últimos 50
    @Column("mic_return_pctl_50")
    private double mic_return_pctl_50;

    // --------------------------------------------------------------------------
    // MOMENTOS SUPERIORES (shape da distribuição de retornos)
    // --------------------------------------------------------------------------
    // skewness dos retornos em janela (assimetria)
    @Column("mic_return_skew")
    private double mic_return_skew;

    // kurtosis dos retornos em janela (cauda gorda x normalidade)
    @Column("mic_return_kurtosis")
    private double mic_return_kurtosis;

    // --------------------------------------------------------------------------
    // VOLATILIDADE E SUAVIDADE DOS RETORNOS
    // --------------------------------------------------------------------------
    // std dos retornos em janela (volatilidade de retorno)
    @Column("mic_return_std_rolling")
    private double mic_return_std_rolling;

    // "smoothness" dos retornos (quão suaves vs erráticos)
    @Column("mic_return_smoothness")
    private double mic_return_smoothness;

    // --------------------------------------------------------------------------
    // ÍNDICES COMPOSTOS (RSP, RDS, RNR)
    // --------------------------------------------------------------------------
    // mic_return_rsp: Return Shock Probability (índice composto de choques)
    @Column("mic_return_rsp")
    private double mic_return_rsp;

    // mic_return_rds: Return Drift Score (tendência média do retorno)
    @Column("mic_return_rds")
    private double mic_return_rds;

    // mic_return_rnr: Return Noise Ratio (razão ruído / sinal do retorno)
    @Column("mic_return_rnr")
    private double mic_return_rnr;

    @Column("mic_return_stdn_w96")
    private double mic_return_stdn_w96;

    // --------------------------------------------------------------------------
    // STD-N 24H NATIVO (regime-aware)
    // --------------------------------------------------------------------------
    @Column("mic_return_stdn_w48")
    private double mic_return_stdn_w48;              // retorno atual / std(retornos últimos 48)

    @Column("mic_return_stdn_w288")
    private double mic_return_stdn_w288;             // retorno atual / std(retornos últimos 288)

    // --------------------------------------------------------------------------
    // RETURN SIGN REGIME (trend vs chop)
    // --------------------------------------------------------------------------
    @Column("mic_return_flip_rate_w20")
    private double mic_return_flip_rate_w20;         // % de flips de sinal do retorno em 20

    @Column("mic_return_autocorr_1_w20")
    private double mic_return_autocorr_1_w20;        // autocorrelação lag-1 dos retornos em 20

}