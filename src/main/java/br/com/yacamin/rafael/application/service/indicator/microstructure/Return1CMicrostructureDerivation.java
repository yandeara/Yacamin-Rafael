package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.AtrNormalizeDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.domain.RafaelBar;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Service
@RequiredArgsConstructor
public class Return1CMicrostructureDerivation {

    private static final double EPS = 1e-12;

    private final AtrNormalizeDerivation atrNormalizeDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =========================================================================
    // HELPERS - acesso a RafaelBar / OHLC / TRUE RANGE
    // =========================================================================

    private RafaelBar bar(BarSeries series, int index) {
        return (RafaelBar) series.getBar(index);
    }

    private double close(BarSeries series, int index) {
        return bar(series, index).getClosePrice().doubleValue();
    }

    private double high(BarSeries series, int index) {
        return bar(series, index).getHighPrice().doubleValue();
    }

    private double low(BarSeries series, int index) {
        return bar(series, index).getLowPrice().doubleValue();
    }

    private double range(BarSeries series, int index) {
        return high(series, index) - low(series, index);
    }

    /**
     * True Range (gap-aware): max( high-low, |high-prevClose|, |low-prevClose| )
     */
    private double trueRange(BarSeries series, int index) {
        RafaelBar b    = bar(series, index);
        RafaelBar prev = bar(series, index - 1);

        double h  = b.getHighPrice().doubleValue();
        double l  = b.getLowPrice().doubleValue();
        double pc = prev.getClosePrice().doubleValue();

        double r1 = h - l;
        double r2 = Math.abs(h - pc);
        double r3 = Math.abs(l - pc);

        return Math.max(r1, Math.max(r2, r3));
    }

    // =========================================================================
    // CORE RETURNS (último candle)
    // =========================================================================

    /** mic_return = (close_t / close_{t-1}) - 1 */
    public double micReturn(BarSeries series, int index) {
        double c1 = close(series, index);
        double c0 = close(series, index - 1);

        if (Math.abs(c0) < EPS) return 0.0;
        return (c1 / c0) - 1.0;
    }

    /** mic_return_log = log(close_t / close_{t-1}) */
    public double micReturnLog(BarSeries series, int index) {
        double c1 = close(series, index);
        double c0 = close(series, index - 1);

        if (Math.abs(c0) < EPS) return 0.0;
        return Math.log(c1 / c0);
    }

    /**
     * mic_return_atrn:
     * normaliza o "equivalente em preço" do retorno (ret * prevClose) pelo ATR.
     */
    public double micReturnAtrn(SymbolCandle candle,
                                BarSeries series,
                                int index,
                                ATRIndicator atr) {

        double ret       = micReturn(series, index);
        double prevClose = close(series, index - 1);
        double priceMove = ret * prevClose;

        return atrNormalizeDerivation.normalize(atr, index, priceMove);
    }

    /**
     * mic_return_stdn:
     * retorno atual / std dos retornos dos últimos "window" candles.
     */
    public double micReturnStdn(BarSeries series, int index, int window) {

        double[] rets = new double[window];

        for (int i = 0; i < window; i++) {
            rets[i] = micReturn(series, index - i);
        }

        double sd = stdHelperDerivation.std(rets);
        if (sd < EPS) return 0.0;

        return rets[0] / sd;
    }

    /** mic_return_direction: +1, -1, 0 */
    public double micReturnDirection(BarSeries series, int index) {
        double r = micReturn(series, index);
        if (r > EPS) return 1.0;
        if (r < -EPS) return -1.0;
        return 0.0;
    }

    // =========================================================================
    // RETURN DYNAMICS
    // =========================================================================

    /** mic_return_acceleration: ret_t - ret_{t-lag} */
    public double micReturnAcceleration(BarSeries series, int index, int lag) {
        if (lag <= 0) return 0.0;
        double rNow  = micReturn(series, index);
        double rPrev = micReturn(series, index - lag);
        return rNow - rPrev;
    }

    /** mic_return_reversal_force: se mudou sinal de t-1 pra t, |rNow - rPrev| */
    public double micReturnReversalForce(BarSeries series, int index) {
        double rNow  = micReturn(series, index);
        double rPrev = micReturn(series, index - 1);

        if (Math.abs(rNow) < EPS || Math.abs(rPrev) < EPS) return 0.0;

        boolean oppositeSign = (rNow > 0 && rPrev < 0) || (rNow < 0 && rPrev > 0);
        if (!oppositeSign) return 0.0;

        return Math.abs(rNow - rPrev);
    }

