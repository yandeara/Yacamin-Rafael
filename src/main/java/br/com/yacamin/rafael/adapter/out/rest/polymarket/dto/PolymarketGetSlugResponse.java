package br.com.yacamin.rafael.adapter.out.rest.polymarket.dto;

import br.com.yacamin.rafael.application.configuration.JsonStringArrayToListDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolymarketGetSlugResponse {

    private String id;
    private String question;
    private String conditionId;
    private String slug;
    private String resolutionSource;

    private Instant endDate;
    private String liquidity;
    private Instant startDate;

    private String image;
    private String icon;
    private String description;

    @JsonDeserialize(using = JsonStringArrayToListDeserializer.class)
    private List<String> outcomes;

    @JsonDeserialize(using = JsonStringArrayToListDeserializer.class)
    private List<String> outcomePrices;

    private String volume;

    private Boolean active;
    private Boolean closed;

    private String marketMakerAddress;

    private Instant createdAt;
    private Instant updatedAt;

    @JsonProperty("new")
    private Boolean isNew;

    private Boolean featured;
    private Boolean archived;
    private Boolean restricted;

    private String groupItemThreshold;

    private String questionID;

    private Boolean enableOrderBook;

    private Double orderPriceMinTickSize;
    private Double orderMinSize;

    private Double volumeNum;
    private Double liquidityNum;

    private String endDateIso;
    private String startDateIso;

    private Boolean hasReviewedDates;

    private Double volume24hr;
    private Double volume1wk;
    private Double volume1mo;
    private Double volume1yr;

    @JsonDeserialize(using = JsonStringArrayToListDeserializer.class)
    private List<String> clobTokenIds;

    private Double volume24hrClob;
    private Double volume1wkClob;
    private Double volume1moClob;
    private Double volume1yrClob;

    private Double volumeClob;
    private Double liquidityClob;

    private Integer makerBaseFee;
    private Integer takerBaseFee;

    private Boolean acceptingOrders;
    private Boolean negRisk;

    private List<PolymarketEventResponse> events;

    private Boolean ready;
    private Boolean funded;

    private Instant acceptingOrdersTimestamp;

    private Boolean cyom;
    private Double competitive;

    private Boolean pagerDutyNotificationEnabled;
    private Boolean approved;

    private Double rewardsMinSize;
    private Double rewardsMaxSpread;

    private Double spread;

    private Double oneDayPriceChange;
    private Double oneHourPriceChange;

    private Double lastTradePrice;
    private Double bestBid;
    private Double bestAsk;

    private Boolean automaticallyActive;
    private Boolean clearBookOnStart;

    private Boolean showGmpSeries;
    private Boolean showGmpOutcome;

    private Boolean manualActivation;
    private Boolean negRiskOther;

    /**
     * Atenção: esse campo vem como STRING contendo JSON:
     * ex: "[]"
     */
    private String umaResolutionStatuses;

    private Boolean pendingDeployment;
    private Boolean deploying;

    private Boolean rfqEnabled;

    private Instant eventStartTime;

    private Boolean holdingRewardsEnabled;
    private Boolean feesEnabled;
    private Boolean requiresTranslation;

    private Integer makerRebatesFeeShareBps;

    public Map<String, String> getTokenMap() {
        if(this.outcomes.size() != this.clobTokenIds.size())
            throw new RuntimeException("Outcome != ClobTokens");

        Map<String, String> tokenMap = new HashMap<>();

        for(int i = 0; i < this.outcomes.size(); i++) {
            tokenMap.put(this.outcomes.get(i), this.clobTokenIds.get(i));
        }

        return tokenMap;
    }

}
