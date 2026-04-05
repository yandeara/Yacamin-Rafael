package br.com.yacamin.rafael.domain.mongo.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document
public class VolumeIndicatorDocument {

    @Id
    private String id;

    private String symbol;
    private Instant openTime;

    // =========================================================================
    // 1) RAW MICROSTRUCTURE
    // =========================================================================
    private Double volVolume;
    private Double volQuoteVolume;
    private Double volNumberOfTrades;

    private Double volTakerBuyBaseVolume;
    private Double volTakerSellBaseVolume;

    private Double volTakerBuyQuoteVolume;
    private Double volTakerSellQuoteVolume;

    private Double volTakerBuyRatio;
    private Double volTakerBuySellImbalance;

    private Double volLogVolume;

    // =========================================================================
    // 1.1) TAKER PRESSURE DYNAMICS
    // =========================================================================
    private Double volTakerBuyRatioRel16;
    private Double volTakerBuyRatioRel48;
    private Double volTakerBuyRatioRel96;
    private Double volTakerBuyRatioRel288;

    private Double volTakerBuyRatioZscore32;
    private Double volTakerBuyRatioZscore96;
    private Double volTakerBuyRatioZscore288;

    private Double volTakerBuyRatioSlpW20;
    private Double volTakerBuyRatioFlipRateW20;
    private Double volTakerBuyRatioPrstW20;

    private Double volTakerBuySellImbalanceZscore32;
    private Double volTakerBuySellImbalanceZscore96;

    private Double volTakerBuySellImbalanceSlpW20;
    private Double volTakerBuySellImbalanceFlipRateW20;
    private Double volTakerBuySellImbalancePrstW20;

    // =========================================================================
    // 2) RELATIVE VOLUME (SMA-based)
    // =========================================================================
    private Double volVolumeRel16;
    private Double volVolumeRel32;
    private Double volVolumeRel48;
    private Double volVolumeRel96;
    private Double volVolumeRel288;

    private Double volTradesRel16;
    private Double volTradesRel32;
    private Double volTradesRel48;
    private Double volTradesRel96;
    private Double volTradesRel288;

    private Double volQuoteVolumeRel16;
    private Double volQuoteVolumeRel32;
    private Double volQuoteVolumeRel48;
    private Double volQuoteVolumeRel96;
    private Double volQuoteVolumeRel288;

    // =========================================================================
    // 3) Z-SCORES
    // =========================================================================
    private Double volVolumeZscore32;
    private Double volVolumeZscore48;
    private Double volVolumeZscore96;
    private Double volVolumeZscore288;

    private Double volTradesZscore32;
    private Double volTradesZscore48;
    private Double volTradesZscore96;
    private Double volTradesZscore288;

    private Double volQuoteVolumeZscore32;
    private Double volQuoteVolumeZscore48;
    private Double volQuoteVolumeZscore96;
    private Double volQuoteVolumeZscore288;

    // =========================================================================
    // 4) DELTAS (short-term)
    // =========================================================================
    private Double volVolumeDelta1;
    private Double volVolumeDelta3;

    private Double volTradesDelta1;
    private Double volTradesDelta3;

    private Double volQuoteVolumeDelta1;
    private Double volQuoteVolumeDelta3;

    // =========================================================================
    // 5) ACTIVITY PRESSURE (TRADES)
    // =========================================================================
    private Double volActTradesSp16;
    private Double volActTradesSp32;
    private Double volActTradesSp48;
    private Double volActTradesSp96;
    private Double volActTradesSp288;

    private Double volActTradesAcc16;
    private Double volActTradesAcc32;
    private Double volActTradesAcc48;
    private Double volActTradesAcc96;
    private Double volActTradesAcc288;

    private Double volActTradesChop16;
    private Double volActTradesChop32;
    private Double volActTradesChop48;
    private Double volActTradesChop96;
    private Double volActTradesChop288;

    // =========================================================================
    // 5) ACTIVITY PRESSURE (QUOTE)
    // =========================================================================
    private Double volActQuoteSp16;
    private Double volActQuoteSp32;
    private Double volActQuoteSp48;
    private Double volActQuoteSp96;
    private Double volActQuoteSp288;

    private Double volActQuoteAcc16;
    private Double volActQuoteAcc32;
    private Double volActQuoteAcc48;
    private Double volActQuoteAcc96;
    private Double volActQuoteAcc288;

    private Double volActQuoteChop16;
    private Double volActQuoteChop32;
    private Double volActQuoteChop48;
    private Double volActQuoteChop96;
    private Double volActQuoteChop288;

    // =========================================================================
    // 6) MICROBURST / SPIKE
    // =========================================================================
    private Double volVolumeSpikeScore16;
    private Double volTradesSpikeScore16;

    private Double volMicroburstVolumeIntensity16;
    private Double volMicroburstTradesIntensity16;

    private Double volMicroburstCombo16;

    private Double volVolumeSpikeScore32;
    private Double volVolumeSpikeScore48;
    private Double volVolumeSpikeScore96;

    private Double volTradesSpikeScore32;
    private Double volTradesSpikeScore48;
    private Double volTradesSpikeScore96;

    private Double volMicroburstVolumeIntensity32;
    private Double volMicroburstVolumeIntensity48;
    private Double volMicroburstVolumeIntensity96;

    private Double volMicroburstTradesIntensity32;
    private Double volMicroburstTradesIntensity48;
    private Double volMicroburstTradesIntensity96;

