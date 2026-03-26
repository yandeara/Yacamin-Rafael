package br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn;

import br.com.yacamin.rafael.domain.scylla.entity.indicator.MomentumIndicatorEntity;
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
@Table("momentum_indicator_30_mn")
public class MomentumIndicator30MnEntity implements MomentumIndicatorEntity {

    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED)
    private String symbol;

    @PrimaryKeyColumn(name = "open_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    @Column("open_time")
    private Instant openTime;

    // CLOSE RETURNS — RAW (intermediaries)
    @Column("mom_close_ret_1") private double mom_close_ret_1;
    @Column("mom_close_ret_2") private double mom_close_ret_2;
    @Column("mom_close_ret_3") private double mom_close_ret_3;
    @Column("mom_close_ret_4") private double mom_close_ret_4;
    @Column("mom_close_ret_5") private double mom_close_ret_5;
    @Column("mom_close_ret_6") private double mom_close_ret_6;
    @Column("mom_close_ret_8") private double mom_close_ret_8;
    @Column("mom_close_ret_10") private double mom_close_ret_10;
    @Column("mom_close_ret_12") private double mom_close_ret_12;
    @Column("mom_close_ret_16") private double mom_close_ret_16;

    // CLOSE RETURNS — ATR-N
    @Column("mom_close_ret_1_atrn") private double mom_close_ret_1_atrn;
    @Column("mom_close_ret_2_atrn") private double mom_close_ret_2_atrn;
    @Column("mom_close_ret_3_atrn") private double mom_close_ret_3_atrn;
    @Column("mom_close_ret_4_atrn") private double mom_close_ret_4_atrn;
    @Column("mom_close_ret_5_atrn") private double mom_close_ret_5_atrn;
    @Column("mom_close_ret_6_atrn") private double mom_close_ret_6_atrn;
    @Column("mom_close_ret_8_atrn") private double mom_close_ret_8_atrn;
    @Column("mom_close_ret_10_atrn") private double mom_close_ret_10_atrn;
    @Column("mom_close_ret_12_atrn") private double mom_close_ret_12_atrn;
    @Column("mom_close_ret_16_atrn") private double mom_close_ret_16_atrn;

    // BURST
    @Column("mom_burst_10") private double mom_burst_10;
    @Column("mom_burst_16") private double mom_burst_16;
    @Column("mom_burst_32") private double mom_burst_32;

    // CONTINUATION RATE
    @Column("mom_cntrate_10") private double mom_cntrate_10;
    @Column("mom_cntrate_16") private double mom_cntrate_16;
    @Column("mom_cntrate_32") private double mom_cntrate_32;

    // DECAY
    @Column("mom_decay_10") private double mom_decay_10;
    @Column("mom_decay_16") private double mom_decay_16;
    @Column("mom_decay_32") private double mom_decay_32;

    // IMPULSE
    @Column("mom_impls_10") private double mom_impls_10;
    @Column("mom_impls_16") private double mom_impls_16;
    @Column("mom_impls_32") private double mom_impls_32;

    // CHOP RATIO
    @Column("mom_chprt_10") private double mom_chprt_10;
    @Column("mom_chprt_16") private double mom_chprt_16;
    @Column("mom_chprt_32") private double mom_chprt_32;

    // RSI DELTA
    @Column("mom_rsi_2_dlt") private double mom_rsi_2_dlt;
    @Column("mom_rsi_3_dlt") private double mom_rsi_3_dlt;
    @Column("mom_rsi_5_dlt") private double mom_rsi_5_dlt;
    @Column("mom_rsi_7_dlt") private double mom_rsi_7_dlt;
    @Column("mom_rsi_14_dlt") private double mom_rsi_14_dlt;

    // RSI ROC
    @Column("mom_rsi_2_roc") private double mom_rsi_2_roc;
    @Column("mom_rsi_3_roc") private double mom_rsi_3_roc;
    @Column("mom_rsi_5_roc") private double mom_rsi_5_roc;
    @Column("mom_rsi_7_roc") private double mom_rsi_7_roc;
    @Column("mom_rsi_14_roc") private double mom_rsi_14_roc;

    // RSI SLOPE
    @Column("mom_rsi_7_slp") private double mom_rsi_7_slp;
    @Column("mom_rsi_14_slp") private double mom_rsi_14_slp;

    // RSI ACC / ATRN
    @Column("mom_rsi_14_acc") private double mom_rsi_14_acc;
    @Column("mom_rsi_14_atrn") private double mom_rsi_14_atrn;

    // RSI DIST MID
    @Column("mom_rsi_7_dst_mid") private double mom_rsi_7_dst_mid;
    @Column("mom_rsi_14_dst_mid") private double mom_rsi_14_dst_mid;

    // RSI TAIL
    @Column("mom_rsi_7_tail_up") private double mom_rsi_7_tail_up;
    @Column("mom_rsi_7_tail_dw") private double mom_rsi_7_tail_dw;
    @Column("mom_rsi_14_tail_up") private double mom_rsi_14_tail_up;
    @Column("mom_rsi_14_tail_dw") private double mom_rsi_14_tail_dw;

    // CMO DELTA
    @Column("mom_cmo_14_dlt") private double mom_cmo_14_dlt;
    @Column("mom_cmo_20_dlt") private double mom_cmo_20_dlt;

    // CMO DIST MID
    @Column("mom_cmo_14_dst_mid") private double mom_cmo_14_dst_mid;
    @Column("mom_cmo_20_dst_mid") private double mom_cmo_20_dst_mid;

    // WPR DELTA
    @Column("mom_wpr_14_dlt") private double mom_wpr_14_dlt;
    @Column("mom_wpr_28_dlt") private double mom_wpr_28_dlt;
    @Column("mom_wpr_42_dlt") private double mom_wpr_42_dlt;

    // WPR DIST MID
    @Column("mom_wpr_14_dst_mid") private double mom_wpr_14_dst_mid;
    @Column("mom_wpr_28_dst_mid") private double mom_wpr_28_dst_mid;
    @Column("mom_wpr_48_dst_mid") private double mom_wpr_48_dst_mid;

    // STOCH
    @Column("mom_stoch_14_k_dlt") private double mom_stoch_14_k_dlt;
    @Column("mom_stoch_14_d_dlt") private double mom_stoch_14_d_dlt;
    @Column("mom_stoch_14_spread") private double mom_stoch_14_spread;
    @Column("mom_stoch_14_k_dst_mid") private double mom_stoch_14_k_dst_mid;

    // TRIX
    @Column("mom_trix_9_dlt") private double mom_trix_9_dlt;
    @Column("mom_trix_9_hist") private double mom_trix_9_hist;

    // TSI
    @Column("mom_tsi_25_13_dlt") private double mom_tsi_25_13_dlt;
    @Column("mom_tsi_25_13_hist") private double mom_tsi_25_13_hist;
    @Column("mom_tsi_25_13_dst_mid") private double mom_tsi_25_13_dst_mid;

    // PPO
    @Column("mom_ppo_hist_12_26_9") private double mom_ppo_hist_12_26_9;
    @Column("mom_ppo_12_26_dlt") private double mom_ppo_12_26_dlt;
    @Column("mom_ppo_hist_12_26_9_dlt") private double mom_ppo_hist_12_26_9_dlt;

    // CLOSE PRICE SLOPE
    @Column("mom_close_3_slp") private double mom_close_3_slp;
    @Column("mom_close_8_slp") private double mom_close_8_slp;
    @Column("mom_close_14_slp") private double mom_close_14_slp;

    // CLOSE PRICE SLOPE ATR-N
    @Column("mom_close_3_slp_atrn") private double mom_close_3_slp_atrn;
    @Column("mom_close_8_slp_atrn") private double mom_close_8_slp_atrn;
    @Column("mom_close_14_slp_atrn") private double mom_close_14_slp_atrn;

    // CLOSE PRICE SLOPE ACC
    @Column("mom_close_3_slp_acc") private double mom_close_3_slp_acc;
    @Column("mom_close_8_slp_acc") private double mom_close_8_slp_acc;
    @Column("mom_close_14_slp_acc") private double mom_close_14_slp_acc;

    // CLOSE PRICE SLOPE ACC ATR-N
    @Column("mom_close_3_slp_acc_atrn") private double mom_close_3_slp_acc_atrn;
    @Column("mom_close_8_slp_acc_atrn") private double mom_close_8_slp_acc_atrn;
    @Column("mom_close_14_slp_acc_atrn") private double mom_close_14_slp_acc_atrn;

    // CCI
    @Column("mom_cci_14_dlt") private double mom_cci_14_dlt;
    @Column("mom_cci_20_dlt") private double mom_cci_20_dlt;
    @Column("mom_cci_14_dst_mid") private double mom_cci_14_dst_mid;
    @Column("mom_cci_20_dst_mid") private double mom_cci_20_dst_mid;

    // ROC
    @Column("mom_roc_1") private double mom_roc_1;
    @Column("mom_roc_2") private double mom_roc_2;
    @Column("mom_roc_3") private double mom_roc_3;
    @Column("mom_roc_5") private double mom_roc_5;

    // STABILITY slp_w20
    @Column("mom_ppo_hist_12_26_9_slp_w20") private double mom_ppo_hist_12_26_9_slp_w20;
    @Column("mom_trix_hist_9_slp_w20") private double mom_trix_hist_9_slp_w20;
    @Column("mom_tsi_hist_25_13_slp_w20") private double mom_tsi_hist_25_13_slp_w20;
}
