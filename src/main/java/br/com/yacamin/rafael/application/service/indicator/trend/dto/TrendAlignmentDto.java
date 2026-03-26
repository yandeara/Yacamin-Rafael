package br.com.yacamin.rafael.application.service.indicator.trend.dto;

import lombok.Data;

@Data
public class TrendAlignmentDto {

    private double score;
    private double normalized;
    private double delta;
    private double binary;

}
