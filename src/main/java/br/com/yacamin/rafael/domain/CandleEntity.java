package br.com.yacamin.rafael.domain;

import java.time.Instant;

public interface CandleEntity {

    String getSymbol();
    void setSymbol(String symbol);

    Instant getOpenTime();
    void setOpenTime(Instant openTime);

    double getOpen();
    void setOpen(double open);

    double getHigh();
    void setHigh(double high);

    double getLow();
    void setLow(double low);

    double getClose();
    void setClose(double close);

    double getVolume();
    void setVolume(double volume);

    double getQuoteVolume();
    void setQuoteVolume(double quoteVolume);

    double getNumberOfTrades();
    void setNumberOfTrades(double numberOfTrades);

    double getTakerBuyBaseVolume();
    void setTakerBuyBaseVolume(double takerBuyBaseVolume);

    double getTakerBuyQuoteVolume();
    void setTakerBuyQuoteVolume(double takerBuyQuoteVolume);

    double getTakerSellBaseVolume();
    void setTakerSellBaseVolume(double takerSellBaseVolume);

    double getTakerSellQuoteVolume();
    void setTakerSellQuoteVolume(double takerSellQuoteVolume);

}
