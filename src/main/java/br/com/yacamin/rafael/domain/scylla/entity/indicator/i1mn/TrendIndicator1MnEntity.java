package br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.TrendIndicatorEntity;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import java.time.Instant;

@Data
@Table("trend_indicator_1_mn")
public class TrendIndicator1MnEntity implements TrendIndicatorEntity {
    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED)
    private String symbol;
    @PrimaryKeyColumn(name = "open_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    @Column("open_time")
    private Instant openTime;
    @Column("trd_aligment_ema_8_20_50_delta") private double trd_aligment_ema_8_20_50_delta;
    @Column("trd_alignment_ema_8_20_50_normalized") private double trd_alignment_ema_8_20_50_normalized;
    @Column("trd_alignment_ema_8_20_50_score") private double trd_alignment_ema_8_20_50_score;
    @Column("trd_cross_ema_20_50_delta_atrn") private double trd_cross_ema_20_50_delta_atrn;
    @Column("trd_cross_ema_8_20_delta_atrn") private double trd_cross_ema_8_20_delta_atrn;
    @Column("trd_delta_close_ema_20_atrn") private double trd_delta_close_ema_20_atrn;
    @Column("trd_delta_close_ema_50_atrn") private double trd_delta_close_ema_50_atrn;
    @Column("trd_delta_close_ema_8_atrn") private double trd_delta_close_ema_8_atrn;
    @Column("trd_delta_ema_20_50_atrn") private double trd_delta_ema_20_50_atrn;
    @Column("trd_delta_ema_8_20_atrn") private double trd_delta_ema_8_20_atrn;
    @Column("trd_di_diff_14") private double trd_di_diff_14;
    @Column("trd_dist_close_ema_20_atrn") private double trd_dist_close_ema_20_atrn;
    @Column("trd_dist_close_ema_50_atrn") private double trd_dist_close_ema_50_atrn;
    @Column("trd_dist_close_ema_8_atrn") private double trd_dist_close_ema_8_atrn;
    @Column("trd_dist_ema_20_50_atrn") private double trd_dist_ema_20_50_atrn;
    @Column("trd_dist_ema_8_20_atrn") private double trd_dist_ema_8_20_atrn;
    @Column("trd_dist_ema_8_50_atrn") private double trd_dist_ema_8_50_atrn;
    @Column("trd_ema_20_slp_acc_atrn") private double trd_ema_20_slp_acc_atrn;
    @Column("trd_ema_20_slp_atrn") private double trd_ema_20_slp_atrn;
    @Column("trd_ema_50_slp_acc_atrn") private double trd_ema_50_slp_acc_atrn;
    @Column("trd_ema_50_slp_atrn") private double trd_ema_50_slp_atrn;
    @Column("trd_ema_8_slp_acc_atrn") private double trd_ema_8_slp_acc_atrn;
    @Column("trd_ema_8_slp_atrn") private double trd_ema_8_slp_atrn;
    @Column("trd_ratio_ema_20_50") private double trd_ratio_ema_20_50;
    @Column("trd_ratio_ema_8_20") private double trd_ratio_ema_8_20;
    @Column("trd_ratio_ema_8_50") private double trd_ratio_ema_8_50;
}
