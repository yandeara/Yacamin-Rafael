package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Extrai features dos indicator documents do MongoDB e monta float[] para XGBoost.
 * Delega ao MongoFeatureAssemblerService (mesma logica do Mikhael).
 *
 * As features e sua ordem vem do ModelRegistryDocument (salvo no treino pelo Mikhael),
 * garantindo que o vetor float[] bata exatamente com o que o modelo espera.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureExtractorService {

    private final MongoFeatureAssemblerService featureAssembler;

    /**
     * Extrai features para um dado symbol e openTime.
     * Retorna float[] com as features na MESMA ORDEM do treinamento.
     *
     * @param featureNames lista ordenada de features conforme registrado no model registry
     */
    public float[] extractFeatures(String symbol, Instant openTime, CandleIntervals interval,
                                   List<String> featureNames) {
        var data = featureAssembler.preloadSelective(
                symbol, Set.of(openTime), featureNames, interval);

        return featureAssembler.assemble(openTime, featureNames, data);
    }
}
