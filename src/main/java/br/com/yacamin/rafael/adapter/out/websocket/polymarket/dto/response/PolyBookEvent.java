package br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolyBookEvent extends PolyWsBaseEvent {

    @JsonProperty("asset_id")
    private String assetId;

    private List<BookLevel> bids;
    private List<BookLevel> asks;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookLevel {
        private double price;
        private double size;
    }
}
