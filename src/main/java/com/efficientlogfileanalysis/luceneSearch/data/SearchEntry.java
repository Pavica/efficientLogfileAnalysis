package com.efficientlogfileanalysis.luceneSearch.data;

import com.efficientlogfileanalysis.logs.data.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about a log file and the ids of its contents
 */
@Data
@AllArgsConstructor
public class SearchEntry {
    private String filename;
    private long firstDate = Long.MAX_VALUE;
    private long lastDate = 0;
    private final List<Long> logEntryIDs = new ArrayList<>();
    private final List<LogLevel> logLevels = new ArrayList<>();

    public SearchEntry(String filename)
    {
        this.filename = filename;
    }

    public void addLogEntry(long entryID, LogLevel logLevel, long date)
    {
        logEntryIDs.add(entryID);

        if(!logLevels.contains(logLevel))
        {
            logLevels.add(logLevel);
        }

        if(date < firstDate)
        {
            firstDate = date;
        }

        if(date > lastDate)
        {
            lastDate = date;
        }
    }
}