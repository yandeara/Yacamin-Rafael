package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.adapter.out.persistence.Candle5MnRepository;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.scylla.entity.NewCandleEntity;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.MicrostructureIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.MomentumIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.TimeIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.TrendIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.VolatilityIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.VolumeIndicator5MnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Extrai features dos indicator entities de 5mn do Scylla para XGBoost.
 * Mesmo mecanismo do FeatureExtractorService mas usando repositórios de 5mn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureExtractor5MnService {

    private final Candle5MnRepository candle5MnRepository;
    private final MicrostructureIndicator5MnRepository microstructureRepo;
    private final MomentumIndicator5MnRepository momentumRepo;
    private final TimeIndicator5MnRepository timeRepo;
    private final TrendIndicator5MnRepository trendRepo;
    private final VolatilityIndicator5MnRepository volatilityRepo;
    private final VolumeIndicator5MnRepository volumeRepo;
    private final HeadHunterFeaturesMap featuresMap;
    private final FeatureMaskService featureMaskService;

    private volatile int[] cachedMask;

    public float[] extractFeatures(String symbol, Instant openTime, CandleIntervals interval) {
        if (interval != CandleIntervals.I5_MN) {
            log.warn("[FEATURE-5M] Expected I5_MN, got {}", interval);
            return null;
        }

        var candleEntities = candle5MnRepository
                .findBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqual(symbol, openTime, openTime);
        NewCandleEntity cdl = candleEntities.isEmpty() ? null : candleEntities.getFirst();

        if (cdl == null) {
            log.warn("[FEATURE-5M] No candle found for {} @ {}", symbol, openTime);
            return null;
        }

        var mic = microstructureRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var mom = momentumRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var tim = timeRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var trd = trendRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var vlt = volatilityRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);
        var vol = volumeRepo.findBySymbolAndOpenTime(symbol, openTime).orElse(null);

        AssemblerDto dto = new AssemblerDto(cdl, mic, mom, tim, trd, vlt, vol, null);

        int[] mask = getMask();
        return featuresMap.buildFeatures(dto, mask);
    }

    private int[] getMask() {
        if (cachedMask != null) return cachedMask;

        synchronized (this) {
            if (cachedMask != null) return cachedMask;

            List<String> featureNames = featureMaskService.getProdMask();
            cachedMask = featuresMap.maskOf(featureNames);
            log.info("[FEATURE-5M] Built feature mask with {} features", cachedMask.length);
            return cachedMask;
        }
    }
}
