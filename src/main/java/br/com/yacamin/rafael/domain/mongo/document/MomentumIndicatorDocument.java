package br.com.yacamin.rafael.domain.mongo.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document
public class MomentumIndicatorDocument {

    @Id
    private String id;

    private String symbol;
    private Instant openTime;

    // close_ret
    private Double momCloseRet1;
    private Double momCloseRet2;
    private Double momCloseRet3;
    private Double momCloseRet4;
    private Double momCloseRet5;
    private Double momCloseRet6;
    private Double momCloseRet8;
    private Double momCloseRet10;
    private Double momCloseRet12;
    private Double momCloseRet16;
    private Double momCloseRet24;
    private Double momCloseRet32;
    private Double momCloseRet48;
    private Double momCloseRet288;

    // close_ret atrn
    private Double momCloseRet1Atrn;
    private Double momCloseRet2Atrn;
    private Double momCloseRet3Atrn;
    private Double momCloseRet4Atrn;
    private Double momCloseRet5Atrn;
    private Double momCloseRet6Atrn;
    private Double momCloseRet8Atrn;
    private Double momCloseRet10Atrn;
    private Double momCloseRet12Atrn;
    private Double momCloseRet16Atrn;
    private Double momCloseRet24Atrn;
    private Double momCloseRet32Atrn;
    private Double momCloseRet48Atrn;
    private Double momCloseRet288Atrn;

    // close_ret stdn
    private Double momCloseRet1Stdn;
    private Double momCloseRet2Stdn;
    private Double momCloseRet3Stdn;
    private Double momCloseRet4Stdn;
    private Double momCloseRet5Stdn;
    private Double momCloseRet6Stdn;
    private Double momCloseRet8Stdn;
    private Double momCloseRet10Stdn;
    private Double momCloseRet12Stdn;
    private Double momCloseRet16Stdn;
    private Double momCloseRet24Stdn;
    private Double momCloseRet32Stdn;
    private Double momCloseRet48Stdn;
    private Double momCloseRet288Stdn;

    // close_ret abs
    private Double momCloseRet1Abs;
    private Double momCloseRet2Abs;
    private Double momCloseRet3Abs;
    private Double momCloseRet4Abs;
    private Double momCloseRet5Abs;
    private Double momCloseRet6Abs;
    private Double momCloseRet8Abs;
    private Double momCloseRet10Abs;
    private Double momCloseRet12Abs;
    private Double momCloseRet16Abs;
    private Double momCloseRet24Abs;
    private Double momCloseRet32Abs;
    private Double momCloseRet48Abs;
    private Double momCloseRet288Abs;

    // close_ret abs_atrn
    private Double momCloseRet1AbsAtrn;
    private Double momCloseRet2AbsAtrn;
    private Double momCloseRet3AbsAtrn;
    private Double momCloseRet4AbsAtrn;
    private Double momCloseRet5AbsAtrn;
    private Double momCloseRet6AbsAtrn;
    private Double momCloseRet8AbsAtrn;
    private Double momCloseRet10AbsAtrn;
    private Double momCloseRet12AbsAtrn;
    private Double momCloseRet16AbsAtrn;
    private Double momCloseRet24AbsAtrn;
    private Double momCloseRet32AbsAtrn;
    private Double momCloseRet48AbsAtrn;
    private Double momCloseRet288AbsAtrn;

    // close_ret abs_stdn
    private Double momCloseRet1AbsStdn;
    private Double momCloseRet2AbsStdn;
    private Double momCloseRet3AbsStdn;
    private Double momCloseRet4AbsStdn;
    private Double momCloseRet5AbsStdn;
    private Double momCloseRet6AbsStdn;
    private Double momCloseRet8AbsStdn;
    private Double momCloseRet10AbsStdn;
    private Double momCloseRet12AbsStdn;
    private Double momCloseRet16AbsStdn;
    private Double momCloseRet24AbsStdn;
    private Double momCloseRet32AbsStdn;
    private Double momCloseRet48AbsStdn;
    private Double momCloseRet288AbsStdn;

    // burst
    private Double momBurst10;
    private Double momBurst16;
    private Double momBurst32;
    private Double momBurst48;
    private Double momBurst288;

    // cntrate
    private Double momCntrate10;
    private Double momCntrate16;
    private Double momCntrate32;
    private Double momCntrate48;
    private Double momCntrate288;

    // decay
    private Double momDecay10;
    private Double momDecay16;
    private Double momDecay32;
    private Double momDecay48;
    private Double momDecay288;

    // impls
    private Double momImpls10;
    private Double momImpls16;
    private Double momImpls32;
    private Double momImpls48;
    private Double momImpls288;

