package br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolyMarketResolvedEvent extends PolyWsBaseEvent {

    @JsonProperty("winning_asset_id")
    private String winningAssetId;

    @JsonProperty("winning_outcome")
    private String winningOutcome;
}