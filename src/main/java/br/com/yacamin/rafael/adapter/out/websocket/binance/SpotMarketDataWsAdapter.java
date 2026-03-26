package br.com.yacamin.rafael.adapter.out.websocket.binance;

import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.request.MarketDataRequest;
import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.request.StreamRequestMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class SpotMarketDataWsAdapter {

    @Value("${binance.paths.spot.ws}")
    private String path;

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom;

    private ScheduledExecutorService reconnectScheduler;
    private ScheduledExecutorService checkScheduler;

    private SpotMarketDataSocket webSocket;
    private long lastMessageSendingTimeMs = System.currentTimeMillis();
    private final Map<Long, MarketDataRequest> cachedSendedMessages = new HashMap<>();
    private final Set<String> signedStreams = new HashSet<>();

    public void connect(boolean blocking) {
        createWebSocket(blocking);

        scheduleReconnect(10, 10, TimeUnit.HOURS);
        scheduleCheck(5, 10, TimeUnit.MINUTES);
    }

    /* Subscriber */
    public void subscribe(List<String> streams) {
        List<String> filtered = streams.stream()
                .filter(s -> !signedStreams.contains(s))
                .toList();

        if (!filtered.isEmpty()) {
            MarketDataRequest request = new MarketDataRequest();
            request.setId(secureRandom.nextLong());
            request.setMethod(StreamRequestMethod.SUBSCRIBE.getValue());
            request.setParams(new ArrayList<>(filtered));
            sendRequest(request);
        }
    }

    public void unsubscribe(List<String> streams) {
        List<String> filtered = streams.stream()
                .filter(signedStreams::contains)
                .toList();

        if (!filtered.isEmpty()) {
            MarketDataRequest request = new MarketDataRequest();
            request.setId(secureRandom.nextLong());
            request.setMethod(StreamRequestMethod.UNSUBSCRIBE.getValue());
            request.setParams(new ArrayList<>(filtered));
            sendRequest(request);
        }
    }

    private void sendRequest(MarketDataRequest request) {
        try {
            waitSecond();
            cachedSendedMessages.put(request.getId(), request);

            String message = objectMapper.writeValueAsString(request);

            webSocket.sendMessage(message);
            lastMessageSendingTimeMs = System.currentTimeMillis();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception ex) {
            log.error("Websocket send failed, reconnecting: {}", ex.getMessage());
            this.reconnect();
            this.sendRequest(request);
        }
    }

    private void waitSecond() {
        long elapsed = System.currentTimeMillis() - lastMessageSendingTimeMs;
        if (elapsed < 1000) {
            try {
                Thread.sleep(1000 - elapsed);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void manageSubMessage(String message) {
        Long id = checkResponseId(message);

        MarketDataRequest request = checkCache(id);

        if (request.getMethod().equals(StreamRequestMethod.SUBSCRIBE.getValue())) {
            signedStreams.addAll(request.getParams());
        } else {
            request.getParams().forEach(signedStreams::remove);
        }

        cachedSendedMessages.remove(id);
    }

    public boolean isConnected() {
        return webSocket != null && webSocket.isOpen();
    }

    private Long checkResponseId(String m) {
        try {
            long id = objectMapper.readTree(m).get("id").asLong();

            if (id == 0L) {
                throw new RuntimeException("Invalid or Null ID");
            }

            return id;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private MarketDataRequest checkCache(Long id) {
        MarketDataRequest request = cachedSendedMessages.get(id);

        if (request == null) {
            throw new RuntimeException("Subscribe request not found in cached messages");
        }

        return request;
    }

    /* Reconnect Scheduler */
    public void reconnect() {
        log.info("Reconnecting: MarketDataClient");
        Set<String> reStreams = new HashSet<>(signedStreams);

        createWebSocket(true);

        signedStreams.clear();
        subscribe(new ArrayList<>(reStreams));
    }

    public void scheduleReconnect(long delay, long period, TimeUnit timeUnit) {
        reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
        reconnectScheduler.scheduleAtFixedRate(this::closeWebSocket, delay, period, timeUnit);
    }

    private void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close();
        }
    }

    private void createWebSocket(boolean blocking) {
        if (webSocket != null) {
            webSocket.close();
        }
        webSocket = new SpotMarketDataSocket(path, eventPublisher, objectMapper, "SPOT");
        webSocket.connect(blocking);
    }

    private void scheduleCheck(long delay, long period, TimeUnit timeUnit) {
        checkScheduler = Executors.newSingleThreadScheduledExecutor();
        checkScheduler.scheduleAtFixedRate(this::checkWebSocket, delay, period, timeUnit);
    }

    private void checkWebSocket() {
        log.info("Checking WebSocket");

        if (webSocket == null || !webSocket.isOpen()) {
            reconnect();
        }
    }

    @PreDestroy
    void shutdown() {
        log.info("Shutting down Binance WS adapter");
        if (webSocket != null) {
            webSocket.close();
        }
        if (reconnectScheduler != null) reconnectScheduler.shutdownNow();
        if (checkScheduler != null) checkScheduler.shutdownNow();
    }
}
