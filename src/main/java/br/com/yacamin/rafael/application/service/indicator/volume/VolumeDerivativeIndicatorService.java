package br.com.yacamin.rafael.application.service.indicator.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolumeDerivativeIndicatorService {

    // =============================================================================================
    // MÉTODOS PRINCIPAIS — Regra de Ouro (sem proteções)
    // =============================================================================================
    public double calculate(SymbolCandle candle, Frame frame) {

        switch (frame) {

            case TAKER_BUY_SELL_IMBALANCE -> {
                double buy  = candle.getTakerBuyBaseVolume();
                double sell = candle.getTakerSellBaseVolume();
                double total = buy + sell;
                return (buy / total) - (sell / total);   // se total==0 → explode
            }

            case TAKER_BUY_RATIO -> {
                double buy  = candle.getTakerBuyBaseVolume();
                double sell = candle.getTakerSellBaseVolume();
                double total = buy + sell;
                return buy / total;                      // se total==0 → explode
            }

            case TAKER_SELL_RATIO -> {
                double buy  = candle.getTakerBuyBaseVolume();
                double sell = candle.getTakerSellBaseVolume();
                double total = buy + sell;
                return sell / total;
            }

            case LOG_VOLUME -> {
                double vol = candle.getVolume();
                return Math.log(vol);                    // se vol<=0 → explode
            }

            case BASE_QUOTE_RATIO -> {
                double vol  = candle.getVolume();
                double qvol = candle.getQuoteVolume();
                return vol / qvol;                       // se qvol==0 → explode
            }

            default ->
                    throw new IllegalStateException("Frame Volume não suportado: " + frame);
        }
    }


    // =============================================================================================
    // SMA PURO — Regra de Ouro (sem proteção)
    // =============================================================================================

    private double smaVolume(BarSeries series, int period) {

        int end   = series.getEndIndex();
        int start = end - period + 1;

        double sum = 0.0;
        for (int i = start; i <= end; i++) {
            sum += series.getBar(i).getVolume().doubleValue();
        }

        return sum / period;      // se period==0 → explode
    }

    private double smaQuoteVolume(BarSeries series, int period) {

        int end   = series.getEndIndex();
        int start = end - period + 1;

        double sum = 0.0;
        for (int i = start; i <= end; i++) {
            RafaelBar bar = (RafaelBar) series.getBar(i);
            sum += bar.getQuoteVolume().doubleValue();
        }

        return sum / period;
    }

    private double smaTrades(BarSeries series, int period) {

        int end   = series.getEndIndex();
        int start = end - period + 1;

        double sum = 0.0;
        for (int i = start; i <= end; i++) {
            sum += series.getBar(i).getTrades();
        }

        return sum / period;
    }

    // =============================================================================================
    // RELATIVE (current / mean) — Regra de Ouro
    // =============================================================================================

    public double volumeRel(BarSeries series, double volumeNow, int period) {
        double mean = smaVolume(series, period);
        return volumeNow / mean;       // se mean==0 → explode
    }

    public double tradesRel(BarSeries series, double tradesNow, int period) {
        double mean = smaTrades(series, period);
        return tradesNow / mean;
    }

    public double quoteVolumeRel(BarSeries series, double qVolNow, int period) {
        double mean = smaQuoteVolume(series, period);
        return qVolNow / mean;
    }
}
