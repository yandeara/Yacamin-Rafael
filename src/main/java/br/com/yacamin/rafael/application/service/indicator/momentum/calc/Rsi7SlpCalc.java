package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;

import org.springframework.stereotype.Component;

@Component
public class Rsi7SlpCalc implements DescribableCalc {

    public static double calculate(LinearRegressionSlopeIndicator rsiSlope, int index) {
        return rsiSlope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_7_slp",
                "RSI(7) Slope",
                "momentum",
                "LinearRegression.slope(RSI(7), 7)",
                "Inclinacao da regressao linear do RSI(7) sobre as ultimas 7 barras. Slope positivo indica tendencia de alta no momentum, negativo indica tendencia de baixa.",
                "unbounded",
                "RSI(7) subindo linearmente -> slope positivo"
        );
    }
}
