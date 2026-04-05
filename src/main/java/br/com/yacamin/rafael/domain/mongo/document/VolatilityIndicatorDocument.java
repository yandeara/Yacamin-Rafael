package br.com.yacamin.rafael.domain.mongo.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document
public class VolatilityIndicatorDocument {

    @Id
    private String id;

    private String symbol;
    private Instant openTime;

    // =========================================================================
    // 1) ATR — LEVEL + CHANGE
    // =========================================================================
    private Double vltAtr7;
    private Double vltAtr14;
    private Double vltAtr21;

    private Double vltAtr7Chg;
    private Double vltAtr14Chg;
    private Double vltAtr21Chg;

    // V3.1 — ATR 24H / MULTI-SCALE
    private Double vltAtr48;
    private Double vltAtr96;
    private Double vltAtr288;

    private Double vltAtr48Chg;
    private Double vltAtr96Chg;
    private Double vltAtr288Chg;

    // =========================================================================
    // 2) ATR — LOCAL / RANGE-LOCAL
    // =========================================================================
    private Double vltRangeAtr14Loc;
    private Double vltRangeAtr14LocChg;

    // V3.1 — ATR LOCAL 24H / MULTI-SCALE
    private Double vltRangeAtr48Loc;
    private Double vltRangeAtr48LocChg;

    private Double vltRangeAtr96Loc;
    private Double vltRangeAtr96LocChg;

    private Double vltRangeAtr288Loc;
    private Double vltRangeAtr288LocChg;

    // =========================================================================
    // 3) ATR — REGIME (7/21) + V3.1 REGIME MULTI-PAIRS
    // =========================================================================
    private Double vltAtr721Ratio;
    private Double vltAtr721Expr;
    private Double vltAtr721Expn;
    private Double vltAtr721Cmpr;

    private Double vltAtr748Ratio;
    private Double vltAtr748Expn;
    private Double vltAtr748Cmpr;

    private Double vltAtr1448Ratio;
    private Double vltAtr1448Expn;
    private Double vltAtr1448Cmpr;

    private Double vltAtr1496Ratio;
    private Double vltAtr1496Expn;
    private Double vltAtr1496Cmpr;

    private Double vltAtr14288Ratio;
    private Double vltAtr14288Expn;
    private Double vltAtr14288Cmpr;

    private Double vltAtr48288Ratio;
    private Double vltAtr48288Expn;
    private Double vltAtr48288Cmpr;

    private Double vltAtr96288Ratio;
    private Double vltAtr96288Expn;
    private Double vltAtr96288Cmpr;

    // =========================================================================
    // 4) ATR — ZSCORE / SLOPE
    // =========================================================================
    private Double vltAtr14Zsc;
    private Double vltAtr14Slp;

    private Double vltAtr7Zsc;
    private Double vltAtr21Zsc;
    private Double vltAtr48Zsc;
    private Double vltAtr96Zsc;
    private Double vltAtr288Zsc;

    private Double vltAtr7Slp;
    private Double vltAtr21Slp;
    private Double vltAtr48Slp;
    private Double vltAtr96Slp;
    private Double vltAtr288Slp;

    // =========================================================================
    // 5) ATR — VOL-OF-VOL
    // =========================================================================
    private Double vltAtr14VvW16;
    private Double vltAtr14VvW32;

    private Double vltAtr14VvW48;
    private Double vltAtr14VvW96;
    private Double vltAtr14VvW288;

    private Double vltAtr48VvW16;
    private Double vltAtr48VvW32;
    private Double vltAtr48VvW48;

    private Double vltAtr96VvW32;
    private Double vltAtr96VvW48;
    private Double vltAtr96VvW96;

    private Double vltAtr288VvW48;
    private Double vltAtr288VvW96;
    private Double vltAtr288VvW288;

    // =========================================================================
    // 6) SEASONALITY — ATR / RANGE (+ additions)
    // =========================================================================
    private Double vltAtr14Season;
    private Double vltRangeSeason;

