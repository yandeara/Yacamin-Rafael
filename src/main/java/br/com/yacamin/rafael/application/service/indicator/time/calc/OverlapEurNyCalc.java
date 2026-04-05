package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class OverlapEurNyCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        int hour = openTime.atZone(ZoneOffset.UTC).getHour();
        return hour == 12 ? 1.0 : 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_overlap_eur_ny",
                "Overlap Europe-NY",
                "time",
                "hour(UTC) == 12 ? 1.0 : 0.0",
                "Flag binaria indicando overlap entre sessao Europa e NY (12:00-12:59 UTC). " +
                "Horario de maior liquidez global: Europa e US operando simultaneamente. " +
                "Pico de volume e volatilidade.",
                "0.0 ou 1.0",
                "openTime = 2026-01-15T12:45Z -> hour=12 -> 12==12 = true -> 1.0"
        );
    }
}
