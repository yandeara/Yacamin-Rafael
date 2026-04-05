package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class SinTimeCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        double minuteOfDay = MinuteOfDayCalc.calculate(openTime);
        return Math.sin(2.0 * Math.PI * minuteOfDay / 1440.0);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_sin_time",
                "Sin Time",
                "time",
                "sin(2*PI * minuteOfDay / 1440)",
                "Codificacao ciclica senoidal do horario do dia. Junto com cosTime, forma um par " +
                "que permite ao modelo entender que 23:59 e 00:00 estao proximos (ciclicidade). " +
                "O valor completa um ciclo completo a cada 24h.",
                "-1.0 a 1.0",
                "openTime = 2026-01-15T06:00Z -> minuteOfDay=360 -> sin(2*PI*360/1440) = sin(PI/2) = 1.0"
        );
    }
}
