package com.efficientlogfileanalysis.logs.data;

import com.efficientlogfileanalysis.util.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@AllArgsConstructor
public enum LogLevel {
    INFO(1),
    DEBUG(2),
    WARN(3),
    ERROR(4),
    TRACE(5),
    FATAL(6);

    private final byte id;

    LogLevel(int id) {
        this.id = (byte)id;
    }

    static final HashMap<Byte, LogLevel> levels = new HashMap<>();
    static {
        for (LogLevel value : values()) {
            levels.put(value.id, value);
        }
    }

    public static LogLevel fromID(byte id) {
        return levels.getOrDefault(id, null);
    }

    @Deprecated
    public static LogLevel getSlow(short id){
        for (LogLevel logLevel : values()){
            if(logLevel.id == id){
                return logLevel;
            }
        }

        return null;
    }

    public static void main(String[] args) {

        Random r = new Random();
        List<Byte> numbers =
                IntStream.generate(() -> r.nextInt(6) + 1)
                .limit(1_000)
                .boxed()
                .map(Integer::byteValue)
                .collect(Collectors.toList());

        Timer.timeIt(() -> numbers.forEach(LogLevel::fromID), 100_000);
        Timer.timeIt(() -> numbers.forEach(LogLevel::getSlow), 100_000);
    }
}
