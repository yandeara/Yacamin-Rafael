package br.com.yacamin.rafael.application.service.warmup;

public final class DoubleValidator {

    private DoubleValidator() {}

    public static double validate(double value, String fieldName) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Invalid value for field '" + fieldName + "': NaN");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException("Invalid value for field '" + fieldName + "': Infinite");
        }
        // Limite seguro de double: acima disso tende a overflow/arithmetic error
        if (value > 1e308 || value < -1e308) {
            throw new IllegalArgumentException("Invalid value for field '" + fieldName + "': Overflow-range " + value);
        }
        return value;
    }
}
