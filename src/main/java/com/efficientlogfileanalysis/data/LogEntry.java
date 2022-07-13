package com.efficientlogfileanalysis.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Data class representing a single log entry.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntry
{
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd LLL yyyy HH:mm:ss,SSS").withLocale(Locale.ENGLISH);

    private long time;
    private String logLevel;
    private String module;
    private String className;
    private String message;
    
    public LogEntry(String logEntry)
    {
        this
        (
            logEntry.substring(0, 24),
            logEntry.substring(24, 31).trim(),
            logEntry.substring(32, 36),
            logEntry.substring(38, logEntry.indexOf(":", 38)),
            logEntry.substring(logEntry.indexOf(" - ", 39) + 3)
        );
    }

    public LogEntry(String time, String logLevel, String module, String className, String message)
    {
        this.time = LocalDateTime.parse(time, DTF).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.logLevel = logLevel;
        this.module = module;
        this.className = className;
        this.message = message;
    }

    public LocalDateTime getLocalDateTime()
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    @Override
    public String toString()
    {
        return String.format("%s %s [%s] %s:? - %s", getLocalDateTime().format(DTF), logLevel, module, className, message);
    }

    public static void main(String[] args)
    {
        //LogEntry logEntry = new LogEntry("14 Apr 2022 13:42:32,798", "INFO", "main", "LocalServiceProvider", "destroying service platform.jdbc.xa.XaManagerService");
        LogEntry logEntry = new LogEntry("14 Apr 2022 13:42:32,798  INFO [main] LocalServiceProvider:? - destroying service platform.jdbc.xa.XaManagerService");
        System.out.println(logEntry);
    }
}
