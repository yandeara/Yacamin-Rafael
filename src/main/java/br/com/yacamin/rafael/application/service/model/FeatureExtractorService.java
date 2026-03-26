package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.adapter.out.persistence.Candle1MnRepository;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.scylla.entity.NewCandleEntity;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.MicrostructureIndicator1MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.MomentumIndicator1MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.TimeIndicator1MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.TrendIndicator1MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.VolatilityIndicator1MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.VolumeIndicator1MnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Extrai features dos indicator entities do Scylla e monta float[] para XGBoost.
 *
 * Usa HeadHunterFeaturesMap (copiado do Mikhael) para garantir:
 * 1. MESMA ORDEM das features usadas no treinamento
 * 2. MESMO MAPEAMENTO de nomes para getters das entities
 *
 * O FeatureMaskService seleciona QUAIS features usar (todas, subconjunto, etc).
 * O int[] mask traduz nomes para índices no REGISTRY, garantindo que o vetor
 * tenha exatamente as features esperadas pelo modelo treinado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureExtractorService {

    private final Candle1MnRepository candle1MnRepository;
    private final MicrostructureIndicator1MnRepository microstructureRepo;
    private final MomentumIndicator1MnRepository momentumRepo;
    private final TimeIndicator1MnRepository timeRepo;
    private final TrendIndicator1MnRepository trendRepo;
    private final VolatilityIndicator1MnRepository volatilityRepo;
    private final VolumeIndicator1MnRepository volumeRepo;
    private final HeadHunterFeaturesMap featuresMap;
    private final FeatureMaskService featureMaskService;

    // Cache do mask (computed once)
    private volatile int[] cachedMask;

    /**
     * Retorna a lista de nomes de features usadas na inferência.
     */
    public List<String> getFeatureNames() {
        return featureMaskService.getProdMask();
    }

    /**
     * Retorna quantas features serão extraídas.
     */
    public int getFeatureCount() {
        return getMask().length;
    }

    /**
     * Extrai features para um dado symbol e openTime.
     * Retorna float[] com as features na MESMA ORDEM do treinamento.
     * Retorna null se não encontrar o candle.
     */
    public float[] extractFeatures(String symbol, Instant openTime, CandleIntervals interval) {
        if (interval != CandleIntervals.I1_MN) {
            log.warn("[FEATURE] Only I1_MN supported for now, got {}", interval);
            return null;
        }

        // Carregar candle para esse openTime
        var candleEntities = candle1MnRepository
                .findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(symbol, openTime, openTime);
        NewCandleEntity cdl = candleEntities.isEmpty() ? null : candleEntities.getFirst();

        if (cdl == null) {
            log.warn("[FEATURE] No candle found for {} @ {}", symbol, openTime);
            return null;
        }

        // Carregar indicadores para esse exato openTime
        var mic = microstructureRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var mom = momentumRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var tim = timeRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var trd = trendRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var vlt = volatilityRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var vol = volumeRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);

        // tpsl = null — nenhuma feature tpsl_ na máscara getTpSlMask()
        AssemblerDto dto = new AssemblerDto(cdl, mic, mom, tim, trd, vlt, vol, null);

        // Extrair features usando mask na ordem EXATA do treinamento
        int[] mask = getMask();
        return featuresMap.buildFeatures(dto, mask);
    }

    private int[] getMask() {
        if (cachedMask != null) return cachedMask;

        synchronized (this) {
            if (cachedMask != null) return cachedMask;

            List<String> featureNames = featureMaskService.getProdMask();
            cachedMask = featuresMap.maskOf(featureNames);
            log.info("[FEATURE] Built feature mask with {} features (from {} in REGISTRY)",
                    cachedMask.length, featuresMap.allKeys().size());
            return cachedMask;
        }
    }
}
