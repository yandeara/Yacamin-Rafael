package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class BigMoveVolIndicatorService {

    private static final int WINDOW_FREQ_20 = 20;
    private static final int WINDOW_FREQ_50 = 50;

    private final AtrCacheService atrCacheService;

    // =============================================================================================
    // Helpers de preço
    // =============================================================================================

    private double close(BarSeries series, int i) {
        return series.getBar(i).getClosePrice().doubleValue();
    }

    // =============================================================================================
    // Definição de BIG MOVE:
    //
    // retAbs = |ln(C_t / C_{t-1})|
    // atrNorm = ATR_14(t) / C_t
    //
    // big move se retAbs > atrNorm
    // =============================================================================================

    private boolean isBigMove(BarSeries series, ATRIndicator atr14, int index) {
        double c = close(series, index);
        double p = close(series, index - 1);

        double retAbs = Math.abs(Math.log(c / p));  // se p=0 → explode → correto
        double atr    = atr14.getValue(index).doubleValue();

        double atrNorm = atr / c;                   // se c=0 → explode → correto

        return retAbs > atrNorm;
    }

    // =============================================================================================
    // Frequência de big moves em janela
    // =============================================================================================

    private double computeFreq(BarSeries series, ATRIndicator atr14, int last, int window) {

        int start = Math.max(1, last - window + 1);
        int total = last - start + 1;

        int count = 0;
        for (int i = start; i <= last; i++) {
            if (isBigMove(series, atr14, i)) {
                count++;
            }
        }

        return (double) count / (double) total;    // se total=0 → explode (correto)
    }

    // =============================================================================================
    // Age: distância desde o último big move (em candles)
    // =============================================================================================

    private double computeAge(BarSeries series, ATRIndicator atr14, int last) {

        int lastBig = -1;

        for (int i = last; i >= 1; i--) {
            if (isBigMove(series, atr14, i)) {
                lastBig = i;
                break;
            }
        }

        if (lastBig == -1) {
            // nunca houve big move → idade = tamanho da série
            return last + 1;
        }

        return last - lastBig;
    }

    // =============================================================================================
    // Cluster length = quantos big moves consecutivos até last
    // =============================================================================================

    private double computeClusterLen(BarSeries series, ATRIndicator atr14, int last) {

        int len = 0;
        int i = last;

        while (i >= 1 && isBigMove(series, atr14, i)) {
            len++;
            i--;
        }

        return len;
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================

    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol  = candle.getSymbol();
        var interval   = candle.getInterval();
        int last       = series.getEndIndex();

        ATRIndicator atr14 = atrCacheService.getAtr14(symbol, interval, series);

        return switch (frame) {

            case VOL_BIGMOVE_FREQ_20 ->
                    computeFreq(series, atr14, last, WINDOW_FREQ_20);

            case VOL_BIGMOVE_FREQ_50 ->
                    computeFreq(series, atr14, last, WINDOW_FREQ_50);

            case VOL_BIGMOVE_AGE ->
                    computeAge(series, atr14, last);

            case VOL_BIGMOVE_CLUSTER_LEN ->
                    computeClusterLen(series, atr14, last);

            default ->
                    throw new IllegalArgumentException("Frame BIGMOVE não suportado: " + frame);
        };
    }
}
