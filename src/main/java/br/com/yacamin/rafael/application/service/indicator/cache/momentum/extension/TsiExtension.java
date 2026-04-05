package br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

/**
 * True Strength Index (TSI)
 * PC = close[t] - close[t-1]
 * AbsPC = |PC|
 * doubleSmoothedPC  = EMA(EMA(PC, longPeriod), shortPeriod)
 * doubleSmoothedAbsPC = EMA(EMA(|PC|, longPeriod), shortPeriod)
 * TSI = (doubleSmoothedPC / doubleSmoothedAbsPC) * 100
 *
 * EMA computed inline to avoid creating sub-indicator classes for PC/AbsPC.
 */
public class TsiExtension extends CachedIndicator<Num> {

    private final ClosePriceIndicator close;
    private final int longPeriod;
    private final int shortPeriod;

    public TsiExtension(ClosePriceIndicator close, int longPeriod, int shortPeriod) {
        super(close.getBarSeries());
        this.close = close;
        this.longPeriod = longPeriod;
        this.shortPeriod = shortPeriod;
    }

    @Override
    protected Num calculate(int index) {
        int minBars = longPeriod + shortPeriod;
        if (index < minBars) {
            return getBarSeries().numFactory().numOf(0);
        }

        int n = index + 1;

        // Step 1: build PC and AbsPC arrays
        double[] pc = new double[n];
        double[] absPC = new double[n];

        pc[0] = 0.0;
        absPC[0] = 0.0;

        for (int i = 1; i < n; i++) {
            double curr = close.getValue(i).doubleValue();
            double prev = close.getValue(i - 1).doubleValue();
            pc[i] = curr - prev;
            absPC[i] = Math.abs(pc[i]);
        }

        // Step 2: first EMA pass (longPeriod)
        double[] ema1PC = ema(pc, longPeriod);
        double[] ema1Abs = ema(absPC, longPeriod);

        // Step 3: second EMA pass (shortPeriod)
        double[] ema2PC = ema(ema1PC, shortPeriod);
        double[] ema2Abs = ema(ema1Abs, shortPeriod);

        double denominator = ema2Abs[index];
        if (Math.abs(denominator) < 1e-12) {
            return getBarSeries().numFactory().numOf(0);
        }

        double tsi = (ema2PC[index] / denominator) * 100.0;
        return getBarSeries().numFactory().numOf(tsi);
    }

    private double[] ema(double[] values, int period) {
        int n = values.length;
        double[] result = new double[n];
        double alpha = 2.0 / (period + 1.0);

        result[0] = values[0];
        for (int i = 1; i < n; i++) {
            result[i] = alpha * values[i] + (1.0 - alpha) * result[i - 1];
        }

        return result;
    }

    @Override
    public int getCountOfUnstableBars() {
        return longPeriod + shortPeriod;
    }
}
