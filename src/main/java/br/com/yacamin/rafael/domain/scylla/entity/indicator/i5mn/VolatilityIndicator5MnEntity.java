package br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.VolatilityIndicatorEntity;
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
@Table("volatility_indicator_5_mn")
public class VolatilityIndicator5MnEntity implements VolatilityIndicatorEntity {

    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED)
    private String symbol;

    @PrimaryKeyColumn(name = "open_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    @Column("open_time")
    private Instant openTime;

    // ATR CHANGE
    @Column("vlt_atr_7_chg")   private double vlt_atr_7_chg;
    @Column("vlt_atr_14_chg")  private double vlt_atr_14_chg;
    @Column("vlt_atr_21_chg")  private double vlt_atr_21_chg;

    // ATR LOCAL
    @Column("vlt_range_atr_14_loc")     private double vlt_range_atr_14_loc;
    @Column("vlt_range_atr_14_loc_chg") private double vlt_range_atr_14_loc_chg;

    // ATR REGIME 7/21
    @Column("vlt_atr_7_21_ratio") private double vlt_atr_7_21_ratio;
    @Column("vlt_atr_7_21_expn")  private double vlt_atr_7_21_expn;
    @Column("vlt_atr_7_21_cmpr")  private double vlt_atr_7_21_cmpr;

    // ATR SLOPE
    @Column("vlt_atr_7_slp")  private double vlt_atr_7_slp;
    @Column("vlt_atr_14_slp") private double vlt_atr_14_slp;
    @Column("vlt_atr_21_slp") private double vlt_atr_21_slp;

    // STD CHANGE
    @Column("vlt_std_14_chg") private double vlt_std_14_chg;
    @Column("vlt_std_20_chg") private double vlt_std_20_chg;
    @Column("vlt_std_50_chg") private double vlt_std_50_chg;

    // STD REGIME 14/50
    @Column("vlt_std_14_50_ratio") private double vlt_std_14_50_ratio;
    @Column("vlt_std_14_50_expn")  private double vlt_std_14_50_expn;
    @Column("vlt_std_14_50_cmpr")  private double vlt_std_14_50_cmpr;

    // STD REGIME 14/48
    @Column("vlt_std_14_48_ratio") private double vlt_std_14_48_ratio;
    @Column("vlt_std_14_48_expn")  private double vlt_std_14_48_expn;
    @Column("vlt_std_14_48_cmpr")  private double vlt_std_14_48_cmpr;

    // STD SLOPE
    @Column("vlt_std_14_slp") private double vlt_std_14_slp;
    @Column("vlt_std_20_slp") private double vlt_std_20_slp;
    @Column("vlt_std_50_slp") private double vlt_std_50_slp;

    // BOLLINGER WIDTH CHANGE
    @Column("vlt_boll_20_width_chg") private double vlt_boll_20_width_chg;

    // SQUEEZE BB/KELT
    @Column("vlt_vol_sqz_bb_kelt_chg")    private double vlt_vol_sqz_bb_kelt_chg;
    @Column("vlt_vol_sqz_bb_kelt_20_chg") private double vlt_vol_sqz_bb_kelt_20_chg;

    // RANGE VOL — GK SLOPE
    @Column("vlt_vol_gk_16_slp") private double vlt_vol_gk_16_slp;
    @Column("vlt_vol_gk_32_slp") private double vlt_vol_gk_32_slp;

    // RANGE VOL — PARK SLOPE
    @Column("vlt_vol_park_16_slp") private double vlt_vol_park_16_slp;
    @Column("vlt_vol_park_32_slp") private double vlt_vol_park_32_slp;

    // RANGE VOL — RS SLOPE
    @Column("vlt_vol_rs_16_slp") private double vlt_vol_rs_16_slp;
    @Column("vlt_vol_rs_32_slp") private double vlt_vol_rs_32_slp;

    // RANGE VOL — GK RATIO
    @Column("vlt_vol_gk_16_48_ratio") private double vlt_vol_gk_16_48_ratio;
    @Column("vlt_vol_gk_32_48_ratio") private double vlt_vol_gk_32_48_ratio;

    // RANGE VOL — PARK RATIO
    @Column("vlt_vol_park_16_48_ratio") private double vlt_vol_park_16_48_ratio;
    @Column("vlt_vol_park_32_48_ratio") private double vlt_vol_park_32_48_ratio;

    // RANGE VOL — RS RATIO
    @Column("vlt_vol_rs_16_48_ratio") private double vlt_vol_rs_16_48_ratio;
    @Column("vlt_vol_rs_32_48_ratio") private double vlt_vol_rs_32_48_ratio;

    // REALIZED VOL — SLOPE
    @Column("vlt_vol_rv_10_slp") private double vlt_vol_rv_10_slp;
    @Column("vlt_vol_rv_30_slp") private double vlt_vol_rv_30_slp;

    // REALIZED VOL — RATIO
    @Column("vlt_vol_rv_10_30_ratio") private double vlt_vol_rv_10_30_ratio;
    @Column("vlt_vol_rv_10_48_ratio") private double vlt_vol_rv_10_48_ratio;

    // EWMA VOL — SLOPE
    @Column("vlt_ewma_vol_20_slp") private double vlt_ewma_vol_20_slp;
    @Column("vlt_ewma_vol_32_slp") private double vlt_ewma_vol_32_slp;

    // EWMA VOL — RATIO
    @Column("vlt_ewma_vol_20_48_ratio") private double vlt_ewma_vol_20_48_ratio;
    @Column("vlt_ewma_vol_32_48_ratio") private double vlt_ewma_vol_32_48_ratio;
}
