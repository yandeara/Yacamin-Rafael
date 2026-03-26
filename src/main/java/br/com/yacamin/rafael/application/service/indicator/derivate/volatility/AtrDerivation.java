package br.com.yacamin.rafael.application.service.indicator.derivate.volatility;

import br.com.yacamin.rafael.domain.SymbolCandle;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;

@Service
@RequiredArgsConstructor
public class AtrDerivation {

    // =============================================================================================
    // ATR level
    // =============================================================================================
    public double atr(ATRIndicator atr, int index) {
        return atr.getValue(index).doubleValue();
    }

    // =============================================================================================
    // ATR change rate: (atr_t / atr_{t-1}) - 1
    // =============================================================================================
    public double atrChange(ATRIndicator atr, int index) {
        double curr = atr.getValue(index).doubleValue();
        double prev = atr.getValue(index - 1).doubleValue();
        return (curr / prev) - 1.0; // prev==0 -> explode (Regra de Ouro)
    }

    // =============================================================================================
    // True Range (gap-aware)
    // =============================================================================================
    public double trueRange(BarSeries series, int index) {
        Bar bar = series.getBar(index);

        double high = bar.getHighPrice().doubleValue();
        double low  = bar.getLowPrice().doubleValue();

        if (index == 0) {
            return Math.abs(high - low);
        }

        Bar prev = series.getBar(index - 1);
        double prevClose = prev.getClosePrice().doubleValue();

        double highLow       = Math.abs(high - low);
        double highPrevClose = Math.abs(high - prevClose);
        double lowPrevClose  = Math.abs(low - prevClose);

        return Math.max(highLow, Math.max(highPrevClose, lowPrevClose));
    }

    // =============================================================================================
    // RANGE_ATR_14_LOC = TR / ATR14
    // =============================================================================================
    public double rangeAtrLocal(BarSeries series, ATRIndicator atr14, int index) {
        double tr  = trueRange(series, index);
        double atr = atr14.getValue(index).doubleValue();
        return tr / atr; // atr==0 -> explode (Regra de Ouro)
    }

    public double rangeAtrLocalChange(BarSeries series, ATRIndicator atr14, int index) {
        double curr = rangeAtrLocal(series, atr14, index);
        double prev = rangeAtrLocal(series, atr14, index - 1);

        var r = (curr / prev) - 1.0;

        if(Double.isInfinite(r) ||  Double.isNaN(r)) {
            return 0;
        }

        return r;// prev==0 -> explode
    }

    // =============================================================================================
    // ATR 7/21 regime
    // =============================================================================================
    public double atrRatio(ATRIndicator fast, ATRIndicator slow, int index) {
        double a = fast.getValue(index).doubleValue();
        double b = slow.getValue(index).doubleValue();
        return a / b;
    }

    public double expansionFromRatio(double ratio) {
        double diff = ratio - 1.0;
        return Math.max(diff, 0.0);
    }

    public double compressionFromRatio(double ratio) {
        double diff = 1.0 - ratio;
        return Math.max(diff, 0.0);
    }

    // =============================================================================================
    // ATR z-score (rolling)
    // =============================================================================================
    public double atrZScore(ATRIndicator atr, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = atr.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double std = Math.sqrt(variance);

        double last = atr.getValue(index).doubleValue();
        return (last - mean) / std; // std==0 -> explode
    }

    // =============================================================================================
    // ATR vol-of-vol (std of ATR values)
    // =============================================================================================
    public double atrVolOfVol(ATRIndicator atr, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = atr.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        return Math.sqrt(variance); // variance==0 -> 0 natural
    }

    // =============================================================================================
    // ATR slope (via cached LinearRegressionSlopeIndicator)
    // =============================================================================================
    public double atrSlope(LinearRegressionSlopeIndicator slp, int index) {
        return slp.getValue(index).doubleValue();
    }

    // =============================================================================================
    // Helpers: minute_of_day (UTC) for seasonality blocks (used later)
    // =============================================================================================
    public int minuteOfDayUtc(Bar bar) {
        var z = bar.getBeginTime().atZone(ZoneOffset.UTC);
        return z.getHour() * 60 + z.getMinute();
    }


}
