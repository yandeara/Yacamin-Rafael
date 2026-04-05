package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class SinDayOfWeekCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        double dow = DayOfWeekCalc.calculate(openTime);
        return Math.sin(2.0 * Math.PI * (dow - 1) / 7.0);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_sin_day_of_week",
                "Sin Day of Week",
                "time",
                "sin(2*PI * (dayOfWeek-1) / 7)",
                "Codificacao ciclica senoidal do dia da semana. Junto com cosDayOfWeek, " +
                "permite ao modelo entender que domingo e segunda estao proximos. " +
                "Segunda(1)->0.0, Quarta(3)->0.78, Sabado(6)->-0.43.",
                "-1.0 a 1.0",
                "openTime = quinta(4) -> sin(2*PI*(4-1)/7) = sin(2*PI*3/7) = 0.9749"
        );
    }
}
