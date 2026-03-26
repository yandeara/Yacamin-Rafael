package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.domain.scylla.entity.NewCandleEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssemblerDto {

    private NewCandleEntity candle;
    private MicrostructureIndicatorEntity mic;
    private MomentumIndicatorEntity mom;
    private TimeIndicatorEntity tim;
    private TrendIndicatorEntity trd;
    private VolatilityIndicatorEntity vlt;
    private VolumeIndicatorEntity vol;
    private TpSlIndicatorEntity tpsl;

}
