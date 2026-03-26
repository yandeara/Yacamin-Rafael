package br.com.yacamin.rafael.domain;

import lombok.Data;

@Data
public class PolyAsset {

    private Long unixTime;
    private String slug;
    private String assetId;
    private String side;

    private double bestBid;
    private double bestAsk;

    public Outcome getOutcome() {
        return side.equals("UP") ? Outcome.UP : Outcome.DOWN;
    }

}
