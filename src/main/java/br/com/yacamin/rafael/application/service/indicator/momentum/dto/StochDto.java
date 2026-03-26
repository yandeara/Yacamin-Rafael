package br.com.yacamin.rafael.application.service.indicator.momentum.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StochDto {
    private double k;
    private double d;
}