# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Documentacao do Projeto

A documentacao do projeto Yacamin fica em `D:\Projetos\Yacamin\documentos`. Sempre que o usuario falar de documentacao, buscar e/ou atualizar arquivos nessa pasta.

Documentos relevantes do Rafael:
- `rafael-predict-logic.md` — Logica completa de predicao (M2M, B2B, H4)
- `rafael-production-tables.md` — SQLs de producao (PostgreSQL)
- `yacamin-eventos.md` — Catalogo de eventos (secao Modulo Rafael)

## Project Overview

Yacamin-Rafael is the **AI/ML prediction module** of the Yacamin ecosystem. Built on Spring Boot 4.0.3 (Java 21), Rafael uses **XGBoost** machine learning models to predict crypto price direction (UP/DOWN) at multiple time horizons. It processes real-time Binance kline data, computes technical indicators, and runs three independent prediction logics per 5-minute block. **Rafael does not execute trades** — it predicts and logs results for analysis.

## Build & Run Commands

```bash
./gradlew build          # Build
./gradlew bootJar        # Build JAR for deployment
./gradlew bootRun        # Run locally (port 8080)
./gradlew clean build    # Clean build
```

Profile local:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Docker:
```bash
docker build -t yacamin-rafael .
docker run -p 8080:8080 yacamin-rafael
```

No test framework is configured.

## Architecture

**Hexagonal Architecture (Ports & Adapters)** with event-driven async processing.

### Core Layers

- **`domain/`** — Entities: `Market`, `SymbolCandle`, `RafaelBar`, `InferencePrediction`, `ModelDescriptor`, `BlockDuration`, `CandleIntervals`, `Frame`
- **`domain/scylla/entity/`** — JPA entities for PostgreSQL: `Candle1Mn`, `Candle5Mn`, indicator entities (1mn/5mn/15mn/30mn)
- **`domain/scylla/entity/indicator/`** — Interfaces: `MicrostructureIndicatorEntity`, `MomentumIndicatorEntity`, `TrendIndicatorEntity`, `VolatilityIndicatorEntity`, `VolumeIndicatorEntity`, `TimeIndicatorEntity`
- **`application/service/model/`** — ML inference pipeline: `ModelRegistryService`, `FeatureExtractorService`, `HeadHunterFeaturesMap`, `FeatureMaskService`, `MinuteByMinuteInferenceService`, `BlockByBlockInferenceService`, `HorizonInferenceService`, `BlockInferenceMemoryService`, `PredictionEventService`
- **`application/service/warmup/`** — Indicator calculation: 6 group orchestrators (MIC, MOM, TRD, VLT, VOL, TIM) with sub-services per indicator type
- **`application/service/candle/`** — Warmup pipeline: `WarmupService`, `SyncCheckService`, `BarSeriesCacheService`, `DownloadCandleService`
- **`application/service/analyse/`** — `AnalyseOrchestratorService`: parallel indicator calculation (6 groups)
- **`adapter/out/persistence/`** — JPA repositories (PostgreSQL) + MongoDB repositories
- **`adapter/out/websocket/`** — Binance Spot + Polymarket CLOB WebSockets
- **`adapter/in/event/`** — Event listeners: `KlineListenerAdapter`, `BinanceListenerAdapter`, `PolyMarketWsMarketListener`
- **`adapter/in/controller/`** — REST API: `DashboardController`, `AuthController`

### XGBoost Prediction Pipeline

Three independent prediction logics run per block:

| Logic | Model | Trigger | Predicts | Frequency |
|---|---|---|---|---|
| **M2M** (Minute-by-Minute) | `xgb_BTCUSDT_h1_1m` | Every 1m candle close | Next minute direction | 5x per block |
| **B2B** (Block-by-Block) | `xgb_BTCUSDT_h1_5m` | Every 5m candle close | Next block direction | 1x per block |
| **H4** (Horizon) | `xgb_BTCUSDT_h4_1m` | First 1m candle of block | End-of-block direction | 1x per block |

Model naming: `{type}_{symbol}_{horizon}_{interval}_{MMYYYY-MMYYYY}.ubj`
Models loaded from `models/` directory on startup. Invalid names crash startup.

### Feature Extraction

- **`HeadHunterFeaturesMap`** — Registry of 309 FeatureDef (name → lambda extractor), exact same order as Mikhael training
- **`FeatureMaskService.getProdMask()`** — 309 features used in production inference
- **`AssemblerDto`** — DTO with candle + 7 indicator entities → `buildFeatures(dto, mask)` → `float[]`
- Feature groups: MIC (97), MOM (90), TRD (26), VLT (46), VOL (37), TIM (13)

### Indicator Calculation (Warmup)

6 indicator groups calculated in parallel by `AnalyseOrchestratorService`:

| Group | Service | Features (mask) | Sub-services |
|---|---|---|---|
| MIC | MicrostructureWarmupService | 97 | Amihud, Body, Hasbrouck, Kyle, PositionBalance, Range, Return1C, Roll, Wick |
| MOM | MomentumWarmupService | 90 | CloseReturn, RSI, CMO, WPR, Stoch, Trix, TSI, PPO, ClosePrice, CCI, ROC, MomentumStability |
| TRD | TrendWarmupService | 26 | EMA, ADX |
| VLT | VolatilityWarmupService | 46 | ATR, Bollinger, EWMA, Keltner, RangeVol, RealizedVol, Std |
| VOL | VolumeWarmupService | 37 | ActivityPressure, BAP, Delta, Microburst, OFI, RawMicrostructure, Slope, SVR, VWAP |
| TIM | TimeWarmupService | 13 | (all in one service) |

### Data Flow

```
Startup:
  ModelRegistry → loads .ubj models into memory
  WarmupService → downloads 2000 candles from Binance REST
                → processes through BarSeries + AnalyseOrchestrator
                → saves indicators to PostgreSQL
                → sets live=true

Real-time:
  Binance kline WS → KlineListenerAdapter
    → persists candle to PostgreSQL
    → BarSeriesCacheService.update()
      → AnalyseOrchestrator (saves indicators to PostgreSQL)
      → MinuteByMinuteInferenceService (M2M)
      → BlockByBlockInferenceService (B2B)
      → HorizonInferenceService (H4)
    → PredictionEventService (saves to MongoDB events + sim_events)

  Polymarket WS → market resolution
    → SimulationOnResolveService
      → resolves B2B and H4 predictions (hitResolve)
      → PredictionEventService (PREDICTION_BLOCK_RESOLVED, PREDICTION_HORIZON_RESOLVED)
```

### Prediction Events (MongoDB)

Written to BOTH `events` (Gabriel) and `sim_events` (Miguel) collections:

| Event | When |
|---|---|
| `PREDICTION_M2M` | 1m candle closes, prediction created |
| `PREDICTION_M2M_RESOLVED` | Next 1m candle closes, hit calculated |
| `PREDICTION_BLOCK` | 5m candle closes, prediction created |
| `PREDICTION_BLOCK_RESOLVED` | Polymarket OnResolve |
| `PREDICTION_HORIZON` | First 1m candle of block, prediction created |
| `PREDICTION_HORIZON_RESOLVED` | Polymarket OnResolve |

### Database

**PostgreSQL** — Candles + indicator features (JPA/Hibernate):
- `candle_1_mn`, `candle_5_mn` — OHLCV data
- `{mic,mom,trd,vlt,vol,tim}_indicator_{1,5}_mn` — Technical indicator features
- `ddl-auto: update` — Hibernate creates/updates tables automatically
- Composite primary key: `(symbol, open_time)` via `IndicatorKey` @IdClass

**MongoDB** — Events + configuration:
- `events` — Prediction events (Gabriel timeline, no algorithm field)
- `sim_events` — Prediction events (Miguel timeline, algorithm=ALPHA)
- `binance_stream_config`, `market_group` — Runtime configuration

### Dashboard (REST API)

- `GET /api/dashboard/markets` — Markets with inferences (M2M dots + B2B + H4 banners)
- `GET /api/dashboard/models` — Loaded XGBoost models
- `GET /api/dashboard/streams` — Binance stream management
- `GET /api/dashboard/markets/groups` — Market group management

### Startup Sequence

1. `ModelRegistryService.loadModels()` — Validate + load .ubj Boosters
2. `WarmupService.warmup("BTCUSDT", I1_MN)` — 2000 candles, indicators for last 1000
3. `WarmupService.warmup("BTCUSDT", I5_MN)` — Same for 5m
4. `SyncCheckService` — Integrity check (throws if desync, no deletes)
5. Connect Polymarket + Binance WebSockets
6. Subscribe `btcusdt@kline_1m` + `btcusdt@kline_5m`
7. `setLive(true)` — Inference starts (only for real-time candles, not warmup)

## Key Dependencies

- **XGBoost4J** (`3.1.1`) — ML model loading and inference (requires `libgomp1` in Docker)
- **OkHttp3** (`4.12.0`) — Polymarket WebSocket + Binance REST (sync calls)
- **ta4j** (`0.18`) — Technical analysis library (BarSeries, indicators)
- **Spring Data JPA** — PostgreSQL persistence
- **Spring Data MongoDB** — Event persistence
- **Jackson** — JSON parsing
- **Lombok** — `@Data`, `@Builder`, `@Slf4j`

## Configuration

`application.yaml`:
- `spring.datasource.*` — PostgreSQL connection
- `spring.jpa.hibernate.ddl-auto: update` — Auto-create tables
- `spring.jpa.properties.hibernate.globally_quoted_identifiers: true` — Escapes reserved words (open, close)
- `spring.mongodb.uri` — MongoDB connection
- `polymarket.*` — Polymarket WebSocket + REST
- `binance.*` — Binance WebSocket + REST

## Docker

```dockerfile
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y libgomp1  # Required for XGBoost4J
COPY models/ models/                                # XGBoost .ubj models
```

Important: `libgomp1` is required for XGBoost native library. Models must be in `/app/models/`.
