package br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn;
import br.com.yacamin.rafael.domain.scylla.entity.IndicatorKey;

import jakarta.persistence.*;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.TrendIndicatorEntity;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "trend_indicator_5_mn")
@IdClass(IndicatorKey.class)
public class TrendIndicator5MnEntity implements TrendIndicatorEntity {
    @Id
    private String symbol;
    @Id
    @Column(name = "open_time")
    private Instant openTime;
    @Column(name = "trd_aligment_ema_8_20_50_delta") private double trd_aligment_ema_8_20_50_delta;
    @Column(name = "trd_alignment_ema_8_20_50_normalized") private double trd_alignment_ema_8_20_50_normalized;
    @Column(name = "trd_alignment_ema_8_20_50_score") private double trd_alignment_ema_8_20_50_score;
    @Column(name = "trd_cross_ema_20_50_delta_atrn") private double trd_cross_ema_20_50_delta_atrn;
    @Column(name = "trd_cross_ema_8_20_delta_atrn") private double trd_cross_ema_8_20_delta_atrn;
    @Column(name = "trd_delta_close_ema_20_atrn") private double trd_delta_close_ema_20_atrn;
    @Column(name = "trd_delta_close_ema_50_atrn") private double trd_delta_close_ema_50_atrn;
    @Column(name = "trd_delta_close_ema_8_atrn") private double trd_delta_close_ema_8_atrn;
    @Column(name = "trd_delta_ema_20_50_atrn") private double trd_delta_ema_20_50_atrn;
    @Column(name = "trd_delta_ema_8_20_atrn") private double trd_delta_ema_8_20_atrn;
    @Column(name = "trd_di_diff_14") private double trd_di_diff_14;
    @Column(name = "trd_dist_close_ema_20_atrn") private double trd_dist_close_ema_20_atrn;
    @Column(name = "trd_dist_close_ema_50_atrn") private double trd_dist_close_ema_50_atrn;
    @Column(name = "trd_dist_close_ema_8_atrn") private double trd_dist_close_ema_8_atrn;
    @Column(name = "trd_dist_ema_20_50_atrn") private double trd_dist_ema_20_50_atrn;
    @Column(name = "trd_dist_ema_8_20_atrn") private double trd_dist_ema_8_20_atrn;
    @Column(name = "trd_dist_ema_8_50_atrn") private double trd_dist_ema_8_50_atrn;
    @Column(name = "trd_ema_20_slp_acc_atrn") private double trd_ema_20_slp_acc_atrn;
    @Column(name = "trd_ema_20_slp_atrn") private double trd_ema_20_slp_atrn;
    @Column(name = "trd_ema_50_slp_acc_atrn") private double trd_ema_50_slp_acc_atrn;
    @Column(name = "trd_ema_50_slp_atrn") private double trd_ema_50_slp_atrn;
    @Column(name = "trd_ema_8_slp_acc_atrn") private double trd_ema_8_slp_acc_atrn;
    @Column(name = "trd_ema_8_slp_atrn") private double trd_ema_8_slp_atrn;
    @Column(name = "trd_ratio_ema_20_50") private double trd_ratio_ema_20_50;
    @Column(name = "trd_ratio_ema_8_20") private double trd_ratio_ema_8_20;
    @Column(name = "trd_ratio_ema_8_50") private double trd_ratio_ema_8_50;
}
