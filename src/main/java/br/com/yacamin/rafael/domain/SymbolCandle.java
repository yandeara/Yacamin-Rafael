package br.com.yacamin.rafael.domain;

import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.KlineEventDataResponse;
import br.com.yacamin.rafael.domain.mongo.document.CandleDocument;
import lombok.Data;

import java.time.Instant;
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

    public static SymbolCandle fromCandleDocument(CandleDocument doc, CandleIntervals interval) {
        SymbolCandle c = new SymbolCandle();
        c.setSymbol(doc.getSymbol());
        c.setOpenTime(doc.getOpenTime());
        c.setCloseTime(doc.getOpenTime().plus(interval.getDuration()));
        c.setInterval(interval);
        c.setOpen(doc.getOpen());
        c.setHigh(doc.getHigh());
        c.setLow(doc.getLow());
        c.setClose(doc.getClose());
        c.setVolume(doc.getVolume());
        c.setQuoteVolume(doc.getQuoteVolume());
        c.setNumberOfTrades(doc.getNumberOfTrades());
        c.setTakerBuyBaseVolume(doc.getTakerBuyBaseVolume());
        c.setTakerBuyQuoteVolume(doc.getTakerBuyQuoteVolume());
        c.setTakerSellBaseVolume(doc.getTakerSellBaseVolume());
        c.setTakerSellQuoteVolume(doc.getTakerSellQuoteVolume());
        return c;
    }

    public CandleDocument toCandleDocument() {
        CandleDocument doc = new CandleDocument();
        doc.setSymbol(symbol);
        doc.setOpenTime(openTime);
        doc.setOpen(open);
        doc.setHigh(high);
        doc.setLow(low);
        doc.setClose(close);
        doc.setVolume(volume);
        doc.setQuoteVolume(quoteVolume);
        doc.setNumberOfTrades(numberOfTrades);
        doc.setTakerBuyBaseVolume(takerBuyBaseVolume);
        doc.setTakerBuyQuoteVolume(takerBuyQuoteVolume);
        doc.setTakerSellBaseVolume(takerSellBaseVolume);
        doc.setTakerSellQuoteVolume(takerSellQuoteVolume);
        return doc;
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
