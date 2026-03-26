package br.com.yacamin.rafael.adapter.out.rest.binance;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@Component
public class BinanceSpotRestClient {

    private final OkHttpClient okHttpClient;

    @Value("${binance.paths.spot.rest}")
    private String basePath;

    @Value("${binance.endpoints.spot.klines}")
    private String klinesEndpoint;

    private static final long MIN_DELAY_MS = 180;

    public BinanceSpotRestClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public CompletableFuture<String> klines(KlineRequest request) {
        throttle();
        return executeWithRetry(() -> doGet(basePath + klinesEndpoint, request.getParams()));
    }

    private CompletableFuture<String> doGet(String baseUrl, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(baseUrl);
        if (!params.isEmpty()) {
            sb.append("?");
            params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            sb.deleteCharAt(sb.length() - 1);
        }

        Request req = new Request.Builder().url(sb.toString()).get().build();

        CompletableFuture<String> future = new CompletableFuture<>();
        okHttpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(new RuntimeException("Binance REST call failed: " + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    future.complete(body);
                } else {
                    future.completeExceptionally(new RuntimeException(
                            "Binance REST error: code=" + response.code() + " body=" + body));
                }
            }
        });
        return future;
    }

    private void throttle() {
        try {
            Thread.sleep(MIN_DELAY_MS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private CompletableFuture<String> executeWithRetry(Supplier<CompletableFuture<String>> supplier) {
        CompletableFuture<String> result = new CompletableFuture<>();
        retryInternal(supplier, result, 0, 500);
        return result;
    }

    private void retryInternal(Supplier<CompletableFuture<String>> supplier,
                               CompletableFuture<String> result, int attempts, long waitMs) {
        supplier.get().whenComplete((value, error) -> {
            if (error == null) {
                result.complete(value);
                return;
            }
            int next = attempts + 1;
            if (next > 5) {
                result.completeExceptionally(error);
                return;
            }
            log.warn("Binance REST retry {} after error: {} (waiting {}ms)", next, error.getMessage(), waitMs);
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            retryInternal(supplier, result, next, waitMs * 2);
        });
    }
}
