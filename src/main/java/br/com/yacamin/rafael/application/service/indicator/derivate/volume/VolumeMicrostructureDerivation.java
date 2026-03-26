package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class VolumeMicrostructureDerivation {

    private static final double EPS = 1e-12;

    // ===========================
    // BASE (já existentes)
    // ===========================
    public double takerBuyRatio(SymbolCandle c) {
        double qv = c.getQuoteVolume();
        double tb = c.getTakerBuyQuoteVolume();

        var r = tb / qv;

        if(Double.isNaN(r) || Double.isInfinite(r))
            return 0;

        return r;
    }

    public double takerBuySellImbalance(SymbolCandle c) {
        double tb = c.getTakerBuyQuoteVolume();
        double ts = c.getTakerSellQuoteVolume();
        return (tb - ts) / (tb + ts + EPS);
    }

    public double logVolume(SymbolCandle c) {
        return Math.log(c.getVolume() + EPS);
    }

    // ===========================
    // Helpers: extrair dados do BarSeries
    // ===========================
    private RafaelBar mb(BarSeries series, int i) {
        return (RafaelBar) series.getBar(i); // assume RafaelBar
    }

    private double takerBuyRatio(BarSeries series, int i) {
        var b = mb(series, i);
        double qv = b.getQuoteVolume().doubleValue();             // <- ajuste se nome diferente
        double tb = b.getTakerBuyQuoteVolume().doubleValue();     // <- ajuste se nome diferente
        return tb / qv;
    }

    private double takerBuySellImbalance(BarSeries series, int i) {
        var b = mb(series, i);
        double tb = b.getTakerBuyQuoteVolume().doubleValue();
        double ts = b.getTakerSellQuoteVolume().doubleValue();
        return (tb - ts) / (tb + ts + EPS);
    }

    // ===========================
    // Rolling utilities
    // ===========================
    private int start(int index, int window) {
        return Math.max(0, index - window + 1);
    }

    private double meanRatio(BarSeries series, int index, int window) {
        int s = start(index, window);
        int n = index - s + 1;
        double sum = 0.0;
        for (int i = s; i <= index; i++) sum += takerBuyRatio(series, i);
        return sum / n;
    }

    private double meanImb(BarSeries series, int index, int window) {
        int s = start(index, window);
        int n = index - s + 1;
        double sum = 0.0;
        for (int i = s; i <= index; i++) sum += takerBuySellImbalance(series, i);
        return sum / n;
    }

    private double zscoreRatio(BarSeries series, int index, int window) {
        int s = start(index, window);
        int n = index - s + 1;

        double sum = 0.0, sumSq = 0.0;
        for (int i = s; i <= index; i++) {
            double v = takerBuyRatio(series, i);
            sum += v; sumSq += v * v;
        }

        double mean = sum / n;
        double var = (sumSq / n) - (mean * mean);
        double sd = Math.sqrt(var);

        double last = takerBuyRatio(series, index);
        return (last - mean) / sd;
    }

    private double zscoreImb(BarSeries series, int index, int window) {
        int s = start(index, window);
        int n = index - s + 1;

        double sum = 0.0, sumSq = 0.0;
        for (int i = s; i <= index; i++) {
            double v = takerBuySellImbalance(series, i);
            sum += v; sumSq += v * v;
        }

        double mean = sum / n;
        double var = (sumSq / n) - (mean * mean);
        double sd = Math.sqrt(var);

        double last = takerBuySellImbalance(series, index);

        var r = (last - mean) / sd;

        if(Double.isNaN(r) || Double.isInfinite(r))
            return 0;

        return r;
    }

    private double slopeSeries(BarSeries series, int index, int window, boolean useRatio) {
        int s = Math.max(1, index - window + 1);
        int n = index - s + 1;

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;

        int k = 0;
        for (int i = s; i <= index; i++) {
            double x = k++;
            double y = useRatio ? takerBuyRatio(series, i) : takerBuySellImbalance(series, i);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (Math.abs(denom) < EPS) return 0.0;

        return (n * sumXY - sumX * sumY) / denom;
    }

    private double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    // flip/persistence baseado no “lado” do ratio em relação a 0.5
    private double flipRateRatio(BarSeries series, int index, int window) {
        int s = Math.max(1, index - window + 1);
        int flips = 0;

        double prevSign = sign(takerBuyRatio(series, s - 1) - 0.5);
        for (int i = s; i <= index; i++) {
            double curSign = sign(takerBuyRatio(series, i) - 0.5);
            if (curSign != 0.0 && prevSign != 0.0 && curSign != prevSign) flips++;
            if (curSign != 0.0) prevSign = curSign;
        }
        return (double) flips / (double) window;
    }

    private double prstRatio(BarSeries series, int index, int window) {
        int s = start(index, window);
        int n = index - s + 1;
        double nowSign = sign(takerBuyRatio(series, index) - 0.5);
        if (nowSign == 0.0) return 0.0;

        int same = 0;
        for (int i = s; i <= index; i++) {
            if (sign(takerBuyRatio(series, i) - 0.5) == nowSign) same++;
        }
        return (double) same / (double) n;
    }

    // flip/persistence para imbalance usa sign direto
    private double flipRateImb(BarSeries series, int index, int window) {
        int s = Math.max(1, index - window + 1);
        int flips = 0;

        double prevSign = sign(takerBuySellImbalance(series, s - 1));
        for (int i = s; i <= index; i++) {
            double curSign = sign(takerBuySellImbalance(series, i));
            if (curSign != 0.0 && prevSign != 0.0 && curSign != prevSign) flips++;
            if (curSign != 0.0) prevSign = curSign;
        }
        return (double) flips / (double) window;
    }

    private double prstImb(BarSeries series, int index, int window) {
        int s = start(index, window);
        int n = index - s + 1;
        double nowSign = sign(takerBuySellImbalance(series, index));
        if (nowSign == 0.0) return 0.0;

        int same = 0;
        for (int i = s; i <= index; i++) {
            if (sign(takerBuySellImbalance(series, i)) == nowSign) same++;
        }
        return (double) same / (double) n;
    }

    // ===========================
    // PUBLIC API — 1.1 fields
    // ===========================
    public double takerBuyRatioRel(BarSeries series, int index, int window) {
        double cur = takerBuyRatio(series, index);
        double ma = meanRatio(series, index, window);
        return cur / ma;
    }

    public double takerBuyRatioZsc(BarSeries series, int index, int window) {
        return zscoreRatio(series, index, window);
    }

    public double takerBuyRatioSlpW20(BarSeries series, int index) {
        return slopeSeries(series, index, 20, true);
    }

    public double takerBuyRatioFlipRateW20(BarSeries series, int index) {
        return flipRateRatio(series, index, 20);
    }

    public double takerBuyRatioPrstW20(BarSeries series, int index) {
        return prstRatio(series, index, 20);
    }

    public double takerImbalanceZsc(BarSeries series, int index, int window) {
        return zscoreImb(series, index, window);
    }

    public double takerImbalanceSlpW20(BarSeries series, int index) {
        return slopeSeries(series, index, 20, false);
    }

    public double takerImbalanceFlipRateW20(BarSeries series, int index) {
        return flipRateImb(series, index, 20);
    }

    public double takerImbalancePrstW20(BarSeries series, int index) {
        return prstImb(series, index, 20);
    }

    // ============================================================================
// VolumeMicrostructureDerivation — expose takerBuyRatioAt
// ============================================================================

    public double takerBuyRatioAt(BarSeries series, int index) {
        RafaelBar b = (RafaelBar) series.getBar(index);
        double qv = b.getQuoteVolume().doubleValue();
        double tb = b.getTakerBuyQuoteVolume().doubleValue();
        return tb / qv; // qv==0 -> explode (Regra de Ouro)
    }

}
