package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.volatility.BollingerCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.BollingerCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class BollingerIndicatorService {

    private static final int ZSCORE_WINDOW_BOLL_20_WIDTH = 200;

    private final BollingerCacheService bollingerCacheService;

    // =============================================================================================
    // Helpers
    // =============================================================================================

    /** Width = upper - lower */
    private double computeWidth(BollingerCacheDto bb, int index) {
        double up  = bb.getIndicatorUp().getValue(index).doubleValue();
        double low = bb.getIndicatorLow().getValue(index).doubleValue();
        return Math.abs(up - low);
    }

    /** Change rate = width_t / width_{t-1} - 1 */
    private double computeWidthChg(BollingerCacheDto bb, int index) {
        double curr = computeWidth(bb, index);
        double prev = computeWidth(bb, index - 1);
        return (curr / prev) - 1.0;  // se prev == 0 → explode (Regra de Ouro)
    }

    /** Z-score do width em janela deslizante */
    private double computeWidthZScore(BollingerCacheDto bb, int index, int window) {

        int start = Math.max(0, index - window + 1);
        int n     = index - start + 1;

        double sum   = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double w = computeWidth(bb, i);
            sum   += w;
            sumSq += w * w;
        }

        double mean = sum / n;
        double var  = (sumSq / n) - (mean * mean);
        double std  = Math.sqrt(var);  // se var < 0 → retorna NaN → OK pela Regra de Ouro

        double last = computeWidth(bb, index);
        return (last - mean) / std;  // se std == 0 → explode (correto)
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        BollingerCacheDto bb20 = bollingerCacheService.getBb20(symbol, interval, series);

        return switch (frame) {

            case BOLL_20_WIDTH ->
                    computeWidth(bb20, last);

            case BOLL_20_WIDTH_CHG ->
                    computeWidthChg(bb20, last);

            case BOLL_20_WIDTH_ZSC ->
                    computeWidthZScore(bb20, last, ZSCORE_WINDOW_BOLL_20_WIDTH);

            default ->
                    throw new IllegalArgumentException("Frame BOLLINGER não suportado: " + frame);
        };
    }
}
