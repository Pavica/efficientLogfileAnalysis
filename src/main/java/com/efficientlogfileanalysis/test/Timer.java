package com.efficientlogfileanalysis.test;

import lombok.ToString;

import java.time.Instant;

/**
 * A class for measuring the performance of code in miliseconds.
 * @author Jan Mandl
 * Last-changed: 11.7.2022
 */
public class Timer {

    /**
     * 10^10
     */
    private static long TEN_TO_THE_POWER_OF_TEN = 10_000_000_000l;

    /**
     * Data class that stores time statistics.
     * @author Jan Mandl
     * Last-changed: 11.7.2022
     */
    @ToString
    public class Time {
        public long totalTime;
        public long longestTime;
        public long shortestTime;
        public double averageTime;
        
        public Time() {
            totalTime = 0;
            longestTime = 0;
            shortestTime = TEN_TO_THE_POWER_OF_TEN;
            averageTime = 0;
        }
    }

    /**
     * Initally stores the time at which the Timer was started. When the timer gets paused and unpaused the time that has passed between those functions gets added.
     */
    private long startTime;
    /**
     * The time that has passed since the last call of pause().
     */
    private long pauseTime;

    public Timer() {
        restart();
    }
    
    /**
     * Uses Instant.now() to get the current time.
     * @return The current time in miliseconds
     */
    private long getCurrentTime() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Restarts the timer.
     */
    public void restart() {
        startTime = getCurrentTime();
        pauseTime = 0;
    }

    /**
     * Returns the time the timer ran until the call of the function.
     * @return The time since the timer started without pauses.
     */
    public long time() {
        return getCurrentTime() - startTime;
    }

    /**
     * Pauses the timer.
     */
    public void pause() {
        pauseTime = getCurrentTime();
    }

    /**
     * Unpauses the timer.
     */
    public void unpause() {
        startTime += getCurrentTime() - pauseTime;
        pauseTime = 0;
    }

    /**
     * Times the execution speed, in miliseconds, of a runnable by running the function for n times. Cant be used to time 2 things simultaniously.
     * @param func The Runnable that is being timed.
     * @param times The amount of times the function should be executed.
     * @return A Time Object with statistics of the ran function.
     */
    public Time timeExecutionSpeed(Runnable func, int times) {
        Time time = new Time();

        for(int i = 0;i < times; ++i) {
            startTime = getCurrentTime();
            try {
                func.run();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-6);
            }
            pauseTime = getCurrentTime() - startTime;

            time.totalTime += pauseTime;
            if(time.longestTime < pauseTime) {
                time.longestTime = pauseTime;
            }
            if(time.shortestTime > pauseTime) {
                time.shortestTime = pauseTime;
            }
        }
        time.averageTime = (double)time.totalTime / (double)times;

        return time;
    }

    public String toString() {
        return "Execution time: " + (getCurrentTime() - startTime) + "ms";
    }

    public static void main(String[] args) throws InterruptedException {
        Timer t3 = new Timer();
        Timer t = new Timer();
        Time stats = t.timeExecutionSpeed(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 128);

        System.out.println(t3.time());

        System.out.println(stats.totalTime);
        //System.out.println(stats.longestTime);
        //System.out.println(stats.shortestTime);
        //System.out.println(stats.averageTime);
    }

}