package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.roll.extension.RollSpreadExtension;

import org.springframework.stereotype.Component;

@Component
public class RollSpreadAccW32Calc implements DescribableCalc {

    public static double calculate(RollSpreadExtension spread, int index) {
        if (index < 1) {
            return 0;
        }
        return spread.getValue(index).doubleValue() - spread.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_roll_spread_acc_w32",
                "Roll Spread Acceleration W32",
                "microstructure",
                "roll_spread(w=32)[t] - roll_spread(w=32)[t-1]",
                "Aceleracao do spread de Roll (janela 32). " +
                "Captura a variacao instantanea do spread implicito, sinalizando mudancas abruptas no custo de transacao.",
                "unbounded",
                "spread[t]=0.10, spread[t-1]=0.08 -> 0.02"
        );
    }
}
