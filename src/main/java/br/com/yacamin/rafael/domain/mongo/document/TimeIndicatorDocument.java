package br.com.yacamin.rafael.domain.mongo.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class TimeIndicatorDocument {

    @Id
    private String id;

    private String symbol;
    private Instant openTime;

    private Double minuteOfDay;
    private Double dayOfWeek;
    private Double sessionAsia;
    private Double sessionEurope;
    private Double sessionNy;
    private Double sinTime;
    private Double cosTime;
    private Double dayOfMonth;
    private Double sinDayOfWeek;
    private Double cosDayOfWeek;
    private Double overlapAsiaEur;
    private Double overlapEurNy;
    private Double candleInH1;
}
