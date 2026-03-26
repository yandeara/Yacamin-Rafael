package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.microstructure.Return1CMicrostructureDerivation;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MicrostructureIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Calcula APENAS os 2 campos Return1C na máscara:
 * mic_high_return, mic_low_return
 * (mic_extreme_range_return e mic_body_return estão no Range e Body services)
 *
 * Nota: mic_high_return e mic_low_return usam RangeAmplitudeDerivation,
 * mas ficam aqui pois são retornos single-candle.
 * Na verdade eles não existiam no Return1C original — estavam perdidos.
 * Agora não precisamos mais deste service: os 4 campos da máscara que tinham prefixo "return"
 * (mic_high_return, mic_low_return, mic_extreme_range_return) estão no RangeWarmupService
 * e mic_body_return está no BodyWarmupService.
 *
 * Este service fica vazio — mantido para não quebrar injeção de dependência.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Return1CMicrostructureWarmupService {

    private final Return1CMicrostructureDerivation returnDerivation;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {
        // Nenhum campo da máscara getProdMask() é calculado aqui.
        // mic_high_return, mic_low_return, mic_extreme_range_return -> RangeWarmupService
        // mic_body_return -> BodyWarmupService
        // Campos mic_return_*, mic_rvr, etc. não estão na máscara.
    }
}
