# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Documentacao do Projeto

A documentacao do projeto Yacamin fica em `D:\Projetos\Yacamin\documentos`. Sempre que o usuario falar de documentacao, buscar e/ou atualizar arquivos nessa pasta.

## Project Overview

Yacamin-Rafael is the **AI/ML module** of the Yacamin ecosystem. Built on Spring Boot 4.0.3 (Java 21), Rafael uses **XGBoost** machine learning models to analyze crypto trading blocks and generate predictions. It combines Polymarket prediction markets with Binance spot market data, processing real-time data through ML-powered analysis for UP/DOWN markets across **multiple symbols and block durations**. Rafael currently inherits the simulation infrastructure from Miguel and will evolve to incorporate XGBoost-based block analysis and prediction capabilities. **Rafael does not execute real trades** — it analyzes and predicts.

### Multi-Market Support

Rafael supports multiple market groups simultaneously, each defined by:
- **Slug prefix** (e.g., `btc-updown-5m-`, `eth-updown-5m-`, `btc-updown-15m-`)
- **Block duration** (`FIVE_MIN`, `FIFTEEN_MIN`, `ONE_HOUR`) via `BlockDuration` enum
- **Binance stream** (e.g., `btcusdt@bookTicker`, `ethusdt@bookTicker`)

Configuration is stored in MongoDB collections `binance_stream_config` and `market_group`, manageable via REST API.

## Build & Run Commands

```bash
./gradlew build          # Build
./gradlew bootRun        # Run (port 8080)
./gradlew clean build    # Clean build
```

Para rodar com MongoDB local (sem afetar o Amitiel):
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

No test framework is configured.

## Architecture

**Hexagonal Architecture (Ports & Adapters)** with event-driven async processing.

### Layers

- **`domain/`** — Pure business entities: `Market`, `PolyAsset`, `BlockState`, `PricePoint`, `EntryRecord`, `SimEvent`, `LocalOrderBook`, `Outcome`, `SideClose`, `EntryStatus`, `BlockDuration`, `BinanceStreamConfig`, `MarketGroup`
- **`application/service/algoritms/`** — Algorithm framework: `BookTickerCalculation` interface, `AlgoCalc` enum, `AlgorithmRegistry`, and 2 algorithm implementations (alpha, gama)
- **`application/service/algoritms/simulation/`** — Simulation pipeline: market memory, open/close/resolve services, event persistence — all parameterized by `AlgoCalc` and `marketGroup`
- **`application/service/trading/`** — Shared services: `TradingConfigService`, `LatencyService`/`LatencyMonitorService`, `BinanceStreamConfigService` (stream management), `MarketGroupService` (market group management), `TickDispatchService` (Binance tick routing), `PolyDispatchService` (Polymarket event routing)
- **`application/service/orderbook/`** — `OrderBookMemoryService`: local order book for VWAP calculations
- **`application/service/usecase/`** — `ConnectBinanceSpotWebsocketUseCase`
- **`application/service/auth/`** — `AuthService`/`AuthFilter`: API authentication
- **`application/configuration/`** — Spring beans: Jackson, OkHttp, RestTemplate, async thread pools
- **`adapter/out/rest/`** — Outbound REST: Gamma API markets client (`PolymarketGammaMarketsClient`, `PolymarketRestClient`)
- **`adapter/out/websocket/`** — Outbound WebSockets: Polymarket CLOB (market channel only) + Binance Spot
- **`adapter/out/persistence/`** — MongoDB repositories: `SimEventRepository`, `BinanceStreamConfigRepository`, `MarketGroupRepository`, `TradingConfigRepository`
- **`adapter/in/event/`** — Inbound event listeners (Spring `@EventListener` + `@Async`)
- **`adapter/in/controller/`** — REST API: `DashboardController` (market data, config, streams, groups), `AuthController`

### Multi-Algorithm Framework

The system supports 2 probability calculation algorithms, defined in `AlgoCalc` enum (order: ALPHA, GAMA):

- **`BookTickerCalculation`** — Common interface with `calculate(BookTickerUpdateResponse tick, String marketGroup, BlockDuration duration)` and `getAlgo()`
- **`TickResult`** — Shared record: `(marketGroup, blockUnix, pSuccess, delta, distance, sigma)`
- **`AlgorithmRegistry`** — Spring service that collects all `BookTickerCalculation` beans into `Map<AlgoCalc, BookTickerCalculation>`

**Algorithm implementations** (each in its own sub-package under `algoritms/`):

| Algorithm | Package | Description |
|-----------|---------|-------------|
| **Alpha** | `algoritms/alpha/` | Simple volatility EMA (alpha=0.1) + Gaussian diffusion. Single 60s window. |
| **Gama** | `algoritms/gama/` | Aggressive momentum-first. Higher drift/accel weights (1.35/1.60). Stronger deceleration penalty (1.90). |

