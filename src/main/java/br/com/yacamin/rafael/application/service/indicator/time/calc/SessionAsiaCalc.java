package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class SessionAsiaCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        int hour = openTime.atZone(ZoneOffset.UTC).getHour();
        return hour < 8 ? 1.0 : 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_session_asia",
                "Session Asia",
                "time",
                "hour(UTC) < 8 ? 1.0 : 0.0",
                "Flag binaria indicando sessao asiatica (00:00-07:59 UTC). " +
                "Corresponde ao horario de Tokyo/Hong Kong/Sydney. Menor liquidez, spreads maiores.",
                "0.0 ou 1.0",
                "openTime = 2026-01-15T03:00Z -> hour=3 -> 3<8 = true -> 1.0"
        );
    }
}
