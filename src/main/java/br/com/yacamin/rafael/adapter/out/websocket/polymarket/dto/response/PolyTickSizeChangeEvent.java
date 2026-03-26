package br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolyTickSizeChangeEvent extends PolyWsBaseEvent {

    @JsonProperty("asset_id")
    private String assetId;

    private String market;

    @JsonProperty("old_tick_size")
    private String oldTickSize;

    @JsonProperty("new_tick_size")
    private String newTickSize;
}
