package com.wheeltime.timewheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimeWheel {
    private static final Logger logger = LoggerFactory.getLogger(TimeWheel.class);

    private final long tickDuration;
    private final int wheelSize;
    private final List<TimedTask>[] wheel;
    private int currentIndex;
    private final ScheduledExecutorService tickExecutor;
    private final ExecutorService taskExecutor;
    private final AtomicBoolean isRunning;

    @SuppressWarnings("unchecked")
    public TimeWheel(long tickDuration, int wheelSize, int taskThreadPoolSize) {
        this.tickDuration = tickDuration;
        this.wheelSize = wheelSize;
        this.wheel = new List[wheelSize];
        this.currentIndex = 0;
        this.isRunning = new AtomicBoolean(false);

        for (int i = 0; i < wheelSize; i++) {
            wheel[i] = new CopyOnWriteArrayList<>();
        }

        this.tickExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TimeWheel-Tick");
            t.setDaemon(true);
            return t;
        });

        this.taskExecutor = new ThreadPoolExecutor(
                taskThreadPoolSize,
                taskThreadPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r, "TimeWheel-Task");
                    t.setDaemon(true);
                    return t;
                }
        );
    }

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Starting TimeWheel with tickDuration={}ms, wheelSize={}", tickDuration, wheelSize);
            tickExecutor.scheduleAtFixedRate(this::tick, tickDuration, tickDuration, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Stopping TimeWheel");
            tickExecutor.shutdown();
            taskExecutor.shutdown();
            try {
                if (!tickExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    tickExecutor.shutdownNow();
                }
                if (!taskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    taskExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                tickExecutor.shutdownNow();
                taskExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addTask(TimedTask task) {
        if (!isRunning.get()) {
            throw new IllegalStateException("TimeWheel is not running");
        }

        long delayMs = task.getDelayMs();
        if (delayMs < 0) {
            logger.warn("Task delay is negative, executing immediately");
            taskExecutor.submit(task::run);
            return;
        }

        int ticks = (int) (delayMs / tickDuration);
        int targetIndex = (currentIndex + ticks) % wheelSize;
        
        wheel[targetIndex].add(task);
        logger.debug("Added task to slot {} (current={}, delay={}ms, ticks={})", 
                    targetIndex, currentIndex, delayMs, ticks);
    }

    private void tick() {
        try {
            List<TimedTask> tasks = wheel[currentIndex];
            List<TimedTask> tasksToExecute = new ArrayList<>(tasks);
            tasks.clear();

            if (!tasksToExecute.isEmpty()) {
                logger.debug("Executing {} tasks at slot {}", tasksToExecute.size(), currentIndex);
                for (TimedTask task : tasksToExecute) {
                    taskExecutor.submit(() -> {
                        try {
                            task.run();
                        } catch (Exception e) {
                            logger.error("Error executing task", e);
                        }
                    });
                }
            }

            currentIndex = (currentIndex + 1) % wheelSize;
        } catch (Exception e) {
            logger.error("Error in tick", e);
        }
    }

    public long getTickDuration() {
        return tickDuration;
    }

    public int getWheelSize() {
        return wheelSize;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
