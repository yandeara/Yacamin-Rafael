package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class CosDayOfWeekCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        double dow = DayOfWeekCalc.calculate(openTime);
        return Math.cos(2.0 * Math.PI * (dow - 1) / 7.0);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_cos_day_of_week",
                "Cos Day of Week",
                "time",
                "cos(2*PI * (dayOfWeek-1) / 7)",
                "Codificacao ciclica cossenoidal do dia da semana. Complementa sinDayOfWeek. " +
                "Segunda(1)->1.0, Quinta(4)->-0.22, Domingo(7)->0.62.",
                "-1.0 a 1.0",
                "openTime = segunda(1) -> cos(2*PI*(1-1)/7) = cos(0) = 1.0"
        );
    }
}
