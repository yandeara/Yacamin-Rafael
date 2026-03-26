package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.application.service.cache.indicator.OfiCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class OrderflowImbalanceDerivation {

    private final OfiCacheService ofiCacheService;

    // OFI raw (indicator)
    public double ofiAt(SymbolCandle candle, BarSeries series, int index) {
        return ofiCacheService
                .getOfiRaw(candle.getSymbol(), candle.getInterval(), series)
                .getValue(index).doubleValue();
    }

    // compat: não grava mais no bar
    public double ofiAt(BarSeries series, int index) {
        var bar = (br.com.yacamin.rafael.domain.RafaelBar) series.getBar(index);
        double buy  = bar.getTakerBuyBaseVolume().doubleValue();
        double sell = bar.getTakerSellBaseVolume().doubleValue();
        return buy - sell;
    }
}
