package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class ClosePriceHelperDerivation {

    private final DeltaDerivation deltaDerivation;

    //Delta = Close[i] - Close[i-1]
    public double delta(ClosePriceIndicator close, int i) {
        double actualClose = close.getValue(i).doubleValue();
        double prevClose = close.getValue(i - 1).doubleValue();

        return deltaDerivation.delta(actualClose, prevClose);
    }

    public double deltaPct(ClosePriceIndicator close, int i) {
        double actualClose = close.getValue(i).doubleValue();
        double prevClose = close.getValue(i - 1).doubleValue();

        return deltaDerivation.deltaPct(actualClose, prevClose);
    }

}
