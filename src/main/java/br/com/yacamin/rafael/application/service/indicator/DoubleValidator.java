package br.com.yacamin.rafael.application.service.indicator;

public final class DoubleValidator {

    private DoubleValidator() {}

    /**
     * Valida o valor calculado de um indicador.
     * NaN e Infinite retornam null (campo nao preenchido),
     * coerente com a convencao do projeto (null = nao calculado).
     */
    public static Double validate(double value, String fieldName) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        return value;
    }
}