    // chprt
    private Double momChprt10;
    private Double momChprt16;
    private Double momChprt32;
    private Double momChprt48;
    private Double momChprt288;

    // RSI raw
    private Double momRsi2;
    private Double momRsi3;
    private Double momRsi4;
    private Double momRsi5;
    private Double momRsi6;
    private Double momRsi7;
    private Double momRsi8;
    private Double momRsi9;
    private Double momRsi10;
    private Double momRsi12;
    private Double momRsi14;
    private Double momRsi16;
    private Double momRsi21;
    private Double momRsi24;
    private Double momRsi28;
    private Double momRsi32;
    private Double momRsi48;
    private Double momRsi288;

    // RSI dlt
    private Double momRsi2Dlt;
    private Double momRsi3Dlt;
    private Double momRsi5Dlt;
    private Double momRsi7Dlt;
    private Double momRsi14Dlt;

    // RSI roc
    private Double momRsi2Roc;
    private Double momRsi3Roc;
    private Double momRsi5Roc;
    private Double momRsi7Roc;
    private Double momRsi14Roc;

    // RSI slp
    private Double momRsi7Slp;
    private Double momRsi14Slp;
    private Double momRsi28Slp;

    // RSI atrn
    private Double momRsi14Atrn;

    // RSI vlt
    private Double momRsi7Vlt;
    private Double momRsi14Vlt;

    // RSI acc
    private Double momRsi14Acc;

    // RSI dst_mid
    private Double momRsi7DstMid;
    private Double momRsi14DstMid;

    // RSI tail
    private Double momRsi7TailUp;
    private Double momRsi7TailDw;
    private Double momRsi14TailUp;
    private Double momRsi14TailDw;

    // RSI 48/288 dlt/roc/slp/vlt/dst_mid/tail
    private Double momRsi48Dlt;
    private Double momRsi288Dlt;
    private Double momRsi48Roc;
    private Double momRsi288Roc;
    private Double momRsi48Slp;
    private Double momRsi288Slp;
    private Double momRsi48Vlt;
    private Double momRsi288Vlt;
    private Double momRsi48DstMid;
    private Double momRsi288DstMid;
    private Double momRsi48TailUp;
    private Double momRsi48TailDw;
    private Double momRsi288TailUp;
    private Double momRsi288TailDw;

    // RSI regime
    private Double momRsi14RegimeState;
    private Double momRsi14RegimePrstW20;

    // RSI zsc/pctile
    private Double momRsi14Zsc80;
    private Double momRsi14PctileW80;

    // RSI shock
    private Double momRsi14Shock1;
    private Double momRsi14Shock1Stdn80;

    // RSI 48 zsc/pctile/shock
    private Double momRsi48Zsc80;
    private Double momRsi48PctileW80;
    private Double momRsi288Zsc80;
    private Double momRsi288PctileW80;
    private Double momRsi48Shock1;
    private Double momRsi288Shock1;
    private Double momRsi48Shock1Stdn80;
    private Double momRsi288Shock1Stdn80;

    // RSI 48/288 regime
    private Double momRsi48RegimeState;
    private Double momRsi48RegimePrstW20;
    private Double momRsi288RegimeState;
    private Double momRsi288RegimePrstW20;

    // CMO
    private Double momCmo14;
    private Double momCmo20;
    private Double momCmo14Dlt;
    private Double momCmo20Dlt;
    private Double momCmo14DstMid;
    private Double momCmo48;
    private Double momCmo288;
    private Double momCmo48Dlt;
    private Double momCmo288Dlt;
    private Double momCmo20DstMid;
    private Double momCmo48DstMid;
    private Double momCmo288DstMid;
    private Double momCmo20Zsc80;
    private Double momCmo20PctileW80;
    private Double momCmo48Zsc80;
    private Double momCmo48PctileW80;
    private Double momCmo288Zsc80;
    private Double momCmo288PctileW80;
    private Double momCmo20Shock1;
    private Double momCmo20Shock1Stdn80;
    private Double momCmo48Shock1;
    private Double momCmo48Shock1Stdn80;
    private Double momCmo288Shock1;
    private Double momCmo288Shock1Stdn80;
    private Double momCmo20RegimeState;
    private Double momCmo20RegimePrstW20;
    private Double momCmo48RegimeState;
    private Double momCmo48RegimePrstW20;
    private Double momCmo288RegimeState;
    private Double momCmo288RegimePrstW20;

