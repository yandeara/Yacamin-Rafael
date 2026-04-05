package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.adapter.out.persistence.mikhael.ModelRegistryMongoRepository;
import br.com.yacamin.rafael.domain.enumeration.PredictionType;
import br.com.yacamin.rafael.domain.model.SavedModel;
import br.com.yacamin.rafael.domain.mongo.document.ModelRegistryDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Carrega modelos .ubj seguindo o padrao do Mikhael:
 *   models/saved/{PRED_TYPE}/{PRED}_xgb_{SYMBOL}_{INTERVAL}_{MMYYYY-MMYYYY}.ubj
 * Exemplo: models/saved/horizon/HORIZON_xgb_BTCUSDT_1m_032024-032026.ubj
 *
 * Sincroniza com MongoDB (yacamin-mikhael, collection model_registry)
 * e carrega Boosters em memoria para inferencia live.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelRegistryService {

    private static final Path MODELS_ROOT = Paths.get("models", "saved");

    // HORIZON_xgb_BTCUSDT_1m_012026-022026.ubj
    private static final Pattern MODEL_PATTERN = Pattern.compile(
            "^(?<pred>[A-Z0-9]+)_(?<algo>[a-zA-Z]+)_(?<symbol>[A-Z0-9]+)_(?<tf>[0-9]+[a-z]+)_(?<rangeStart>\\d+)-(?<rangeEnd>\\d+)\\.ubj$"
    );

    private final ModelRegistryMongoRepository registryRepository;
    private final FeatureMaskService featureMaskService;

    private volatile List<SavedModel> models = List.of();
    private volatile Map<String, ModelRegistryDocument> registryByFileName = Map.of();
    private final Map<String, Booster> boosterCache = new ConcurrentHashMap<>();

    public void loadModels() {
        log.info("========== MODEL REGISTRY START ==========");

        // 1) Escanear arquivos .ubj
        List<SavedModel> foundFiles = scanModelFiles();
        models = Collections.unmodifiableList(foundFiles);
        log.info("[MODEL] {} arquivo(s) .ubj encontrado(s)", models.size());

        // 2) Sincronizar com MongoDB
        syncWithMongo(foundFiles);

        // 3) Carregar registros do MongoDB
        Map<String, ModelRegistryDocument> regMap = new HashMap<>();
        for (ModelRegistryDocument doc : registryRepository.findAll()) {
            regMap.put(doc.getFileName(), doc);
        }
        registryByFileName = Collections.unmodifiableMap(regMap);
        log.info("[MODEL] {} registro(s) no MongoDB", registryByFileName.size());

        // 4) Carregar Boosters em memoria (Rafael-specific)
        for (SavedModel model : models) {
            Path modelPath = MODELS_ROOT
                    .resolve(model.predictionType().name().toLowerCase())
                    .resolve(model.fileName());

            try {
                Booster booster = XGBoost.loadModel(modelPath.toAbsolutePath().toString());
                boosterCache.put(model.fileName(), booster);
                log.info("[MODEL] Booster loaded: {} (pred={}, symbol={}, h{}, {})",
                        model.fileName(), model.predictionType(), model.symbol(),
                        model.horizon(), model.timeframe());
            } catch (XGBoostError e) {
                throw new IllegalStateException(
                        "Failed to load XGBoost model '" + model.fileName() + "': " + e.getMessage(), e);
            }
        }

        log.info("[MODEL] {} modelo(s) carregados em memoria", boosterCache.size());
        log.info("========== MODEL REGISTRY END ==========");
    }

    // ── Queries ──

    public List<SavedModel> getAll() {
        return models;
    }

    public List<SavedModel> getByPredictionType(PredictionType predictionType) {
        return models.stream()
                .filter(m -> m.predictionType() == predictionType)
                .toList();
    }

    public Optional<ModelRegistryDocument> getRegistry(String fileName) {
        return Optional.ofNullable(registryByFileName.get(fileName));
    }

    public Optional<List<String>> getFeatureNames(String fileName) {
        return getRegistry(fileName).map(ModelRegistryDocument::getFeatureNames);
    }

    public Booster getBooster(String fileName) {
        Booster b = boosterCache.get(fileName);
        if (b == null) {
            throw new IllegalStateException("No Booster loaded for: " + fileName);
        }
        return b;
    }

    /**
     * Retorna o modelo ativo para o PredictionType dado.
     * Busca pelo campo 'active=true' no registry. Se nenhum estiver marcado,
     * cai no fallback do primeiro encontrado.
     */
    public Optional<SavedModel> findFirst(PredictionType predictionType) {
        // Buscar modelo marcado como ativo no registry
        for (SavedModel m : models) {
            if (m.predictionType() != predictionType) continue;
            ModelRegistryDocument reg = registryByFileName.get(m.fileName());
            if (reg != null && reg.isActive()) {
                return Optional.of(m);
            }
        }
        // Fallback: primeiro disponível
        return models.stream()
                .filter(m -> m.predictionType() == predictionType)
                .findFirst();
    }

    /**
     * Ativa um modelo para seu predictionType, desativando os demais do mesmo tipo.
     */
    public void activateModel(String fileName) {
        ModelRegistryDocument target = registryByFileName.get(fileName);
        if (target == null) {
            throw new IllegalArgumentException("Model not found in registry: " + fileName);
        }

        String predType = target.getPredictionType();

        // Desativar todos do mesmo predictionType
        for (ModelRegistryDocument doc : registryByFileName.values()) {
            if (predType.equals(doc.getPredictionType()) && doc.isActive()) {
                doc.setActive(false);
                registryRepository.save(doc);
            }
        }

        // Ativar o selecionado
        target.setActive(true);
        registryRepository.save(target);

        // Atualizar cache local
        Map<String, ModelRegistryDocument> updated = new HashMap<>(registryByFileName);
        registryByFileName = Collections.unmodifiableMap(updated);

        log.info("[MODEL] Modelo ativado: {} ({})", fileName, predType);
    }

    // ── Scan ──

    private List<SavedModel> scanModelFiles() {
        List<SavedModel> found = new ArrayList<>();

        if (!Files.isDirectory(MODELS_ROOT)) {
            log.warn("[MODEL] Pasta de modelos nao encontrada: {}", MODELS_ROOT.toAbsolutePath());
            return found;
        }

        try (DirectoryStream<Path> typeStream = Files.newDirectoryStream(MODELS_ROOT)) {
            for (Path typeDir : typeStream) {
                if (!Files.isDirectory(typeDir)) continue;

                try (DirectoryStream<Path> modelStream = Files.newDirectoryStream(typeDir, "*.ubj")) {
                    for (Path modelFile : modelStream) {
                        SavedModel parsed = parseFileName(modelFile);
                        if (parsed != null) {
                            found.add(parsed);
                        } else {
                            log.warn("[MODEL] Nome de arquivo nao reconhecido: {}", modelFile.getFileName());
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("[MODEL] Erro ao escanear pasta de modelos", e);
        }

        return found;
    }

    // ── Sync MongoDB ──

    private void syncWithMongo(List<SavedModel> foundFiles) {
        for (SavedModel model : foundFiles) {
            Optional<ModelRegistryDocument> existing = registryRepository.findByFileName(model.fileName());

            if (existing.isPresent()) {
                log.info("[MODEL] Registro ja existe no MongoDB: {}", model.fileName());
                continue;
            }

            ModelRegistryDocument doc = new ModelRegistryDocument();
            doc.setFileName(model.fileName());
            doc.setPredictionType(model.predictionType().name());
            doc.setType(model.type());
            doc.setAlgorithm(model.algorithm());
            doc.setSymbol(model.symbol());
            doc.setHorizon(model.horizon());
            doc.setTimeframe(model.timeframe());
            doc.setRangeStart(model.rangeStart());
            doc.setRangeEnd(model.rangeEnd());
            doc.setFeatureNames(featureMaskService.getProdMask());

            registryRepository.save(doc);
            log.info("[MODEL] Registro criado no MongoDB: {} ({} features)", model.fileName(), doc.getFeatureNames().size());
        }
    }

    // ── Parse ──

    private SavedModel parseFileName(Path file) {
        String name = file.getFileName().toString();
        Matcher m = MODEL_PATTERN.matcher(name);
        if (!m.matches()) return null;

        PredictionType predType;
        try {
            predType = PredictionType.valueOf(m.group("pred"));
        } catch (IllegalArgumentException e) {
            log.warn("[MODEL] PredictionType desconhecido '{}' no arquivo: {}", m.group("pred"), name);
            return null;
        }

        long size;
        try {
            size = Files.size(file);
        } catch (IOException e) {
            size = -1;
        }

        int horizon = switch (predType) {
            case HORIZON -> 4;
            case M2M -> 1;
            case BLOCK -> 1;
            case C2C -> 1;
        };

        return new SavedModel(
                name,
                predType,
                predType.name().toLowerCase(),
                m.group("algo"),
                m.group("symbol"),
                horizon,
                m.group("tf"),
                formatRange(m.group("rangeStart")),
                formatRange(m.group("rangeEnd")),
                size
        );
    }

    private String formatRange(String raw) {
        if (raw.length() == 6) {
            return raw.substring(0, 2) + "/" + raw.substring(2);
        }
        return raw;
    }
}
