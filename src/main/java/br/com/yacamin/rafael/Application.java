package br.com.yacamin.rafael;

import br.com.yacamin.rafael.adapter.out.websocket.binance.SpotMarketDataWsAdapter;
import br.com.yacamin.rafael.adapter.out.websocket.polymarket.PolymarketMarketClobWsAdapter;
import br.com.yacamin.rafael.application.service.candle.WarmupService;
import br.com.yacamin.rafael.application.service.model.BlockByBlockInferenceService;
import br.com.yacamin.rafael.application.service.model.HorizonInferenceService;
import br.com.yacamin.rafael.application.service.model.MinuteByMinuteInferenceService;
import br.com.yacamin.rafael.application.service.model.ModelRegistryService;
import br.com.yacamin.rafael.application.service.trading.BinanceStreamConfigService;
import br.com.yacamin.rafael.application.service.trading.MarketGroupService;
import br.com.yacamin.rafael.application.service.usecase.ConnectBinanceSpotWebsocketUseCase;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Slf4j
@EnableAsync
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class Application implements CommandLineRunner  {

    private final ConnectBinanceSpotWebsocketUseCase connectBinanceSpotWebsocketUseCase;
    private final PolymarketMarketClobWsAdapter polymarketMarketClobWsAdapter;
    private final BinanceStreamConfigService binanceStreamConfigService;
    private final MarketGroupService marketGroupService;
    private final WarmupService warmupService;
    private final SpotMarketDataWsAdapter spotMarketDataWsAdapter;
    private final ModelRegistryService modelRegistryService;
    private final MinuteByMinuteInferenceService minuteByMinuteInferenceService;
    private final BlockByBlockInferenceService blockByBlockInferenceService;
    private final HorizonInferenceService horizonInferenceService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
	}

    @Override
    public void run(String... args) {
        // Model registry: valida e carrega modelos XGBoost
        modelRegistryService.loadModels();

        // Warmup: carrega candles historicos do Scylla para o cache ta4j
        warmupService.warmup("BTCUSDT", CandleIntervals.I1_MN);
        warmupService.warmup("BTCUSDT", CandleIntervals.I5_MN);

        marketGroupService.initOnStartup();
        connectPolymarket();
        connectBinance();

        // Subscreve ao stream de kline 1m e 5m para candles real-time
        spotMarketDataWsAdapter.subscribe(List.of("btcusdt@kline_1m", "btcusdt@kline_5m"));

        // Agora sim: modo live ativo — inferência roda apenas para candles real-time
        minuteByMinuteInferenceService.setLive(true);
        blockByBlockInferenceService.setLive(true);
        horizonInferenceService.setLive(true);
    }

    private void connectBinance() {
        connectBinanceSpotWebsocketUseCase.connect();
        binanceStreamConfigService.initOnStartup();
    }

    private void connectPolymarket() {
        polymarketMarketClobWsAdapter.start();
    }
}
