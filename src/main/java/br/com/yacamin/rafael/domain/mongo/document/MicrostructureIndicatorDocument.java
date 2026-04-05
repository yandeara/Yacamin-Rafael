package br.com.yacamin.rafael.domain.mongo.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document
public class MicrostructureIndicatorDocument {

    @Id
    private String id;

    private String symbol;
    private Instant openTime;

    // Amihud raw
    private Double micAmihud;

    // Amihud z-score
    private Double micAmihudZscore20;
    private Double micAmihudZscore80;

    // Amihud relative
    private Double micAmihudRel10;
    private Double micAmihudRel40;

    // Amihud slope
    private Double micAmihudSlpW4;
    private Double micAmihudSlpW20;
    private Double micAmihudSlpW50;

    // Amihud acceleration
    private Double micAmihudAccW4;
    private Double micAmihudAccW5;
    private Double micAmihudAccW10;
    private Double micAmihudAccW16;

    // Amihud moving average
    private Double micAmihudMa10;
    private Double micAmihudMa20;
    private Double micAmihudMa30;

    // Amihud volatility (std)
    private Double micAmihudVol10;
    private Double micAmihudVol20;
    private Double micAmihudVol40;

    // Amihud turnover
    private Double micAmihudTurnover;

    // Amihud percentile
    private Double micAmihudPctileW20;

    // Amihud signed
    private Double micAmihudSigned;

    // Amihud LRMR (SMA10 - SMA40)
    private Double micAmihudLrmr1040;

    // Amihud stability (STD40 / SMA40)
    private Double micAmihudStability40;

    // Amihud volatility relative (STD10 / STD40)
    private Double micAmihudVolRel40;

    // Amihud ATR normalized
    private Double micAmihudAtrn;

    // Amihud persistence
    private Double micAmihudPrstW10;
    private Double micAmihudPrstW20;
    private Double micAmihudPrstW40;

    // Amihud divergence
    private Double micAmihudDvgc;

    // Amihud regime state
    private Double micAmihudRegimeState;

    // Amihud trend alignment
    private Double micAmihudTrendAlignment;

    // Amihud breakdown risk
    private Double micAmihudBreakdownRisk;

    // Amihud regime confidence
    private Double micAmihudRegimeConf;

    // ── Body ──────────────────────────────────
    private Double micCandleBody;
    private Double micCandleBodyAbs;
    private Double micBodyRatio;
    private Double micCandleEnergyRaw;
    private Double micCandleBodySlpW10;
    private Double micCandleBodySlpW20;
    private Double micCandleBodyMa10;
    private Double micCandleBodyMa20;
    private Double micCandleBodyVol10;
    private Double micCandleBodyVol20;
    private Double micBodyRatioSlpW10;
    private Double micBodyRatioVolW10;
    private Double micBodyAtrRatio;
    private Double micCandleEnergyAtrn;
    private Double micCandleBodyCenterPosition;
    private Double micCandlePressureRaw;
    private Double micCandleStrength;
    private Double micCandleBodyStrengthScore;
    private Double micCandleBodyPct;
    private Double micBodyPerc;
    private Double micCandleBodyRatio;
    private Double micBodyReturn;
    private Double micBodyShockAtrn;
    private Double micBodySignPrstW20;
    private Double micBodyRunLen;

    // ── Hasbrouck (windows: 16, 48, 64, 288) ──────────
    private Double micHasbLambdaW16;
    private Double micHasbLambdaW16Zsc40;
    private Double micHasbLambdaW16Ma20;
    private Double micHasbLambdaW16Dvgc;
    private Double micHasbLambdaW16SlpW20;
    private Double micHasbLambdaW16Vol40;
    private Double micHasbLambdaW16Stability40;
    private Double micHasbLambdaW16PctileW40;
    private Double micHasbLambdaW16Atrn;

    private Double micHasbLambdaW48;
    private Double micHasbLambdaW48Zsc40;
    private Double micHasbLambdaW48Ma20;
    private Double micHasbLambdaW48Dvgc;
    private Double micHasbLambdaW48SlpW20;
    private Double micHasbLambdaW48Vol40;
    private Double micHasbLambdaW48Stability40;
    private Double micHasbLambdaW48PctileW40;
    private Double micHasbLambdaW48Atrn;

