package br.com.yacamin.rafael.adapter.out.rest.polymarket;

import br.com.yacamin.rafael.adapter.out.rest.polymarket.dto.PolymarketGetSlugResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolymarketGammaMarketsClient {

    @Value("${polymarket.paths.gamma.markets.rest}")
    private String path;

    @Value("${polymarket.endpoints.gamma.markets.slug}")
    private String slugEndpoint;

    private final PolymarketRestClient restClient;
    private final ObjectMapper objectMapper;

    public PolymarketGetSlugResponse getBySlug(String slug) {
        log.info("Call Markets By Slug");

        var strRes = restClient.get(path + slugEndpoint + "/" + slug, null);

        try {
            return objectMapper.readValue(strRes.get(), PolymarketGetSlugResponse.class);
        } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
