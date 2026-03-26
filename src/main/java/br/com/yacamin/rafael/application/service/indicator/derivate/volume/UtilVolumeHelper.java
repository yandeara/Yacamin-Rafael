package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.Function;
import org.ta4j.core.BarSeries;

public class UtilVolumeHelper {

    public static final MathContext MC = MathContext.DECIMAL64;

    // =======================================================================
    // OFI — média simples (últimas N observações de OFI)
    // =======================================================================
    public static BigDecimal smaOfi(BarSeries s, int window) {
        int end = s.getEndIndex();
        if (end <= 0 || end < window) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int k = 0; k < window; k++) {
            int idx = end - k;
            sum = sum.add(ofiAt(s, idx), MC);
        }

        return sum.divide(BigDecimal.valueOf(window), MC);
    }

    public static BigDecimal ofiAt(BarSeries s, int index) {
        if (index <= 0) {
            return BigDecimal.ZERO;
        }

        RafaelBar b  = (RafaelBar) s.getBar(index);
        RafaelBar bp = (RafaelBar) s.getBar(index - 1);

        BigDecimal lowNow   = b.getLowPrice().bigDecimalValue();
        BigDecimal lowPrev  = bp.getLowPrice().bigDecimalValue();
        BigDecimal highNow  = b.getHighPrice().bigDecimalValue();
        BigDecimal highPrev = bp.getHighPrice().bigDecimalValue();

        BigDecimal bidChange = lowNow.subtract(lowPrev, MC);
        BigDecimal askChange = highNow.subtract(highPrev, MC);

        return bidChange.subtract(askChange, MC);
    }

    // =======================================================================
    // Z-SCORE genérico de um indicador (últimas N observações)
    // f: BarSeries -> valor do indicador na ÚLTIMA barra do series
    // =======================================================================
    public static BigDecimal zscore(BarSeries s, int window, Function<BarSeries, BigDecimal> f) {
        int end = s.getEndIndex();
        if (end < window - 1) {
            return BigDecimal.ZERO;
        }

        BigDecimal[] vals = new BigDecimal[window];

        // vals[0] = valor atual, vals[k] = valor k barras atrás
        for (int k = 0; k < window; k++) {
            int idx = end - k;
            BarSeries prefix = s.getSubSeries(0, idx + 1);
            vals[k] = f.apply(prefix);
        }

        BigDecimal mean = mean(vals);
        BigDecimal sd   = std(vals, mean);
        BigDecimal v    = vals[0]; // valor atual

        if (sd.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return v.subtract(mean, MC).divide(sd, MC);
    }

    // =======================================================================
    // MÉDIA / DESVIO de um array de valores já calculados
    // =======================================================================
    private static BigDecimal mean(BigDecimal[] vals) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : vals) {
            sum = sum.add(v, MC);
        }
        return sum.divide(BigDecimal.valueOf(vals.length), MC);
    }

    private static BigDecimal std(BigDecimal[] vals, BigDecimal mean) {
        BigDecimal sum = BigDecimal.ZERO;

        for (BigDecimal v : vals) {
            BigDecimal d = v.subtract(mean, MC);
            sum = sum.add(d.multiply(d, MC), MC);
        }

        BigDecimal var = sum.divide(BigDecimal.valueOf(vals.length), MC);
        return sqrt(var);
    }

    public static BigDecimal sqrt(BigDecimal x) {
        if (x.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        double d = Math.sqrt(x.doubleValue());
        return BigDecimal.valueOf(d);
    }

    // =======================================================================
    // RELATIVO: valor atual / média dos últimos N valores do indicador
    // =======================================================================
    public static BigDecimal rel(BarSeries s, int window, Function<BarSeries, BigDecimal> f) {
        int end = s.getEndIndex();
        if (end < window - 1) {
            return BigDecimal.ZERO;
        }

        BigDecimal now = f.apply(s);

        BigDecimal sum = BigDecimal.ZERO;
        for (int k = 0; k < window; k++) {
            int idx = end - k;
            BarSeries prefix = s.getSubSeries(0, idx + 1);
            sum = sum.add(f.apply(prefix), MC);
        }

        BigDecimal avg = sum.divide(BigDecimal.valueOf(window), MC);
        if (avg.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return now.divide(avg, MC);
    }

    // =======================================================================
    // VoV — desvio padrão do volume nas últimas N barras
    // =======================================================================
    public static BigDecimal stdVolume(BarSeries s, int window) {
        int end = s.getEndIndex();
        if (end < window - 1) {
            return BigDecimal.ZERO;
        }

        BigDecimal mean = BigDecimal.ZERO;

        for (int i = 0; i < window; i++) {
            BigDecimal v = s.getBar(end - i).getVolume().bigDecimalValue();
            mean = mean.add(v, MC);
        }
        mean = mean.divide(BigDecimal.valueOf(window), MC);

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < window; i++) {
            BigDecimal v = s.getBar(end - i).getVolume().bigDecimalValue();
            BigDecimal d = v.subtract(mean, MC);
            sum = sum.add(d.multiply(d, MC), MC);
        }

        BigDecimal var = sum.divide(BigDecimal.valueOf(window), MC);
        return sqrt(var);
    }

    // =======================================================================
    // VPIN
    // =======================================================================
    public static BigDecimal vpin(BarSeries s, int buckets) {
        return VpinCalculator.compute(s, buckets);
    }

    // =======================================================================
    // SLOPE genérico: regressão linear das últimas N observações do indicador
    // =======================================================================
    public static BigDecimal slope(BarSeries s, int window, Function<BarSeries, BigDecimal> f) {
        int end = s.getEndIndex();
        if (end < window - 1) {
            return BigDecimal.ZERO;
        }

        BigDecimal[] arr = new BigDecimal[window];

        int start = end - window + 1;
        for (int i = 0; i < window; i++) {
            int idx = start + i;
            BarSeries prefix = s.getSubSeries(0, idx + 1);
            arr[i] = f.apply(prefix);
        }

        return SlopeCalculator.slope(arr);
    }
}
