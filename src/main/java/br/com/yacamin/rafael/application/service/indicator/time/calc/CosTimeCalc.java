package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class CosTimeCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        double minuteOfDay = MinuteOfDayCalc.calculate(openTime);
        return Math.cos(2.0 * Math.PI * minuteOfDay / 1440.0);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_cos_time",
                "Cos Time",
                "time",
                "cos(2*PI * minuteOfDay / 1440)",
                "Codificacao ciclica cossenoidal do horario do dia. Complementa sinTime para formar " +
                "a representacao completa do ciclo diario. cos=1 em 00:00, cos=-1 em 12:00.",
                "-1.0 a 1.0",
                "openTime = 2026-01-15T00:00Z -> minuteOfDay=0 -> cos(0) = 1.0"
        );
    }
}
