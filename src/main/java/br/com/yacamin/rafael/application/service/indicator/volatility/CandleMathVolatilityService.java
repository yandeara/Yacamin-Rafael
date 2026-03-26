package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleMathVolatilityService {

    private static final int PERIOD_RV_10 = 10;
    private static final int PERIOD_RV_30 = 30;
    private static final int PERIOD_RV_50 = 50;
    private static final int ZSCORE_WINDOW_RV_10 = 200;

    // =============================================================================================
    // Helpers: acesso aos preços
    // =============================================================================================

    private double open(BarSeries s, int i)  { return s.getBar(i).getOpenPrice().doubleValue(); }
    private double high(BarSeries s, int i)  { return s.getBar(i).getHighPrice().doubleValue(); }
    private double low(BarSeries s, int i)   { return s.getBar(i).getLowPrice().doubleValue(); }
    private double close(BarSeries s, int i) { return s.getBar(i).getClosePrice().doubleValue(); }

    // =============================================================================================
    // 4.1 Garman–Klass Volatility
    // σ² = (1/N) Σ [ 0.5 (ln(H/L))² − (2 ln 2 − 1) (ln(C/O))² ]
    // =============================================================================================

    private double computeGarmanKlass(BarSeries series, int lastIndex, int period) {

        int start = Math.max(0, lastIndex - period + 1);
        int n = lastIndex - start + 1;

        double sum = 0.0;
        double k = 2.0 * Math.log(2.0) - 1.0;

        for (int i = start; i <= lastIndex; i++) {
            double o = open(series, i);
            double h = high(series, i);
            double l = low(series, i);
            double c = close(series, i);

            double logHL = Math.log(h / l);
            double logCO = Math.log(c / o);

            double term = 0.5 * logHL * logHL - k * logCO * logCO;
            sum += term;
        }

        double variance = sum / n;
        return Math.sqrt(variance); // pode ser NaN ou Infinity → ok (Regra de Ouro)
    }

    // =============================================================================================
    // 4.1 Parkinson Volatility
    // σ² = (1 / (4 N ln 2)) Σ (ln(H/L))²
    // =============================================================================================

    private double computeParkinson(BarSeries series, int lastIndex, int period) {

        int start = Math.max(0, lastIndex - period + 1);
        int n = lastIndex - start + 1;

        double sumSq = 0.0;

        for (int i = start; i <= lastIndex; i++) {
            double h = high(series, i);
            double l = low(series, i);

            double logHL = Math.log(h / l);
            sumSq += logHL * logHL;
        }

        double denom = 4.0 * n * Math.log(2.0);
        double variance = sumSq / denom;
        return Math.sqrt(variance);
    }

    // =============================================================================================
    // 4.1 Rogers–Satchell Volatility
    // σ² = (1/N) Σ [ ln(H/O)(ln(H/O) − ln(C/O)) + ln(L/O)(ln(L/O) − ln(C/O)) ]
    // =============================================================================================

    private double computeRogersSatchell(BarSeries series, int lastIndex, int period) {

        int start = Math.max(0, lastIndex - period + 1);
        int n = lastIndex - start + 1;

        double sum = 0.0;

        for (int i = start; i <= lastIndex; i++) {
            double o = open(series, i);
            double h = high(series, i);
            double l = low(series, i);
            double c = close(series, i);

            double logHO = Math.log(h / o);
            double logLO = Math.log(l / o);
            double logCO = Math.log(c / o);

            sum += logHO * (logHO - logCO)
                    + logLO * (logLO - logCO);
        }

        double variance = sum / n;
        return Math.sqrt(variance);
    }

    // =============================================================================================
    // 4.2 Realized Volatility
    // RV = sqrt( Σ ln(C_t/C_{t-1})² )
    // =============================================================================================

    private double computeRealizedVol(BarSeries series, int lastIndex, int period) {

        int start = Math.max(1, lastIndex - period + 1);
        double sumSq = 0.0;

        for (int i = start; i <= lastIndex; i++) {
            double c = close(series, i);
            double p = close(series, i - 1);

            double r = Math.log(c / p);
            sumSq += r * r;
        }

        return Math.sqrt(sumSq);
    }

    // =============================================================================================
    // Realized Vol Z-Score
    // =============================================================================================

    private double computeRealizedVolZScore(BarSeries series,
                                            int lastIndex,
                                            int rvPeriod,
                                            int window) {

        int start = Math.max(1, lastIndex - window + 1);
        int n = lastIndex - start + 1;

        double[] vals = new double[n];
        double sum = 0.0;
        double sumSq = 0.0;

        int idx = 0;
        for (int i = start; i <= lastIndex; i++) {
            double rv = computeRealizedVol(series, i, rvPeriod);
            vals[idx++] = rv;
            sum   += rv;
            sumSq += rv * rv;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double std = Math.sqrt(variance);

        double lastRv = vals[n - 1];
        return (lastRv - mean) / std; // std==0 → explode (correto)
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================

    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        int last = series.getEndIndex();

        return switch (frame) {

            // Garman–Klass
            case VOL_GK_14 -> computeGarmanKlass(series, last, 14);
            case VOL_GK_30 -> computeGarmanKlass(series, last, 30);

            // Parkinson
            case VOL_PARK_14 -> computeParkinson(series, last, 14);
            case VOL_PARK_30 -> computeParkinson(series, last, 30);

            // Rogers–Satchell
            case VOL_RS_14 -> computeRogersSatchell(series, last, 14);
            case VOL_RS_30 -> computeRogersSatchell(series, last, 30);

            // Realized Vol
            case VOL_RV_10 -> computeRealizedVol(series, last, PERIOD_RV_10);
            case VOL_RV_30 -> computeRealizedVol(series, last, PERIOD_RV_30);
            case VOL_RV_50 -> computeRealizedVol(series, last, PERIOD_RV_50);

            // Realized Vol Z-score
            case VOL_RV_10_ZSC ->
                    computeRealizedVolZScore(series, last, PERIOD_RV_10, ZSCORE_WINDOW_RV_10);

            // Ratio RV10 / RV50
            case VOL_RV_10_50_RATIO -> {
                double rv10 = computeRealizedVol(series, last, PERIOD_RV_10);
                double rv50 = computeRealizedVol(series, last, PERIOD_RV_50);
                yield rv10 / rv50; // se rv50==0 → explode (correto)
            }

            default -> throw new IllegalArgumentException("Frame Volatility não suportado: " + frame);
        };
    }
}
