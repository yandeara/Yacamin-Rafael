package br.com.yacamin.rafael.application.service.indicator;

import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleReturnIndicatorService {

    private static final double EPS = 1e-9;

    // =========================================================================
    // UTILS (NOVO PADRÃO)
    // =========================================================================

    private double div(double a, double b) {
        if (Math.abs(b) < EPS) {
            throw new IllegalStateException("Divisão por zero em CandleReturnIndicatorService");
        }
        return a / b;
    }

    // =========================================================================
    // NORMALIZED RETURNS
    // =========================================================================

    private double computeBodyReturn(SymbolCandle c) {
        double open = c.getOpen();
        double diff = c.getClose() - open;
        return div(diff, open);
    }

    private double computeRangeReturn(SymbolCandle c) {
        double h = c.getHigh();
        double l = c.getLow();
        double close = c.getClose();

        double hlc3 = (h + l + close) / 3.0;
        double diff = h - l;

        return div(diff, hlc3);
    }

    private double computeUpperWickReturn(SymbolCandle c) {
        double h = c.getHigh();
        double oc = Math.max(c.getOpen(), c.getClose());
        return div(h - oc, h);
    }

    private double computeLowerWickReturn(SymbolCandle c) {
        double h = c.getHigh();
        double oc = Math.min(c.getOpen(), c.getClose());
        double l = c.getLow();

        return div(oc - l, h);
    }

    // =========================================================================
    // DISPATCHER (NOVO PADRÃO)
    // =========================================================================

    public double calculate(SymbolCandle candle, Frame frame) {

        return switch (frame) {

            case BODY_RETURN -> computeBodyReturn(candle);

            case RANGE_RETURN -> computeRangeReturn(candle);

            case UPPER_WICK_RETURN -> computeUpperWickReturn(candle);

            case LOWER_WICK_RETURN -> computeLowerWickReturn(candle);

            default -> throw new IllegalArgumentException(
                    "Frame CandleReturn não suportado: " + frame
            );
        };
    }
}