    /** mic_return_absolute_strength: |ret| */
    public double micReturnAbsoluteStrength(BarSeries series, int index) {
        return Math.abs(micReturn(series, index));
    }

    // =========================================================================
    // DOMINANCE & RVR
    // =========================================================================

    /**
     * mic_return_dominance_ratio:
     * ret / (range/prevClose)  => "ret consumiu quanto do range"
     */
    public double micReturnDominanceRatio(BarSeries series, int index) {
        double ret       = micReturn(series, index);
        double prevClose = close(series, index - 1);
        double rng       = range(series, index);

        if (Math.abs(prevClose) < EPS) return 0.0;

        double rRange = rng / prevClose;
        if (Math.abs(rRange) < EPS) return 0.0;

        return ret / rRange;
    }

    /**
     * mic_rvr (AJUSTADO):
     * Return vs TrueRange (gap-aware): (close - prevClose) / trueRange
     * (mantido por compatibilidade, mas agora é gap-aware)
     */
    public double micRvr(BarSeries series, int index) {
        double prevClose = close(series, index - 1);
        double c1        = close(series, index);
        double tr        = trueRange(series, index);

        if (Math.abs(tr) < EPS) return 0.0;

        return (c1 - prevClose) / (tr + EPS);
    }

    // =========================================================================
    // LOG-RETURN DOMINANCE (AJUSTADO — mic_log_return_dominance)
    // =========================================================================

    /**
     * mic_log_return_dominance (AJUSTADO):
     * (logRet * prevClose) / trueRange
     * => eficiência do movimento (em preço) vs espaço gap-aware
     */
    public double micLogReturnDominance(BarSeries series, int index) {

        double c1 = close(series, index);
        double c0 = close(series, index - 1);

        if (Math.abs(c0) < EPS) return 0.0;

        double logRet = Math.log(c1 / c0);
        double tr     = trueRange(series, index);

        if (tr < EPS) return 0.0;

        double logMovePrice = logRet * c0;
        return logMovePrice / (tr + EPS);
    }

    // =========================================================================
    // V3 EXTENSIONS (NOVOS CAMPOS)
    // =========================================================================

    /** mic_return_tr_dominance = (close - prevClose) / trueRange */
    public double micReturnTrDominance(BarSeries series, int index) {
        double prevClose = close(series, index - 1);
        double c1        = close(series, index);
        double tr        = trueRange(series, index);

        if (Math.abs(tr) < EPS) return 0.0;
        return (c1 - prevClose) / (tr + EPS);
    }

    /** mic_return_gap_pressure = sign(ret) * (trueRange - range) / (trueRange + eps) */
    public double micReturnGapPressure(BarSeries series, int index) {
        double tr  = trueRange(series, index);
        if (tr < EPS) return 0.0;

        double rng = range(series, index);
        double gapShare = (tr - rng) / (tr + EPS);

        double dir = micReturnDirection(series, index);
        if (dir == 0.0) return 0.0;

        return dir * gapShare;
    }

    /** mic_return_sign_prst_w20 = % de returns > 0 nos últimos window */
    public double micReturnSignPersistence(BarSeries series, int index, int window) {

        int w = Math.max(1, Math.min(window, index));
        int positives = 0;

        for (int i = 0; i < w; i++) {
            double r = micReturn(series, index - i);
            if (r > 0) positives++;
        }

        return positives / (double) w;
    }

    /** mic_return_run_len = sequência atual de sinais do retorno (signed) */
    public double micReturnRunLen(BarSeries series, int index, int maxLookback) {

        double rNow = micReturn(series, index);
        if (Math.abs(rNow) < EPS) return 0.0;

        int sign = rNow > 0 ? 1 : -1;
        int lookback = Math.max(1, Math.min(maxLookback, index));

        int count = 0;

        for (int i = 0; i < lookback; i++) {
            double r = micReturn(series, index - i);

            if (Math.abs(r) < EPS) break;

            int s = r > 0 ? 1 : -1;
            if (s != sign) break;

            count++;
        }

        return sign * (double) count;
    }
}