    private Double micHasbLambdaW64;
    private Double micHasbLambdaW64Zsc40;
    private Double micHasbLambdaW64Ma20;
    private Double micHasbLambdaW64Dvgc;
    private Double micHasbLambdaW64SlpW20;
    private Double micHasbLambdaW64Vol40;
    private Double micHasbLambdaW64Stability40;
    private Double micHasbLambdaW64PctileW40;
    private Double micHasbLambdaW64Atrn;

    private Double micHasbLambdaW288;
    private Double micHasbLambdaW288Zsc40;
    private Double micHasbLambdaW288Ma20;
    private Double micHasbLambdaW288Dvgc;
    private Double micHasbLambdaW288SlpW20;
    private Double micHasbLambdaW288Vol40;
    private Double micHasbLambdaW288Stability40;
    private Double micHasbLambdaW288PctileW40;
    private Double micHasbLambdaW288Atrn;

    // Hasbrouck-to-Kyle ratios
    private Double micHasbToKyleRatioW16;
    private Double micHasbToKyleRatioW48;
    private Double micHasbToKyleRatioW64;
    private Double micHasbToKyleRatioW288;

    // ── Kyle (windows: 4, 16, 48, 96, 200, 288) ──────────
    private Double micKyleLambdaW4;
    private Double micKyleLambdaW4Atrn;
    private Double micKyleLambdaW4Zsc20;
    private Double micKyleLambdaW4Rel10;
    private Double micKyleLambdaW4Rel40;
    private Double micKyleLambdaW4SlpW4;
    private Double micKyleLambdaW4SlpW20;
    private Double micKyleLambdaW4SlpW50;
    private Double micKyleLambdaW4AccW4;
    private Double micKyleLambdaW4AccW5;
    private Double micKyleLambdaW4AccW10;
    private Double micKyleLambdaW4AccW16;
    private Double micKyleLambdaW4Ma10;
    private Double micKyleLambdaW4Ma20;
    private Double micKyleLambdaW4Ma30;
    private Double micKyleLambdaW4Vol10;
    private Double micKyleLambdaW4Vol20;
    private Double micKyleLambdaW4Vol40;
    private Double micKyleLambdaW4VolRel40;
    private Double micKyleLambdaW4PrstW10;
    private Double micKyleLambdaW4PrstW20;
    private Double micKyleLambdaW4PrstW40;
    private Double micKyleLambdaW4Dvgc;
    private Double micKyleLambdaW4PctileW20;
    private Double micKyleLambdaW4Lrmr1040;
    private Double micKyleLambdaW4Stability40;

    private Double micKyleLambdaW16;
    private Double micKyleLambdaW16Atrn;
    private Double micKyleLambdaW16Zsc20;
    private Double micKyleLambdaW16Rel10;
    private Double micKyleLambdaW16Rel40;
    private Double micKyleLambdaW16SlpW4;
    private Double micKyleLambdaW16SlpW20;
    private Double micKyleLambdaW16SlpW50;
    private Double micKyleLambdaW16AccW4;
    private Double micKyleLambdaW16AccW5;
    private Double micKyleLambdaW16AccW10;
    private Double micKyleLambdaW16AccW16;
    private Double micKyleLambdaW16Ma10;
    private Double micKyleLambdaW16Ma20;
    private Double micKyleLambdaW16Ma30;
    private Double micKyleLambdaW16Vol10;
    private Double micKyleLambdaW16Vol20;
    private Double micKyleLambdaW16Vol40;
    private Double micKyleLambdaW16VolRel40;
    private Double micKyleLambdaW16PrstW10;
    private Double micKyleLambdaW16PrstW20;
    private Double micKyleLambdaW16PrstW40;
    private Double micKyleLambdaW16Dvgc;
    private Double micKyleLambdaW16PctileW20;
    private Double micKyleLambdaW16Lrmr1040;
    private Double micKyleLambdaW16Stability40;

