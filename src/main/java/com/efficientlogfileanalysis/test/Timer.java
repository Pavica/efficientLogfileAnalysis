package com.efficientlogfileanalysis.test;

import lombok.ToString;

import java.time.Instant;

/**
 * A class for measuring the performance of code in >milliseconds.
 * @author Jan Mandl, Andreas Kurz
 * Last-changed: 24.8.2022
 */
public class Timer {

    /**
     * 10^10
     */
    private static final long TEN_TO_THE_POWER_OF_TEN = 10_000_000_000L;

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
     * A functional interface containing a Function that can be timed
     */
    public interface TimerTask{
        void runTask() throws Exception;
    }

    /**
     * Convenience method that times a given Function and prints the result
     * @param func The Function that is being timed.
     * @param times The amount of times the function should be executed.
     * @return A Time Object with statistics of the runTask function.
     */
    public static Time timeIt(TimerTask func, int times)
    {
        Timer timer = new Timer();
        Time time = timer.timeExecutionSpeed(func, times);
        System.out.println(time);
        return time;
    }

    /**
     * Initially stores the time at which the Timer was started. When the timer gets paused and unpaused the time that has passed between those functions gets added.
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
     * @return The current time in milliseconds
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
     * Times the execution speed, in milliseconds, of a Task by running the function for n times. Can't be used to time 2 things simultaneously.
     * @param func The Task that is being timed.
     * @param times The amount of times the function should be executed.
     * @return A Time Object with statistics of the runTask function.
     */
    public Time timeExecutionSpeed(TimerTask func, int times) {
        Time time = new Time();

        for(int i = 0;i < times; ++i) {
            startTime = getCurrentTime();
            try {
                func.runTask();
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
        Time stats = t.timeExecutionSpeed(() -> {
            Thread.sleep(1);
        }, 128);

        System.out.println(t3.time());

        System.out.println(stats.totalTime);
        //System.out.println(stats.longestTime);
        //System.out.println(stats.shortestTime);
        //System.out.println(stats.averageTime);
    }

}