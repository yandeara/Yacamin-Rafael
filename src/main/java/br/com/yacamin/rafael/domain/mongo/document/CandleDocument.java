package br.com.yacamin.rafael.domain.mongo.document;

import br.com.yacamin.rafael.domain.CandleEntity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class CandleDocument implements CandleEntity {

    @Id
    private String id;

    private String symbol;
    private Instant openTime;

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
}
