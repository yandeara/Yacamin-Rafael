package br.com.yacamin.rafael.application.service.candle;

import br.com.yacamin.rafael.application.service.analyse.AnalyseOrchestratorService;
import br.com.yacamin.rafael.application.service.model.HorizonInferenceService;
import br.com.yacamin.rafael.application.service.model.MinuteByMinuteInferenceService;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNumFactory;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarSeriesCacheService {

    private final DownloadCandleService downloadCandleService;
    private final AnalyseOrchestratorService analyseOrchestratorService;
    private final MinuteByMinuteInferenceService minuteByMinuteInferenceService;
    private final HorizonInferenceService horizonInferenceService;

    @Getter
    private final Map<String, BarSeries> barSeriesMap = new HashMap<>();

    @Getter
    private final Map<String, LastCandleDto> lastCandleMaps = new HashMap<>();

    private static final int MIN_REQUIRED = 999;

    public BarSeries get(String symbol, CandleIntervals interval) {
        return barSeriesMap.get(cacheKey(symbol, interval));
    }

    public BarSeries update(String symbol, CandleIntervals interval, SymbolCandle candle, boolean logThis) {
        String key = cacheKey(symbol, interval);
        BarSeries series = barSeriesMap.computeIfAbsent(key, k -> createSeries(k));

        LastCandleDto last = lastCandleMaps.get(key);

        boolean hasGap = last != null && hasGapBetween(last, candle, interval);
        if (hasGap) {
            handleGap(symbol, interval, last, candle);
        }

        if (logThis) {
            log.info("[CACHE] Add Bar: {} {} @ {}", symbol, interval, candle.getOpenTime());
        }

        addCandleToSeries(series, candle, interval);
        verifyLastCandle(key, symbol, interval, candle.getOpenTime());

        if (series.getBarCount() > MIN_REQUIRED) {
            long t0 = System.currentTimeMillis();
            log.debug("[CACHE] Triggering analyse for {} @ {} (bars={})", symbol, candle.getOpenTime(), series.getBarCount());
            analyseOrchestratorService.analyse(candle, interval, series);
            log.debug("[CACHE] Analyse done for {} @ {} in {}ms", symbol, candle.getOpenTime(), System.currentTimeMillis() - t0);

            // Inferências após features calculadas
            minuteByMinuteInferenceService.onCandleClosed(candle);
            horizonInferenceService.onCandleClosed(candle);
        }

        return series;
    }

    public void addCandleToSeries(BarSeries series, SymbolCandle candle, CandleIntervals interval) {
        if (candle.getNumberOfTrades() == 0) return;

        // Normalizar closeTime: Binance manda 17:14:59.999Z, warmup calcula 17:15:00.000Z
        // Truncar para segundo inteiro e somar a duração do intervalo a partir do openTime
        Instant normalizedEndTime = candle.getOpenTime().plus(interval.getDuration());

        // Rejeitar duplicata: se o bar já existe na série (overlap com último bar)
        if (series.getBarCount() > 0) {
            Instant lastEndTime = series.getLastBar().getEndTime();
            if (!normalizedEndTime.isAfter(lastEndTime)) {
                log.trace("[CACHE] Skipping duplicate/overlap bar: {} endTime={} <= seriesEnd={}",
                        candle.getSymbol(), normalizedEndTime, lastEndTime);
                return;
            }
        }

        Num open = series.numFactory().numOf(candle.getOpen());
        Num high = series.numFactory().numOf(candle.getHigh());
        Num low = series.numFactory().numOf(candle.getLow());
        Num close = series.numFactory().numOf(candle.getClose());
        Num volume = series.numFactory().numOf(candle.getVolume());
        Num quoteVolume = series.numFactory().numOf(candle.getQuoteVolume());
        Num takerBuyBase = series.numFactory().numOf(candle.getTakerBuyBaseVolume());
        Num takerSellBase = series.numFactory().numOf(candle.getTakerSellBaseVolume());
        Num takerBuyQuote = series.numFactory().numOf(candle.getTakerBuyQuoteVolume());
        Num takerSellQuote = series.numFactory().numOf(candle.getTakerSellQuoteVolume());
        Num amount = close.multipliedBy(volume);

        MikhaelBar bar = new MikhaelBar(
                interval.getDuration(), normalizedEndTime,
                open, high, low, close, volume, quoteVolume, amount,
                (long) candle.getNumberOfTrades(),
                takerBuyBase, takerSellBase, takerBuyQuote, takerSellQuote
        );

        series.addBar(bar);
    }

    private BarSeries createSeries(String key) {
        return new BaseBarSeriesBuilder()
                .withName(key)
                .withMaxBarCount(1000)
                .withNumFactory(DoubleNumFactory.getInstance())
                .build();
    }

    private void verifyLastCandle(String key, String symbol, CandleIntervals interval, Instant openTime) {
        LastCandleDto dto = new LastCandleDto();
        dto.setSymbol(symbol);
        dto.setInterval(interval);
        dto.setOpen(openTime);
        lastCandleMaps.put(key, dto);
    }

    private boolean hasGapBetween(LastCandleDto last, SymbolCandle current, CandleIntervals interval) {
        Duration expectedGap = interval.getDuration();
        Instant expectedNext = last.getOpen().plus(expectedGap);
        long diff = Duration.between(expectedNext, current.getOpenTime()).abs().toSeconds();
        return diff > 1;
    }

    private void handleGap(String symbol, CandleIntervals interval, LastCandleDto last, SymbolCandle current) {
        long gapMinutes = Duration.between(last.getOpen(), current.getOpenTime()).toMinutes();
        log.warn("[GAP] Detected in {}: from {} to {} ({} min gap, expected {}s)",
                symbol, last.getOpen(), current.getOpenTime(), gapMinutes, interval.getDuration().toSeconds());

        long t0 = System.currentTimeMillis();
        var list = downloadCandleService.download(interval, symbol, last.getOpen(), current.getOpenTime());

        var gapCandles = list.stream()
                .filter(c -> c.getOpenTime().isAfter(last.getOpen()))
                .filter(c -> c.getOpenTime().isBefore(current.getOpenTime()))
                .toList();

        log.info("[GAP] Downloaded {} candles to fill gap ({} after filter), took {}ms",
                list.size(), gapCandles.size(), System.currentTimeMillis() - t0);

        for (SymbolCandle c : gapCandles) {
            log.debug("[GAP] Filling: {} @ {}", symbol, c.getOpenTime());
            update(symbol, interval, c, false);
        }

        log.info("[GAP] Fill complete for {}: {} candles inserted", symbol, gapCandles.size());
    }

    private static String cacheKey(String symbol, CandleIntervals interval) {
        return symbol + "-" + interval.name();
    }
}
