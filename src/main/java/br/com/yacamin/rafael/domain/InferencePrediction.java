package br.com.yacamin.rafael.domain;

import lombok.Data;

import java.time.Instant;

/**
 * Resultado de uma inferência XGBoost.
 *
 * Usado tanto para M2M (minute-by-minute, candles 1m) quanto B2B (block-by-block, candles 5m).
 *
 * Três avaliações independentes (B2B):
 * 1. hit (candle): close do próximo candle vs modelThreshold — "o modelo acertou a direção do preço?"
 * 2. valid: marketOpen vs modelThreshold — "a previsão é acionável no mercado?"
 * 3. hitResolve: resolução Polymarket (UP/DOWN) vs direction — "o mercado resolveu na direção prevista?"
 *
 * M2M usa apenas hit (candle).
 */
@Data
public class InferencePrediction {

    // Setados na criação
    private final String direction;       // "UP" ou "DOWN"
    private final double confidence;      // 0.0 a 1.0
    private final int minuteInBlock;      // 1 a 5 (M2M) ou 1 (B2B)
    private final Instant candleOpenTime; // openTime do candle que gerou
    private final String modelKey;        // ex: "xgb_BTCUSDT_h1_1m"
    private final double openMid;         // close price do candle que gerou a previsão

    // Threshold do modelo = open do candle analisado (M2M e B2B)
    private Double modelThreshold;

    // Preenchidos quando o candle seguinte fecha (avaliação 1: hit candle)
    private Double closeMid;              // close price do candle seguinte (null = aguardando)
    private Boolean hit;                  // close vs threshold acertou? (null = aguardando)

    // B2B: preço de abertura do mercado previsto (avaliação 2: valid)
    private Double marketOpen;            // open do candle do bloco previsto (null = aguardando)
    private Boolean valid;                // previsão acionável? (null = aguardando)

    // B2B: resolução do mercado Polymarket (avaliação 3: hit resolve)
    private String resolvedOutcome;       // "Up" ou "Down" da Polymarket (null = não resolvido)
    private Boolean hitResolve;           // direction bateu com resolução? (null = não resolvido)

    public InferencePrediction(String direction, double confidence, int minuteInBlock,
                                Instant candleOpenTime, String modelKey, double openMid) {
        this.direction = direction;
        this.confidence = confidence;
        this.minuteInBlock = minuteInBlock;
        this.candleOpenTime = candleOpenTime;
        this.modelKey = modelKey;
        this.openMid = openMid;
    }

    /**
     * Resolve M2M: preenche closeMid e calcula hit (candle).
     */
    public void resolve(double closeMid) {
        this.closeMid = closeMid;
        double threshold = modelThreshold != null ? modelThreshold : openMid;
        if ("UP".equals(direction)) {
            this.hit = closeMid > threshold;
        } else {
            this.hit = closeMid < threshold;
        }
    }

    /**
     * Preenche marketOpen e calcula VALID/INVALID.
     * Chamado no INÍCIO do bloco (quando primeiro candle 1m chega), via MID Binance.
     *
     * UP  válido se MID abriu ABAIXO do threshold (espaço pra subir alinhado com mercado)
     * DOWN válido se MID abriu ACIMA do threshold (espaço pra cair alinhado com mercado)
     */
    public void validateWithMarketOpen(double marketOpen) {
        this.marketOpen = marketOpen;
        if (modelThreshold != null) {
            if ("UP".equals(direction)) {
                this.valid = marketOpen < modelThreshold;
            } else {
                this.valid = marketOpen > modelThreshold;
            }
        }
    }

    /**
     * Resolve B2B candle: preenche closeMid e calcula hit candle.
     * Chamado quando o candle 5m do bloco previsto FECHA.
     */
    public void resolveBlock(double closeMid) {
        this.closeMid = closeMid;
        if (modelThreshold != null) {
            if ("UP".equals(direction)) {
                this.hit = closeMid > modelThreshold;
            } else {
                this.hit = closeMid < modelThreshold;
            }
        }
    }

    /**
     * Resolve B2B mercado: preenche hitResolve quando o mercado Polymarket resolve.
     *
     * @param outcome "Up" ou "Down" (vindo da Polymarket)
     */
    public void resolveMarket(String outcome) {
        this.resolvedOutcome = outcome;
        if (outcome != null) {
            this.hitResolve = direction.equalsIgnoreCase(outcome);
        }
    }
}
