package br.com.yacamin.rafael.application.service.indicator.derivate;

import org.springframework.stereotype.Service;

@Service
public class RatioDerivation {
    public double ratio(double a, double b) {
        return a / b;
    }
}