    private Double micKyleLambdaW48;
    private Double micKyleLambdaW48Atrn;
    private Double micKyleLambdaW48Zsc20;
    private Double micKyleLambdaW48Rel10;
    private Double micKyleLambdaW48Rel40;
    private Double micKyleLambdaW48SlpW4;
    private Double micKyleLambdaW48SlpW20;
    private Double micKyleLambdaW48SlpW50;
    private Double micKyleLambdaW48AccW4;
    private Double micKyleLambdaW48AccW5;
    private Double micKyleLambdaW48AccW10;
    private Double micKyleLambdaW48AccW16;
    private Double micKyleLambdaW48Ma10;
    private Double micKyleLambdaW48Ma20;
    private Double micKyleLambdaW48Ma30;
    private Double micKyleLambdaW48Vol10;
    private Double micKyleLambdaW48Vol20;
    private Double micKyleLambdaW48Vol40;
    private Double micKyleLambdaW48VolRel40;
    private Double micKyleLambdaW48PrstW10;
    private Double micKyleLambdaW48PrstW20;
    private Double micKyleLambdaW48PrstW40;
    private Double micKyleLambdaW48Dvgc;
    private Double micKyleLambdaW48PctileW20;
    private Double micKyleLambdaW48Lrmr1040;
    private Double micKyleLambdaW48Stability40;

    private Double micKyleLambdaW96;
    private Double micKyleLambdaW96Atrn;
    private Double micKyleLambdaW96Zsc20;
    private Double micKyleLambdaW96Rel10;
    private Double micKyleLambdaW96Rel40;
    private Double micKyleLambdaW96SlpW4;
    private Double micKyleLambdaW96SlpW20;
    private Double micKyleLambdaW96SlpW50;
    private Double micKyleLambdaW96AccW4;
    private Double micKyleLambdaW96AccW5;
    private Double micKyleLambdaW96AccW10;
    private Double micKyleLambdaW96AccW16;
    private Double micKyleLambdaW96Ma10;
    private Double micKyleLambdaW96Ma20;
    private Double micKyleLambdaW96Ma30;
    private Double micKyleLambdaW96Vol10;
    private Double micKyleLambdaW96Vol20;
    private Double micKyleLambdaW96Vol40;
    private Double micKyleLambdaW96VolRel40;
    private Double micKyleLambdaW96PrstW10;
    private Double micKyleLambdaW96PrstW20;
    private Double micKyleLambdaW96PrstW40;
    private Double micKyleLambdaW96Dvgc;
    private Double micKyleLambdaW96PctileW20;
    private Double micKyleLambdaW96Lrmr1040;
    private Double micKyleLambdaW96Stability40;

    private Double micKyleLambdaW200;
    private Double micKyleLambdaW200Atrn;
    private Double micKyleLambdaW200Zsc20;
    private Double micKyleLambdaW200Rel10;
    private Double micKyleLambdaW200Rel40;
    private Double micKyleLambdaW200SlpW4;
    private Double micKyleLambdaW200SlpW20;
    private Double micKyleLambdaW200SlpW50;
    private Double micKyleLambdaW200AccW4;
    private Double micKyleLambdaW200AccW5;
    private Double micKyleLambdaW200AccW10;
    private Double micKyleLambdaW200AccW16;
    private Double micKyleLambdaW200Ma10;
    private Double micKyleLambdaW200Ma20;
    private Double micKyleLambdaW200Ma30;
    private Double micKyleLambdaW200Vol10;
    private Double micKyleLambdaW200Vol20;
    private Double micKyleLambdaW200Vol40;
    private Double micKyleLambdaW200VolRel40;
    private Double micKyleLambdaW200PrstW10;
    private Double micKyleLambdaW200PrstW20;
    private Double micKyleLambdaW200PrstW40;
    private Double micKyleLambdaW200Dvgc;
    private Double micKyleLambdaW200PctileW20;
    private Double micKyleLambdaW200Lrmr1040;
    private Double micKyleLambdaW200Stability40;

