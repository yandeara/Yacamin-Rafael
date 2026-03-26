package br.com.yacamin.rafael.application.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final MdcTaskDecorator MDC_DECORATOR = new MdcTaskDecorator();

    private ThreadPoolTaskExecutor createExecutor(int core, int max, int queue, String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queue);
        executor.setThreadNamePrefix(prefix);
        executor.setTaskDecorator(MDC_DECORATOR);
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean("klineUpdateListenerExecutor")
    public Executor klineUpdateListenerExecutor() {
        return createExecutor(1, 1, Integer.MAX_VALUE, "kline-listener-");
    }

    @Bean("bookTickUpdateListenerExecutor")
    public Executor bookTickUpdateListenerExecutor() {
        return createExecutor(1, 1, Integer.MAX_VALUE, "bookTick-listener-");
    }

    @Bean("reconnectMarketDataSocketExecutor")
    public Executor reconnectMarketDataSocketExecutor() {
        return createExecutor(1, 1, Integer.MAX_VALUE, "reconnect-marketdata-socket-");
    }

    @Bean("reconnectPolyMarketExecutor")
    public Executor reconnectPolyMarketExecutor() {
        return createExecutor(1, 1, Integer.MAX_VALUE, "reconnect-poly-market-socket-");
    }

    @Bean("subMessageMarketDataSocketExecutor")
    public Executor subMessageMarketDataSocketExecutor() {
        return createExecutor(1, 1, Integer.MAX_VALUE, "submessage-marketdata-socket-");
    }

    @Bean("polyOnResolveExecutor")
    public Executor polyOnResolveExecutor() {
        return createExecutor(1, 1, Integer.MAX_VALUE, "polymarket-onResolve-");
    }

    /** Pool para market discovery paralelo (Gamma API calls) */
    @Bean("marketDiscoveryExecutor")
    public Executor marketDiscoveryExecutor() {
        ThreadPoolTaskExecutor executor = createExecutor(4, 8, 50, "market-discovery-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /** Thread dedicada para writes no MongoDB */
    @Bean("mongoWriteExecutor")
    public Executor mongoWriteExecutor() {
        return createExecutor(1, 2, Integer.MAX_VALUE, "mongo-write-");
    }
}
