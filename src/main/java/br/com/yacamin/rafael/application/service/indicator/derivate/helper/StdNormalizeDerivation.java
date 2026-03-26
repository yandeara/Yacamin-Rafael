package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

@Service
public class StdNormalizeDerivation {

    public double normalize(StandardDeviationIndicator std, int index, double value) {
        double stdValue = std.getValue(index).doubleValue();

        if(stdValue == 0) {
            return 0;
        }

        return value / stdValue;
    }

}