    private Double vltAtr48Season;
    private Double vltAtr96Season;
    private Double vltAtr288Season;

    private Double vltStd20Season;
    private Double vltStd48Season;
    private Double vltStd96Season;
    private Double vltStd288Season;

    private Double vltBoll20WidthSeason;
    private Double vltBoll48WidthSeason;
    private Double vltBoll96WidthSeason;
    private Double vltBoll288WidthSeason;

    private Double vltVolRv30Season;
    private Double vltVolRv48Season;
    private Double vltVolRv96Season;
    private Double vltVolRv288Season;

    // =========================================================================
    // 7) STD — LEVEL + CHANGE (+ 24h additions)
    // =========================================================================
    private Double vltStd14;
    private Double vltStd20;
    private Double vltStd50;

    private Double vltStd14Chg;
    private Double vltStd20Chg;
    private Double vltStd50Chg;

    private Double vltStd48;
    private Double vltStd96;
    private Double vltStd288;

    private Double vltStd48Chg;
    private Double vltStd96Chg;
    private Double vltStd288Chg;

    // =========================================================================
    // 8) STD — REGIME (14/50) + multi-pairs
    // =========================================================================
    private Double vltStd1450Ratio;
    private Double vltStd1450Expn;
    private Double vltStd1450Cmpr;

    private Double vltStd1448Ratio;
    private Double vltStd1448Expn;
    private Double vltStd1448Cmpr;

    private Double vltStd1496Ratio;
    private Double vltStd1496Expn;
    private Double vltStd1496Cmpr;

    private Double vltStd14288Ratio;
    private Double vltStd14288Expn;
    private Double vltStd14288Cmpr;

    private Double vltStd2048Ratio;
    private Double vltStd2048Expn;
    private Double vltStd2048Cmpr;

    private Double vltStd48288Ratio;
    private Double vltStd48288Expn;
    private Double vltStd48288Cmpr;

    private Double vltStd96288Ratio;
    private Double vltStd96288Expn;
    private Double vltStd96288Cmpr;

    // =========================================================================
    // 9) STD — ZSCORE / SLOPE / VOL-OF-VOL (+ additions)
    // =========================================================================
    private Double vltStd20Zsc;
    private Double vltStd20Slp;
    private Double vltStd20VvW20;

    private Double vltStd14Zsc;
    private Double vltStd50Zsc;
    private Double vltStd48Zsc;
    private Double vltStd96Zsc;
    private Double vltStd288Zsc;

    private Double vltStd14Slp;
    private Double vltStd50Slp;
    private Double vltStd48Slp;
    private Double vltStd96Slp;
    private Double vltStd288Slp;

    private Double vltStd14VvW20;
    private Double vltStd50VvW20;
    private Double vltStd48VvW20;
    private Double vltStd96VvW20;
    private Double vltStd288VvW20;

    private Double vltStd20VvW48;
    private Double vltStd48VvW48;
    private Double vltStd96VvW48;
    private Double vltStd288VvW48;

    // =========================================================================
    // 10) BOLLINGER WIDTH (BB) (+ 24h additions)
    // =========================================================================
    private Double vltBoll20Width;
    private Double vltBoll20WidthChg;
    private Double vltBoll20WidthZsc;
    private Double vltBoll20WidthAtrn;

    private Double vltBoll48Width;
    private Double vltBoll48WidthChg;
    private Double vltBoll48WidthZsc;
    private Double vltBoll48WidthAtrn;

    private Double vltBoll96Width;
    private Double vltBoll96WidthChg;
    private Double vltBoll96WidthZsc;
    private Double vltBoll96WidthAtrn;

    private Double vltBoll288Width;
    private Double vltBoll288WidthChg;
    private Double vltBoll288WidthZsc;
    private Double vltBoll288WidthAtrn;

    // =========================================================================
    // 11) KELTNER + SQUEEZE (+ 24h additions)
    // =========================================================================
    private Double vltKelt20Width;
    private Double vltVolSqzBbKelt;
    private Double vltVolSqzBbKeltChg;

