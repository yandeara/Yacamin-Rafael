package br.com.yacamin.rafael.application.service.indicator;

public record FeatureDescription(
        String key,
        String name,
        String family,
        String formula,
        String description,
        String range,
        String example
) {}