    private Double volMicroburstCombo32;
    private Double volMicroburstCombo48;
    private Double volMicroburstCombo96;

    // =========================================================================
    // 7) EXHAUSTION SIGNALS / DRYUP
    // =========================================================================
    private Double volExhaustionClimaxScore;
    private Double volExhaustionClimaxScore48;
    private Double volExhaustionClimaxScore96;

    private Double volVolumeDryupScore32;
    private Double volVolumeDryupScore48;
    private Double volVolumeDryupScore96;
    private Double volVolumeDryupScore288;

    private Double volExhaustionDryupAfterTrend32;
    private Double volExhaustionDryupAfterTrend48;
    private Double volExhaustionDryupAfterTrend96;
    private Double volExhaustionDryupAfterTrend288;

    // =========================================================================
    // 8) VOLUME REGIME / CLUSTERS
    // =========================================================================
    private Double volHighVolumeFreq32;
    private Double volHighVolumeFreq48;
    private Double volHighVolumeFreq96;
    private Double volHighVolumeFreq288;

    private Double volHighVolumeAge;
    private Double volHighVolumeClusterLen;

    private Double volHighVolumeAge32;
    private Double volHighVolumeAge96;
    private Double volHighVolumeAge288;

    private Double volHighVolumeClusterLen32;
    private Double volHighVolumeClusterLen96;
    private Double volHighVolumeClusterLen288;

    // =========================================================================
    // 9) MICROSTRUCTURE - TRADE SIZE
    // =========================================================================
    private Double volAvgTradeSize;

    private Double volAvgTradeSizeRel32;
    private Double volAvgTradeSizeRel48;
    private Double volAvgTradeSizeRel96;
    private Double volAvgTradeSizeRel288;

    private Double volAvgTradeSizeZscore32;
    private Double volAvgTradeSizeZscore48;
    private Double volAvgTradeSizeZscore96;
    private Double volAvgTradeSizeZscore288;

    private Double volAvgQuotePerTrade;

    private Double volAvgQuotePerTradeRel32;
    private Double volAvgQuotePerTradeRel48;
    private Double volAvgQuotePerTradeRel96;
    private Double volAvgQuotePerTradeRel288;

    private Double volAvgQuotePerTradeZscore32;
    private Double volAvgQuotePerTradeZscore48;
    private Double volAvgQuotePerTradeZscore96;
    private Double volAvgQuotePerTradeZscore288;

    // =========================================================================
    // 10) VWAP DISTANCE
    // =========================================================================
    private Double volVwap;
    private Double volVwapDistance;

    // =========================================================================
    // 11) MICROBURST / PRESSURE SLOPES
    // =========================================================================
    private Double volMicroburstSlope16;
    private Double volPressureSlope16;

    private Double volMicroburstSlope32;
    private Double volMicroburstSlope48;
    private Double volMicroburstSlope96;

    private Double volPressureSlope32;
    private Double volPressureSlope48;
    private Double volPressureSlope96;

    // =========================================================================
    // 12) VOLUME ACCELERATION
    // =========================================================================
    private Double volVolumeAcceleration;

    // =========================================================================
    // 13) ORDER FLOW IMBALANCE (OFI)
    // =========================================================================
    private Double volOfi;

    private Double volOfiRel16;
    private Double volOfiRel48;
    private Double volOfiRel96;
    private Double volOfiRel288;

    private Double volOfiZscore32;
    private Double volOfiZscore96;
    private Double volOfiZscore288;

    private Double volOfiSlpW20;
    private Double volOfiFlipRateW20;
    private Double volOfiPrstW20;
    private Double volOfiVvW20;

    // =========================================================================
    // 14) VPIN
    // =========================================================================
    private Double volVpin50;
    private Double volVpin100;

    private Double volVpin200;
    private Double volVpin100Zscore200;
    private Double volVpin100Delta1;
    private Double volVpin100Delta3;

    // =========================================================================
    // 15) BID/ASK PRESSURE PROXY (BAP)
    // =========================================================================
    private Double volBap;
    private Double volBapSlope16;
    private Double volBapAcc16;

    private Double volBapRel16;
    private Double volBapZscore32;
    private Double volBapFlipRateW20;
    private Double volBapPrstW20;
    private Double volBapVvW20;

    // =========================================================================
    // 16) SIGNED VOLUME RATIO (SVR)
    // =========================================================================
    private Double volSvr;

    private Double volSvrRel16;
    private Double volSvrRel48;
    private Double volSvrRel96;
    private Double volSvrRel288;

    private Double volSvrZscore32;
    private Double volSvrZscore96;
    private Double volSvrZscore288;

    private Double volSvrSlpW20;
    private Double volSvrFlipRateW20;
    private Double volSvrPrstW20;
    private Double volSvrVvW20;

    private Double volSvrAcc5;
    private Double volSvrAcc10;

    // =========================================================================
    // 17) VOLATILITY OF VOLUME (VoV)
    // =========================================================================
    private Double volVov16;
    private Double volVov32;
    private Double volVovZscore32;

    private Double volVov48;
    private Double volVov96;
    private Double volVov288;

    private Double volVovZscore96;
    private Double volVovZscore288;

    // =========================================================================
    // 18) VOLUME REGIME - COMPOSITES
    // =========================================================================
    private Double volRegimeState;
    private Double volRegimeConf;
    private Double volRegimePrstW20;
    private Double volRegimeFlipRateW50;
}
