package br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;

import jakarta.persistence.*;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.VolumeIndicatorEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "volume_indicator_30_mn")
@IdClass(IndicatorKey.class)
public class VolumeIndicator30MnEntity implements VolumeIndicatorEntity {

    @Id
    private String symbol;

    @Id
    @Column(name = "open_time")
    private Instant openTime;

    // 1) RAW MICROSTRUCTURE
    @Column(name = "vol_taker_buy_ratio") private double vol_taker_buy_ratio;
    @Column(name = "vol_taker_buy_sell_imbalance") private double vol_taker_buy_sell_imbalance;

    // 1.1) TAKER PRESSURE DYNAMICS
    @Column(name = "vol_taker_buy_ratio_rel_16") private double vol_taker_buy_ratio_rel_16;
    @Column(name = "vol_taker_buy_ratio_slp_w20") private double vol_taker_buy_ratio_slp_w20;
    @Column(name = "vol_taker_buy_sell_imbalance_slp_w20") private double vol_taker_buy_sell_imbalance_slp_w20;

    // 2) DELTAS
    @Column(name = "vol_volume_delta_1") private double vol_volume_delta_1;
    @Column(name = "vol_volume_delta_3") private double vol_volume_delta_3;
    @Column(name = "vol_trades_delta_1") private double vol_trades_delta_1;
    @Column(name = "vol_trades_delta_3") private double vol_trades_delta_3;
    @Column(name = "vol_quote_volume_delta_1") private double vol_quote_volume_delta_1;
    @Column(name = "vol_quote_volume_delta_3") private double vol_quote_volume_delta_3;

    // 3) ACTIVITY PRESSURE
    @Column(name = "vol_act_trades_sp_16") private double vol_act_trades_sp_16;
    @Column(name = "vol_act_trades_sp_32") private double vol_act_trades_sp_32;
    @Column(name = "vol_act_trades_acc_16") private double vol_act_trades_acc_16;
    @Column(name = "vol_act_trades_acc_32") private double vol_act_trades_acc_32;
    @Column(name = "vol_act_quote_sp_16") private double vol_act_quote_sp_16;
    @Column(name = "vol_act_quote_sp_32") private double vol_act_quote_sp_32;
    @Column(name = "vol_act_quote_acc_16") private double vol_act_quote_acc_16;
    @Column(name = "vol_act_quote_acc_32") private double vol_act_quote_acc_32;

    // 4) SPIKE SCORE
    @Column(name = "vol_volume_spike_score_16") private double vol_volume_spike_score_16;
    @Column(name = "vol_trades_spike_score_16") private double vol_trades_spike_score_16;

    // 5) MICROBURST
    @Column(name = "vol_microburst_volume_intensity_16") private double vol_microburst_volume_intensity_16;
    @Column(name = "vol_microburst_trades_intensity_16") private double vol_microburst_trades_intensity_16;
    @Column(name = "vol_microburst_combo_16") private double vol_microburst_combo_16;
    @Column(name = "vol_microburst_slope_16") private double vol_microburst_slope_16;
    @Column(name = "vol_pressure_slope_16") private double vol_pressure_slope_16;

    // 6) VWAP
    @Column(name = "vol_vwap") private double vol_vwap;
    @Column(name = "vol_vwap_distance") private double vol_vwap_distance;

    // 7) OFI
    @Column(name = "vol_ofi") private double vol_ofi;
    @Column(name = "vol_ofi_rel_16") private double vol_ofi_rel_16;
    @Column(name = "vol_ofi_slp_w20") private double vol_ofi_slp_w20;

    // 8) BAP
    @Column(name = "vol_bap") private double vol_bap;
    @Column(name = "vol_bap_slope_16") private double vol_bap_slope_16;
    @Column(name = "vol_bap_acc_16") private double vol_bap_acc_16;

    // 9) SVR
    @Column(name = "vol_svr") private double vol_svr;
    @Column(name = "vol_svr_slp_w20") private double vol_svr_slp_w20;
    @Column(name = "vol_svr_acc_5") private double vol_svr_acc_5;
    @Column(name = "vol_svr_acc_10") private double vol_svr_acc_10;
}
