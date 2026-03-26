package br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolyBestBidAskEvent extends PolyWsBaseEvent {

    private String market;

    @JsonProperty("asset_id")
    private String assetId;

    @JsonProperty("best_bid")
    private String bestBid;

    @JsonProperty("best_ask")
    private String bestAsk;

    private String spread;
}
