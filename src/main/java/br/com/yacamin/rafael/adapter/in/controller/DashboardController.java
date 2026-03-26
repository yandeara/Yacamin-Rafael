package br.com.yacamin.rafael.adapter.in.controller;

import br.com.yacamin.rafael.adapter.out.websocket.binance.SpotMarketDataWsAdapter;
import br.com.yacamin.rafael.adapter.out.websocket.polymarket.PolymarketMarketClobSocket;
import br.com.yacamin.rafael.application.service.algoritms.simulation.SimulationMarketMemoryService;
import br.com.yacamin.rafael.application.service.model.BlockInferenceMemoryService;
import br.com.yacamin.rafael.application.service.model.ModelRegistryService;
import br.com.yacamin.rafael.application.service.trading.BinanceStreamConfigService;
import br.com.yacamin.rafael.application.service.trading.MarketGroupService;
import br.com.yacamin.rafael.domain.BlockDuration;
import br.com.yacamin.rafael.domain.InferencePrediction;
import br.com.yacamin.rafael.domain.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final String ALGO = "RAFAEL";
    private static final ZoneId SP_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter SP_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final SimulationMarketMemoryService simulationMarketMemoryService;
    private final SpotMarketDataWsAdapter binanceWs;
    private final PolymarketMarketClobSocket polyMarketWs;
    private final BinanceStreamConfigService binanceStreamConfigService;
    private final MarketGroupService marketGroupService;
    private final BlockInferenceMemoryService blockInferenceMemory;
    private final ModelRegistryService modelRegistryService;

    @GetMapping("/markets")
    public Map<String, Object> getMarkets(
            @RequestParam(required = false) String marketGroup) {

        Map<String, Market> all = simulationMarketMemoryService.getAllMarkets(ALGO);

        List<Map<String, Object>> markets = new ArrayList<>();
        long nowEpoch = Instant.now().getEpochSecond();

        for (Map.Entry<String, Market> entry : all.entrySet()) {
            Market m = entry.getValue();
            long unix = m.getUnixTime();

            if (marketGroup != null && !marketGroup.isBlank()
                    && m.getMarketGroup() != null && !marketGroup.equals(m.getMarketGroup())) {
                continue;
            }

            boolean started = unix <= nowEpoch;
            if (!started) continue;

            BlockDuration duration = m.getBlockDuration() != null
                    ? m.getBlockDuration()
                    : marketGroupService.getBlockDuration(m.getMarketGroup());
            long blockEndUnix = duration.blockEnd(unix);

            String startSP = Instant.ofEpochSecond(unix)
                    .atZone(SP_ZONE).format(SP_FMT);
            String endSP = Instant.ofEpochSecond(blockEndUnix)
                    .atZone(SP_ZONE).format(SP_FMT);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("unixTime", unix);
            row.put("timeSP", startSP + " – " + endSP);
            row.put("slug", m.getSlug());
            row.put("marketGroup", m.getMarketGroup());
            row.put("displayName", m.getDisplayName());
            row.put("blockDurationSeconds", duration.getSeconds());
            row.put("timeRemaining", round(m.getTimeRemaining(), 0));
            row.put("midPrice", round(m.getMidPrice(), 2));
            row.put("tickCount", m.getTickCount());
            row.put("outcome", m.getOutcome());
            row.put("resolved", m.isResolved());
            row.put("upBid", round(m.getUpBid(), 3));
            row.put("upAsk", round(m.getUpAsk(), 3));
            row.put("downBid", round(m.getDownBid(), 3));
            row.put("downAsk", round(m.getDownAsk(), 3));

            // Inferências minute-by-minute para este bloco
            List<InferencePrediction> predictions = blockInferenceMemory.getPredictions(unix);
            List<Map<String, Object>> inferenceList = new ArrayList<>();
            Double blockOpenPrice = null;
            for (InferencePrediction p : predictions) {
                Map<String, Object> inf = new LinkedHashMap<>();
                inf.put("minute", p.getMinuteInBlock());
                inf.put("direction", p.getDirection());
                inf.put("confidence", round(p.getConfidence(), 4));
                inf.put("openMid", round(p.getOpenMid(), 2));
                inf.put("modelThreshold", p.getModelThreshold() != null ? round(p.getModelThreshold(), 2) : null);
                inf.put("closeMid", p.getCloseMid() != null ? round(p.getCloseMid(), 2) : null);
                inf.put("hit", p.getHit());
                inferenceList.add(inf);

                if (p.getMinuteInBlock() == 1) {
                    blockOpenPrice = p.getOpenMid();
                }
            }
            row.put("inferences", inferenceList);
            row.put("blockOpenPrice", blockOpenPrice != null ? round(blockOpenPrice, 2) : null);

            // Inferência block-by-block (candle 5m prevê este bloco)
            InferencePrediction b2b = blockInferenceMemory.getBlockPrediction(unix);
            if (b2b != null) {
                Map<String, Object> b2bMap = new LinkedHashMap<>();
                b2bMap.put("direction", b2b.getDirection());
                b2bMap.put("confidence", round(b2b.getConfidence(), 4));
                b2bMap.put("openMid", round(b2b.getOpenMid(), 2));
                b2bMap.put("closeMid", b2b.getCloseMid() != null ? round(b2b.getCloseMid(), 2) : null);
                b2bMap.put("hit", b2b.getHit());
                b2bMap.put("modelThreshold", b2b.getModelThreshold() != null ? round(b2b.getModelThreshold(), 2) : null);
                b2bMap.put("marketOpen", b2b.getMarketOpen() != null ? round(b2b.getMarketOpen(), 2) : null);
                b2bMap.put("valid", b2b.getValid());
                b2bMap.put("resolvedOutcome", b2b.getResolvedOutcome());
                b2bMap.put("hitResolve", b2b.getHitResolve());
                row.put("blockPrediction", b2bMap);
            } else {
                row.put("blockPrediction", null);
            }

            // Inferência horizon H4 (candle 1m min1 prevê o fim do mesmo bloco)
            InferencePrediction h4 = blockInferenceMemory.getHorizonPrediction(unix);
            if (h4 != null) {
                Map<String, Object> h4Map = new LinkedHashMap<>();
                h4Map.put("direction", h4.getDirection());
                h4Map.put("confidence", round(h4.getConfidence(), 4));
                h4Map.put("openMid", round(h4.getOpenMid(), 2));
                h4Map.put("modelThreshold", h4.getModelThreshold() != null ? round(h4.getModelThreshold(), 2) : null);
                h4Map.put("closeMid", h4.getCloseMid() != null ? round(h4.getCloseMid(), 2) : null);
                h4Map.put("hit", h4.getHit());
                h4Map.put("resolvedOutcome", h4.getResolvedOutcome());
                h4Map.put("hitResolve", h4.getHitResolve());
                row.put("horizonPrediction", h4Map);
            } else {
                row.put("horizonPrediction", null);
            }

            markets.add(row);
        }

        Map<String, Boolean> wsStatus = new LinkedHashMap<>();
        wsStatus.put("binance", binanceWs.isConnected());
        wsStatus.put("polyMarket", polyMarketWs.isConnected());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("algorithm", ALGO);
        response.put("wsStatus", wsStatus);
        response.put("markets", markets);
        return response;
    }

    // ── Binance Streams ──

    @GetMapping("/streams")
    public List<Map<String, Object>> listStreams() {
        return binanceStreamConfigService.listAll();
    }

    @PostMapping("/streams/add")
    public Object addStream(@RequestParam String symbol) {
        return binanceStreamConfigService.add(symbol);
    }

    @PostMapping("/streams/start")
    public void startStream(@RequestParam String id) {
        binanceStreamConfigService.start(id);
    }

    @PostMapping("/streams/pause")
    public void pauseStream(@RequestParam String id) {
        binanceStreamConfigService.pause(id);
    }

    @PostMapping("/streams/remove")
    public void removeStream(@RequestParam String id) {
        binanceStreamConfigService.remove(id);
    }

    // ── Market Groups ──

    @GetMapping("/markets/groups")
    public List<Map<String, Object>> listMarketGroups() {
        return marketGroupService.listAll();
    }

    @PostMapping("/markets/groups/add")
    public Object addMarketGroup(
            @RequestParam String slugPrefix,
            @RequestParam String displayName,
            @RequestParam String blockDuration,
            @RequestParam String binanceStream) {
        BlockDuration duration = BlockDuration.valueOf(blockDuration);
        return marketGroupService.add(slugPrefix, displayName, duration, binanceStream);
    }

    @PostMapping("/markets/groups/start")
    public void startMarketGroup(@RequestParam String id) {
        marketGroupService.start(id);
    }

    @PostMapping("/markets/groups/pause")
    public void pauseMarketGroup(@RequestParam String id) {
        marketGroupService.pause(id);
    }

    @PostMapping("/markets/groups/remove")
    public void removeMarketGroup(@RequestParam String id) {
        marketGroupService.remove(id);
    }

    // ── Models ──

    @GetMapping("/models")
    public List<Map<String, Object>> listModels() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var m : modelRegistryService.getAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", m.key());
            row.put("type", m.type());
            row.put("symbol", m.symbol());
            row.put("horizon", m.horizon());
            row.put("interval", m.interval().getValue());
            row.put("trainStart", m.trainStart().toString());
            row.put("trainEnd", m.trainEnd().toString());
            row.put("fileName", m.fileName());
            result.add(row);
        }
        return result;
    }

    @GetMapping("/markets/durations")
    public List<Map<String, Object>> listDurations() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (BlockDuration d : marketGroupService.getAvailableDurations()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", d.name());
            row.put("seconds", d.getSeconds());
            result.add(row);
        }
        return result;
    }

    private double round(double value, int decimals) {
        if (!Double.isFinite(value)) return 0;
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}
