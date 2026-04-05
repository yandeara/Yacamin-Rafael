package br.com.yacamin.rafael.domain.mongo.document;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

@Data
public class ModelRegistryDocument {

    @Id
    private String id;

    private String fileName;
    private String predictionType;
    private String type;
    private String algorithm;
    private String symbol;
    private int horizon;
    private String timeframe;
    private String rangeStart;
    private String rangeEnd;

    private List<String> featureNames;
    private Map<String, Object> hyperparameters;

    /** Modelo ativo para este predictionType. Apenas um por tipo deve ser true. */
    private boolean active;
}
