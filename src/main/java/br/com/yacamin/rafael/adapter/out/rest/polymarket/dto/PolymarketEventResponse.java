package br.com.yacamin.rafael.adapter.out.rest.polymarket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolymarketEventResponse {

    private String id;
    private String ticker;
    private String slug;
    private String title;

    private String description;
    private String resolutionSource;

    private Instant startDate;
    private Instant creationDate;
    private Instant endDate;

    private String image;
    private String icon;

    private Boolean active;
    private Boolean closed;
    private Boolean archived;

    @JsonProperty("new")
    private Boolean isNew;

    private Boolean featured;
    private Boolean restricted;

    private Double liquidity;
    private Double volume;

    private Double openInterest;

    private Instant createdAt;
    private Instant updatedAt;

    private Double competitive;

    private Double volume24hr;
    private Double volume1wk;
    private Double volume1mo;
    private Double volume1yr;

    private Boolean enableOrderBook;

    private Double liquidityClob;

    private Boolean negRisk;

    private Integer commentCount;
    private Boolean cyom;

    private Boolean showAllOutcomes;
    private Boolean showMarketImages;

    private Boolean enableNegRisk;
    private Boolean automaticallyActive;

    private Instant startTime;

    private String seriesSlug;

    private Boolean negRiskAugmented;

    private Boolean pendingDeployment;
    private Boolean deploying;

    private Boolean requiresTranslation;
}