    // WPR
    private Double momWpr14;
    private Double momWpr14Dlt;
    private Double momWpr28;
    private Double momWpr28Dlt;
    private Double momWpr42;
    private Double momWpr42Dlt;
    private Double momWpr14DstMid;
    private Double momWpr48;
    private Double momWpr48Dlt;
    private Double momWpr288;
    private Double momWpr288Dlt;
    private Double momWpr28DstMid;
    private Double momWpr42DstMid;
    private Double momWpr48DstMid;
    private Double momWpr288DstMid;
    private Double momWpr28Zsc80;
    private Double momWpr28PctileW80;
    private Double momWpr48Zsc80;
    private Double momWpr48PctileW80;
    private Double momWpr288Zsc80;
    private Double momWpr288PctileW80;
    private Double momWpr28Shock1;
    private Double momWpr28Shock1Stdn80;
    private Double momWpr48Shock1;
    private Double momWpr48Shock1Stdn80;
    private Double momWpr288Shock1;
    private Double momWpr288Shock1Stdn80;
    private Double momWpr28RegimeState;
    private Double momWpr28RegimePrstW20;
    private Double momWpr48RegimeState;
    private Double momWpr48RegimePrstW20;
    private Double momWpr288RegimeState;
    private Double momWpr288RegimePrstW20;

    // Stochastic
    private Double momStoch14K;
    private Double momStoch14D;
    private Double momStoch14KDlt;
    private Double momStoch14DDlt;
    private Double momStoch48K;
    private Double momStoch48D;
    private Double momStoch48KDlt;
    private Double momStoch48DDlt;
    private Double momStoch288K;
    private Double momStoch288D;
    private Double momStoch288KDlt;
    private Double momStoch288DDlt;
    private Double momStoch14Spread;
    private Double momStoch48Spread;
    private Double momStoch288Spread;
    private Double momStoch14CrossState;
    private Double momStoch48CrossState;
    private Double momStoch288CrossState;
    private Double momStoch14KDstMid;
    private Double momStoch48KDstMid;
    private Double momStoch288KDstMid;
    private Double momStoch14KZsc80;
    private Double momStoch14KPctileW80;
    private Double momStoch48KZsc80;
    private Double momStoch48KPctileW80;
    private Double momStoch288KZsc80;
    private Double momStoch288KPctileW80;
    private Double momStoch14KShock1;
    private Double momStoch14KShock1Stdn80;
    private Double momStoch48KShock1;
    private Double momStoch48KShock1Stdn80;
    private Double momStoch288KShock1;
    private Double momStoch288KShock1Stdn80;
    private Double momStoch14KRegimeState;
    private Double momStoch14KRegimePrstW20;
    private Double momStoch48KRegimeState;
    private Double momStoch48KRegimePrstW20;
    private Double momStoch288KRegimeState;
    private Double momStoch288KRegimePrstW20;

    // TRIX
    private Double momTrix9;
    private Double momTrix48;
    private Double momTrix288;
    private Double momTrix9Dlt;
    private Double momTrix48Dlt;
    private Double momTrix288Dlt;
    private Double momTrix9Sig9;
    private Double momTrix9Hist;
    private Double momTrix9CrossState;
    private Double momTrix48Sig9;
    private Double momTrix48Hist;
    private Double momTrix48CrossState;
    private Double momTrix288Sig9;
    private Double momTrix288Hist;
    private Double momTrix288CrossState;
    private Double momTrix9Zsc80;
    private Double momTrix9PctileW80;
    private Double momTrix48Zsc80;
    private Double momTrix48PctileW80;
    private Double momTrix288Zsc80;
    private Double momTrix288PctileW80;
    private Double momTrix9Shock1;
    private Double momTrix9Shock1Stdn80;
    private Double momTrix48Shock1;
    private Double momTrix48Shock1Stdn80;
    private Double momTrix288Shock1;
    private Double momTrix288Shock1Stdn80;
    private Double momTrix9RegimeState;
    private Double momTrix9RegimePrstW20;
    private Double momTrix48RegimeState;
    private Double momTrix48RegimePrstW20;
    private Double momTrix288RegimeState;
    private Double momTrix288RegimePrstW20;

