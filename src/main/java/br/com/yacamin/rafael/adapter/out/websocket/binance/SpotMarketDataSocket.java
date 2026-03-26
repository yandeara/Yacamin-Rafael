package br.com.yacamin.rafael.adapter.out.websocket.binance;

import br.com.yacamin.rafael.adapter.in.event.dto.BookTickerUpdateSocketEvent;
import br.com.yacamin.rafael.adapter.in.event.dto.KlineUpdateSocketEvent;
import br.com.yacamin.rafael.adapter.in.event.dto.ReconnectMarketDataSocketEvent;
import br.com.yacamin.rafael.adapter.in.event.dto.SubMessageMarketDataSocketEvent;
import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.BookTickerUpdateResponse;
import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.KlineEventResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SpotMarketDataSocket {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final String type;
    private final String url;

    private final OkHttpClient client;
    private final AtomicReference<WebSocket> wsRef = new AtomicReference<>();
    private volatile boolean opened;

    private final ConcurrentLinkedQueue<String> pendingSends = new ConcurrentLinkedQueue<>();

    public SpotMarketDataSocket(String url,
                                ApplicationEventPublisher eventPublisher,
                                ObjectMapper objectMapper,
                                String type) {
        this.url = url;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.type = type;
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ZERO)
                .pingInterval(Duration.ofSeconds(10))
                .build();
    }

    public void connect(boolean blocking) {
        if (opened || wsRef.get() != null) {
            log.info("Binance WS já conectado/connecting");
            return;
        }

        CountDownLatch latch = blocking ? new CountDownLatch(1) : null;

        Request request = new Request.Builder().url(url + "/ws").build();

        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                log.info("MarketDataSocket - Open");
                wsRef.set(webSocket);
                opened = true;
                startPingLoop();
                flushPending();
                if (latch != null) latch.countDown();
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                try {
                    managerResponse(text);
                } catch (Exception e) {
                    log.error("Erro ao processar mensagem Binance: {}", e.getMessage(), e);
                }
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                log.info("MarketDataSocket - Closing: {}", reason);
                webSocket.close(1000, reason);
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                log.info("MarketDataSocket - Closed: {}", reason);
                opened = false;
                stopPingLoop();
                wsRef.set(null);
                sendReconnectEvent(reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                log.error("MarketDataSocket - Error: {}", t.getMessage());
                opened = false;
                stopPingLoop();
                wsRef.set(null);
                if (latch != null) latch.countDown();
                sendReconnectEvent(t.getMessage());
            }
        });

        if (latch != null) {
            try {
                if (!latch.await(30, TimeUnit.SECONDS)) {
                    log.warn("Binance WS connect timeout (30s)");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void close() {
        opened = false;
        WebSocket ws = wsRef.getAndSet(null);
        if (ws != null) {
            ws.close(1000, "manual close");
        }
    }

    public boolean isOpen() {
        return opened && wsRef.get() != null;
    }

    public void sendMessage(String message) {
        log.info("Send: {}", message);
        sendOrQueue(message);
    }

    private void sendOrQueue(String msg) {
        WebSocket ws = wsRef.get();
        if (opened && ws != null) {
            ws.send(msg);
        } else {
            pendingSends.add(msg);
        }
    }

    private void flushPending() {
        WebSocket ws = wsRef.get();
        if (ws == null) return;
        String msg;
        while ((msg = pendingSends.poll()) != null) {
            ws.send(msg);
        }
    }

    /* ── Message handling ─────────────────────────────────────── */

    private void managerResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            // Respostas de controle: {"result":...,"id":X}
            if (root.has("result")) {
                JsonNode idNode = root.get("id");
                if (idNode != null && idNode.isNumber() && idNode.asLong() != 0) {
                    managerSubMessage(response);
                }
                return;
            }

            // Stream wrapper format: {"stream":"btcusdt@bookTicker","data":{...}}
            JsonNode dataNode = root.get("data");
            if (dataNode != null) {
                root = dataNode;
            }

            JsonNode event = root.get("e");
            JsonNode eventByPass = root.get("A");

            if (event == null && eventByPass == null) {
                managerSubMessage(response);
                return;
            }

            // Book Ticker — fast path
            if (event == null) {
                managerBookTicker(root);
                return;
            }

            String eventType = event.asText();
            if ("kline".equals(eventType)) {
                managerKlineUpdate(root);
                return;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void managerBookTicker(JsonNode root) {
        BookTickerUpdateResponse bookTickerUpdateResponse = new BookTickerUpdateResponse();
        bookTickerUpdateResponse.setBestBid(Double.parseDouble(root.get("b").asText()));
        bookTickerUpdateResponse.setBestAsk(Double.parseDouble(root.get("a").asText()));
        JsonNode symbolNode = root.get("s");
        if (symbolNode != null) {
            bookTickerUpdateResponse.setSymbol(symbolNode.asText().toLowerCase());
        }

        BookTickerUpdateSocketEvent event = new BookTickerUpdateSocketEvent();
        event.setResponse(bookTickerUpdateResponse);

        eventPublisher.publishEvent(event);
    }

    private void managerKlineUpdate(JsonNode root) {
        try {
            KlineEventResponse klineResponse = objectMapper.treeToValue(root, KlineEventResponse.class);
            KlineUpdateSocketEvent event = KlineUpdateSocketEvent.builder()
                    .dateTime(java.time.LocalDateTime.now())
                    .response(klineResponse)
                    .build();
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Erro ao processar kline event: {}", e.getMessage());
        }
    }

    private void managerSubMessage(String s) {
        SubMessageMarketDataSocketEvent event = new SubMessageMarketDataSocketEvent();
        event.setMessage(s);
        event.setType(type);
        eventPublisher.publishEvent(event);
    }

    /* managerMarkPriceUpdate, managerTradeUpdate, managerKlineUpdate removidos — sem listener ativo */

    /* ── Ping ──────────────────────────────────────────────────── */
    // OkHttp pingInterval(10s) gerencia keep-alive automaticamente via frame-level ping/pong.
    // Latência é medida pelo intervalo entre ticks recebidos (zero overhead de rede).

    private void startPingLoop() { }
    private void stopPingLoop() { }

    /* ── Events ───────────────────────────────────────────────── */

    private void sendReconnectEvent(String msg) {
        ReconnectMarketDataSocketEvent event = new ReconnectMarketDataSocketEvent();
        event.setMessage(msg);
        event.setType(type);
        eventPublisher.publishEvent(event);
    }
}
