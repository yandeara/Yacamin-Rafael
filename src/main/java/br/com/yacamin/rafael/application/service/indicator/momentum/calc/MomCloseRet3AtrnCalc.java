package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.CloseReturnExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class MomCloseRet3AtrnCalc implements DescribableCalc {

    public static double calculate(CloseReturnExtension closeReturn, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < 1e-12) return 0;
        return closeReturn.getValue(index).doubleValue() / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_close_ret_3_atrn",
                "Close Return 3 ATR-Normalized",
                "momentum",
                "close_ret_3 / ATR(14)",
                "Retorno simples de 3 periodos normalizado pelo ATR(14). " +
                "Ajusta o retorno pela volatilidade corrente do ativo.",
                "R",
                "ret=0.02, ATR(14)=50 -> 0.02/50 = 0.0004"
        );
    }
}
