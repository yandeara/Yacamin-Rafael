package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class SessionEuropeCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        int hour = openTime.atZone(ZoneOffset.UTC).getHour();
        return (hour >= 7 && hour < 13) ? 1.0 : 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_session_europe",
                "Session Europe",
                "time",
                "hour(UTC) >= 7 && hour < 13 ? 1.0 : 0.0",
                "Flag binaria indicando sessao europeia (07:00-12:59 UTC). " +
                "Corresponde ao horario de Londres/Frankfurt. Aumento de liquidez, abertura de posicoes institucionais.",
                "0.0 ou 1.0",
                "openTime = 2026-01-15T10:00Z -> hour=10 -> 10>=7 && 10<13 = true -> 1.0"
        );
    }
}
