package br.com.yacamin.rafael.application.service.indicator.sentiment;

import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentIndicatorService {

    private static final double EPS = 1e-9;

    // ========================================================================
    // BASE UTILS
    // ========================================================================

    private double div(double a, double b) {
        if (Math.abs(b) < EPS) {
            throw new IllegalStateException("Divisão por zero em SentimentIndicatorService");
        }
        return a / b;
    }

    // ========================================================================
    // SENTIMENT INDICATORS
    // ========================================================================

    private double computeVolumeDelta(SymbolCandle c) {
        return c.getTakerBuyBaseVolume() - c.getTakerSellBaseVolume();
    }

    private double computeAggBuyRatio(SymbolCandle c) {
        double buy = c.getTakerBuyBaseVolume();
        double sell = c.getTakerSellBaseVolume();
        double total = buy + sell;

        return div(buy, total);
    }

    private double computeAggSellRatio(SymbolCandle c) {
        double buy = c.getTakerBuyBaseVolume();
        double sell = c.getTakerSellBaseVolume();
        double total = buy + sell;

        return div(sell, total);
    }

    private double computeCandleColor(SymbolCandle c) {
        double diff = c.getClose() - c.getOpen();
        return Math.signum(diff);
    }

    private double computeCandleStrength(SymbolCandle c) {
        double high = c.getHigh();
        double low = c.getLow();
        double close = c.getClose();

        double range = high - low;
        if (Math.abs(range) < EPS) {
            throw new IllegalStateException("Range zero em computeCandleStrength");
        }

        return (close - low) / range;
    }

    // ========================================================================
    // DISPATCHER
    // ========================================================================

    public double calculate(BarSeries series, SymbolCandle c, Frame frame) {

        return switch (frame) {

            case SENT_VOL_DELTA ->
                    computeVolumeDelta(c);

            case SENT_AGG_BUY_RATIO ->
                    computeAggBuyRatio(c);

            case SENT_AGG_SELL_RATIO ->
                    computeAggSellRatio(c);

            case SENT_CANDLE_COLOR ->
                    computeCandleColor(c);

            case SENT_CANDLE_STRENGTH ->
                    computeCandleStrength(c);

            default ->
                    throw new IllegalArgumentException("Frame Sentiment não suportado: " + frame);
        };
    }
}
