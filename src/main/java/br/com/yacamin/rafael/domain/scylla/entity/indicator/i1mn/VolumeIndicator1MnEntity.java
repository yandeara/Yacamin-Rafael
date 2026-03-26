package br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn;

import java.time.Instant;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.VolumeIndicatorEntity;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("volume_indicator_1_mn")
public class VolumeIndicator1MnEntity implements VolumeIndicatorEntity  {

    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED)
    private String symbol;

    @PrimaryKeyColumn(name = "open_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    @Column("open_time")
    private Instant openTime;

    // 1) RAW MICROSTRUCTURE
    @Column("vol_taker_buy_ratio") private double vol_taker_buy_ratio;
    @Column("vol_taker_buy_sell_imbalance") private double vol_taker_buy_sell_imbalance;

    // 1.1) TAKER PRESSURE DYNAMICS
    @Column("vol_taker_buy_ratio_rel_16") private double vol_taker_buy_ratio_rel_16;
    @Column("vol_taker_buy_ratio_slp_w20") private double vol_taker_buy_ratio_slp_w20;
    @Column("vol_taker_buy_sell_imbalance_slp_w20") private double vol_taker_buy_sell_imbalance_slp_w20;

    // 2) DELTAS
    @Column("vol_volume_delta_1") private double vol_volume_delta_1;
    @Column("vol_volume_delta_3") private double vol_volume_delta_3;
    @Column("vol_trades_delta_1") private double vol_trades_delta_1;
    @Column("vol_trades_delta_3") private double vol_trades_delta_3;
    @Column("vol_quote_volume_delta_1") private double vol_quote_volume_delta_1;
    @Column("vol_quote_volume_delta_3") private double vol_quote_volume_delta_3;

    // 3) ACTIVITY PRESSURE
    @Column("vol_act_trades_sp_16") private double vol_act_trades_sp_16;
    @Column("vol_act_trades_sp_32") private double vol_act_trades_sp_32;
    @Column("vol_act_trades_acc_16") private double vol_act_trades_acc_16;
    @Column("vol_act_trades_acc_32") private double vol_act_trades_acc_32;
    @Column("vol_act_quote_sp_16") private double vol_act_quote_sp_16;
    @Column("vol_act_quote_sp_32") private double vol_act_quote_sp_32;
    @Column("vol_act_quote_acc_16") private double vol_act_quote_acc_16;
    @Column("vol_act_quote_acc_32") private double vol_act_quote_acc_32;

    // 4) SPIKE SCORE
    @Column("vol_volume_spike_score_16") private double vol_volume_spike_score_16;
    @Column("vol_trades_spike_score_16") private double vol_trades_spike_score_16;

    // 5) MICROBURST
    @Column("vol_microburst_volume_intensity_16") private double vol_microburst_volume_intensity_16;
    @Column("vol_microburst_trades_intensity_16") private double vol_microburst_trades_intensity_16;
    @Column("vol_microburst_combo_16") private double vol_microburst_combo_16;
    @Column("vol_microburst_slope_16") private double vol_microburst_slope_16;
    @Column("vol_pressure_slope_16") private double vol_pressure_slope_16;

    // 6) VWAP
    @Column("vol_vwap") private double vol_vwap;
    @Column("vol_vwap_distance") private double vol_vwap_distance;

    // 7) OFI
    @Column("vol_ofi") private double vol_ofi;
    @Column("vol_ofi_rel_16") private double vol_ofi_rel_16;
    @Column("vol_ofi_slp_w20") private double vol_ofi_slp_w20;

    // 8) BAP
    @Column("vol_bap") private double vol_bap;
    @Column("vol_bap_slope_16") private double vol_bap_slope_16;
    @Column("vol_bap_acc_16") private double vol_bap_acc_16;

    // 9) SVR
    @Column("vol_svr") private double vol_svr;
    @Column("vol_svr_slp_w20") private double vol_svr_slp_w20;
    @Column("vol_svr_acc_5") private double vol_svr_acc_5;
    @Column("vol_svr_acc_10") private double vol_svr_acc_10;
}
