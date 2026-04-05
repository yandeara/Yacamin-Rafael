package br.com.yacamin.rafael.domain.enumeration;

public enum PredictionType {
    M2M("Minute-to-Minute", "Preve proximo candle 1m, todo minuto"),
    BLOCK("Block-by-Block", "Preve proximo bloco 5m, cada 5 minutos"),
    HORIZON("Horizon Intra-Block", "Preve final do bloco a partir do minuto 1"),
    C2C("Close-to-Close", "Preve direcao close atual vs close proximo candle");

    private final String displayName;
    private final String description;

    PredictionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
