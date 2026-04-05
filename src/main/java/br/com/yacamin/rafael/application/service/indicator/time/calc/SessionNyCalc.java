package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class SessionNyCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        int hour = openTime.atZone(ZoneOffset.UTC).getHour();
        return (hour >= 12 && hour < 20) ? 1.0 : 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_session_ny",
                "Session New York",
                "time",
                "hour(UTC) >= 12 && hour < 20 ? 1.0 : 0.0",
                "Flag binaria indicando sessao de Nova York (12:00-19:59 UTC). " +
                "Maior volume do dia em crypto, concentracao de noticias macro (FOMC, CPI, etc).",
                "0.0 ou 1.0",
                "openTime = 2026-01-15T14:35Z -> hour=14 -> 14>=12 && 14<20 = true -> 1.0"
        );
    }
}
