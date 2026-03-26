package br.com.yacamin.rafael.adapter.out.websocket.polymarket;

import br.com.yacamin.rafael.adapter.in.event.dto.PolyMarketReconnectMarketEvent;
import br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
@Service
public class PolymarketMarketClobSocket {

    public static final String MARKET_CHANNEL = "market";
    public static final String USER_CHANNEL   = "user";

    private final OkHttpClient client;
    private final ObjectMapper om;

    private final AtomicReference<WebSocket> wsRef = new AtomicReference<>();
    private final Queue<String> pendingSends = new ConcurrentLinkedQueue<>();

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pingTask;

    private volatile boolean opened = false;

    public boolean isConnected() {
        return opened && wsRef.get() != null;
    }

    // configuração atual
    private volatile String channelType;
    private volatile String baseUrl;
    private volatile List<String> data = List.of();
    private volatile Map<String, String> auth = Map.of();
    private volatile boolean verbose = true;

    // callbacks (injeção via setter)
    private volatile Consumer<String> onMessage = msg -> {};
    private volatile Consumer<Throwable> onError = err -> {};
    private volatile Consumer<String> onClose = reason -> {};

    private final Map<String, String> subscribedMap = new ConcurrentHashMap<>();

    private final ApplicationEventPublisher eventPublisher;
    private final AtomicLong pingSentAt = new AtomicLong(0);

    public PolymarketMarketClobSocket(OkHttpClient okHttpClient, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.client = okHttpClient != null
                ? okHttpClient
                : new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ZERO)
                .build();

        this.om = objectMapper != null ? objectMapper : new ObjectMapper();
        this.eventPublisher = eventPublisher;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-orderbook-ping");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Configure antes de conectar.
     */
    public void configure(String channelType,
                          String baseUrl,
                          List<String> data,
                          Map<String, String> auth,
                          boolean verbose) {

        this.channelType = Objects.requireNonNull(channelType, "channelType");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.data = data != null ? List.copyOf(data) : List.of();
        this.auth = auth != null ? Map.copyOf(auth) : Map.of();
        this.verbose = verbose;
    }