    private Double micKyleLambdaW288;
    private Double micKyleLambdaW288Atrn;
    private Double micKyleLambdaW288Zsc20;
    private Double micKyleLambdaW288Rel10;
    private Double micKyleLambdaW288Rel40;
    private Double micKyleLambdaW288SlpW4;
    private Double micKyleLambdaW288SlpW20;
    private Double micKyleLambdaW288SlpW50;
    private Double micKyleLambdaW288AccW4;
    private Double micKyleLambdaW288AccW5;
    private Double micKyleLambdaW288AccW10;
    private Double micKyleLambdaW288AccW16;
    private Double micKyleLambdaW288Ma10;
    private Double micKyleLambdaW288Ma20;
    private Double micKyleLambdaW288Ma30;
    private Double micKyleLambdaW288Vol10;
    private Double micKyleLambdaW288Vol20;
    private Double micKyleLambdaW288Vol40;
    private Double micKyleLambdaW288VolRel40;
    private Double micKyleLambdaW288PrstW10;
    private Double micKyleLambdaW288PrstW20;
    private Double micKyleLambdaW288PrstW40;
    private Double micKyleLambdaW288Dvgc;
    private Double micKyleLambdaW288PctileW20;
    private Double micKyleLambdaW288Lrmr1040;
    private Double micKyleLambdaW288Stability40;

    // Kyle signed (instantaneous)
    private Double micKyleLambdaSigned;

    // ── PositionBalance (22 features) ──────────
    private Double micCloseOpenRatio;
    private Double micCloseOpenNorm;
    private Double micCloseToHighNorm;
    private Double micCloseToLowNorm;
    private Double micClosePosNorm;
    private Double micCandleClosePosNorm;
    private Double micCloseHlc3Delta;
    private Double micCloseVwapDelta;
    private Double micCloseHlc3Atrn;
    private Double micCloseVwapAtrn;
    private Double micCandleBalanceScore;
    private Double micClosePosZscore20;
    private Double micCandleBalanceZscore20;
    private Double micClosePosMa10;
    private Double micClosePosVol10;
    private Double micCloseOpenRatioMa10;
    private Double micCloseOpenRatioVol10;
    private Double micCandleBalanceMa10;
    private Double micCandleBalanceVol10;
    private Double micBalanceState;
    private Double micCloseTriangleScoreAtrn;
    private Double micCloseTriangleScore;

    // ── Range (41 features) ──────────
    private Double micRange;
    private Double micTrueRange;
    private Double micHlc3;
    private Double micLogRange;
    private Double micRangeSlpW10;
    private Double micRangeSlpW20;
    private Double micRangeAccW5;
    private Double micRangeAccW10;
    private Double micRangeMa10;
    private Double micRangeMa20;
    private Double micRangeMa30;
    private Double micRangeVol10;
    private Double micRangeVol20;
    private Double micRangeCompressionW20;
    private Double micCandleBrr;
    private Double micCandleRange;
    private Double micCandleVolatilityInside;
    private Double micCandleSpreadRatio;
    private Double micCandleLmr;
    private Double micRangeReturn;
    private Double micHighReturn;
    private Double micLowReturn;
    private Double micExtremeRangeReturn;
    private Double micRangeAtrn;
    private Double micTrAtrn;
    private Double micRangeStdn;
    private Double micRangeAtrRatio;
    private Double micRangeAsymmetry;
    private Double micRangeHeadroomAtr;
    private Double micHlc3Ma10;
    private Double micHlc3Ma20;
    private Double micHlc3SlpW20;
    private Double micHlc3Vol10;
    private Double micLogRangeMa10;
    private Double micLogRangeSlpW20;
    private Double micLogRangeVol10;
    private Double micRangeSqueezeW20;
    private Double micGapRatio;
    private Double micTrRangeRatio;
    private Double micLogRangePctileW48;
    private Double micRangeRegimeState;

    // ── Return1C (15 features) ──────────
    private Double micReturn;
    private Double micReturnLog;
    private Double micReturnDirection;
    private Double micReturnAbsoluteStrength;
    private Double micReturnAcceleration;
    private Double micReturnReversalForce;
    private Double micReturnDominanceRatio;
    private Double micRvr;
    private Double micLogReturnDominance;
    private Double micReturnTrDominance;
    private Double micReturnGapPressure;
    private Double micReturnSignPrstW20;
    private Double micReturnRunLen;
    private Double micReturnAtrn;
    private Double micReturnStdn;

