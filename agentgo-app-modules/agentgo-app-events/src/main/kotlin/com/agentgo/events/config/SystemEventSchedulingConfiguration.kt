package com.agentgo.events.config

import java.util.concurrent.ThreadPoolExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

/**
 * Scheduler used only for system-event retention.
 *
 * The pool has one thread because retention is a single periodic task. The
 * scheduled executor's delayed queue is the queue for that task, and rejection
 * uses the executor's abort policy.
 */
@Configuration
class SystemEventSchedulingConfiguration {
    /**
     * Returns the retention scheduler.
     *
     * Spring initializes the scheduler after the bean is created.
     *
     * @return one-thread scheduler named for system-event retention
     */
    @Bean(name = ["systemEventTaskScheduler"])
    fun systemEventTaskScheduler(): ThreadPoolTaskScheduler =
        ThreadPoolTaskScheduler().apply {
            poolSize = 1
            setThreadNamePrefix("agentgo-system-event-retention-")
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(5)
            setRejectedExecutionHandler(ThreadPoolExecutor.AbortPolicy())
        }
}
