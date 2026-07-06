package miiiiiin.com.vinyler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "esSyncExeutor")
    public Executor esSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 평시 유지 스레드 수
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        // 스레드 다 찼을 때 대기 큐
        executor.setQueueCapacity(100);
        // 로그 식별용
        executor.setThreadNamePrefix("es-sync");
        executor.initialize();
        return executor;
    }
}
