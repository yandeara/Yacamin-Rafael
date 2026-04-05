package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class DayOfMonthCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        return openTime.atZone(ZoneOffset.UTC).getDayOfMonth();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_day_of_month",
                "Day of Month",
                "time",
                "getDayOfMonth() — UTC",
                "Dia do mes (1 a 31). Util para capturar padroes mensais como " +
                "efeito de inicio/fim de mes, vencimentos, etc.",
                "1 - 31",
                "openTime = 2026-01-15T14:35Z -> 15.0"
        );
    }
}
