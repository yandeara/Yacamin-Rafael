package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.CloseReturnCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class CloseReturnDerivation {

    private final CloseReturnCacheService closeReturnCacheService;

    // Retorno simples: close(t)/close(t-window) - 1 (via Indicator)
    public double calculateSimpleReturn(SymbolCandle candle, BarSeries series, int last, int window) {
        return closeReturnCacheService
                .getCloseReturn(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(last).doubleValue();
    }

    // Retorno absoluto
    public double calculateAbsoluteReturn(double simpleReturn) {
        return Math.abs(simpleReturn);
    }

    // Burst Strength — max |ret_1|
    public double calculateBurstStrength(SymbolCandle candle, BarSeries series, int index, int bWindow) {
        return closeReturnCacheService
                .getBurstStrength(candle.getSymbol(), candle.getInterval(), series, bWindow)
                .getValue(index).doubleValue();
    }

    // Continuation Rate — directional persistence of ret_1
    public double calculateContinuationRate(SymbolCandle candle, BarSeries series, int index, int window) {
        return closeReturnCacheService
                .getContinuationRate(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // Decay Rate — slope of |ret_1|
    public double calculateDecayRate(SymbolCandle candle, BarSeries series, int index, int window) {
        return closeReturnCacheService
                .getDecayRate(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // Impulse — directional accumulated ret_1
    public double calculateImpulse(SymbolCandle candle, BarSeries series, int index, int window) {
        return closeReturnCacheService
                .getImpulse(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // Chop Ratio — sign change frequency of ret_1
    public double calculateChopRatio(SymbolCandle candle, BarSeries series, int index, int window) {
        return closeReturnCacheService
                .getChopRatio(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }
}
