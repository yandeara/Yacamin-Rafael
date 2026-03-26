package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.AtrNormalizeDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.SmoothDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import br.com.yacamin.rafael.domain.RafaelBar;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Service
@RequiredArgsConstructor
public class PositionBalanceDerivation {

    private static final double EPS = 1e-12;

    private final SmoothDerivation smoothDerivation;
    private final StdHelperDerivation stdHelperDerivation;
    private final ZscoreDerivation zscoreDerivation;
    private final AtrNormalizeDerivation atrNormalizeDerivation;

    // =========================================================================
    // HELPERS — acesso ao RafaelBar / OHLC
    // =========================================================================

    private RafaelBar bar(BarSeries series, int index) {
        return (RafaelBar) series.getBar(index);
    }

    private double open(BarSeries series, int index) {
        return bar(series, index).getOpenPrice().doubleValue();
    }

    private double high(BarSeries series, int index) {
        return bar(series, index).getHighPrice().doubleValue();
    }

    private double low(BarSeries series, int index) {
        return bar(series, index).getLowPrice().doubleValue();
    }

    private double close(BarSeries series, int index) {
        return bar(series, index).getClosePrice().doubleValue();
    }

    private double rangeFromBar(BarSeries series, int index) {
        return high(series, index) - low(series, index);
    }

    private double closePosNormFromBar(BarSeries series, int index) {
        double r = rangeFromBar(series, index);
        if (r < EPS) return 0.0;
        return (close(series, index) - low(series, index)) / r;
    }

    private double closeOpenRatioFromBar(BarSeries series, int index) {
        double o = open(series, index);
        if (Math.abs(o) < EPS) return 0.0;
        return close(series, index) / o;
    }

    private double candleBalanceFromBar(BarSeries series, int index) {
        double h = high(series, index);
        double l = low(series, index);
        double o = open(series, index);
        double c = close(series, index);

        double r = h - l;
        if (r < EPS) return 0.0;

        double bodyTop    = Math.max(o, c);
        double bodyBottom = Math.min(o, c);

        double upper = h - bodyTop;
        double lower = bodyBottom - l;
        double pos   = (c - l) / r;

        double wickImb = Math.abs(upper - lower) / r;       // 0..1
        double posImb  = Math.abs(pos - 0.5) * 2.0;         // 0..1

        return 1.0 - 0.5 * (wickImb + posImb);              // ~1 equilibrado, ~0 pendente
    }

    // =========================================================================
    // 1. CORE POSITION (single candle) — RAW
    // =========================================================================

    /** mic_close_open_ratio = close / open */
    public double calculateCloseOpenRatio(SymbolCandle c) {
        if (Math.abs(c.getOpen()) < EPS) return 0.0;
        return c.getClose() / c.getOpen();
    }

    /** mic_close_open_norm = (close - open)/open */
    public double calculateCloseOpenNorm(SymbolCandle c) {
        if (Math.abs(c.getOpen()) < EPS) return 0.0;
        return (c.getClose() - c.getOpen()) / c.getOpen();
    }

    /** mic_close_pos_norm = (close - low) / range */
    public double calculateClosePosNorm(SymbolCandle c) {
        double r = c.getHigh() - c.getLow();
        if (r < EPS) return 0.0;
        return (c.getClose() - c.getLow()) / r;
    }

    // =========================================================================
    // 2. POSITION VS REFERENCES (HLC3, VWAP)
    // =========================================================================

    /** mic_close_hlc3_delta = close - HLC3 */
    public double calculateCloseHlc3Delta(SymbolCandle c) {
        double hlc3 = (c.getHigh() + c.getLow() + c.getClose()) / 3.0;
        return c.getClose() - hlc3;
    }

    /**
     * mic_close_vwap_delta = close - VWAP
     * VWAP vem de cache externo e é passado por parâmetro.
     */
    public double calculateCloseVwapDelta(SymbolCandle c, double vwap) {
        return c.getClose() - vwap;
    }

    // =========================================================================
    // 3. NORMALIZATION (ATR-N)
    // =========================================================================

    public double calculateCloseHlc3AtrN(ATRIndicator atr,
                                         SymbolCandle c,
                                         int index) {
        double delta = calculateCloseHlc3Delta(c);
        return atrNormalizeDerivation.normalize(atr, index, delta);
    }

    public double calculateCloseVwapAtrN(ATRIndicator atr,
                                         SymbolCandle c,
                                         double vwap,
                                         int index) {
        double delta = calculateCloseVwapDelta(c, vwap);
        return atrNormalizeDerivation.normalize(atr, index, delta);
    }

    // =========================================================================
    // 4. BALANCE SCORE (quanto o candle está equilibrado ou pendendo)
// =========================================================================

    /** mic_candle_balance_score — versão single-candle usando SymbolCandle */
    public double calculateCandleBalanceScore(SymbolCandle c) {
        double r = c.getHigh() - c.getLow();
        if (r < EPS) return 0.0;

        double bodyTop    = Math.max(c.getOpen(), c.getClose());
        double bodyBottom = Math.min(c.getOpen(), c.getClose());

        double upper = c.getHigh() - bodyTop;
        double lower = bodyBottom - c.getLow();
        double pos   = (c.getClose() - c.getLow()) / r;

        double wickImb = Math.abs(upper - lower) / r;
        double posImb  = Math.abs(pos - 0.5) * 2.0;

        return 1.0 - 0.5 * (wickImb + posImb);
    }

