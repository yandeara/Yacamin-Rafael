package br.com.yacamin.rafael.domain;

import java.nio.file.Path;
import java.time.YearMonth;

/**
 * Descreve um modelo XGBoost carregado da pasta models/.
 * Padrão de nome: {tipo}_{moeda}_{horizon}_{intervalo}_{MMYYYY-MMYYYY}.ubj
 * Exemplo: xgb_BTCUSDT_h1_1m_012026-022026.ubj
 */
public record ModelDescriptor(
        String type,              // ex: "xgb"
        String symbol,            // ex: "BTCUSDT"
        String horizon,           // ex: "h1" (sempre começa com 'h' ou 'H')
        CandleIntervals interval, // ex: I1_MN
        YearMonth trainStart,     // ex: 2026-01
        YearMonth trainEnd,       // ex: 2026-02
        String fileName,          // nome completo do arquivo
        Path filePath             // caminho absoluto
) {

    public String key() {
        return type + "_" + symbol + "_" + horizon + "_" + interval.getValue();
    }
}