    public void setOnMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage != null ? onMessage : msg -> {};
    }

    public void setOnError(Consumer<Throwable> onError) {
        this.onError = onError != null ? onError : err -> {};
    }

    public void setOnClose(Consumer<String> onClose) {
        this.onClose = onClose != null ? onClose : reason -> {};
    }

    /**
     * Conecta com a configuração atual.
     */
    public synchronized void connect() {
        if (opened || wsRef.get() != null) {
            if (verbose) System.out.println("WS já conectado/connecting");
            return;
        }
        if (channelType == null || baseUrl == null) {
            throw new IllegalStateException("Chame configure(...) antes de connect()");
        }

        String fullUrl = baseUrl + "/ws/" + channelType;

        Request req = new Request.Builder()
                .url(fullUrl)
                .build();

        client.newWebSocket(req, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                wsRef.set(webSocket);
                opened = true;

                if (verbose) {
                    System.out.println("OPEN [" + channelType + "] " + fullUrl);
                }

                try {
                    String initMsg = buildInitMessage();
                    if (initMsg == null) {
                        webSocket.close(1008, "invalid config");
                        opened = false;
                        onError.accept(new IllegalStateException("Config inválida para channel=" + channelType));
                        return;
                    }

                    webSocket.send(initMsg);
                   // if (verbose) System.out.println(">> " + initMsg);

                    flushPending();

                    startPingLoop(); // manda "PING" texto a cada 10s (igual seu Python)

                } catch (Exception e) {
                    opened = false;
                    onError.accept(e);
                    webSocket.close(1011, "init failed");
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                //if (verbose) System.out.println(text);

                try {
                    // ignorar keep-alives simples
                    if ("PONG".equalsIgnoreCase(text)) {
                        return;
                    }
                    if ("PING".equalsIgnoreCase(text) || "NO NEW ASSETS".equalsIgnoreCase(text)) {
                        return;
                    }

                    JsonNode root = om.readTree(text);

                    JsonNode et = root.get("event_type");
                    if (et == null || et.isNull()) {
                        // fallback: manda cru
                        onMessage.accept(text);
                        return;
                    }

                    String eventType = et.asText("");

                    switch (eventType) {
                        case "price_change" -> {
                            PolyPriceChangeEvent dto = om.treeToValue(root, PolyPriceChangeEvent.class);
                            onPriceChange(dto);
                        }
                        // tick_size_change, last_trade_price, best_bid_ask, new_market:
                        // sem listener ativo — não parsear para economizar CPU
                        case "tick_size_change", "last_trade_price", "best_bid_ask", "new_market" -> {}
                        case "market_resolved" -> {
                            PolyMarketResolvedEvent dto = om.treeToValue(root, PolyMarketResolvedEvent.class);
                            onMarketResolved(dto);
                        }
                        case "book" -> {
                            PolyBookEvent dto = om.treeToValue(root, PolyBookEvent.class);
                            onBook(dto);
                        }
                        default -> {
                            // desconhecido: manda cru
                            onUnknownEvent(eventType, text);
                        }
                    }

                } catch (Exception e) {
                    // se não parsear, manda cru mesmo e reporta erro
                    onError.accept(e);
                    onMessage.accept(text);
                }
            }

            private void onBook(PolyBookEvent dto) {
                eventPublisher.publishEvent(dto);
            }

            private String extractSide(String slug) {
                String s = slug.toUpperCase();
                if (s.endsWith("-UP")) return "UP";
                if (s.endsWith("-DOWN")) return "DOWN";
                return null;
            }

            private long extractMarketUnix(String slug) {
                // exemplo:
                // btc-updown-15m-1769889600-UP

                int lastDash = slug.lastIndexOf('-');
                if (lastDash < 0) return -1;

                // remove o -UP / -DOWN
                String withoutSide = slug.substring(0, lastDash);

                int dash = withoutSide.lastIndexOf('-');
                if (dash < 0) return -1;

                String tsStr = withoutSide.substring(dash + 1);

                try {
                    return Long.parseLong(tsStr);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }

            private void onUnknownEvent(String eventType, String text) {
                log.info("Unknown event type: " + eventType);
            }

            private void onMarketResolved(PolyMarketResolvedEvent dto) {
                eventPublisher.publishEvent(dto);
            }

            private void onPriceChange(PolyPriceChangeEvent dto) {
                eventPublisher.publishEvent(dto);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                if (verbose) System.out.println("CLOSING [" + channelType + "] code=" + code + " reason=" + reason);
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                opened = false;
                stopPingLoop();
                wsRef.set(null);

                if (verbose) System.out.println("CLOSED [" + channelType + "] code=" + code + " reason=" + reason);
                onClose.accept("code=" + code + " reason=" + reason);

                PolyMarketReconnectMarketEvent event = new PolyMarketReconnectMarketEvent();

                eventPublisher.publishEvent(event);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (verbose) {
                    System.err.println("ERROR [" + channelType + "]: " + t.getMessage());
                    if (response != null) {
                        System.err.println("HTTP: " + response.code() + " " + response.message());
                        try {
                            ResponseBody body = response.body();
                            if (body != null) System.err.println("Body: " + body.string());
                        } catch (IOException ignored) {}
                    }
                }

                opened = false;
                stopPingLoop();
                wsRef.set(null);

                onError.accept(t);

                PolyMarketReconnectMarketEvent event = new PolyMarketReconnectMarketEvent();

                eventPublisher.publishEvent(event);
            }
        });
    }


    /**
     * Fecha conexão.
     */
    public synchronized void close() {
        stopPingLoop();
        opened = false;

        WebSocket ws = wsRef.getAndSet(null);
        if (ws != null) {
            ws.close(1000, "bye");
        }
    }

    // -------------------- comandos iguais ao Python --------------------
    public void subscribeToTokensIds(String slug, String outcome, String tokenId) {
        if (!MARKET_CHANNEL.equals(channelType)) return;
        sendOrQueueJson(Map.of(
                "assets_ids", tokenId != null ? List.of(tokenId) : List.of(),
                "operation", "subscribe",
                "custom_feature_enabled", true
        ));

        subscribedMap.put(tokenId, slug + "-" + outcome);
    }

    public void subscribeToTokensIds(List<String> assetsIds) {
        if (!MARKET_CHANNEL.equals(channelType)) return;
        sendOrQueueJson(Map.of(
                "assets_ids", assetsIds != null ? assetsIds : List.of(),
                "operation", "subscribe"
        ));
    }

    public void unsubscribeToTokensIds(List<String> assetsIds) {
        if (!MARKET_CHANNEL.equals(channelType)) return;
        sendOrQueueJson(Map.of(
                "assets_ids", assetsIds != null ? assetsIds : List.of(),
                "operation", "unsubscribe"
        ));
    }

    /**
     * Remove entradas do subscribedMap que não estão mais nos tokenIds válidos.
     */
    public void cleanupSubscriptions(java.util.Set<String> validTokenIds) {
        int before = subscribedMap.size();
        subscribedMap.keySet().removeIf(tokenId -> !validTokenIds.contains(tokenId));
        int removed = before - subscribedMap.size();
        if (removed > 0) {
            log.info("Cleanup: removed {} stale subscriptions from subscribedMap", removed);
        }
    }

    // -------------------- helpers --------------------

    private String buildInitMessage() throws Exception {
        if (MARKET_CHANNEL.equals(channelType)) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("assets_ids", data);
            payload.put("type", MARKET_CHANNEL);
            return om.writeValueAsString(payload);
        }

        if (USER_CHANNEL.equals(channelType) && auth != null && !auth.isEmpty()) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("markets", data); // igual seu Python
            payload.put("type", USER_CHANNEL);
            payload.put("auth", auth);
            return om.writeValueAsString(payload);
        }

        return null;
    }

    private void sendOrQueueJson(Map<String, ?> payload) {
        try {
            sendOrQueue(om.writeValueAsString(payload));
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    private void sendOrQueue(String msg) {
        WebSocket ws = wsRef.get();
        if (opened && ws != null) {
            ws.send(msg);
           // if (verbose) System.out.println(">> " + msg);
        } else {
            pendingSends.add(msg);
            //if (verbose) System.out.println("(queued) >> " + msg);
        }
    }

    private void flushPending() {
        WebSocket ws = wsRef.get();
        if (ws == null) return;

        String msg;
        while ((msg = pendingSends.poll()) != null) {
            ws.send(msg);
            //if (verbose) System.out.println(">> " + msg);
        }
    }

    private synchronized void startPingLoop() {
        stopPingLoop();
        pingTask = scheduler.scheduleAtFixedRate(() -> {
            WebSocket ws = wsRef.get();
            if (ws != null) {
                pingSentAt.set(System.currentTimeMillis());
                ws.send("PING");
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private synchronized void stopPingLoop() {
        if (pingTask != null) {
            pingTask.cancel(true);
            pingTask = null;
        }
    }

    @PreDestroy
    public void destroy() {
        close();
        scheduler.shutdownNow();
    }
}
