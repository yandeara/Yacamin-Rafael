package br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolyPriceChangeEvent extends PolyWsBaseEvent {

    private String market;

    @JsonProperty("price_changes")
    private List<PriceChangeItem> priceChanges;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PriceChangeItem {
        @JsonProperty("asset_id")
        private String assetId;

        private double price;
        private double size;
        private String side;

        @JsonProperty("best_bid")
        private double bestBid;

        @JsonProperty("best_ask")
        private double bestAsk;
    }
}