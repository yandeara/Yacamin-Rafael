package br.com.yacamin.rafael.application.service.indicator.derivate.trend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;

@Service
@RequiredArgsConstructor
public class AdxDerivation {

    public double adx(ADXIndicator adx, int last) {
        return adx.getValue(last).doubleValue();
    }

    public double pdi(PlusDIIndicator pdi, int last) {
        return pdi.getValue(last).doubleValue();
    }

    public double mdi(MinusDIIndicator mdi, int last) {
        return mdi.getValue(last).doubleValue();
    }

    public double diDiff(PlusDIIndicator pdi, MinusDIIndicator mdi, int last) {
        return pdi.getValue(last).doubleValue() - mdi.getValue(last).doubleValue();
    }
}