    // TSI
    private Double momTsi2513;
    private Double momTsi2513Dlt;
    private Double momTsi4825;
    private Double momTsi4825Dlt;
    private Double momTsi288144;
    private Double momTsi288144Dlt;
    private Double momTsi2513Sig7;
    private Double momTsi2513Hist;
    private Double momTsi2513CrossState;
    private Double momTsi4825Sig7;
    private Double momTsi4825Hist;
    private Double momTsi4825CrossState;
    private Double momTsi288144Sig7;
    private Double momTsi288144Hist;
    private Double momTsi288144CrossState;
    private Double momTsi2513DstMid;
    private Double momTsi4825DstMid;
    private Double momTsi288144DstMid;
    private Double momTsi2513Zsc80;
    private Double momTsi2513PctileW80;
    private Double momTsi4825Zsc80;
    private Double momTsi4825PctileW80;
    private Double momTsi288144Zsc80;
    private Double momTsi288144PctileW80;
    private Double momTsi2513Shock1;
    private Double momTsi2513Shock1Stdn80;
    private Double momTsi4825Shock1;
    private Double momTsi4825Shock1Stdn80;
    private Double momTsi288144Shock1;
    private Double momTsi288144Shock1Stdn80;
    private Double momTsi2513RegimeState;
    private Double momTsi2513RegimePrstW20;
    private Double momTsi4825RegimeState;
    private Double momTsi4825RegimePrstW20;
    private Double momTsi288144RegimeState;
    private Double momTsi288144RegimePrstW20;

    // PPO 12/26
    private Double momPpo1226;
    private Double momPpoSig12269;
    private Double momPpoHist12269;
    private Double momPpo1226Dlt;
    private Double momPpoHist12269Dlt;
    private Double momPpo1226Zsc80;
    private Double momPpo1226PctileW80;
    private Double momPpoHist12269Zsc80;
    private Double momPpoHist12269PctileW80;
    private Double momPpoHist12269Shock1;
    private Double momPpoHist12269Shock1Stdn80;
    private Double momPpoRegimeState;
    private Double momPpoRegimePrstW20;

    // PPO 48/104
    private Double momPpo48104;
    private Double momPpoSig481049;
    private Double momPpoHist481049;
    private Double momPpo48104Dlt;
    private Double momPpoHist481049Dlt;
    private Double momPpo48104Zsc80;
    private Double momPpo48104PctileW80;
    private Double momPpoHist481049Zsc80;
    private Double momPpoHist481049PctileW80;
    private Double momPpoHist481049Shock1;
    private Double momPpoHist481049Shock1Stdn80;
    private Double momPpo48104RegimeState;
    private Double momPpo48104RegimePrstW20;

    // PPO 288/576
    private Double momPpo288576;
    private Double momPpoSig2885769;
    private Double momPpoHist2885769;
    private Double momPpo288576Dlt;
    private Double momPpoHist2885769Dlt;
    private Double momPpo288576Zsc80;
    private Double momPpo288576PctileW80;
    private Double momPpoHist2885769Zsc80;
    private Double momPpoHist2885769PctileW80;
    private Double momPpoHist2885769Shock1;
    private Double momPpoHist2885769Shock1Stdn80;
    private Double momPpo288576RegimeState;
    private Double momPpo288576RegimePrstW20;

    // close slp
    private Double momClose3Slp;
    private Double momClose8Slp;
    private Double momClose14Slp;
    private Double momClose50Slp;
    private Double momClose3SlpAtrn;
    private Double momClose8SlpAtrn;
    private Double momClose14SlpAtrn;
    private Double momClose50SlpAtrn;
    private Double momClose3SlpAcc;
    private Double momClose8SlpAcc;
    private Double momClose14SlpAcc;
    private Double momClose3SlpAccAtrn;
    private Double momClose8SlpAccAtrn;
    private Double momClose14SlpAccAtrn;
    private Double momClose3Zsc;
    private Double momClose8Zsc;
    private Double momClose14Zsc;
    private Double momClose50Zsc;

    // close slp 48/288
    private Double momClose48Slp;
    private Double momClose288Slp;
    private Double momClose48SlpAtrn;
    private Double momClose288SlpAtrn;
    private Double momClose48SlpAcc;
    private Double momClose288SlpAcc;
    private Double momClose48SlpAccAtrn;
    private Double momClose288SlpAccAtrn;
    private Double momClose48Zsc;
    private Double momClose288Zsc;

    // CCI
    private Double momCci14;
    private Double momCci20;
    private Double momCci48;
    private Double momCci288;
    private Double momCci14Dlt;
    private Double momCci20Dlt;
    private Double momCci48Dlt;
    private Double momCci288Dlt;
    private Double momCci14DstMid;
    private Double momCci20DstMid;
    private Double momCci48DstMid;
    private Double momCci288DstMid;
    private Double momCci20Zsc80;
    private Double momCci20PctileW80;
    private Double momCci48Zsc80;
    private Double momCci48PctileW80;
    private Double momCci288Zsc80;
    private Double momCci288PctileW80;
    private Double momCci20Shock1;
    private Double momCci20Shock1Stdn80;
    private Double momCci48Shock1;
    private Double momCci48Shock1Stdn80;
    private Double momCci288Shock1;
    private Double momCci288Shock1Stdn80;
    private Double momCci20RegimeState;
    private Double momCci20RegimePrstW20;
    private Double momCci48RegimeState;
    private Double momCci48RegimePrstW20;
    private Double momCci288RegimeState;
    private Double momCci288RegimePrstW20;

