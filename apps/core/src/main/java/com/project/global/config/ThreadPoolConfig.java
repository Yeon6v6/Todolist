package com.project.global.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
@EnableScheduling
public class ThreadPoolConfig {

    // Thread Pool 설정 상수
    private static final class ThreadPoolConstants {
        private static final int CPU_CORE_COUNT = Runtime.getRuntime().availableProcessors();
        private static final long TASK_WAIT_TIME_THRESHOLD_MS = 2000; // 2초
        private static final double QUEUE_WARNING_THRESHOLD = 0.8; // 80%
        private static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;
    }

    static class CustomThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
        @Getter
        private final String executorName;
        private final AtomicLong totalWaitTime = new AtomicLong(0);
        private final AtomicLong taskCount = new AtomicLong(0);

        public CustomThreadPoolTaskExecutor(String executorName) {
            this.executorName = executorName;
        }

        @Override
        public void execute(Runnable task) {
            checkQueueStatus();
            long startTime = System.currentTimeMillis();
            super.execute(() -> {
                long waitTime = System.currentTimeMillis() - startTime;
                totalWaitTime.addAndGet(waitTime);
                taskCount.incrementAndGet();

                if (waitTime >= ThreadPoolConstants.TASK_WAIT_TIME_THRESHOLD_MS) {
                    log.warn("[{}] Task wait time exceeded: {}ms (threshold: {}ms)",
                            executorName, waitTime, ThreadPoolConstants.TASK_WAIT_TIME_THRESHOLD_MS);
                }

                task.run();
            });
        }

        private void checkQueueStatus() {
            ThreadPoolExecutor executor;
            try {
                executor = getThreadPoolExecutor();
            } catch (IllegalStateException e) {
                return; // Not yet initialized
            }

            if (executor != null) {
                int queueSize = executor.getQueue().size();
                int totalCapacity = queueSize + executor.getQueue().remainingCapacity();
                double usageRate = totalCapacity > 0 ? (double) queueSize / totalCapacity : 0;

                if (usageRate >= ThreadPoolConstants.QUEUE_WARNING_THRESHOLD) {
                    log.warn("[{}] High queue usage: {:.1f}% ({}/{})",
                            executorName, usageRate * 100, queueSize, totalCapacity);
                }
            }
        }

        public double getAverageWaitTime() {
            long count = taskCount.get();
            return count > 0 ? (double) totalWaitTime.get() / count : 0;
        }
    }

    // 거부된 작업 처리 핸들러
    static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        private final String executorName;

        public CustomRejectedExecutionHandler(String executorName) {
            this.executorName = executorName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("[{}] Thread pool capacity exceeded. Running in caller thread.", executorName);
            if (!executor.isShutdown()) {
                r.run();
            }
        }
    }

    // 컨텍스트 전파 데코레이터
    private static class ContextAwareTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            SecurityContext securityContext = SecurityContextHolder.getContext();
            return () -> {
                try {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    SecurityContextHolder.setContext(securityContext);
                    runnable.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                    SecurityContextHolder.clearContext();
                }
            };
        }
    }

    // Task Executor 생성 헬퍼 메서드
    private CustomThreadPoolTaskExecutor createExecutor(String name, int coreMultiplier, int maxMultiplier, int queueCapacity) {
        CustomThreadPoolTaskExecutor executor = new CustomThreadPoolTaskExecutor(name);
        executor.setCorePoolSize(ThreadPoolConstants.CPU_CORE_COUNT * coreMultiplier);
        executor.setMaxPoolSize(ThreadPoolConstants.CPU_CORE_COUNT * maxMultiplier);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(ThreadPoolConstants.DEFAULT_KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(name + "-");
        executor.setRejectedExecutionHandler(new CustomRejectedExecutionHandler(name));
        executor.setTaskDecorator(new ContextAwareTaskDecorator());
        executor.initialize();
        return executor;
    }

    // 인증 관련 작업용
    @Bean(name = "authExecutor")
    public ThreadPoolTaskExecutor authExecutor() {
        return createExecutor("auth", 2, 4, 100);
    }

    @Bean(name = "loginTaskExecutor")
    public Executor loginTaskExecutor() {
        return new DelegatingSecurityContextAsyncTaskExecutor(authExecutor());
    }

    // 일반 업무 처리용
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        return createExecutor("task", 1, 3, 200);
    }

    // API 호출용
    @Bean(name = "apiExecutor")
    public ThreadPoolTaskExecutor apiExecutor() {
        return createExecutor("api", 1, 2, 500);
    }

    // 백그라운드 작업용
    @Bean(name = "backgroundExecutor")
    public ThreadPoolTaskExecutor backgroundExecutor() {
        return createExecutor("background", 1, 2, 1000);
    }
}
