package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class OverlapAsiaEurCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        int hour = openTime.atZone(ZoneOffset.UTC).getHour();
        return hour == 7 ? 1.0 : 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_overlap_asia_eur",
                "Overlap Asia-Europe",
                "time",
                "hour(UTC) == 7 ? 1.0 : 0.0",
                "Flag binaria indicando overlap entre sessao Asia e Europa (07:00-07:59 UTC). " +
                "Momento de transicao de liquidez: traders asiaticos fechando posicoes, europeus abrindo. " +
                "Pode gerar movimentos direcionais fortes.",
                "0.0 ou 1.0",
                "openTime = 2026-01-15T07:30Z -> hour=7 -> 7==7 = true -> 1.0"
        );
    }
}
