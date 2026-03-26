package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloseReturnIndicatorService {

    private static final int WINDOW_10 = 10;

    // =============================================================================================
    // Retorno simples: close(t) / close(t - window) - 1
    // =============================================================================================
    private double computeReturn(BarSeries series, int window) {

        int last = series.getEndIndex();
        int prevIndex = last - window;

        double closeNow  = series.getBar(last).getClosePrice().doubleValue();
        double closePrev = series.getBar(prevIndex).getClosePrice().doubleValue();

        return (closeNow / closePrev) - 1.0;   // se closePrev=0 → explode → Regra de Ouro
    }

    // =============================================================================================
    // Retorno absoluto: |retorno|
    // =============================================================================================
    private double computeAbsReturn(BarSeries series, int window) {
        return Math.abs(computeReturn(series, window));
    }

    // =============================================================================================
    // Vetor de retornos de 1 candle: ret_1[i] = close(i)/close(i-1) - 1
    // =============================================================================================
    private double[] computeReturns1Window(BarSeries series, int window) {

        int last = series.getEndIndex();
        int start = last - window + 1;

        double[] rets = new double[window];

        for (int i = 0; i < window; i++) {
            int idx = start + i;

            double closeNow  = series.getBar(idx).getClosePrice().doubleValue();
            double closePrev = series.getBar(idx - 1).getClosePrice().doubleValue();

            rets[i] = (closeNow / closePrev) - 1.0;   // se closePrev=0 → explode
        }

        return rets;
    }

    private int sign(double v) {
        return v > 0.0 ? 1 : (v < 0.0 ? -1 : 0);
    }

    // =============================================================================================
    // Burst Strength — max |ret_1|
    // =============================================================================================
    private double computeBurstStrength(BarSeries series, int window) {

        double[] rets = computeReturns1Window(series, window);

        double maxAbs = 0.0;
        for (double r : rets) {
            double abs = Math.abs(r);
            if (abs > maxAbs) maxAbs = abs;
        }

        return maxAbs;
    }

    // =============================================================================================
    // Continuation rate: fração de retornos com mesmo sinal do último
    // =============================================================================================
    private double computeContinuationRate(BarSeries series, int window) {

        double[] rets = computeReturns1Window(series, window);

        double lastRet = rets[rets.length - 1];
        int currSign = sign(lastRet);

        int count = 0;
        for (double r : rets) {
            if (sign(r) == currSign) count++;
        }

        return (double) count / (double) rets.length;
    }

    // =============================================================================================
    // Decay rate: slope de |ret| ao longo do tempo
    // =============================================================================================
    private double computeDecayRate(BarSeries series, int window) {

        double[] rets = computeReturns1Window(series, window);

        int n = rets.length;
        double[] y = new double[n];
        for (int i = 0; i < n; i++) y[i] = Math.abs(rets[i]);

        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double v = y[i];

            sumX  += x;
            sumY  += v;
            sumXY += x * v;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);  // se denom=0, explode → Regra de Ouro
        return (n * sumXY - sumX * sumY) / denom;
    }

    // =============================================================================================
    // Impulse: sum(sign(ret_i) * |ret_i|)
    // =============================================================================================
    private double computeImpulse(BarSeries series, int window) {

        double[] rets = computeReturns1Window(series, window);
        double sum = 0.0;

        for (double r : rets) {
            sum += sign(r) * Math.abs(r);
        }

        return sum;
    }

    // =============================================================================================
    // Chop Ratio: (# trocas de sinal) / (n - 1)
    // =============================================================================================
    private double computeChopRatio(BarSeries series, int window) {

        double[] rets = computeReturns1Window(series, window);
        int n = rets.length;

        int changes = 0;
        int prev = sign(rets[0]);

        for (int i = 1; i < n; i++) {
            int s = sign(rets[i]);
            if (s != 0 && prev != 0 && s != prev) changes++;
            if (s != 0) prev = s;
        }

        return changes / (double) (n - 1);  // se n==1 explode → OK
    }

    // =============================================================================================
    // Dispatcher
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        return switch (frame) {

            // Retornos simples
            case CLOSE_RET_1   -> computeReturn(series, 1);
            case CLOSE_RET_3   -> computeReturn(series, 3);
            case CLOSE_RET_5   -> computeReturn(series, 5);
            case CLOSE_RET_10  -> computeReturn(series, 10);

            // Retornos absolutos
            case CLOSE_RET_1_ABS  -> computeAbsReturn(series, 1);
            case CLOSE_RET_3_ABS  -> computeAbsReturn(series, 3);
            case CLOSE_RET_5_ABS  -> computeAbsReturn(series, 5);
            case CLOSE_RET_10_ABS -> computeAbsReturn(series, 10);

            // Momentum (janela 10)
            case CLOSE_RET_BUSTR_10   -> computeBurstStrength(series, WINDOW_10);
            case CLOSE_RET_CNTRATE_10 -> computeContinuationRate(series, WINDOW_10);
            case CLOSE_RET_DCAY_10    -> computeDecayRate(series, WINDOW_10);
            case CLOSE_RET_IMPLS_10   -> computeImpulse(series, WINDOW_10);
            case CLOSE_RET_CHPRT_10   -> computeChopRatio(series, WINDOW_10);

            default ->
                    throw new IllegalArgumentException("Frame Return Momentum não suportado: " + frame);
        };
    }
}
