package com.wheeltime.timewheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class TimeWheelConfig {
    private static final Logger logger = LoggerFactory.getLogger(TimeWheelConfig.class);

    @Value("${timewheel.tick-duration:100}")
    private long tickDuration;

    @Value("${timewheel.wheel-size:60}")
    private int wheelSize;

    @Value("${timewheel.task-thread-pool-size:10}")
    private int taskThreadPoolSize;

    private TimeWheel timeWheel;

    @Bean
    public TimeWheel timeWheel() {
        logger.info("Starting TimeWheel with configuration: tickDuration={}ms, wheelSize={}, taskThreadPoolSize={}", 
                    tickDuration, wheelSize, taskThreadPoolSize);
        timeWheel = new TimeWheel(tickDuration, wheelSize, taskThreadPoolSize);
        timeWheel.start();
        return timeWheel;
    }

    @PreDestroy
    public void stopTimeWheel() {
        if (timeWheel != null) {
            logger.info("Stopping TimeWheel");
            timeWheel.stop();
        }
    }
}
