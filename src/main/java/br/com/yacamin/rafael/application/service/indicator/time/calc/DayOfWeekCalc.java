package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class DayOfWeekCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        return openTime.atZone(ZoneOffset.UTC).getDayOfWeek().getValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_day_of_week",
                "Day of Week",
                "time",
                "DayOfWeek.getValue() — ISO-8601: Monday=1 ... Sunday=7",
                "Dia da semana no padrao ISO-8601. Segunda=1, Terca=2, ..., Domingo=7. " +
                "Crypto opera 24/7 mas o comportamento muda por dia (volume menor no fim de semana).",
                "1 - 7",
                "openTime = 2026-01-15T14:35Z (quinta) -> 4.0"
        );
    }
}
