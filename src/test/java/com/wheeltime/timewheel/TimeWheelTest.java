package com.wheeltime.timewheel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TimeWheelTest {

    private TimeWheel timeWheel;

    @BeforeEach
    public void setUp() {
        timeWheel = new TimeWheel(100, 10, 5);
        timeWheel.start();
    }

    @AfterEach
    public void tearDown() {
        if (timeWheel != null) {
            timeWheel.stop();
        }
    }

    @Test
    public void testTimeWheelStart() {
        assertTrue(timeWheel.isRunning());
    }

    @Test
    public void testAddAndExecuteTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(0);

        TimedTask task = new SimpleTimedTask(() -> {
            counter.incrementAndGet();
            latch.countDown();
        }, 500);

        timeWheel.addTask(task);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed, "Task should complete within timeout");
        assertEquals(1, counter.get(), "Task should execute exactly once");
    }

    @Test
    public void testMultipleTasks() throws InterruptedException {
        int taskCount = 5;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            TimedTask task = new SimpleTimedTask(() -> {
                counter.incrementAndGet();
                System.out.println("Task " + taskId + " executed");
                latch.countDown();
            }, 200 + (i * 100));

            timeWheel.addTask(task);
        }

        boolean completed = latch.await(3, TimeUnit.SECONDS);
        assertTrue(completed, "All tasks should complete within timeout");
        assertEquals(taskCount, counter.get(), "All tasks should execute");
    }

    @Test
    public void testImmediateExecution() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        TimedTask task = new SimpleTimedTask(() -> {
            latch.countDown();
        }, -100);

        timeWheel.addTask(task);

        boolean completed = latch.await(1, TimeUnit.SECONDS);
        assertTrue(completed, "Negative delay task should execute immediately");
    }

    @Test
    public void testTimeWheelStop() {
        timeWheel.stop();
        assertFalse(timeWheel.isRunning());
        
        assertThrows(IllegalStateException.class, () -> {
            TimedTask task = new SimpleTimedTask(() -> {}, 100);
            timeWheel.addTask(task);
        }, "Should throw exception when adding task to stopped wheel");
    }
}
