package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BodySignPersistenceIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> body;
    private final int window;

    public BodySignPersistenceIndicator(Indicator<Num> body, int window) {
        super(body.getBarSeries());
        this.body = body;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 0 || index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);

        int positives = 0;
        int n = 0;

        for (int i = start; i <= index; i++) {
            double v = body.getValue(i).doubleValue();
            if (v > EPS) positives++;
            n++;
        }

        double out = (n == 0) ? 0.0 : (positives / (double) n);
        return getBarSeries().numFactory().numOf(out);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
