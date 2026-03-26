package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.volatility.BollingerCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.BollingerCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeltnerIndicatorService {

    // width = 4 × ATR_14  (equivalente a multiplier = 2.0 no canal)
    private static final double KELT_WIDTH_FACTOR = 4.0;

    private final BollingerCacheService bollingerCacheService;
    private final AtrCacheService atrCacheService;

    // =============================================================================================
    // Helpers (double-only)
    // =============================================================================================

    /** Bollinger width = |upper - lower| */
    private double computeBollWidth(BollingerCacheDto bb, int index) {
        double up  = bb.getIndicatorUp().getValue(index).doubleValue();
        double low = bb.getIndicatorLow().getValue(index).doubleValue();
        return Math.abs(up - low);
    }

    /** Keltner width = 4 × ATR_14 */
    private double computeKeltnerWidth(ATRIndicator atr14, int index) {
        double atr = atr14.getValue(index).doubleValue();
        return atr * KELT_WIDTH_FACTOR;    // se atr == 0 → explode em quem dividir DEPOIS → correto
    }

    /** Squeeze = BOLL_width / KELT_width */
    private double computeSqueeze(double bollWidth, double keltWidth) {
        return bollWidth / keltWidth;      // se keltWidth == 0 → explode → Regra de Ouro
    }

    /** Squeeze change = sqz(t) / sqz(t-1) - 1 */
    private double computeSqueezeChange(BollingerCacheDto bb20,
                                        ATRIndicator atr14,
                                        int index) {

        double bwCurr = computeBollWidth(bb20, index);
        double kwCurr = computeKeltnerWidth(atr14, index);
        double sqzCurr = computeSqueeze(bwCurr, kwCurr);

        double bwPrev = computeBollWidth(bb20, index - 1);
        double kwPrev = computeKeltnerWidth(atr14, index - 1);
        double sqzPrev = computeSqueeze(bwPrev, kwPrev);

        return (sqzCurr / sqzPrev) - 1.0;   // se sqzPrev == 0 → explode → correto
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        // Cache base
        BollingerCacheDto bb20 = bollingerCacheService.getBb20(symbol, interval, series);
        ATRIndicator atr14     = atrCacheService.getAtr14(symbol, interval, series);

        return switch (frame) {

            // -----------------------------------------
            // KELT_20_WIDTH = largura total do canal Keltner
            // -----------------------------------------
            case KELT_20_WIDTH ->
                    computeKeltnerWidth(atr14, last);

            // -----------------------------------------
            // VOL_SQZ_BB_KELT = BOLL_20_WIDTH / KELT_20_WIDTH
            // -----------------------------------------
            case VOL_SQZ_BB_KELT ->
                    computeSqueeze(
                            computeBollWidth(bb20, last),
                            computeKeltnerWidth(atr14, last)
                    );

            // -----------------------------------------
            // VOL_SQZ_BB_KELT_CHG
            // -----------------------------------------
            case VOL_SQZ_BB_KELT_CHG ->
                    computeSqueezeChange(bb20, atr14, last);

            default ->
                    throw new IllegalArgumentException("Frame KELTNER não suportado: " + frame);
        };
    }
}