    private Double vltKelt48Width;
    private Double vltKelt96Width;
    private Double vltKelt288Width;

    private Double vltVolSqzBbKelt20;
    private Double vltVolSqzBbKelt20Chg;
    private Double vltVolSqzBbKelt20Zsc;

    private Double vltVolSqzBbKelt48;
    private Double vltVolSqzBbKelt48Chg;
    private Double vltVolSqzBbKelt48Zsc;

    private Double vltVolSqzBbKelt96;
    private Double vltVolSqzBbKelt96Chg;
    private Double vltVolSqzBbKelt96Zsc;

    private Double vltVolSqzBbKelt288;
    private Double vltVolSqzBbKelt288Chg;
    private Double vltVolSqzBbKelt288Zsc;

    // =========================================================================
    // 12) RANGE-BASED VOLATILITY (GK / PARK / RS) (+ 24h additions)
    // =========================================================================
    private Double vltVolGk16;
    private Double vltVolGk32;

    private Double vltVolPark16;
    private Double vltVolPark32;

    private Double vltVolRs16;
    private Double vltVolRs32;

    private Double vltVolGk48;
    private Double vltVolGk96;
    private Double vltVolGk288;

    private Double vltVolPark48;
    private Double vltVolPark96;
    private Double vltVolPark288;

    private Double vltVolRs48;
    private Double vltVolRs96;
    private Double vltVolRs288;

    private Double vltVolGk16Zsc;
    private Double vltVolGk32Zsc;
    private Double vltVolGk48Zsc;
    private Double vltVolGk96Zsc;
    private Double vltVolGk288Zsc;

    private Double vltVolPark16Zsc;
    private Double vltVolPark32Zsc;
    private Double vltVolPark48Zsc;
    private Double vltVolPark96Zsc;
    private Double vltVolPark288Zsc;

    private Double vltVolRs16Zsc;
    private Double vltVolRs32Zsc;
    private Double vltVolRs48Zsc;
    private Double vltVolRs96Zsc;
    private Double vltVolRs288Zsc;

    private Double vltVolGk16Slp;
    private Double vltVolGk32Slp;
    private Double vltVolGk48Slp;
    private Double vltVolGk96Slp;
    private Double vltVolGk288Slp;

    private Double vltVolPark16Slp;
    private Double vltVolPark32Slp;
    private Double vltVolPark48Slp;
    private Double vltVolPark96Slp;
    private Double vltVolPark288Slp;

    private Double vltVolRs16Slp;
    private Double vltVolRs32Slp;
    private Double vltVolRs48Slp;
    private Double vltVolRs96Slp;
    private Double vltVolRs288Slp;

    private Double vltVolGk1648Ratio;
    private Double vltVolGk3248Ratio;
    private Double vltVolGk48288Ratio;

    private Double vltVolPark1648Ratio;
    private Double vltVolPark3248Ratio;
    private Double vltVolPark48288Ratio;

    private Double vltVolRs1648Ratio;
    private Double vltVolRs3248Ratio;
    private Double vltVolRs48288Ratio;

    // =========================================================================
    // 13) REALIZED VOLATILITY (RV) (+ 24h additions)
    // =========================================================================
    private Double vltVolRv10;
    private Double vltVolRv30;
    private Double vltVolRv50;

    private Double vltVolRv10Zsc;
    private Double vltVolRv1050Ratio;
    private Double vltVolRv30Slp;

    private Double vltVolRv48;
    private Double vltVolRv96;
    private Double vltVolRv288;

    private Double vltVolRv30Zsc;
    private Double vltVolRv50Zsc;
    private Double vltVolRv48Zsc;
    private Double vltVolRv96Zsc;
    private Double vltVolRv288Zsc;

    private Double vltVolRv10PctileW80;
    private Double vltVolRv30PctileW80;
    private Double vltVolRv50PctileW80;
    private Double vltVolRv48PctileW80;
    private Double vltVolRv96PctileW80;
    private Double vltVolRv288PctileW80;

