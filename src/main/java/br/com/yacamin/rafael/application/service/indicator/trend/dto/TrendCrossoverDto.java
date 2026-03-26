package br.com.yacamin.rafael.application.service.indicator.trend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrendCrossoverDto {

    private double binary;   // -1, 0, +1
    private double delta;    // (emaA - emaB) / close
    private double atrN;     // delta normalizado por ATR
    private double stdN;     // delta normalizado por STD
}