package com.xskr.onk_v1.core;

import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTaskTester {
    @Test
    public void testTimerTask() throws InterruptedException {
        System.out.println("Main: " + Thread.currentThread().getName());
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("TimerTask: " + Thread.currentThread().getName());
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 2000);
        Thread.sleep(3000);
        System.out.println("finish...");
    }
}