    private Double vltVolRv1030Ratio;
    private Double vltVolRv1048Ratio;
    private Double vltVolRv1096Ratio;
    private Double vltVolRv10288Ratio;

    private Double vltVolRv3048Ratio;
    private Double vltVolRv3096Ratio;
    private Double vltVolRv30288Ratio;

    private Double vltVolRv48288Ratio;
    private Double vltVolRv96288Ratio;

    private Double vltVolRv10Slp;
    private Double vltVolRv50Slp;
    private Double vltVolRv48Slp;
    private Double vltVolRv96Slp;
    private Double vltVolRv288Slp;

    private Double vltVolRv30VvW20;
    private Double vltVolRv48VvW20;
    private Double vltVolRv96VvW20;
    private Double vltVolRv288VvW20;

    // =========================================================================
    // 14) EWMA VOLATILITY
    // =========================================================================
    private Double vltEwmaVol20;
    private Double vltEwmaVol32;

    private Double vltEwmaVol48;
    private Double vltEwmaVol96;
    private Double vltEwmaVol288;

    private Double vltEwmaVol20Zsc;
    private Double vltEwmaVol32Zsc;
    private Double vltEwmaVol48Zsc;
    private Double vltEwmaVol96Zsc;
    private Double vltEwmaVol288Zsc;

    private Double vltEwmaVol20Slp;
    private Double vltEwmaVol32Slp;
    private Double vltEwmaVol48Slp;
    private Double vltEwmaVol96Slp;
    private Double vltEwmaVol288Slp;

    private Double vltEwmaVol2048Ratio;
    private Double vltEwmaVol3248Ratio;
    private Double vltEwmaVol48288Ratio;
    private Double vltEwmaVol96288Ratio;

    // =========================================================================
    // 15) MEAN ABS RETURN
    // =========================================================================
    private Double vltRetAbsMean16;
    private Double vltRetAbsMean32;
    private Double vltRetAbsMean1632Ratio;

    private Double vltRetAbsMean48;
    private Double vltRetAbsMean96;
    private Double vltRetAbsMean288;

    private Double vltRetAbsMean16Zsc;
    private Double vltRetAbsMean32Zsc;
    private Double vltRetAbsMean48Zsc;
    private Double vltRetAbsMean96Zsc;
    private Double vltRetAbsMean288Zsc;

    private Double vltRetAbsMean16Slp;
    private Double vltRetAbsMean32Slp;
    private Double vltRetAbsMean48Slp;
    private Double vltRetAbsMean96Slp;
    private Double vltRetAbsMean288Slp;

    private Double vltRetAbsMean1648Ratio;
    private Double vltRetAbsMean3248Ratio;
    private Double vltRetAbsMean48288Ratio;
    private Double vltRetAbsMean96288Ratio;

    // =========================================================================
    // 16) BIG MOVE VOLATILITY (events) + explicit 1%/2%
    // =========================================================================
    private Double vltVolBigmoveFreq20;
    private Double vltVolBigmoveFreq50;
    private Double vltVolBigmoveAge;
    private Double vltVolBigmoveClusterLen;

    private Double vltBigmove1pctFreq48;
    private Double vltBigmove1pctAge48;
    private Double vltBigmove1pctClusterLen48;

    private Double vltBigmove1pctFreq96;
    private Double vltBigmove1pctAge96;
    private Double vltBigmove1pctClusterLen96;

    private Double vltBigmove1pctFreq288;
    private Double vltBigmove1pctAge288;
    private Double vltBigmove1pctClusterLen288;

    private Double vltBigmove2pctFreq48;
    private Double vltBigmove2pctAge48;
    private Double vltBigmove2pctClusterLen48;

    private Double vltBigmove2pctFreq96;
    private Double vltBigmove2pctAge96;
    private Double vltBigmove2pctClusterLen96;

    private Double vltBigmove2pctFreq288;
    private Double vltBigmove2pctAge288;
    private Double vltBigmove2pctClusterLen288;

    // =========================================================================
    // 17) RETURN DISTRIBUTION (+ longer windows)
    // =========================================================================
    private Double vltRet10Skew;
    private Double vltRet10Kurt;

