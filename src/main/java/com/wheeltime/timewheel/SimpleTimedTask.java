package com.wheeltime.timewheel;

public class SimpleTimedTask implements TimedTask {
    private final Runnable task;
    private final long delayMs;

    public SimpleTimedTask(Runnable task, long delayMs) {
        this.task = task;
        this.delayMs = delayMs;
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public long getDelayMs() {
        return delayMs;
    }
}