    // ── ReturnWindow (16 features) ──────────
    private Double micReturnZscore5;
    private Double micReturnZscore14;
    private Double micReturnStdnW96;
    private Double micReturnStdnW48;
    private Double micReturnStdnW288;
    private Double micReturnPctl20;
    private Double micReturnPctl50;
    private Double micReturnSkew;
    private Double micReturnKurtosis;
    private Double micReturnStdRolling;
    private Double micReturnSmoothness;
    private Double micReturnRsp;
    private Double micReturnRds;
    private Double micReturnRnr;
    private Double micReturnFlipRateW20;
    private Double micReturnAutocorr1W20;

    // ── Roll (7 windows × 17 features = 119) ──────────
    private Double micRollCovW16;
    private Double micRollCovPctW16;
    private Double micRollCovZscW16;
    private Double micRollCovPctZscW16;
    private Double micRollSpreadW16;
    private Double micRollSpreadPctW16;
    private Double micRollSpreadZscW16;
    private Double micRollSpreadPctZscW16;
    private Double micRollSpreadMaW16;
    private Double micRollSpreadPrstW16;
    private Double micRollSpreadPctileW16;
    private Double micRollSpreadAccW16;
    private Double micRollSpreadSlpW16;
    private Double micRollSpreadVolW16;
    private Double micRollSpreadDvgcW16;
    private Double micRollSpreadAtrn14W16;
    private Double micRollSpreadPctAtrn14W16;

    private Double micRollCovW32;
    private Double micRollCovPctW32;
    private Double micRollCovZscW32;
    private Double micRollCovPctZscW32;
    private Double micRollSpreadW32;
    private Double micRollSpreadPctW32;
    private Double micRollSpreadZscW32;
    private Double micRollSpreadPctZscW32;
    private Double micRollSpreadMaW32;
    private Double micRollSpreadPrstW32;
    private Double micRollSpreadPctileW32;
    private Double micRollSpreadAccW32;
    private Double micRollSpreadSlpW32;
    private Double micRollSpreadVolW32;
    private Double micRollSpreadDvgcW32;
    private Double micRollSpreadAtrn14W32;
    private Double micRollSpreadPctAtrn14W32;

    private Double micRollCovW48;
    private Double micRollCovPctW48;
    private Double micRollCovZscW48;
    private Double micRollCovPctZscW48;
    private Double micRollSpreadW48;
    private Double micRollSpreadPctW48;
    private Double micRollSpreadZscW48;
    private Double micRollSpreadPctZscW48;
    private Double micRollSpreadMaW48;
    private Double micRollSpreadPrstW48;
    private Double micRollSpreadPctileW48;
    private Double micRollSpreadAccW48;
    private Double micRollSpreadSlpW48;
    private Double micRollSpreadVolW48;
    private Double micRollSpreadDvgcW48;
    private Double micRollSpreadAtrn14W48;
    private Double micRollSpreadPctAtrn14W48;

    private Double micRollCovW96;
    private Double micRollCovPctW96;
    private Double micRollCovZscW96;
    private Double micRollCovPctZscW96;
    private Double micRollSpreadW96;
    private Double micRollSpreadPctW96;
    private Double micRollSpreadZscW96;
    private Double micRollSpreadPctZscW96;
    private Double micRollSpreadMaW96;
    private Double micRollSpreadPrstW96;
    private Double micRollSpreadPctileW96;
    private Double micRollSpreadAccW96;
    private Double micRollSpreadSlpW96;
    private Double micRollSpreadVolW96;
    private Double micRollSpreadDvgcW96;
    private Double micRollSpreadAtrn14W96;
    private Double micRollSpreadPctAtrn14W96;

