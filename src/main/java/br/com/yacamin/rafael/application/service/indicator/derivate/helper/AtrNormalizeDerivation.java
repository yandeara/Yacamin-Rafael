package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.ATRIndicator;

@Service
public class AtrNormalizeDerivation {

    public double normalize(ATRIndicator atr, int index, double value) {
        double atrValue = atr.getValue(index).doubleValue();
        if (atrValue == 0.0) {
            return 0.0; // ou Regra de Ouro: jogar erro, se preferir
        }
        return value / atrValue;
    }
}

