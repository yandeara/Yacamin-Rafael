package br.com.yacamin.rafael.adapter.out.rest.binance;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class KlineRequest {

    private String symbol;
    private String interval;
    private Long startTime;
    private Long endTime;
    private Integer limit;

    public HashMap<String, Object> getParams() {
        HashMap<String, Object> params = new HashMap<>();
        if (symbol != null) params.put("symbol", symbol);
        if (interval != null) params.put("interval", interval);
        if (startTime != null) params.put("startTime", startTime);
        if (endTime != null) params.put("endTime", endTime);
        if (limit != null) params.put("limit", limit);
        return params;
    }
}
