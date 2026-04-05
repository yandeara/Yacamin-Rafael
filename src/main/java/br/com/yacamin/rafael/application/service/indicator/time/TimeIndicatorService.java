package br.com.yacamin.rafael.application.service.indicator.time;

import br.com.yacamin.rafael.application.service.indicator.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.time.calc.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.mongo.document.TimeIndicatorDocument;
import br.com.yacamin.rafael.adapter.out.persistence.mikhael.TimeIndicatorMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeIndicatorService {

    private final TimeIndicatorMongoRepository timeIndicatorMongoRepository;

    public void analyse(SymbolCandle candle, BarSeries series) {
        analyse(candle, series, false);
    }

    public void analyse(SymbolCandle candle, BarSeries series, boolean forceRecalculate) {
        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var interval = candle.getInterval();

        log.info("[WARMUP][TIM] {} - {}", symbol, openTime);

        TimeIndicatorDocument doc;
        if (forceRecalculate) {
            doc = new TimeIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        } else {
            doc = timeIndicatorMongoRepository
                    .findBySymbolAndOpenTime(symbol, openTime, interval)
                    .orElseGet(() -> {
                        var d = new TimeIndicatorDocument();
                        d.setSymbol(symbol);
                        d.setOpenTime(openTime);
                        return d;
                    });
        }

        List<Callable<Void>> tasks = buildTasks(doc, openTime);

        if (!tasks.isEmpty()) {
            execute(tasks);
        }

        timeIndicatorMongoRepository.save(doc, interval);
    }

    public TimeIndicatorDocument analyseBuffered(SymbolCandle candle, BarSeries series, boolean forceRecalculate, TimeIndicatorDocument preloadedDoc) {
        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();

        log.info("[WARMUP][TIM] {} - {}", symbol, openTime);

        TimeIndicatorDocument doc;
        if (forceRecalculate) {
            doc = new TimeIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        } else if (preloadedDoc != null) {
            doc = preloadedDoc;
        } else {
            doc = new TimeIndicatorDocument();
            doc.setSymbol(symbol);
            doc.setOpenTime(openTime);
        }

        List<Callable<Void>> tasks = buildTasks(doc, openTime);

        if (!tasks.isEmpty()) {
            execute(tasks);
        }

        return doc;
    }

    private List<Callable<Void>> buildTasks(TimeIndicatorDocument doc, Instant openTime) {
        List<Callable<Void>> tasks = new ArrayList<>();

        ifNull("minuteOfDay",   doc::getMinuteOfDay,   () -> MinuteOfDayCalc.calculate(openTime),   doc::setMinuteOfDay,   tasks);
        ifNull("dayOfWeek",     doc::getDayOfWeek,     () -> DayOfWeekCalc.calculate(openTime),     doc::setDayOfWeek,     tasks);
        ifNull("sessionAsia",   doc::getSessionAsia,   () -> SessionAsiaCalc.calculate(openTime),   doc::setSessionAsia,   tasks);
        ifNull("sessionEurope", doc::getSessionEurope, () -> SessionEuropeCalc.calculate(openTime), doc::setSessionEurope, tasks);
        ifNull("sessionNy",     doc::getSessionNy,     () -> SessionNyCalc.calculate(openTime),     doc::setSessionNy,     tasks);
        ifNull("sinTime",       doc::getSinTime,       () -> SinTimeCalc.calculate(openTime),       doc::setSinTime,       tasks);
        ifNull("cosTime",       doc::getCosTime,       () -> CosTimeCalc.calculate(openTime),       doc::setCosTime,       tasks);
        ifNull("dayOfMonth",    doc::getDayOfMonth,    () -> DayOfMonthCalc.calculate(openTime),    doc::setDayOfMonth,    tasks);
        ifNull("sinDayOfWeek",  doc::getSinDayOfWeek,  () -> SinDayOfWeekCalc.calculate(openTime),  doc::setSinDayOfWeek,  tasks);
        ifNull("cosDayOfWeek",  doc::getCosDayOfWeek,  () -> CosDayOfWeekCalc.calculate(openTime),  doc::setCosDayOfWeek,  tasks);
        ifNull("overlapAsiaEur",doc::getOverlapAsiaEur,() -> OverlapAsiaEurCalc.calculate(openTime),doc::setOverlapAsiaEur,tasks);
        ifNull("overlapEurNy",  doc::getOverlapEurNy,  () -> OverlapEurNyCalc.calculate(openTime),  doc::setOverlapEurNy,  tasks);
        ifNull("candleInH1",   doc::getCandleInH1,    () -> CandleInH1Calc.calculate(openTime),    doc::setCandleInH1,    tasks);

        return tasks;
    }

    private void ifNull(String name,
                        Supplier<Double> getter,
                        Supplier<Double> calculator,
                        Consumer<Double> setter,
                        List<Callable<Void>> tasks) {
        if (getter.get() == null) {
            tasks.add(() -> {
                setter.accept(DoubleValidator.validate(calculator.get(), name));
                return null;
            });
        }
    }

    private void execute(List<Callable<Void>> tasks) {
        for (var task : tasks) {
            try { task.call(); }
            catch (Exception e) { throw new RuntimeException("[TIM] erro", e); }
        }
    }
}
