package br.com.yacamin.rafael.domain;

import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.KlineEventDataResponse;
import br.com.yacamin.rafael.domain.scylla.entity.Candle1Mn;
import br.com.yacamin.rafael.domain.scylla.entity.Candle5Mn;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
public class SymbolCandle {

    private String symbol;
    private Instant openTime;
    private Instant closeTime;
    private CandleIntervals interval;

    private double open;
    private double high;
    private double low;
    private double close;

    private double volume;
    private double quoteVolume;
    private double numberOfTrades;
    private double takerBuyBaseVolume;
    private double takerBuyQuoteVolume;
    private double takerSellBaseVolume;
    private double takerSellQuoteVolume;

    public static SymbolCandle fromCandle1Mn(Candle1Mn entity) {
        SymbolCandle c = new SymbolCandle();
        c.setSymbol(entity.getSymbol());
        c.setOpenTime(entity.getOpenTime());
        c.setCloseTime(entity.getOpenTime().plus(1, ChronoUnit.MINUTES));
        c.setInterval(CandleIntervals.I1_MN);
        c.setOpen(entity.getOpen());
        c.setHigh(entity.getHigh());
        c.setLow(entity.getLow());
        c.setClose(entity.getClose());
        c.setVolume(entity.getVolume());
        c.setQuoteVolume(entity.getQuoteVolume());
        c.setNumberOfTrades(entity.getNumberOfTrades());
        c.setTakerBuyBaseVolume(entity.getTakerBuyBaseVolume());
        c.setTakerBuyQuoteVolume(entity.getTakerBuyQuoteVolume());
        c.setTakerSellBaseVolume(entity.getTakerSellBaseVolume());
        c.setTakerSellQuoteVolume(entity.getTakerSellQuoteVolume());
        return c;
    }

    public static SymbolCandle fromKlineData(KlineEventDataResponse data) {
        SymbolCandle c = new SymbolCandle();
        c.setSymbol(data.symbol());
        c.setOpenTime(data.openTime());
        c.setCloseTime(data.closeTime());
        c.setInterval(CandleIntervals.valueOfLabel(data.interval()));
        c.setOpen(data.open().doubleValue());
        c.setHigh(data.high().doubleValue());
        c.setLow(data.low().doubleValue());
        c.setClose(data.close().doubleValue());
        c.setVolume(data.volume().doubleValue());
        c.setQuoteVolume(data.quoteVolume().doubleValue());
        c.setNumberOfTrades(data.numberOfTrades() != null ? data.numberOfTrades().doubleValue() : 0);
        c.setTakerBuyBaseVolume(data.takerBuyBaseVolume().doubleValue());
        c.setTakerBuyQuoteVolume(data.takerBuyQuoteVolume().doubleValue());
        c.setTakerSellBaseVolume(c.getVolume() - c.getTakerBuyBaseVolume());
        c.setTakerSellQuoteVolume(c.getQuoteVolume() - c.getTakerBuyQuoteVolume());
        return c;
    }

    public Candle1Mn toCandle1Mn() {
        Candle1Mn e = new Candle1Mn();
        e.setSymbol(symbol);
        e.setOpenTime(openTime);
        e.setOpen(open);
        e.setHigh(high);
        e.setLow(low);
        e.setClose(close);
        e.setVolume(volume);
        e.setQuoteVolume(quoteVolume);
        e.setNumberOfTrades(numberOfTrades);
        e.setTakerBuyBaseVolume(takerBuyBaseVolume);
        e.setTakerBuyQuoteVolume(takerBuyQuoteVolume);
        e.setTakerSellBaseVolume(takerSellBaseVolume);
        e.setTakerSellQuoteVolume(takerSellQuoteVolume);
        return e;
    }

    public static SymbolCandle fromCandle5Mn(Candle5Mn entity) {
        SymbolCandle c = new SymbolCandle();
        c.setSymbol(entity.getSymbol());
        c.setOpenTime(entity.getOpenTime());
        c.setCloseTime(entity.getOpenTime().plus(5, ChronoUnit.MINUTES));
        c.setInterval(CandleIntervals.I5_MN);
        c.setOpen(entity.getOpen());
        c.setHigh(entity.getHigh());
        c.setLow(entity.getLow());
        c.setClose(entity.getClose());
        c.setVolume(entity.getVolume());
        c.setQuoteVolume(entity.getQuoteVolume());
        c.setNumberOfTrades(entity.getNumberOfTrades());
        c.setTakerBuyBaseVolume(entity.getTakerBuyBaseVolume());
        c.setTakerBuyQuoteVolume(entity.getTakerBuyQuoteVolume());
        c.setTakerSellBaseVolume(entity.getTakerSellBaseVolume());
        c.setTakerSellQuoteVolume(entity.getTakerSellQuoteVolume());
        return c;
    }

    public Candle5Mn toCandle5Mn() {
        Candle5Mn e = new Candle5Mn();
        e.setSymbol(symbol);
        e.setOpenTime(openTime);
        e.setOpen(open);
        e.setHigh(high);
        e.setLow(low);
        e.setClose(close);
        e.setVolume(volume);
        e.setQuoteVolume(quoteVolume);
        e.setNumberOfTrades(numberOfTrades);
        e.setTakerBuyBaseVolume(takerBuyBaseVolume);
        e.setTakerBuyQuoteVolume(takerBuyQuoteVolume);
        e.setTakerSellBaseVolume(takerSellBaseVolume);
        e.setTakerSellQuoteVolume(takerSellQuoteVolume);
        return e;
    }

    public static List<SymbolCandle> parseCandles(String symbol, CandleIntervals interval, List<List<Object>> rows) {
        List<SymbolCandle> candles = new ArrayList<>();

        for (List<Object> row : rows) {
            SymbolCandle candle = new SymbolCandle();
            candle.setSymbol(symbol);

            long tsOpen = ((Number) row.get(0)).longValue();
            Instant openTime = Instant.ofEpochMilli(tsOpen);
            candle.setOpenTime(openTime);
            candle.setCloseTime(openTime.plus(interval.getDuration()));
            candle.setInterval(interval);

            candle.setOpen(Double.parseDouble((String) row.get(1)));
            candle.setHigh(Double.parseDouble((String) row.get(2)));
            candle.setLow(Double.parseDouble((String) row.get(3)));
            candle.setClose(Double.parseDouble((String) row.get(4)));
            candle.setVolume(Double.parseDouble((String) row.get(5)));
            candle.setQuoteVolume(Double.parseDouble((String) row.get(7)));
            candle.setNumberOfTrades(((Number) row.get(8)).doubleValue());
            candle.setTakerBuyBaseVolume(Double.parseDouble((String) row.get(9)));
            candle.setTakerBuyQuoteVolume(Double.parseDouble((String) row.get(10)));
            candle.setTakerSellBaseVolume(candle.getVolume() - candle.getTakerBuyBaseVolume());
            candle.setTakerSellQuoteVolume(candle.getQuoteVolume() - candle.getTakerBuyQuoteVolume());

            candles.add(candle);
        }

        return candles;
    }
}
