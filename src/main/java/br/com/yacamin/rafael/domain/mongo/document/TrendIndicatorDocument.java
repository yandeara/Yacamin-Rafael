package br.com.yacamin.rafael.domain.mongo.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document
public class TrendIndicatorDocument {

    @Id
    private String id;

    private String symbol;
    private Instant openTime;

    // =========================================================================
    // 1) EMA - RAW (Ribbon / Regime State)
    // =========================================================================
    private Double trdEma8;
    private Double trdEma12;
    private Double trdEma16;
    private Double trdEma20;
    private Double trdEma21;
    private Double trdEma32;
    private Double trdEma34;
    private Double trdEma50;
    private Double trdEma55;
    private Double trdEma100;
    private Double trdEma144;
    private Double trdEma200;
    private Double trdEma233;

    // =========================================================================
    // 2) EMA - SLOPE (Trend Direction / Strength)
    // =========================================================================
    private Double trdEma8Slp;
    private Double trdEma20Slp;
    private Double trdEma50Slp;
    private Double trdEma16Slp;
    private Double trdEma32Slp;

    // =========================================================================
    // 3) EMA - SLOPE (ATR-N)
    // =========================================================================
    private Double trdEma8SlpAtrn;
    private Double trdEma20SlpAtrn;
    private Double trdEma50SlpAtrn;
    private Double trdEma16SlpAtrn;
    private Double trdEma32SlpAtrn;

    // =========================================================================
    // 4) EMA - SLOPE ACC (Acceleration)
    // =========================================================================
    private Double trdEma8SlpAcc;
    private Double trdEma20SlpAcc;
    private Double trdEma50SlpAcc;

    // =========================================================================
    // 5) EMA - SLOPE ACC (ATR-N)
    // =========================================================================
    private Double trdEma8SlpAccAtrn;
    private Double trdEma20SlpAccAtrn;
    private Double trdEma50SlpAccAtrn;

    // =========================================================================
    // 6) EMA - TDS / TVR
    // =========================================================================
    private Double trdEma8SlpTds;
    private Double trdEma20SlpTds;
    private Double trdEma50SlpTds;
    private Double trdEma8SlpTvr;
    private Double trdEma20SlpTvr;
    private Double trdEma50SlpTvr;

    // =========================================================================
    // 7) DISTANCE - ATR-N ONLY
    // =========================================================================
    private Double trdDistCloseEma8Atrn;
    private Double trdDistCloseEma20Atrn;
    private Double trdDistCloseEma50Atrn;
    private Double trdDistCloseEma16Atrn;
    private Double trdDistCloseEma32Atrn;
    private Double trdDistEma820Atrn;
    private Double trdDistEma2050Atrn;
    private Double trdDistEma850Atrn;

    // =========================================================================
    // 8) RATIOS - RAW ONLY
    // =========================================================================
    private Double trdRatioEma820;
    private Double trdRatioEma2050;
    private Double trdRatioEma850;

    // =========================================================================
    // 9) ALIGNMENT / CROSS / DURATION
    // =========================================================================
    private Double trdAlignmentEma82050Score;
    private Double trdAlignmentEma82050Normalized;
    private Double trdAligmentEma82050Delta;
    private Double trdAligmentEma82050Binary;
    private Double trdCrossEma820Binary;
    private Double trdCrossEma820Delta;
    private Double trdCrossEma820DeltaAtrn;
    private Double trdCrossEma2050Binary;
    private Double trdCrossEma2050Delta;
    private Double trdCrossEma2050DeltaAtrn;
    private Double trdDurationEma820;
    private Double trdDurationEma2050;
    private Double trdDurationEma850;

    // =========================================================================
    // 10) PUSH / GAP
    // =========================================================================
    private Double trdDeltaCloseEma8;
    private Double trdDeltaCloseEma20;
    private Double trdDeltaCloseEma50;
    private Double trdDeltaCloseEma8Atrn;
    private Double trdDeltaCloseEma20Atrn;
    private Double trdDeltaCloseEma50Atrn;
    private Double trdDeltaEma820;
    private Double trdDeltaEma2050;
    private Double trdDeltaEma820Atrn;
    private Double trdDeltaEma2050Atrn;

    // =========================================================================
    // 11) QUALITY / REGIME COMPOSITES
    // =========================================================================
    private Double trdCtsCloseEma20W50;
    private Double trdCtsCloseEma20W10;
    private Double trdCtsEma82050;
    private Double trdEma82050SlpTcs;
    private Double trdTmEma820W10;
    private Double trdEma20SlpSnrW10;
    private Double trdCloseTcpW50;
    private Double trdEma8Zsc;
    private Double trdEma20Zsc;
    private Double trdEma50Zsc;

    // =========================================================================
    // 12) ADX / DI - Trend Strength
    // =========================================================================
    private Double trdAdx14;
    private Double trdPdi14;
    private Double trdMdi14;
    private Double trdDiDiff14;
}
