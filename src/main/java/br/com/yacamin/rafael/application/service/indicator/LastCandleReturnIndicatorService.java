package br.com.yacamin.rafael.application.service.indicator;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.StdCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class LastCandleReturnIndicatorService {

    private final AtrCacheService atrCacheService;
    private final StdCacheService stdCacheService;

    private static final double EPS = 1e-9;

    // =========================================================================
    // BASE
    // =========================================================================

    private double close(BarSeries s, int i) {
        return s.getBar(i).getClosePrice().doubleValue();
    }

    private double high(BarSeries s, int i) {
        return s.getBar(i).getHighPrice().doubleValue();
    }

    private double low(BarSeries s, int i) {
        return s.getBar(i).getLowPrice().doubleValue();
    }

    private double div(double a, double b) {
        if (Math.abs(b) < EPS) {
            throw new IllegalStateException("Divisão por zero em LastCandleReturnIndicatorService");
        }
        return a / b;
    }

    // =========================================================================
    // RETURNS PUROS
    // =========================================================================

    private double computeReturn(double c, double pc) {
        return div(c - pc, pc);
    }

    private double computeReturnLog(double c, double pc) {
        if (c <= 0 || pc <= 0) {
            throw new IllegalStateException("Valores não positivos em log-return");
        }
        return Math.log(c / pc);
    }

    private double computeReturnAtrN(double c, double pc, double atr) {
        return div(c - pc, atr);
    }

    private double computeReturnStdN(double c, double pc, double std) {
        return div(c - pc, std);
    }

    private double computeHighReturn(double high, double pc) {
        return div(high - pc, pc);
    }

    private double computeLowReturn(double low, double pc) {
        return div(low - pc, pc);
    }

    private double computeExtremeRange(double high, double low, double pc) {
        double up = Math.abs(high - pc);
        double dn = Math.abs(low - pc);
        return div(Math.max(up, dn), pc);
    }

    private double computeDirection(double c, double pc) {
        return Math.signum(c - pc);
    }

    private double computeAcceleration(double c, double pc, double prevReturn) {
        return computeReturn(c, pc) - prevReturn;
    }

    private double computeReversalForce(double c, double pc, double prevReturn) {
        double curr = computeReturn(c, pc);
        if (Math.signum(curr) != Math.signum(prevReturn)) {
            return Math.abs(curr);
        }
        return 0.0;
    }

    private double computeAbsStrength(double c, double pc) {
        return Math.abs(computeReturn(c, pc));
    }

    private double computeDominance(double c, double high, double low, double pc) {
        double up = Math.abs(high - pc);
        double dn = Math.abs(pc - low);
        double denom = up + dn;

        if (Math.abs(denom) < EPS) {
            throw new IllegalStateException("Denom zero em dominance");
        }

        return div(c - pc, denom);
    }

    private double computeRvr(double c, double pc, double atr) {
        return div(Math.abs(c - pc), atr);
    }

    // =========================================================================
    // DISPATCHER (NOVO PADRÃO)
    // =========================================================================

    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        int last = series.getEndIndex();
        if (last < 2) {
            throw new IllegalStateException("BarSeries insuficiente (< 2 candles) em LastCandleReturnIndicatorService");
        }

        double c = close(series, last);
        double pc = close(series, last - 1);
        double ppc = close(series, last - 2);

        double prevReturn = computeReturn(pc, ppc);

        double high = high(series, last);
        double low  = low(series, last);

        double atr = atrCacheService.getAtr14(
                candle.getSymbol(),
                candle.getInterval(),
                series
        ).getValue(last).doubleValue();

        double std = stdCacheService.getStd14(
                candle.getSymbol(),
                candle.getInterval(),
                series
        ).getValue(last).doubleValue();

        return switch (frame) {

            case RETURN -> computeReturn(c, pc);
            case RETURN_LOG -> computeReturnLog(c, pc);
            case RETURN_ATRN -> computeReturnAtrN(c, pc, atr);
            case RETURN_STDN -> computeReturnStdN(c, pc, std);

            case HIGH_RETURN -> computeHighReturn(high, pc);
            case LOW_RETURN -> computeLowReturn(low, pc);

            case EXTREME_RANGE_RETURN -> computeExtremeRange(high, low, pc);

            case RETURN_DIRECTION -> computeDirection(c, pc);
            case RETURN_ACCELERATION -> computeAcceleration(c, pc, prevReturn);
            case RETURN_REVERSAL_FORCE -> computeReversalForce(c, pc, prevReturn);

            case RETURN_ABSOLUTE_STRENGTH -> computeAbsStrength(c, pc);

            case RETURN_DOMINANCE_RATIO -> computeDominance(c, high, low, pc);

            case RVR -> computeRvr(c, pc, atr);

            default -> throw new IllegalArgumentException(
                    "Frame LastCandleReturn não suportado: " + frame
            );
        };
    }
}
