package br.com.yacamin.rafael.adapter.out.rest.polymarket;

import br.com.yacamin.rafael.adapter.out.rest.polymarket.dto.ConnectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Component
public class PolymarketRestClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    public static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");

    public CompletableFuture<String> get(String url, Map<String, Object> params) {
        Request request = new Request.Builder()
                .url(buildUrlWithParams(url, params))
                .headers(buildHeaders())
                .get()
                .build();

        return call(request);
    }

    public CompletableFuture<String> getWithToken(String url, String token, Map<String, Object> params) {
        Request request = new Request.Builder()
                .url(buildUrlWithParams(url, params))
                .headers(buildHeaders())
                .get()
                .build();

        return call(request);
    }

    private String buildUrlWithParams(String baseUrl, Map<String, Object> params) {
        if(params == null) {
            params = new HashMap<>();
        }

        StringBuilder sb = new StringBuilder().append(baseUrl);

        if(!params.isEmpty()) {
            sb.append("?");
        }

        return sb.append(concatenate(params)).toString();
    }

    private Headers buildHeaders() {
        return new Headers.Builder()
                .build();
    }

    private Headers buildHeadersWithToken(String token) {
        return new Headers.Builder()
                .build();
    }

    private String concatenate(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        Set<String> keySet = params.keySet();

        for (String key : keySet) {
            sb.append(key)
                    .append("=")
                    .append(params.get(key))
                    .append("&");
        }

        if(!sb.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public CompletableFuture<String> call(Request request) {
        CompletableFuture<String> future = new CompletableFuture<>();

        try {
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(new ConnectionException("Call Failure: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = "";

                    if(response.body() != null) {
                        String body = response.body().string();
                        responseBody = body;
                    }

                    if (response.isSuccessful()) {
                        future.complete(responseBody);
                    } else {
                        future.completeExceptionally(new ConnectionException("Response is not successful: Code " + response.code() + " - " + responseBody, response.code(), responseBody));
                    }
                }
            });

            return future;
        } catch (Exception e) {
            throw new ConnectionException("Failed to make async call: " + e.getMessage());
        }
    }

}
