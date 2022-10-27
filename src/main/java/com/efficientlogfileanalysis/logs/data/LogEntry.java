package com.efficientlogfileanalysis.logs.data;

import com.efficientlogfileanalysis.logs.LogReader;
import com.efficientlogfileanalysis.util.DateConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

/**
 * Data class representing a single log entry.
 * @author Andreas Kurz, Jan Mandl
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntry
{
    //Regex which find an exception name
    private static final Pattern REGEX_GET_EXCEPTION_NAME = Pattern.compile("(\\w+Exception\\w*)");

    //Regex which returns the exception name including all preceding packages
    private static final Pattern REGEX_GET_EXCEPTION_FULL = Pattern.compile("((?:[a-zA-Z_]\\w*\\.)*\\w+Exception\\w*)");

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd LLL yyyy HH:mm:ss,SSS").withLocale(Locale.ENGLISH);

    /**
     * The time the message was logged in miliseconds
     */
    private long time;
    private LogLevel logLevel;
    /**
     * The value between a pair of []
     */
    private String module;
    /**
     * The value before :?
     */
    private String className;
    /**
     * The logged message
     */
    private String message;
    /**
     * The nth byte at which position the log entry starts in the file
     */
    private long entryID;

    public LogEntry(String time, LogLevel logLevel, String module, String className, String message, long entryID)
    {
        setDateFromString(time);
        this.logLevel = logLevel;
        this.module = module;
        this.className = className;
        this.message = message;
        this.entryID = entryID;
    }

    public LogEntry(String logEntry)
    {
        setDateFromString(logEntry.substring(0, 24));
        this.logLevel = LogLevel.valueOf(logEntry.substring(24, 31).trim());

        int indexOfClosedParenthesis = logEntry.indexOf("]", 32);
        this.module = logEntry.substring(32, indexOfClosedParenthesis);

        int indexOfColon = logEntry.indexOf(":", indexOfClosedParenthesis + 2);
        this.className = logEntry.substring(indexOfClosedParenthesis + 2, indexOfColon);
        this.message = logEntry.substring(logEntry.indexOf(" - ", indexOfColon) + 3);
    }

    public LogEntry(String logEntry, long entryID)
    {
        this(logEntry);
        this.entryID = entryID;
    }

    /**
     * Searches the message for a Java Exception
     * @return the full exception name (including the package)<br>
     *         null - if the message doesnt contain an exception
     */
    public Optional<String> findException()
    {
        if(message.contains("Exception"))
        {
            Matcher matcher = REGEX_GET_EXCEPTION_NAME.matcher(message);
            if(matcher.find())
            {
                return Optional.ofNullable(matcher.group(1));
            }
        }

        return Optional.empty();
    }

    public void setDateFromString(String time)
    {
        this.time = DateConverter.toLong(LocalDateTime.parse(time, DTF));
    }

    public void setDateAsLocalDateTime(LocalDateTime ldt) {
        this.time = DateConverter.toLong(ldt);
    }

    public LocalDateTime retrieveDateAsLocalDateTime()
    {
        return DateConverter.toDateTime(time);
    }

    @Override
    public String toString()
    {
        return String.format("%s %s [%s] %s:? - %s", retrieveDateAsLocalDateTime().format(DTF), logLevel, module, className, message);
    }

    public static void main(String[] args)
    {
        //--- Print exceptions in all log files ---//

        StringBuilder laterPrint = new StringBuilder("\n");

        for(LogFile logFile : LogReader.readAllLogFiles("test_logs"))
        {
            System.out.printf("--- %s ---\n", logFile.getFilename());

            for(LogEntry entry : logFile.getEntries())
            {
                Optional<String> exception = entry.findException();

                if(exception.isPresent())
                {
                    String id = logFile.getFilename() + "@" + entry.getEntryID();

                    System.out.printf("\t%s : %s\n", id, exception.get());
                    laterPrint.append(id).append(":\n").append(entry).append("\n");
                }
            }

            System.out.append('\n');
        }

        System.out.print(laterPrint);



//        //--- Measure the time needed to find exceptions ---//
//
//        //read all log entries in a single list:
//        LogFile[] logFiles = LogReader.readAllLogFiles("test_logs");
//        List<LogEntry> logEntryList = new ArrayList<>();
//        Arrays.stream(logFiles).map(LogFile::getEntries).forEach(logEntryList::addAll);
//
//        System.out.println(logEntryList.size());
//
//        //result:
//        //finding the exception in 167mb of log entries (~600_000 entries) takes about 50ms
//        //200gb would take ~60seconds :D
//        Timer.timeIt(() -> {
//            for (LogEntry entry : logEntryList)
//            {
//                entry.findException();
//            }
//        }, 100);

    }
}
