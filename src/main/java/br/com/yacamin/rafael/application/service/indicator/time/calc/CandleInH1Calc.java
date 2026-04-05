package br.com.yacamin.rafael.application.service.indicator.time.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class CandleInH1Calc implements DescribableCalc {

    public static double calculate(Instant openTime) {
        int minute = openTime.atZone(ZoneOffset.UTC).getMinute();
        return minute / 15;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "tim_candle_in_h1",
                "Candle Position in H1",
                "time",
                "minute(UTC) / 15 (divisao inteira)",
                "Posicao do candle dentro da hora (H1). Divide a hora em 4 blocos de 15 minutos. " +
                "0=primeiro quarto (00-14min), 1=segundo (15-29min), 2=terceiro (30-44min), 3=ultimo (45-59min). " +
                "Util para capturar padroes intra-hora.",
                "0, 1, 2, 3",
                "openTime = 2026-01-15T14:35Z -> minute=35 -> 35/15 = 2 (int) -> 2.0"
        );
    }
}