Each algorithm maintains its own `BlockState` map with **composite key `"marketGroup|blockUnix"`** for per-group isolation. All share the same base formula: `pSuccess = 1 - exp(-2 * distance² / (sigma² * timeRemaining))` with algorithm-specific distance/sigma calculations.

### Data Flows

**Market Discovery (hourly, per active market group):**
`PolymarketMarketClobWsAdapter.run()` → iterates `MarketGroupService.getActiveGroups()` → for each group: `discoverGroup(slugPrefix, duration)` → Gamma API (`/markets/slug/{slugPrefix}{unix}`) → extract token IDs → creates sim markets (one per algorithm) in `SimulationMarketMemoryService` → subscribe via WebSocket. Markets to load: `ceil(4200 / durationSeconds)` (~70 min ahead).

**Tick Processing (real-time, multi-market dispatch):**
Binance bookTicker → `SpotMarketDataSocket` (extracts symbol from `"s"` field) → `BookTickerUpdateSocketEvent` → `BinanceListenerAdapter` → `TickDispatchService.dispatch(tick)`:
1. Identifies stream from symbol (e.g., `btcusdt@bookTicker`)
2. Looks up market groups for that stream via `MarketGroupService.getGroupsForStream()`
3. For each group: submits to that group's single-thread executor (`mktgrp-{prefix}`)
4. Inside executor: iterates all algorithms → `calculate(tick, slugPrefix, duration)` → `SimulationOnBookTickerService.updateMarket()`

**Simulation Pessimism (realistic execution modeling):**
- **Size**: Budget of 1.1 USDC relative to buy price: `size = 1.1 / ask`. Example: ask=0.50 → size=2.2 shares.
- **Configurable execution delay**: When opening/closing, position enters OPENING/CLOSING state. The actual entry/exit price is taken from the BID/ASK after the delay (default 5s), simulating real order execution latency.
- **VWAP fill**: Uses local order book VWAP when available, otherwise raw bid/ask with configurable spread penalty.
- **PnL with size**: All PnL calculations use `(exitPrice - entryPrice) * size`.

**Polymarket Price Events (real-time):**
`PolyMarketWsMarketListener.onPriceChange()` → `PolyDispatchService.dispatchPriceChange()` → identifies market group via asset ID → submits to group executor → for each algorithm: `SimulationOnPriceChangeService` opens/closes positions based on pSuccess/delta

**Market Resolution:**
`PolyMarketWsMarketListener.onMarketResolve()` → `PolyDispatchService.dispatchResolve()` → identifies market group → submits to group executor → positions settled at 1.0 (won) or 0.0 (lost) → RESOLVE, FEES and PNL events persisted to MongoDB

**Time Remaining (independent, every 1 second):**
`@Scheduled(fixedRate = 1_000)` in `SimulationMarketMemoryService` updates `timeRemaining` using each market's `BlockDuration` for correct end time calculation. This runs independently of Binance ticks.

**Reconnection:**
WebSocket closes → reconnect event published → async listener → re-subscribe all streams

### Simulation Pipeline (per-algorithm, per-market-group isolation)

All simulation services live in `application/service/algoritms/simulation/` and are parameterized with `AlgoCalc`. State dimension: `(AlgoCalc, marketGroup, blockUnix)`.

- **`SimulationMarketMemoryService`** — `Map<AlgoCalc, Map<String, Market>>` with composite key `"marketGroup|unixTime"`. Each algorithm × market group has its own isolated market state, entry history, and position tracking.
- **`SimulationOnBookTickerService`** — Updates sim market metrics (pSuccess, delta, distance, sigma, tickCount) and delta tracking (flip detection, momentum). Uses `result.marketGroup()` for lookup.
- **`SimulationOnPriceChangeService`** — Handles Polymarket price events for a specific algorithm's markets → delegates to open/close services.
- **`SimulationOpenPositionService`** — Opens positions when: `pSuccess >= 0.85`, `timeRemaining` between 10s and 80% of block duration, delta direction matches outcome. Skips startup block per market group (uses `Instant startupTime` + `BlockDuration.currentBlockUnix()`).
- **`SimulationClosePositionService`** — Closes on delta reversal with 1s confirmation delay (SL/TP).
- **`SimulationOnResolveService`** — Settles positions on market resolution (UP/DOWN win). Emits RESOLVE, FEES and PNL events.
- **`SimEventPersistenceService`** — Async MongoDB writes via `record(algo, marketGroup, slug, type, payload)`. Uses `mongoWriteExecutor`. Fire-and-forget — failures log warning and don't block the pipeline.

### Database (MongoDB)

**Collections:**

