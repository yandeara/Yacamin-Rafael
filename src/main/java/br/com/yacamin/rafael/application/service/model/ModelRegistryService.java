package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.ModelDescriptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Carrega e valida todos os modelos .ubj da pasta models/ no startup.
 * Padrão: {tipo}_{moeda}_{horizon}_{intervalo}_{MMYYYY-MMYYYY}.ubj
 * Exemplo: xgb_BTCUSDT_h1_1m_012026-022026.ubj
 *
 * Se qualquer arquivo .ubj estiver fora do padrão, lança IllegalStateException
 * e impede o startup da aplicação.
 */
@Slf4j
@Service
public class ModelRegistryService {

    private static final Path MODELS_DIR = Paths.get("models");

    /**
     * Regex para o padrão oficial:
     * grupo1=tipo, grupo2=moeda, grupo3=horizon, grupo4=intervalo, grupo5=MMYYYY inicio, grupo6=MMYYYY fim
     */
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^([a-zA-Z]+)_([A-Z0-9]+)_(h\\d+)_(\\d+[mh])_(\\d{6})-(\\d{6})\\.ubj$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> VALID_INTERVALS = Set.of("1m", "5m", "15m", "1h");

    @Getter
    private final Map<String, ModelDescriptor> models = new LinkedHashMap<>();
    private final Map<String, Booster> boosterCache = new ConcurrentHashMap<>();

    public void loadModels() {
        log.info("========== MODEL REGISTRY START ==========");

        if (!Files.isDirectory(MODELS_DIR)) {
            log.warn("[MODEL] Directory '{}' not found — no models to load", MODELS_DIR.toAbsolutePath());
            log.info("========== MODEL REGISTRY END ==========");
            return;
        }

        List<Path> ubjFiles;
        try (Stream<Path> stream = Files.list(MODELS_DIR)) {
            ubjFiles = stream
                    .filter(p -> p.getFileName().toString().endsWith(".ubj"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read models directory: " + MODELS_DIR.toAbsolutePath(), e);
        }

        if (ubjFiles.isEmpty()) {
            log.info("[MODEL] No .ubj files found in '{}'", MODELS_DIR.toAbsolutePath());
            log.info("========== MODEL REGISTRY END ==========");
            return;
        }

        log.info("[MODEL] Found {} .ubj file(s) in '{}'", ubjFiles.size(), MODELS_DIR.toAbsolutePath());

        for (Path file : ubjFiles) {
            String fileName = file.getFileName().toString();
            ModelDescriptor descriptor = parseFileName(fileName, file.toAbsolutePath());
            models.put(descriptor.key(), descriptor);

            // Carregar Booster em memória
            try {
                Booster booster = XGBoost.loadModel(file.toAbsolutePath().toString());
                boosterCache.put(descriptor.key(), booster);
                log.info("[MODEL] Loaded: {} -> type={}, symbol={}, horizon={}, interval={}, train={}-{}",
                        fileName, descriptor.type(), descriptor.symbol(), descriptor.horizon(),
                        descriptor.interval().getValue(), descriptor.trainStart(), descriptor.trainEnd());
            } catch (XGBoostError e) {
                throw new IllegalStateException(
                        "Failed to load XGBoost model '" + fileName + "': " + e.getMessage(), e);
            }
        }

        log.info("[MODEL] {} model(s) loaded into memory", models.size());
        log.info("========== MODEL REGISTRY END ==========");
    }

    public List<ModelDescriptor> getAll() {
        return List.copyOf(models.values());
    }

    public Optional<ModelDescriptor> getByKey(String key) {
        return Optional.ofNullable(models.get(key));
    }

    public List<ModelDescriptor> getBySymbol(String symbol) {
        return models.values().stream()
                .filter(m -> m.symbol().equalsIgnoreCase(symbol))
                .toList();
    }

    public Booster getBooster(String key) {
        Booster b = boosterCache.get(key);
        if (b == null) {
            throw new IllegalStateException("No Booster loaded for key: " + key);
        }
        return b;
    }

    public List<ModelDescriptor> getByInterval(CandleIntervals interval) {
        return models.values().stream()
                .filter(m -> m.interval() == interval)
                .toList();
    }

    private ModelDescriptor parseFileName(String fileName, Path absolutePath) {
        Matcher matcher = NAME_PATTERN.matcher(fileName);

        if (!matcher.matches()) {
            throw new IllegalStateException(
                    "Model file '" + fileName + "' does not match the required naming pattern. " +
                    "Expected: {tipo}_{moeda}_{horizon}_{intervalo}_{MMYYYY-MMYYYY}.ubj " +
                    "(ex: xgb_BTCUSDT_h1_1m_012026-022026.ubj). " +
                    "Fix the file name or remove it from the models/ directory.");
        }

        String type = matcher.group(1).toLowerCase();
        String symbol = matcher.group(2).toUpperCase();
        String horizon = matcher.group(3).toLowerCase();
        String intervalStr = matcher.group(4).toLowerCase();
        String trainStartStr = matcher.group(5);
        String trainEndStr = matcher.group(6);

        // Validar intervalo
        if (!VALID_INTERVALS.contains(intervalStr)) {
            throw new IllegalStateException(
                    "Model file '" + fileName + "' has invalid interval '" + intervalStr + "'. " +
                    "Valid intervals: " + VALID_INTERVALS);
        }

        CandleIntervals interval = CandleIntervals.valueOfLabel(intervalStr);

        // Parsear treinamento MMYYYY
        YearMonth trainStart = parseYearMonth(trainStartStr, fileName);
        YearMonth trainEnd = parseYearMonth(trainEndStr, fileName);

        if (trainEnd.isBefore(trainStart)) {
            throw new IllegalStateException(
                    "Model file '" + fileName + "' has trainEnd (" + trainEnd +
                    ") before trainStart (" + trainStart + ").");
        }

        return new ModelDescriptor(type, symbol, horizon, interval, trainStart, trainEnd, fileName, absolutePath);
    }

    private YearMonth parseYearMonth(String mmyyyy, String fileName) {
        if (mmyyyy.length() != 6) {
            throw new IllegalStateException(
                    "Model file '" + fileName + "' has invalid training date '" + mmyyyy +
                    "'. Expected format: MMYYYY (ex: 012026).");
        }
        try {
            int month = Integer.parseInt(mmyyyy.substring(0, 2));
            int year = Integer.parseInt(mmyyyy.substring(2, 6));
            return YearMonth.of(year, month);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Model file '" + fileName + "' has invalid training date '" + mmyyyy +
                    "'. Expected format: MMYYYY (ex: 012026). Error: " + e.getMessage());
        }
    }
}
