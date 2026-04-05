package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Component
public class MinuteOfDayCalc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        ZonedDateTime zdt = openTime.atZone(ZoneOffset.UTC);
        return zdt.getHour() * 60 + zdt.getMinute();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_minute_of_day",
                "Minute of Day",
                "time",
                "hour * 60 + minute (UTC)",
                "Minuto do dia em UTC. Converte a hora do candle para um valor linear de 0 a 1439. " +
                "Exemplo: 00:00 = 0, 01:30 = 90, 12:00 = 720, 23:59 = 1439.",
                "0 - 1439",
                "openTime = 2026-01-15T14:35:00Z -> hour=14, minute=35 -> 14*60+35 = 875.0"
        );
    }
}
