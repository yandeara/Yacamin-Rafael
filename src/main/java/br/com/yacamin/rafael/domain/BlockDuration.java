package br.com.yacamin.rafael.domain;

import java.time.Instant;

public enum BlockDuration {

    FIVE_MIN(300),
    FIFTEEN_MIN(900),
    ONE_HOUR(3600);

    private final int seconds;

    BlockDuration(int seconds) {
        this.seconds = seconds;
    }

    public int getSeconds() {
        return seconds;
    }

    public long getDurationMillis() {
        return seconds * 1000L;
    }

    public long currentBlockUnix(Instant now) {
        long epoch = now.getEpochSecond();
        return (epoch / seconds) * seconds;
    }

    public long currentBlockUnix() {
        return currentBlockUnix(Instant.now());
    }

    public long blockEnd(long blockUnix) {
        return blockUnix + seconds;
    }

    public double timeRemaining(long blockUnix, Instant now) {
        return blockEnd(blockUnix) - now.getEpochSecond();
    }

    public double timeRemaining(long blockUnix) {
        return timeRemaining(blockUnix, Instant.now());
    }

    public double normalizedTimeRemaining(long blockUnix, Instant now) {
        return timeRemaining(blockUnix, now) / (double) seconds;
    }

    public long boundaryForEpoch(long epoch) {
        return (epoch / seconds) * seconds;
    }
}
