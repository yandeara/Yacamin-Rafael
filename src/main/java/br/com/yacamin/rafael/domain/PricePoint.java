package br.com.yacamin.rafael.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PricePoint {

    private double price;
    private long timestamp;

}
