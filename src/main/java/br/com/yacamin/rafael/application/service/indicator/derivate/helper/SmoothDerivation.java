package br.com.yacamin.rafael.application.service.indicator.derivate.helper;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SmoothDerivation {

    public double smooth(double[] values) {
        return values.length == 0 ? 0.0 :
                Arrays.stream(values).average().orElse(0.0);
    }

}