    private Double vltRet30Skew;
    private Double vltRet30Kurt;

    private Double vltRet50Skew;
    private Double vltRet50Kurt;
    private Double vltRet96Skew;
    private Double vltRet96Kurt;
    private Double vltRet288Skew;
    private Double vltRet288Kurt;

    // =========================================================================
    // 18) STRUCTURAL VOL (long memory) + extensions
    // =========================================================================
    private Double vltHurst100;
    private Double vltHurst200;
    private Double vltEntropyRet50;

    private Double vltHurst50;
    private Double vltHurst400;
    private Double vltHurst800;

    private Double vltEntropyRet100;
    private Double vltEntropyRet200;
    private Double vltEntropyRet400;

    private Double vltEntropyAbsret50;
    private Double vltEntropyAbsret100;
    private Double vltEntropyAbsret200;

    // =========================================================================
    // 19) VOLATILITY REGIME — COMPOSITES (new group)
    // =========================================================================
    private Double vltRegimeState;
    private Double vltRegimeConf;
    private Double vltRegimePrstW20;
    private Double vltRegimeFlipRateW50;

    // =========================================================================
    // 20) SQUEEZE DURATION / AGE (new group)
    // =========================================================================
    private Double vltBoll20SqueezeLen;
    private Double vltBoll20SqueezeAge;
    private Double vltBoll20SqueezePrstW20;

    private Double vltBoll48SqueezeLen;
    private Double vltBoll48SqueezeAge;
    private Double vltBoll48SqueezePrstW20;

    private Double vltBoll96SqueezeLen;
    private Double vltBoll96SqueezeAge;
    private Double vltBoll96SqueezePrstW20;

    private Double vltBoll288SqueezeLen;
    private Double vltBoll288SqueezeAge;
    private Double vltBoll288SqueezePrstW20;

    private Double vltVolSqzBbKelt20PrstW20;
    private Double vltVolSqzBbKelt48PrstW20;
    private Double vltVolSqzBbKelt96PrstW20;
    private Double vltVolSqzBbKelt288PrstW20;

    private Double vltVolSqzBbKelt20State;
    private Double vltVolSqzBbKelt48State;
    private Double vltVolSqzBbKelt96State;
    private Double vltVolSqzBbKelt288State;

    // =========================================================================
    // 21) TARGET VIABILITY — DIRECT FEATURES FOR 1% / 2% IN 24H (new group)
    // =========================================================================
    private Double vltAtr14Pct;
    private Double vltAtr48Pct;
    private Double vltAtr96Pct;
    private Double vltAtr288Pct;

    private Double vltStd20Pct;
    private Double vltStd48Pct;
    private Double vltStd96Pct;
    private Double vltStd288Pct;

    private Double vltVolRv30Pct;
    private Double vltVolRv48Pct;
    private Double vltVolRv96Pct;
    private Double vltVolRv288Pct;

    private Double vltEwmaVol32Pct;
    private Double vltEwmaVol48Pct;
    private Double vltEwmaVol96Pct;
    private Double vltEwmaVol288Pct;

    private Double vltTarget1pctInAtr14;
    private Double vltTarget2pctInAtr14;

    private Double vltTarget1pctInRv48;
    private Double vltTarget2pctInRv48;

    private Double vltTarget1pctInEwma48;
    private Double vltTarget2pctInEwma48;

    private Double vltTarget1pctInRv96;
    private Double vltTarget2pctInRv96;

    private Double vltTarget1pctInRv288;
    private Double vltTarget2pctInRv288;

    private Double vltTarget1pctInEwma96;
    private Double vltTarget2pctInEwma96;

    private Double vltTarget1pctInEwma288;
    private Double vltTarget2pctInEwma288;

    // =========================================================================
    // 22) EXTRA RATIOS (added at end of entity)
    // =========================================================================
    private Double vltVolGk1696Ratio;
    private Double vltVolPark1696Ratio;
    private Double vltVolRs1696Ratio;
}
