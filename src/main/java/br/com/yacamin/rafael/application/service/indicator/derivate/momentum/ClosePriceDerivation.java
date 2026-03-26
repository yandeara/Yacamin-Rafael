package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

@Service
@RequiredArgsConstructor
public class ClosePriceDerivation {

    private static final double EPS = 1e-12;

    public double calculateSlope(LinearRegressionSlopeIndicator slope, int last) {
        return slope.getValue(last).doubleValue();
    }

    public double calculateSlopeAcc(DifferenceIndicator slopeAcc, int last) {
        return slopeAcc.getValue(last).doubleValue();
    }

    public double calculateCloseZScore(
            ClosePriceIndicator close,
            SMAIndicator sma,
            StandardDeviationIndicator std,
            int last
    ) {
        double c  = close.getValue(last).doubleValue();
        double m  = sma.getValue(last).doubleValue();
        double sd = std.getValue(last).doubleValue();

        if (Math.abs(sd) < EPS) return 0.0;
        return (c - m) / sd;
    }
}
