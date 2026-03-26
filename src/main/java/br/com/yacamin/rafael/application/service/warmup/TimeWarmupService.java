package br.com.yacamin.rafael.application.service.warmup;

import br.com.yacamin.rafael.application.service.indicator.derivate.time.OpenTimeDerivation;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.TimeIndicatorEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.TimeIndicator15MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.TimeIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.TimeIndicator5MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.TimeIndicator1MnEntity;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn.TimeIndicator15MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn.TimeIndicator30MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.TimeIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.TimeIndicator1MnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(32);

    private final TimeIndicator15MnRepository i15Repository;
    private final TimeIndicator30MnRepository i30Repository;
    private final TimeIndicator1MnRepository i1Repository;
    private final TimeIndicator5MnRepository i5Repository;

    private final OpenTimeDerivation timeDerivation;

    public void analyse(SymbolCandle candle, BarSeries series) {

        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();

        log.info("[WARMUP][TIM] {} - {}", symbol, openTime);

        TimeIndicatorEntity entity;

        switch (candle.getInterval()) {
            case I1_MN -> entity = i1Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewOneMn(candle));
            case I15_MN -> entity = i15Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewFifteenMn(candle));
            case I5_MN -> entity = i5Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew5m(candle));
            case I30_MN -> entity = i30Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew30m(candle));
            default -> throw new RuntimeException();
        }

        List<Callable<Void>> tasks = List.of(
                timed("tim_minute_of_day", () -> entity.setTim_minute_of_day(DoubleValidator.validate(timeDerivation.minuteOfDay(candle), "tim_minute_of_day"))),
                timed("tim_day_of_week", () -> entity.setTim_day_of_week(DoubleValidator.validate(timeDerivation.dayOfWeek(candle), "tim_day_of_week"))),
                timed("tim_session_asia", () -> entity.setTim_session_asia(DoubleValidator.validate(timeDerivation.sessionAsia(candle), "tim_session_asia"))),
                timed("tim_session_europe", () -> entity.setTim_session_europe(DoubleValidator.validate(timeDerivation.sessionEurope(candle), "tim_session_europe"))),
                timed("tim_session_ny", () -> entity.setTim_session_ny(DoubleValidator.validate(timeDerivation.sessionNy(candle), "tim_session_ny"))),
                timed("tim_sin_time", () -> entity.setTim_sin_time(DoubleValidator.validate(timeDerivation.sinTime(candle), "tim_sin_time"))),
                timed("tim_cos_time", () -> entity.setTim_cos_time(DoubleValidator.validate(timeDerivation.cosTime(candle), "tim_cos_time"))),
                timed("tim_day_of_month", () -> entity.setTim_day_of_month(DoubleValidator.validate(timeDerivation.dayOfMonth(candle), "tim_day_of_month"))),
                timed("tim_sin_day_of_week", () -> entity.setTim_sin_day_of_week(DoubleValidator.validate(timeDerivation.sinDayOfWeek(candle), "tim_sin_day_of_week"))),
                timed("tim_cos_day_of_week", () -> entity.setTim_cos_day_of_week(DoubleValidator.validate(timeDerivation.cosDayOfWeek(candle), "tim_cos_day_of_week"))),
                timed("tim_overlap_asia_eur", () -> entity.setTim_overlap_asia_eur(DoubleValidator.validate(timeDerivation.overlapAsiaEur(candle), "tim_overlap_asia_eur"))),
                timed("tim_overlap_eur_ny", () -> entity.setTim_overlap_eur_ny(DoubleValidator.validate(timeDerivation.overlapEurNy(candle), "tim_overlap_eur_ny"))),
                timed("tim_candle_in_h1", () -> entity.setTim_candle_in_h1(DoubleValidator.validate(timeDerivation.candleInH1(candle), "tim_candle_in_h1")))
        );

        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][TIM] erro interno no cálculo",
                            ee.getCause()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        switch (candle.getInterval()) {
            case I1_MN -> i1Repository.save((TimeIndicator1MnEntity) entity);
            case I15_MN -> i15Repository.save((TimeIndicator15MnEntity) entity);
            case I5_MN ->  i5Repository.save((TimeIndicator5MnEntity) entity);
            case I30_MN -> i30Repository.save((TimeIndicator30MnEntity) entity);
            default -> throw new RuntimeException();
        }
    }

    private TimeIndicator15MnEntity createNew(SymbolCandle candle) {
        var e = new TimeIndicator15MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private Callable<Void> timed(String name, Runnable task) {
        return () -> {
            long t1 = System.nanoTime();
            try {
                task.run();
                return null;
            } finally {
                long t2 = System.nanoTime();
            }
        };
    }

    private TimeIndicator1MnEntity createNewOneMn(SymbolCandle candle) {
        var e = new TimeIndicator1MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private TimeIndicator15MnEntity createNewFifteenMn(SymbolCandle candle) {
        var e = new TimeIndicator15MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private TimeIndicator30MnEntity createNew30m(SymbolCandle candle) {
        var e = new TimeIndicator30MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private TimeIndicator5MnEntity createNew5m(SymbolCandle candle) {
        var e = new TimeIndicator5MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }
}
