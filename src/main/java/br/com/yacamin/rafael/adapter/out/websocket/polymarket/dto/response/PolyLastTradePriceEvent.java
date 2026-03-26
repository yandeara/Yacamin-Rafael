package br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolyLastTradePriceEvent extends PolyWsBaseEvent {

    @JsonProperty("asset_id")
    private String assetId;

    private String market;
    private String price;
    private String side;
    private String size;

    @JsonProperty("fee_rate_bps")
    private String feeRateBps;
}