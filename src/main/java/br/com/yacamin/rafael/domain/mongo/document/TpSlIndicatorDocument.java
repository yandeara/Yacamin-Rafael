package br.com.yacamin.rafael.domain.mongo.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class TpSlIndicatorDocument {

    @Id
    private String id;

    private String symbol;
    private Instant openTime;

    // Setup prices
    private Double tpslTpPriceLong;
    private Double tpslSlPriceLong;
    private Double tpslTpPriceShort;
    private Double tpslSlPriceShort;

    // Distances
    private Double tpslTpDistLong;
    private Double tpslTpDistShort;
    private Double tpslSlDistLong;
    private Double tpslSlDistShort;

    // ATR14 normalized distances
    private Double tpslTpDistAtr14Long;
    private Double tpslTpDistAtr14Short;
    private Double tpslSlDistAtr14Long;
    private Double tpslSlDistAtr14Short;

    // RV48 normalized distances
    private Double tpslTpDistRv48Long;
    private Double tpslTpDistRv48Short;
    private Double tpslSlDistRv48Long;
    private Double tpslSlDistRv48Short;

    // EWMA48 normalized distances
    private Double tpslTpDistEwma48Long;
    private Double tpslTpDistEwma48Short;
    private Double tpslSlDistEwma48Long;
    private Double tpslSlDistEwma48Short;

    // Risk/Reward
    private Double tpslRrRatioLong;
    private Double tpslRrRatioShort;

    // Headroom/Room ATR14
    private Double tpslHeadroomToTpAtr14Long;
    private Double tpslHeadroomToTpAtr14Short;
    private Double tpslRoomToSlAtr14Long;
    private Double tpslRoomToSlAtr14Short;

    // Headroom/Room candle extremes
    private Double tpslHeadroomHighToTpAtr14Long;
    private Double tpslHeadroomLowToTpAtr14Short;
    private Double tpslRoomLowToSlAtr14Long;
    private Double tpslRoomHighToSlAtr14Short;

    // Donchian w48
    private Double tpslDonchHighW48;
    private Double tpslDonchLowW48;
    private Double tpslDonchPosW48;
    private Double tpslDistToDonchHighW48Atrn;
    private Double tpslDistToDonchLowW48Atrn;

    // Donchian w96
    private Double tpslDonchHighW96;
    private Double tpslDonchLowW96;
    private Double tpslDonchPosW96;
    private Double tpslDistToDonchHighW96Atrn;
    private Double tpslDistToDonchLowW96Atrn;

    // Donchian w288
    private Double tpslDonchHighW288;
    private Double tpslDonchLowW288;
    private Double tpslDonchPosW288;
    private Double tpslDistToDonchHighW288Atrn;
    private Double tpslDistToDonchLowW288Atrn;

    // Bollinger
    private Double tpslBbPercentb20;
    private Double tpslBbPercentb48;
    private Double tpslBbPercentb96;
    private Double tpslBbZ20;
    private Double tpslBbZ48;
    private Double tpslBbZ96;

    // Keltner
    private Double tpslKeltPos20;
    private Double tpslKeltPos48;
    private Double tpslKeltPos96;

    // Efficiency
    private Double tpslEffRatioW48;
    private Double tpslChopEffW48;
}
