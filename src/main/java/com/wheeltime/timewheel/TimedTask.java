package com.wheeltime.timewheel;

public interface TimedTask {
    void run();
    
    long getDelayMs();
}
