package br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolyNewMarketEvent extends PolyWsBaseEvent {

    private String id;
    private String question;
    private String market;
    private String slug;
    private String description;

    @JsonProperty("assets_ids")
    private List<String> assetsIds;

    private List<String> outcomes;

    @JsonProperty("event_message")
    private EventMessage eventMessage;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventMessage {
        private String id;
        private String ticker;
        private String slug;
        private String title;
        private String description;
    }
}
