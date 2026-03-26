package br.com.yacamin.rafael.domain;

import lombok.Data;

@Data
public class Market {

    /* Parametros Bases do Mercado */
    private Long unixTime;
    private String slug;
    private String tokenUp;
    private String tokenDown;
    private String outcome;

    /* Multi-market */
    private String marketGroup;
    private String displayName;
    private BlockDuration blockDuration;

    /* Flags de Controle */
    private boolean resolved = false;

    /* Informacoes Financeiras do Mercado - Polymarket */
    private double upBid;
    private double upAsk;
    private double downBid;
    private double downAsk;

    /* Informacoes do Bloco - Binance */
    private double timeRemaining;
    private double midPrice;
    private double openPrice;
    private int tickCount;

    /* Polymarket metadata */
    private String conditionId;
    private boolean negRisk = false;
    private int takerBaseFee = 0;
}
