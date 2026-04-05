package br.com.yacamin.rafael.adapter.in.controller;

import br.com.yacamin.rafael.adapter.out.websocket.binance.SpotMarketDataWsAdapter;
import br.com.yacamin.rafael.adapter.out.websocket.polymarket.PolymarketMarketClobSocket;
import br.com.yacamin.rafael.application.service.algoritms.simulation.SimulationMarketMemoryService;
import br.com.yacamin.rafael.application.service.model.BlockInferenceMemoryService;
import br.com.yacamin.rafael.application.service.model.ModelRegistryService;
import br.com.yacamin.rafael.application.service.trading.BinanceStreamConfigService;
import br.com.yacamin.rafael.application.service.trading.MarketGroupService;
import br.com.yacamin.rafael.domain.mongo.document.ModelRegistryDocument;
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
            @RequestParam(name = "marketGroup", required = false) String marketGroup) {

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
                inf.put("valid", p.getValid());
                inferenceList.add(inf);

                if (p.getMinuteInBlock() == 1) {
                    blockOpenPrice = p.getOpenMid();
                }
            }
            row.put("inferences", inferenceList);
            row.put("blockOpenPrice", blockOpenPrice != null ? round(blockOpenPrice, 2) : null);

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
    public Object addStream(@RequestParam(name = "symbol") String symbol) {
        return binanceStreamConfigService.add(symbol);
    }

    @PostMapping("/streams/start")
    public void startStream(@RequestParam(name = "id") String id) {
        binanceStreamConfigService.start(id);
    }

    @PostMapping("/streams/pause")
    public void pauseStream(@RequestParam(name = "id") String id) {
        binanceStreamConfigService.pause(id);
    }

    @PostMapping("/streams/remove")
    public void removeStream(@RequestParam(name = "id") String id) {
        binanceStreamConfigService.remove(id);
    }

    // ── Market Groups ──

    @GetMapping("/markets/groups")
    public List<Map<String, Object>> listMarketGroups() {
        return marketGroupService.listAll();
    }

    @PostMapping("/markets/groups/add")
    public Object addMarketGroup(
            @RequestParam(name = "slugPrefix") String slugPrefix,
            @RequestParam(name = "displayName") String displayName,
            @RequestParam(name = "blockDuration") String blockDuration,
            @RequestParam(name = "binanceStream") String binanceStream) {
        BlockDuration duration = BlockDuration.valueOf(blockDuration);
        return marketGroupService.add(slugPrefix, displayName, duration, binanceStream);
    }

    @PostMapping("/markets/groups/start")
    public void startMarketGroup(@RequestParam(name = "id") String id) {
        marketGroupService.start(id);
    }

    @PostMapping("/markets/groups/pause")
    public void pauseMarketGroup(@RequestParam(name = "id") String id) {
        marketGroupService.pause(id);
    }

    @PostMapping("/markets/groups/remove")
    public void removeMarketGroup(@RequestParam(name = "id") String id) {
        marketGroupService.remove(id);
    }

    // ── Models ──

    @GetMapping("/models")
    public List<Map<String, Object>> listModels() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var m : modelRegistryService.getAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("predictionType", m.predictionType().name());
            row.put("type", m.type());
            row.put("symbol", m.symbol());
            row.put("horizon", m.horizon());
            row.put("timeframe", m.timeframe());
            row.put("range", m.displayRange());
            row.put("fileName", m.fileName());

            var reg = modelRegistryService.getRegistry(m.fileName());
            row.put("active", reg.map(ModelRegistryDocument::isActive).orElse(false));
            row.put("featureCount", reg.map(r -> r.getFeatureNames() != null ? r.getFeatureNames().size() : 0).orElse(0));

            result.add(row);
        }
        return result;
    }

    @PostMapping("/models/activate")
    public Map<String, Object> activateModel(@RequestParam(name = "fileName") String fileName) {
        modelRegistryService.activateModel(fileName);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("ok", true);
        res.put("fileName", fileName);
        return res;
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
