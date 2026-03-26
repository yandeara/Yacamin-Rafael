package br.com.yacamin.rafael.domain;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Getter;

public enum CandleIntervals {

    I1_MN("1m", Duration.ofMinutes(1), 500, ChronoUnit.MINUTES),
    I5_MN("5m", Duration.ofMinutes(5), 2500, ChronoUnit.MINUTES),
    I15_MN("15m", Duration.ofMinutes(15), 7500, ChronoUnit.MINUTES),
    I30_MN("30m", Duration.ofMinutes(30), 3500, ChronoUnit.MINUTES),

    INTRADAY_LONG("5m", Duration.ofMinutes(5), 1000, ChronoUnit.MINUTES),
    TENDENCY_SHORT("1h", Duration.ofHours(1), 24, ChronoUnit.HOURS),
    TENDENCY_MID("4h", Duration.ofHours(4), 7, ChronoUnit.DAYS),
    TENDENCY_LONG("1d", Duration.ofDays(1), 30, ChronoUnit.DAYS),
    TENDENCY_STRUCTURAL("1w", Duration.ofDays(7), 180, ChronoUnit.DAYS);

    @Getter
    private final String value;

    @Getter
    private final Duration duration;

    private final int chunkValue;
    private final ChronoUnit chunkUnit;

    CandleIntervals(String value, Duration duration, int chunkValue, ChronoUnit chunkUnit) {
        this.value = value;
        this.duration = duration;
        this.chunkValue = chunkValue;
        this.chunkUnit = chunkUnit;
    }

    public Chunk getChunk() {
        return new Chunk(chunkValue, chunkUnit);
    }

    public static CandleIntervals valueOfLabel(String label) {
        for (CandleIntervals interval : values()) {
            if (interval.value.equalsIgnoreCase(label)) {
                return interval;
            }
        }
        throw new IllegalArgumentException("Intervalo desconhecido: " + label);
    }

    public record Chunk(int value, ChronoUnit unit) {}
}