    // =========================================================================
    // 5. ANOMALIA / CHOQUE (Z-SCORE CURTO)
// =========================================================================

    public double calculateClosePosZscore(BarSeries series, int index, int window) {
        double[] vals = new double[window];

        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = closePosNormFromBar(series, index - i);
        }

        return zscoreDerivation.zscore(vals);
    }

    public double calculateBalanceZscore(BarSeries series, int index, int window) {
        double[] vals = new double[window];

        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = candleBalanceFromBar(series, index - i);
        }

        return zscoreDerivation.zscore(vals);
    }

    // =========================================================================
    // 6. DYNAMICS (WINDOW FEATURES, curto prazo)
// =========================================================================

    // CLOSE POSITION DYNAMICS
    public double calculateClosePosMa(BarSeries series, int index, int window) {
        double[] vals = new double[window];

        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = closePosNormFromBar(series, index - i);
        }

        return smoothDerivation.smooth(vals);
    }

    public double calculateClosePosVol(BarSeries series, int index, int window) {
        double[] vals = new double[window];

        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = closePosNormFromBar(series, index - i);
        }

        return stdHelperDerivation.std(vals);
    }

    // CLOSE / OPEN DYNAMICS
    public double calculateCloseOpenRatioMa(BarSeries series, int index, int window) {
        double[] vals = new double[window];

        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = closeOpenRatioFromBar(series, index - i);
        }

        return smoothDerivation.smooth(vals);
    }

    public double calculateCloseOpenRatioVol(BarSeries series, int index, int window) {
        double[] vals = new double[window];

        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = closeOpenRatioFromBar(series, index - i);
        }

        return stdHelperDerivation.std(vals);
    }

    // BALANCE DYNAMICS
    public double calculateBalanceMa(BarSeries series, int index, int window) {
        double[] vals = new double[window];

        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = candleBalanceFromBar(series, index - i);
        }

        return smoothDerivation.smooth(vals);
    }

    public double calculateBalanceVol(BarSeries series, int index, int window) {
        double[] vals = new double[window];

        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = candleBalanceFromBar(series, index - i);
        }

        return stdHelperDerivation.std(vals);
    }

    // =========================================================================
// TRIANGLE SCORE (geometric positional score)
// =========================================================================
    public double calculateCloseTriangleScore(SymbolCandle c) {
        double range = c.getHigh() - c.getLow();
        if (range < EPS) return 0.0;

        // posição normalizada do close dentro do range (0..1)
        double pos = (c.getClose() - c.getLow()) / range;

        // distância do centro, normalizada (0 = centro, 1 = extremos)
        double dist = Math.abs(pos - 0.5) * 2.0;

        return dist;  // 0 → neutro, 1 → extremo geométrico
    }

    // =========================================================================
// V3 EXTENSIONS
// =========================================================================

    /** mic_close_to_high_norm = (high - close) / range */
    public double calculateCloseToHighNorm(SymbolCandle c) {
        double r = c.getHigh() - c.getLow();
        if (r < EPS) return 0.0;
        return (c.getHigh() - c.getClose()) / (r + EPS);
    }

    /** mic_close_to_low_norm = (close - low) / range */
    public double calculateCloseToLowNorm(SymbolCandle c) {
        double r = c.getHigh() - c.getLow();
        if (r < EPS) return 0.0;
        return (c.getClose() - c.getLow()) / (r + EPS);
    }

    /**
     * mic_balance_state:
     * 0=NEUTRAL, 1=BULL_CONTROL, 2=BEAR_CONTROL, 3=INDECISION/EXHAUSTION
     *
     * Regra simples (independente de outros warmups):
     * - Se balance_score alto => NEUTRAL
     * - Senão: close alto => BULL_CONTROL
     * - Senão: close baixo => BEAR_CONTROL
     * - Senão: indecisão/exaustão
     */
    public double calculateBalanceState(SymbolCandle c) {
        double pos = calculateClosePosNorm(c);              // 0..1
        double bal = calculateCandleBalanceScore(c);        // ~1 balanced, ~0 pendente

        if (bal >= 0.75) return 0;          // NEUTRAL / balanced
        if (pos >= 0.66) return 1;          // BULL_CONTROL
        if (pos <= 0.34) return 2;          // BEAR_CONTROL
        return 3;                           // INDECISION / EXHAUSTION
    }

    /**
     * mic_close_triangle_score_atrn:
     * distância do close ao mid-range, em unidades de ATR.
     * (isso dá “regime-awareness” de verdade; melhor que normalizar um score 0..1)
     */
    public double calculateCloseTriangleScoreAtrn(ATRIndicator atr, SymbolCandle c, int index) {
        double atrV = atr.getValue(index).doubleValue();
        if (atrV < EPS) return 0.0;

        double mid = (c.getHigh() + c.getLow()) / 2.0;
        double dist = Math.abs(c.getClose() - mid);   // em preço

        return dist / (atrV + EPS);
    }

}
