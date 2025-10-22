package com.wheeltime.timewheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/timewheel")
public class TimeWheelController {
    private static final Logger logger = LoggerFactory.getLogger(TimeWheelController.class);

    @Autowired
    private TimeWheel timeWheel;

    @PostMapping("/task")
    public Map<String, Object> addTask(@RequestParam(defaultValue = "1000") long delayMs,
                                       @RequestParam(defaultValue = "Hello from TimeWheel!") String message) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            TimedTask task = new SimpleTimedTask(() -> {
                logger.info("Executing delayed task: {}", message);
            }, delayMs);
            
            timeWheel.addTask(task);
            
            response.put("success", true);
            response.put("message", "Task added successfully");
            response.put("delayMs", delayMs);
            response.put("taskMessage", message);
        } catch (Exception e) {
            logger.error("Error adding task", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", timeWheel.isRunning());
        status.put("currentIndex", timeWheel.getCurrentIndex());
        status.put("wheelSize", timeWheel.getWheelSize());
        status.put("tickDuration", timeWheel.getTickDuration());
        return status;
    }
}
