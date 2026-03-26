package br.com.yacamin.rafael.application.service.usecase;

import br.com.yacamin.rafael.adapter.out.websocket.binance.SpotMarketDataWsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConnectBinanceSpotWebsocketUseCase {

    private final SpotMarketDataWsAdapter spotMarketDataWsAdapter;

    public void connect() {
        spotMarketDataWsAdapter.connect(true);
    }

    public void subscribeStreams(List<String> streams) {
        if (spotMarketDataWsAdapter.isConnected()) {
            spotMarketDataWsAdapter.subscribe(streams);
        }
    }

    /**
     * @deprecated Use {@link #subscribeStreams(List)} instead.
     */
    @Deprecated
    public void subscribeSpotBookTick(String symbol) {
        subscribeStreams(List.of(symbol.toLowerCase() + "@bookTicker"));
    }
}
