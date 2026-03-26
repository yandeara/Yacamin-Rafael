package br.com.yacamin.rafael.application.service.indicator;

import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleGeometryIndicatorService {

    private static final double EPS = 1e-9;

    // =========================================================================
    // BASE
    // =========================================================================

    private double computeBody(SymbolCandle c) {
        return c.getClose() - c.getOpen();
    }

    private double computeRange(SymbolCandle c) {
        return c.getHigh() - c.getLow();
    }

    private double computeUpperWick(SymbolCandle c) {
        double high = c.getHigh();
        double maxOC = Math.max(c.getOpen(), c.getClose());
        return high - maxOC;
    }

    private double computeLowerWick(SymbolCandle c) {
        double minOC = Math.min(c.getOpen(), c.getClose());
        return minOC - c.getLow();
    }

    private double computePct(double value, double range) {
        if (Math.abs(range) < EPS) {
            return 0;
        }
        return value / range;
    }

    private double computeCandleType(SymbolCandle c) {
        return Math.signum(c.getClose() - c.getOpen());
    }

    // =========================================================================
    // AVANÇADOS
    // =========================================================================

    private double computeClosePosNorm(SymbolCandle c) {
        double low = c.getLow();
        double high = c.getHigh();
        double range = high - low;

        if (Math.abs(range) < EPS) {
            return 0;
        }

        return (c.getClose() - low) / range;
    }

    private double computeBodyRatio(SymbolCandle c) {
        double body = Math.abs(computeBody(c));
        double range = computeRange(c);

        if (Math.abs(range) < EPS) {
            return 0;
        }

        return body / range;
    }

    private double computeWickImbalance(SymbolCandle c) {
        double up = computeUpperWick(c);
        double dn = computeLowerWick(c);
        double denom = up + dn;

        if (Math.abs(denom) < EPS) {
            return 0.0;
        }

        return (dn - up) / denom;
    }

    private double computePressureRaw(SymbolCandle c) {
        return computeBody(c);
    }

    private double computeStrength(SymbolCandle c) {
        double range = computeRange(c);

        if (Math.abs(range) < EPS) {
            return 0;
        }

        return computeBody(c) / range;
    }

    private double computeBodyStrengthScore(SymbolCandle c) {
        return computeStrength(c);
    }

    private double computeWickPressureScore(SymbolCandle c) {
        return computeWickImbalance(c);
    }

    private double computeEntropy(SymbolCandle c) {

        double open = c.getOpen();
        double close = c.getClose();
        double high = c.getHigh();
        double low = c.getLow();

        double range = high - low;

        if (Math.abs(range) < EPS) {
            return 0;
        }

        double uw = high - Math.max(open, close);
        double lw = Math.min(open, close) - low;
        double body = Math.abs(close - open);

        double p1 = uw / range;
        double p2 = body / range;
        double p3 = lw / range;

        double e = 0;
        if (p1 > 0) e -= p1 * Math.log(p1);
        if (p2 > 0) e -= p2 * Math.log(p2);
        if (p3 > 0) e -= p3 * Math.log(p3);

        return e / Math.log(3.0);
    }

    private double computeDirection(SymbolCandle c) {
        return Math.signum(computeBody(c));
    }

    private double computeGeometryScore(SymbolCandle c) {

        double open = c.getOpen();
        double close = c.getClose();
        double high = c.getHigh();
        double low = c.getLow();

        double range = high - low;

        if (Math.abs(range) < EPS) {
            return 0;
        }

        double bodyPct = (close - open) / range;
        double up = computeUpperWick(c) / range;
        double dn = computeLowerWick(c) / range;

        return bodyPct * 0.5 + dn * 0.2 + up * 0.1 + Math.signum(bodyPct) * 0.2;
    }

    private double computeTriangleScore(SymbolCandle c) {

        double open = c.getOpen();
        double close = c.getClose();
        double high = c.getHigh();
        double low = c.getLow();

        double range = high - low;

        if (Math.abs(range) < EPS) {
            return 0;
        }

        double bodyCenter = (open + close) / 2.0;
        double mid = (high + low) / 2.0;

        return (bodyCenter - mid) / range;
    }

    private double computeBodyAbs(SymbolCandle c) {
        return Math.abs(computeBody(c));
    }

    private double computeBodyCenterPosition(SymbolCandle c) {

        double open = c.getOpen();
        double close = c.getClose();
        double high = c.getHigh();
        double low = c.getLow();

        double range = high - low;

        if (Math.abs(range) < EPS)
            return 0;

        double bodyCenter = (open + close) / 2.0;
        return (bodyCenter - low) / range;
    }

    private double computeShapeIndex(SymbolCandle c) {

        double open = c.getOpen();
        double close = c.getClose();
        double high = c.getHigh();
        double low = c.getLow();

        double range = high - low;

        if (Math.abs(range) < EPS)
            return 0;

        double body = Math.abs(close - open) / range;
        double up   = (high - Math.max(open, close)) / range;
        double dn   = (Math.min(open, close) - low) / range;

        double sum = body + up + dn;

        if (sum < EPS)
            return 0;

        body /= sum;
        up   /= sum;
        dn   /= sum;

        double raw = body * body + up * up + dn * dn;

        return (raw - 1.0/3.0) * 1.5;
    }

    private double computeSymmetryScore(SymbolCandle c) {
        double up = computeUpperWick(c);
        double dn = computeLowerWick(c);
        double denom = up + dn;

        //Calculo correto, candle sem sombra precisa de valor zerado
        if (Math.abs(denom) < EPS)
            return 0;

        return Math.abs(up - dn) / denom;
    }

    private double computeBrr(SymbolCandle c) {
        double body = Math.abs(computeBody(c));
        double range = computeRange(c);

        if (Math.abs(range) < EPS)
            return 0;

        return (body * body) / (range * range);
    }

    private double computeVolInside(SymbolCandle c) {
        double body = Math.abs(computeBody(c));
        double range = computeRange(c);

        if (Math.abs(range) < EPS)
            return 0;

        return body / range;
    }

    private double computeShadowRatio(SymbolCandle c) {
        double up = computeUpperWick(c);
        double dn = computeLowerWick(c);
        double r = computeRange(c);

        //Calculo correto, candle sem sombra precisa de valor zerado
        if (Math.abs(r) < EPS)
            return 0;

        return (up + dn) / r;
    }

    private double computeSpreadRatio(SymbolCandle c) {
        double high = c.getHigh();
        double low  = c.getLow();
        double close = c.getClose();

        double hlc3 = (high + low + close) / 3.0;
        if (Math.abs(hlc3) < EPS)
            return 0;

        return (high - low) / hlc3;
    }

    private double computeWickBodyAlignment(SymbolCandle c) {
        double up = computeUpperWick(c);
        double dn = computeLowerWick(c);
        double r = computeRange(c);

        if (Math.abs(r) < EPS)
            return 0;

        return (up - dn) / r;
    }

    private double computeCompressionIndex(SymbolCandle c) {
        return computeBodyRatio(c);
    }

    private double computeLMR(SymbolCandle c) {
        return computeBodyRatio(c);
    }

    // =========================================================================
    // DISPATCH
    // =========================================================================

    public double calculate(BarSeries series, SymbolCandle c, Frame frame) {

        return switch (frame) {

            case CANDLE_BODY -> computeBody(c);
            case CANDLE_RANGE -> computeRange(c);

            case CANDLE_UPPER_WICK -> computeUpperWick(c);
            case CANDLE_LOWER_WICK -> computeLowerWick(c);

            case CANDLE_BODY_PCT -> computePct(computeBody(c), computeRange(c));
            case CANDLE_UPPER_WICK_PCT -> computePct(computeUpperWick(c), computeRange(c));
            case CANDLE_LOWER_WICK_PCT -> computePct(computeLowerWick(c), computeRange(c));

            case CANDLE_TYPE -> computeCandleType(c);

            case CANDLE_CLOSE_POS_NORM -> computeClosePosNorm(c);

            case CANDLE_BODY_RATIO -> computeBodyRatio(c);
            case CANDLE_WICK_IMBALANCE -> computeWickImbalance(c);

            case CANDLE_PRESSURE_RAW -> computePressureRaw(c);
            case CANDLE_STRENGTH -> computeStrength(c);
            case CANDLE_BODY_STRENGTH_SCORE -> computeBodyStrengthScore(c);
            case CANDLE_WICK_PRESSURE_SCORE -> computeWickPressureScore(c);

            case CANDLE_ENTROPY -> computeEntropy(c);
            case CANDLE_DIRECTION -> computeDirection(c);
            case CANDLE_GEOMETRY_SCORE -> computeGeometryScore(c);

            case CANDLE_TRIANGLE_SCORE -> computeTriangleScore(c);
            case CANDLE_BODY_ABS -> computeBodyAbs(c);

            case CANDLE_BODY_CENTER_POSITION -> computeBodyCenterPosition(c);

            case CANDLE_SHAPE_INDEX -> computeShapeIndex(c);
            case CANDLE_SYMMETRY_SCORE -> computeSymmetryScore(c);

            case CANDLE_BRR -> computeBrr(c);
            case CANDLE_VOLATILITY_INSIDE -> computeVolInside(c);

            case CANDLE_SHADOW_RATIO -> computeShadowRatio(c);
            case CANDLE_SPREAD_RATIO -> computeSpreadRatio(c);

            case CANDLE_WICK_BODY_ALIGNMENT -> computeWickBodyAlignment(c);
            case CANDLE_COMPRESSION_INDEX -> computeCompressionIndex(c);

            case CANDLE_LMR -> computeLMR(c);

            default -> throw new IllegalArgumentException("Frame CandleGeometry não suportado: " + frame);
        };
    }
}
