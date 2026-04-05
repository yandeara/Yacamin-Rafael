package br.com.yacamin.rafael.domain.model;

import br.com.yacamin.rafael.domain.enumeration.PredictionType;

public record SavedModel(
        String fileName,
        PredictionType predictionType,
        String type,
        String algorithm,
        String symbol,
        int horizon,
        String timeframe,
        String rangeStart,
        String rangeEnd,
        long fileSizeBytes
) {
    public String displayRange() {
        return rangeStart + " - " + rangeEnd;
    }
}