    private Double micRollCovW336;
    private Double micRollCovPctW336;
    private Double micRollCovZscW336;
    private Double micRollCovPctZscW336;
    private Double micRollSpreadW336;
    private Double micRollSpreadPctW336;
    private Double micRollSpreadZscW336;
    private Double micRollSpreadPctZscW336;
    private Double micRollSpreadMaW336;
    private Double micRollSpreadPrstW336;
    private Double micRollSpreadPctileW336;
    private Double micRollSpreadAccW336;
    private Double micRollSpreadSlpW336;
    private Double micRollSpreadVolW336;
    private Double micRollSpreadDvgcW336;
    private Double micRollSpreadAtrn14W336;
    private Double micRollSpreadPctAtrn14W336;

    private Double micRollCovW512;
    private Double micRollCovPctW512;
    private Double micRollCovZscW512;
    private Double micRollCovPctZscW512;
    private Double micRollSpreadW512;
    private Double micRollSpreadPctW512;
    private Double micRollSpreadZscW512;
    private Double micRollSpreadPctZscW512;
    private Double micRollSpreadMaW512;
    private Double micRollSpreadPrstW512;
    private Double micRollSpreadPctileW512;
    private Double micRollSpreadAccW512;
    private Double micRollSpreadSlpW512;
    private Double micRollSpreadVolW512;
    private Double micRollSpreadDvgcW512;
    private Double micRollSpreadAtrn14W512;
    private Double micRollSpreadPctAtrn14W512;

    private Double micRollCovW672;
    private Double micRollCovPctW672;
    private Double micRollCovZscW672;
    private Double micRollCovPctZscW672;
    private Double micRollSpreadW672;
    private Double micRollSpreadPctW672;
    private Double micRollSpreadZscW672;
    private Double micRollSpreadPctZscW672;
    private Double micRollSpreadMaW672;
    private Double micRollSpreadPrstW672;
    private Double micRollSpreadPctileW672;
    private Double micRollSpreadAccW672;
    private Double micRollSpreadSlpW672;
    private Double micRollSpreadVolW672;
    private Double micRollSpreadDvgcW672;
    private Double micRollSpreadAtrn14W672;
    private Double micRollSpreadPctAtrn14W672;

    // ── Wick (25 features) ──────────
    private Double micCandleUpperWick;
    private Double micCandleLowerWick;
    private Double micCandleUpperWickPct;
    private Double micCandleLowerWickPct;
    private Double micWickPercUp;
    private Double micWickPercDown;
    private Double micCandleWickImbalance;
    private Double micWickImbalance;
    private Double micCandleWickPressureScore;
    private Double micCandleShadowRatio;
    private Double micCandleWickBodyAlignment;
    private Double micShadowImbalanceScore;
    private Double micCandleWickDominance;
    private Double micCandleWickExhaustion;
    private Double micCandleTotalWick;
    private Double micCandleTotalWickPct;
    private Double micCandleTotalWickAtrn;
    private Double micCandleWickImbalanceNorm;
    private Double micCandleWickImbalanceSlpW10;
    private Double micCandleWickImbalanceVol10;
    private Double micClosePosSlpW20;
    private Double micCandleUpperWickMa10;
    private Double micCandleLowerWickMa10;
    private Double micUpperWickReturn;
    private Double micLowerWickReturn;

    // ── ShapePattern (25 features) ──────────
    private Double micCandleDirection;
    private Double micCandleType;
    private Double micCandleShapeIndex;
    private Double micCandleSymmetryScore;
    private Double micCandleTriangleScore;
    private Double micCandleGeometryScore;
    private Double micCandleEntropy;
    private Double micCandleDojiScore;
    private Double micCandleImpulseScore;
    private Double micCandleCompressionIndex;
    private Double micCandleDirectionPrstW10;
    private Double micCandleDirectionPrstW20;
    private Double micCandleGeometryMa10;
    private Double micCandleGeometrySlpW20;
    private Double micCandleGeometryVol10;
    private Double micCandleShapeIndexMa20;
    private Double micCandleShapeIndexVol20;
    private Double micCandleCompressionMa20;
    private Double micCandleCompressionMa48;
    private Double micCandleCompressionVol48;
    private Double micCandleCompressionZscore20;
    private Double micCandleShapeRegimeState;
    private Double micCandleTypeFlipRateW20;
    private Double micCandleDirectionFlipRateW20;
    private Double micCandleGeometryMa48;
}
