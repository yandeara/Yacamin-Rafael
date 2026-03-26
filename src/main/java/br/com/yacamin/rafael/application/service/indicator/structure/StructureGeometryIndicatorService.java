package br.com.yacamin.rafael.application.service.indicator.structure;

import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class StructureGeometryIndicatorService {

    private static final double EPS = 1e-9;

    // =========================================================================
    // BASE
    // =========================================================================

    private double computeBodySize(SymbolCandle c) {
        return Math.abs(c.getClose() - c.getOpen());
    }

    private double computeBodyDelta(SymbolCandle c) {
        return c.getClose() - c.getOpen();
    }

    private double computeUpperWickSize(SymbolCandle c) {
        double high = c.getHigh();
        double maxOC = Math.max(c.getOpen(), c.getClose());
        return high - maxOC;
    }

    private double computeLowerWickSize(SymbolCandle c) {
        double minOC = Math.min(c.getOpen(), c.getClose());
        return minOC - c.getLow();
    }

    private double computeBodyRangeRatio(SymbolCandle c) {
        double body = Math.abs(c.getClose() - c.getOpen());
        double range = c.getHigh() - c.getLow();

        if (Math.abs(range) < EPS) {
            throw new IllegalStateException("Range zero em computeBodyRangeRatio");
        }

        return body / range;
    }

    private double computeHlocCompression(SymbolCandle c) {
        double high  = c.getHigh();
        double low   = c.getLow();
        double open  = c.getOpen();
        double close = c.getClose();

        double range = high - low;
        if (Math.abs(range) < EPS) {
            throw new IllegalStateException("Range zero em computeHlocCompression");
        }

        double body = Math.abs(close - open);

        if (Math.abs(body) < EPS) {
            // Regra de Ouro: sem fallback → erro explícito
            throw new IllegalStateException("Body zero em computeHlocCompression (doji)");
        }

        return range / body;
    }

    // =========================================================================
    // DISPATCHER
    // =========================================================================

    public double calculate(BarSeries series, SymbolCandle c, Frame frame) {

        return switch (frame) {

            case GEO_BODY_SIZE ->
                    computeBodySize(c);

            case GEO_BODY_DELTA ->
                    computeBodyDelta(c);

            case GEO_UPPER_WICK_SIZE ->
                    computeUpperWickSize(c);

            case GEO_LOWER_WICK_SIZE ->
                    computeLowerWickSize(c);

            case GEO_BODY_RANGE_RATIO ->
                    computeBodyRangeRatio(c);

            case GEO_HLOC_COMPRESSION ->
                    computeHlocCompression(c);

            default ->
                    throw new IllegalArgumentException("Frame StructureGeometry não suportado: " + frame);
        };
    }
}