| Collection | Description |
|---|---|
| `sim_events` | All simulation events (trades, resolves, PnL) |
| `binance_stream_config` | Binance streams configuration |
| `market_group` | Market group configuration |
| `trading_config` | Trading parameters |

**`sim_events` schema:**
```json
{
  "_id": "ObjectId",
  "slug": "btc-updown-5m-1711000200",
  "marketGroup": "btc-updown-5m-",
  "timestamp": 1711000100500,
  "type": "BUY_ORDER_PLACED",
  "algorithm": "ALPHA",
  "payload": { ... }
}
```

7 event types: `BUY_ORDER_PLACED`, `BUY_ORDER_RESPONSE`, `SELL_ORDER_PLACED`, `SELL_ORDER_RESPONSE`, `RESOLVE`, `FEES`, `PNL`.

### Dashboard (REST API)

**Market Data:**
- `GET /api/dashboard/markets?algorithm=ALPHA&marketGroup=btc-updown-5m-` — Market data with algorithm and group filter
- `GET /api/dashboard/trading/config` — Current simulation config
- `POST /api/dashboard/trading/config` — Update simulation parameters

**Binance Streams:**
- `GET /api/dashboard/streams` — List all streams with runtime status
- `POST /api/dashboard/streams/add?symbol=X` — Add stream (inactive)
- `POST /api/dashboard/streams/start?id=X` — Subscribe and activate
- `POST /api/dashboard/streams/pause?id=X` — Unsubscribe and deactivate
- `POST /api/dashboard/streams/remove?id=X` — Remove from database

**Market Groups:**
- `GET /api/dashboard/markets/groups` — List all groups with runtime status
- `POST /api/dashboard/markets/groups/add?slugPrefix=X&displayName=Y&blockDuration=Z&binanceStream=W` — Add group
- `POST /api/dashboard/markets/groups/start?id=X` — Activate group + trigger discovery
- `POST /api/dashboard/markets/groups/pause?id=X` — Deactivate group
- `POST /api/dashboard/markets/groups/remove?id=X` — Remove from database
- `GET /api/dashboard/markets/durations` — List available BlockDuration values

### Thread Model

Single-thread executors for ordered event processing:
- `bookTickUpdateListenerExecutor` — Binance book ticker events (entry point)
- `polyOnPriceChangeExecutor` — Polymarket price change events (entry point)
- `polyOnResolveExecutor` — Market resolution events (entry point)
- `reconnectMarketDataSocketExecutor` / `reconnectPolyMarketExecutor` — WebSocket reconnects
- `subMessageMarketDataSocketExecutor` — Binance subscription confirmations

**Dynamic executors (per-symbol and per-group):**
- `tick-{symbol}` (single-thread per symbol) — Created by `BinanceStreamConfigService`. Parallel between symbols, sequential within.
- `mktgrp-{prefix}` (single-thread per market group) — Created by `MarketGroupService`. **Shared** between Binance ticks and Polymarket events for the same group, ensuring sequential processing.

Pool executors:
- `mongoWriteExecutor` (pool 1-2) — Async event persistence
- `marketDiscoveryExecutor` (pool 4-8) — Parallel Gamma API calls for market discovery
- `orderBookUpdateExecutor` (pool 1) — Local order book updates

Thread-safe collections throughout: `ConcurrentHashMap` (markets, assets, blocks, executors), `ConcurrentLinkedDeque` (price history), `EnumMap` (algorithm registries), `CopyOnWriteArrayList` (entry history).

### External Integrations

- **Polymarket WebSocket** (`ws-subscriptions-clob.polymarket.com`) — Real-time market data (market channel only: price changes, order book, resolution)
- **Gamma API** (`gamma-api.polymarket.com/markets`) — Market metadata by slug (hourly discovery per group)
- **Binance Spot WebSocket** (`stream.binance.com:9443/ws`) — bookTicker streams (dynamic, configured via MongoDB)
- **MongoDB** — Event persistence (`sim_events`), configuration (`binance_stream_config`, `market_group`, `trading_config`)

## Key Dependencies

- **OkHttp3** (`4.12.0`) — Polymarket WebSocket + async REST
- **Java-WebSocket** (`1.6.0`) — Binance WebSocket client
- **Jackson** — UTC timezone, ISO-8601 format, custom deserializers for Polymarket responses
- **Lombok** — `@Data`, `@Builder`, `@Slf4j` throughout
- **Spring Data MongoDB** — Repository pattern for event persistence

## Configuration

All secrets and endpoints are in `application.yaml` under `polymarket.*` and `binance.*`. Values with `0x` prefix (addresses, private keys) **must be quoted** in YAML to prevent hex number interpretation.

Profile `local` (`application-local.yaml`) uses `mongodb://localhost:27017/yacamin-gabriel` for local development.