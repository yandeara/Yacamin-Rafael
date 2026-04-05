package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.*;
import br.com.yacamin.rafael.adapter.out.persistence.mikhael.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Monta vetores de features (float[]) a partir dos documentos MongoDB,
 * usando a lista de feature names do registro do modelo.
 *
 * Cada feature name (ex: "mic_amihud_slp_w4") eh resolvido via reflexao:
 * 1. Prefixo determina qual documento (mic → MicrostructureIndicatorDocument)
 * 2. snake_case → camelCase → getter (getMicAmihudSlpW4)
 * 3. Valor Double extraido e convertido pra float
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MongoFeatureAssemblerService {

    private final MicrostructureIndicatorMongoRepository micRepo;
    private final MomentumIndicatorMongoRepository momRepo;
    private final TimeIndicatorMongoRepository timRepo;
    private final TrendIndicatorMongoRepository trdRepo;
    private final VolatilityIndicatorMongoRepository vltRepo;
    private final VolumeIndicatorMongoRepository volRepo;
    private final TpSlIndicatorMongoRepository tpslRepo;

    @Qualifier("mikhaelMongoTemplate")
    private final MongoTemplate mongoTemplate;

    // Cache de Method por (classe + featureName) — evita reflection repetida
    private final ConcurrentHashMap<String, Method> methodCache = new ConcurrentHashMap<>();

    /**
     * Container com todos os documentos de um range, pre-carregados e indexados por openTime.
     */
    public record PreloadedIndicators(
            Map<Instant, MicrostructureIndicatorDocument> mic,
            Map<Instant, MomentumIndicatorDocument> mom,
            Map<Instant, TimeIndicatorDocument> tim,
            Map<Instant, TrendIndicatorDocument> trd,
            Map<Instant, VolatilityIndicatorDocument> vlt,
            Map<Instant, VolumeIndicatorDocument> vol,
            Map<Instant, TpSlIndicatorDocument> tpsl
    ) {}

    /**
     * Pre-carrega todos os documentos de indicadores para um range (7 queries em paralelo).
     */
    public PreloadedIndicators preload(String symbol, Instant start, Instant end, CandleIntervals interval) {
        log.info("[FEAT-ASM] Pre-carregando indicadores (paralelo) {} {} {} a {}", symbol, interval, start, end);

        ExecutorService pool = Executors.newFixedThreadPool(7);

        Future<Map<Instant, MicrostructureIndicatorDocument>> fMic = pool.submit(() -> {
            var r = micRepo.findByRangeExclEnd(symbol, start, end, interval)
                    .stream().collect(Collectors.toMap(MicrostructureIndicatorDocument::getOpenTime, Function.identity()));
            log.info("[FEAT-ASM] mic={}", r.size()); return r;
        });
        Future<Map<Instant, MomentumIndicatorDocument>> fMom = pool.submit(() -> {
            var r = momRepo.findByRangeExclEnd(symbol, start, end, interval)
                    .stream().collect(Collectors.toMap(MomentumIndicatorDocument::getOpenTime, Function.identity()));
            log.info("[FEAT-ASM] mom={}", r.size()); return r;
        });
        Future<Map<Instant, TimeIndicatorDocument>> fTim = pool.submit(() -> {
            var r = timRepo.findByRangeExclEnd(symbol, start, end, interval)
                    .stream().collect(Collectors.toMap(TimeIndicatorDocument::getOpenTime, Function.identity()));
            log.info("[FEAT-ASM] tim={}", r.size()); return r;
        });
        Future<Map<Instant, TrendIndicatorDocument>> fTrd = pool.submit(() -> {
            var r = trdRepo.findByRangeExclEnd(symbol, start, end, interval)
                    .stream().collect(Collectors.toMap(TrendIndicatorDocument::getOpenTime, Function.identity()));
            log.info("[FEAT-ASM] trd={}", r.size()); return r;
        });
        Future<Map<Instant, VolatilityIndicatorDocument>> fVlt = pool.submit(() -> {
            var r = vltRepo.findByRangeExclEnd(symbol, start, end, interval)
                    .stream().collect(Collectors.toMap(VolatilityIndicatorDocument::getOpenTime, Function.identity()));
            log.info("[FEAT-ASM] vlt={}", r.size()); return r;
        });
        Future<Map<Instant, VolumeIndicatorDocument>> fVol = pool.submit(() -> {
            var r = volRepo.findByRangeExclEnd(symbol, start, end, interval)
                    .stream().collect(Collectors.toMap(VolumeIndicatorDocument::getOpenTime, Function.identity()));
            log.info("[FEAT-ASM] vol={}", r.size()); return r;
        });
        Future<Map<Instant, TpSlIndicatorDocument>> fTpsl = pool.submit(() -> {
            var r = tpslRepo.findByRangeExclEnd(symbol, start, end, interval)
                    .stream().collect(Collectors.toMap(TpSlIndicatorDocument::getOpenTime, Function.identity()));
            log.info("[FEAT-ASM] tpsl={}", r.size()); return r;
        });

        try {
            var result = new PreloadedIndicators(fMic.get(), fMom.get(), fTim.get(), fTrd.get(), fVlt.get(), fVol.get(), fTpsl.get());
            log.info("[FEAT-ASM] Pre-load concluido: mic={}, mom={}, tim={}, trd={}, vlt={}, vol={}, tpsl={}",
                    result.mic().size(), result.mom().size(), result.tim().size(), result.trd().size(),
                    result.vlt().size(), result.vol().size(), result.tpsl().size());
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("[FEAT-ASM] Interrompido durante pre-load", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("[FEAT-ASM] Erro no pre-load", e.getCause());
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Pre-carrega indicadores APENAS para os openTimes fornecidos, com projection
     * dos campos necessarios. Muito mais rapido que carregar o mes inteiro.
     *
     * @param openTimes os openTimes que precisamos (ex: so minuto-1 dos blocos)
     * @param featureNames lista de features do modelo — usado pra determinar quais familias/campos carregar
     * @param interval o intervalo (pra resolver o nome da collection)
     */
    public PreloadedIndicators preloadSelective(String symbol, Set<Instant> openTimes,
                                                 List<String> featureNames, CandleIntervals interval) {
        log.info("[FEAT-ASM] Pre-load seletivo: {} openTimes, {} features", openTimes.size(), featureNames.size());

        // Determinar quais familias sao necessarias
        Set<String> neededPrefixes = new HashSet<>();
        Map<String, Set<String>> fieldsByPrefix = new HashMap<>();
        for (String name : featureNames) {
            String prefix = name.substring(0, name.indexOf('_'));
            neededPrefixes.add(prefix);
            fieldsByPrefix.computeIfAbsent(prefix, k -> new HashSet<>()).add(toMongoField(name));
        }

        List<Instant> sortedTimes = new ArrayList<>(openTimes);

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(neededPrefixes.size(), 7));
        Map<String, Future<?>> futures = new HashMap<>();

        // Maps de resultado
        Map<Instant, MicrostructureIndicatorDocument> micMap = new HashMap<>();
        Map<Instant, MomentumIndicatorDocument> momMap = new HashMap<>();
        Map<Instant, TimeIndicatorDocument> timMap = new HashMap<>();
        Map<Instant, TrendIndicatorDocument> trdMap = new HashMap<>();
        Map<Instant, VolatilityIndicatorDocument> vltMap = new HashMap<>();
        Map<Instant, VolumeIndicatorDocument> volMap = new HashMap<>();
        Map<Instant, TpSlIndicatorDocument> tpslMap = new HashMap<>();

        if (neededPrefixes.contains("mic"))
            futures.put("mic", pool.submit(() -> {
                var r = querySelective(symbol, sortedTimes, fieldsByPrefix.get("mic"),
                        MicrostructureIndicatorDocument.class, MicrostructureIndicatorMongoRepository.collectionName(interval));
                micMap.putAll(r);
                log.info("[FEAT-ASM] mic={}", r.size());
            }));
        if (neededPrefixes.contains("mom"))
            futures.put("mom", pool.submit(() -> {
                var r = querySelective(symbol, sortedTimes, fieldsByPrefix.get("mom"),
                        MomentumIndicatorDocument.class, MomentumIndicatorMongoRepository.collectionName(interval));
                momMap.putAll(r);
                log.info("[FEAT-ASM] mom={}", r.size());
            }));
        if (neededPrefixes.contains("tim"))
            futures.put("tim", pool.submit(() -> {
                var r = querySelective(symbol, sortedTimes, fieldsByPrefix.get("tim"),
                        TimeIndicatorDocument.class, TimeIndicatorMongoRepository.collectionName(interval));
                timMap.putAll(r);
                log.info("[FEAT-ASM] tim={}", r.size());
            }));
        if (neededPrefixes.contains("trd"))
            futures.put("trd", pool.submit(() -> {
                var r = querySelective(symbol, sortedTimes, fieldsByPrefix.get("trd"),
                        TrendIndicatorDocument.class, TrendIndicatorMongoRepository.collectionName(interval));
                trdMap.putAll(r);
                log.info("[FEAT-ASM] trd={}", r.size());
            }));
        if (neededPrefixes.contains("vlt"))
            futures.put("vlt", pool.submit(() -> {
                var r = querySelective(symbol, sortedTimes, fieldsByPrefix.get("vlt"),
                        VolatilityIndicatorDocument.class, VolatilityIndicatorMongoRepository.collectionName(interval));
                vltMap.putAll(r);
                log.info("[FEAT-ASM] vlt={}", r.size());
            }));
        if (neededPrefixes.contains("vol"))
            futures.put("vol", pool.submit(() -> {
                var r = querySelective(symbol, sortedTimes, fieldsByPrefix.get("vol"),
                        VolumeIndicatorDocument.class, VolumeIndicatorMongoRepository.collectionName(interval));
                volMap.putAll(r);
                log.info("[FEAT-ASM] vol={}", r.size());
            }));
        if (neededPrefixes.contains("tpsl"))
            futures.put("tpsl", pool.submit(() -> {
                var r = querySelective(symbol, sortedTimes, fieldsByPrefix.get("tpsl"),
                        TpSlIndicatorDocument.class, TpSlIndicatorMongoRepository.collectionName(interval));
                tpslMap.putAll(r);
                log.info("[FEAT-ASM] tpsl={}", r.size());
            }));

        try {
            for (var f : futures.values()) f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("[FEAT-ASM] Interrompido", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("[FEAT-ASM] Erro no pre-load seletivo", e.getCause());
        } finally {
            pool.shutdown();
        }

        log.info("[FEAT-ASM] Pre-load seletivo concluido");
        return new PreloadedIndicators(micMap, momMap, timMap, trdMap, vltMap, volMap, tpslMap);
    }

    /**
     * Query com $in nos openTimes + projection dos campos necessarios.
     */
    private <T> Map<Instant, T> querySelective(String symbol, List<Instant> openTimes,
                                                Set<String> fields, Class<T> docClass, String collection) {
        Query query = new Query(Criteria.where("symbol").is(symbol)
                .and("openTime").in(openTimes));

        // Projection: so carregar openTime + campos necessarios
        query.fields().include("openTime").include("symbol");
        for (String field : fields) {
            query.fields().include(field);
        }

        List<T> results = mongoTemplate.find(query, docClass, collection);

        Map<Instant, T> map = new HashMap<>(results.size());
        for (T doc : results) {
            try {
                Method m = doc.getClass().getMethod("getOpenTime");
                Instant ot = (Instant) m.invoke(doc);
                map.put(ot, doc);
            } catch (Exception e) {
                log.warn("[FEAT-ASM] Erro ao extrair openTime de {}", doc.getClass().getSimpleName());
            }
        }
        return map;
    }

    /**
     * Monta o float[] de features para um candle, na ordem exata da lista featureNames.
     * Retorna null se algum documento necessario estiver faltando.
     */
    public float[] assemble(Instant openTime, List<String> featureNames, PreloadedIndicators data) {
        float[] features = new float[featureNames.size()];

        MicrostructureIndicatorDocument micDoc = data.mic().get(openTime);
        MomentumIndicatorDocument momDoc = data.mom().get(openTime);
        TimeIndicatorDocument timDoc = data.tim().get(openTime);
        TrendIndicatorDocument trdDoc = data.trd().get(openTime);
        VolatilityIndicatorDocument vltDoc = data.vlt().get(openTime);
        VolumeIndicatorDocument volDoc = data.vol().get(openTime);
        TpSlIndicatorDocument tpslDoc = data.tpsl().get(openTime);

        for (int i = 0; i < featureNames.size(); i++) {
            String name = featureNames.get(i);
            Double value = resolveFeature(name, micDoc, momDoc, timDoc, trdDoc, vltDoc, volDoc, tpslDoc);
            features[i] = (value != null && Double.isFinite(value)) ? value.floatValue() : Float.NaN;
        }

        return features;
    }

    private Double resolveFeature(String featureName,
                                   MicrostructureIndicatorDocument mic,
                                   MomentumIndicatorDocument mom,
                                   TimeIndicatorDocument tim,
                                   TrendIndicatorDocument trd,
                                   VolatilityIndicatorDocument vlt,
                                   VolumeIndicatorDocument vol,
                                   TpSlIndicatorDocument tpsl) {
        String prefix = featureName.substring(0, featureName.indexOf('_'));
        Object doc = switch (prefix) {
            case "mic" -> mic;
            case "mom" -> mom;
            case "tim" -> tim;
            case "trd" -> trd;
            case "vlt" -> vlt;
            case "vol" -> vol;
            case "tpsl" -> tpsl;
            default -> null;
        };

        if (doc == null) return null;

        String mongoField = toMongoField(featureName);
        String getterName = "get" + capitalize(mongoField);
        String cacheKey = doc.getClass().getSimpleName() + "." + getterName;

        try {
            Method method = methodCache.computeIfAbsent(cacheKey, k -> {
                try {
                    return doc.getClass().getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    log.warn("[FEAT-ASM] Getter nao encontrado: {}.{} (feature={})", doc.getClass().getSimpleName(), getterName, featureName);
                    return null;
                }
            });

            if (method == null) return null;

            Object result = method.invoke(doc);
            if (result instanceof Double d) return d;
            if (result instanceof Number n) return n.doubleValue();
            return null;

        } catch (Exception e) {
            log.warn("[FEAT-ASM] Erro ao resolver feature '{}': {}", featureName, e.getMessage());
            return null;
        }
    }

    /**
     * Converte feature name (snake_case) para o nome do campo no MongoDB.
     * TimeIndicatorDocument NAO usa prefixo tim_ nos campos:
     *   tim_minute_of_day → minuteOfDay (strip "tim_")
     * Todas as outras familias USAM o prefixo:
     *   mic_amihud_slp_w4 → micAmihudSlpW4
     *   mom_rsi_14 → momRsi14
     */
    static String toMongoField(String featureName) {
        if (featureName.startsWith("tim_")) {
            // Strip prefixo "tim_" e converter o resto
            return snakeToCamel(featureName.substring(4));
        }
        return snakeToCamel(featureName);
    }

    static String snakeToCamel(String snake) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