    // ROC
    private Double momRoc1;
    private Double momRoc2;
    private Double momRoc3;
    private Double momRoc5;
    private Double momRoc48;
    private Double momRoc288;
    private Double momRoc1Abs;
    private Double momRoc2Abs;
    private Double momRoc3Abs;
    private Double momRoc5Abs;
    private Double momRoc48Abs;
    private Double momRoc288Abs;
    private Double momRoc5Zsc80;
    private Double momRoc5PctileW80;
    private Double momRoc48Zsc80;
    private Double momRoc48PctileW80;
    private Double momRoc288Zsc80;
    private Double momRoc288PctileW80;
    private Double momRoc5Shock1;
    private Double momRoc5Shock1Stdn80;
    private Double momRoc48Shock1;
    private Double momRoc48Shock1Stdn80;
    private Double momRoc288Shock1;
    private Double momRoc288Shock1Stdn80;
    private Double momRoc5RegimeState;
    private Double momRoc5RegimePrstW20;
    private Double momRoc48RegimeState;
    private Double momRoc48RegimePrstW20;
    private Double momRoc288RegimeState;
    private Double momRoc288RegimePrstW20;

    // Alignment
    private Double momAlignRsi14PpoHist12269;
    private Double momAlignRsi48PpoHist481049;
    private Double momAlignRsi288PpoHist2885769;
    private Double momAlignTrixHist9TsiHist2513;
    private Double momAlignTrixHist48TsiHist4825;
    private Double momAlignTrixHist288TsiHist288144;

    // Consensus / Chop
    private Double momMomentumConsensusScore;
    private Double momChopScore;

    // Consensus dynamics
    private Double momMomentumConflictScore;
    private Double momMomentumConsensusDlt;
    private Double momMomentumConsensusPrstW20;

    // PPO hist flip rate
    private Double momPpoHist12269FlipRateW20;
    private Double momPpoHist481049FlipRateW20;
    private Double momPpoHist2885769FlipRateW20;

    // TRIX hist flip rate
    private Double momTrixHist9FlipRateW20;
    private Double momTrixHist48FlipRateW20;
    private Double momTrixHist288FlipRateW20;

    // TSI hist flip rate
    private Double momTsiHist2513FlipRateW20;
    private Double momTsiHist4825FlipRateW20;
    private Double momTsiHist288144FlipRateW20;

    // PPO hist coherence
    private Double momPpoHistCoh1226Vs48104;
    private Double momPpoHistCoh1226Vs288576;

    // TRIX hist coherence
    private Double momTrixHistCoh9Vs48;

    // TSI hist coherence
    private Double momTsiHistCoh2513Vs4825;

    // Consensus slp/flip
    private Double momMomentumConsensusSlpW20;
    private Double momMomentumConsensusFlipRateW20;

    // Divergence
    private Double momDivCloseSlp48VsPpoHist48;
    private Double momDivCloseSlp48VsRsi48;
    private Double momDivCloseSlp288VsPpoHist288;

    // Consensus vol/shock
    private Double momMomentumConsensusVolW20;
    private Double momMomentumConsensusShock1;
    private Double momMomentumConsensusShock1StdnW20;

    // Consensus counts
    private Double momMomentumDisagreementCount;
    private Double momMomentumAgreementCount;

    // Consensus quality
    private Double momMomentumConsensusAbs;
    private Double momMomentumNeutralCount;
    private Double momMomentumVoteEntropy;
    private Double momMomentumConsensusRunLen;
    private Double momMomentumSignalQualityScore;

    // PPO hist slp w20
    private Double momPpoHist12269SlpW20;
    private Double momPpoHist481049SlpW20;
    private Double momPpoHist2885769SlpW20;

    // Consensus extremes
    private Double momMomentumConsensusZsc80;
    private Double momMomentumConsensusPctileW80;

    // TRIX hist slp w20
    private Double momTrixHist9SlpW20;
    private Double momTrixHist48SlpW20;
    private Double momTrixHist288SlpW20;

    // TSI hist slp w20
    private Double momTsiHist2513SlpW20;
    private Double momTsiHist4825SlpW20;
    private Double momTsiHist288144SlpW20;
}
