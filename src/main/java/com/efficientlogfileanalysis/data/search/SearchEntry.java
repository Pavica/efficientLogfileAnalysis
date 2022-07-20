package com.efficientlogfileanalysis.data.search;

import com.efficientlogfileanalysis.data.LogEntry;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about a single logFile in a search result
 */
@Data
@AllArgsConstructor
public class SearchEntry {
    private String filename;
    private long firstDate = Long.MAX_VALUE;
    private long lastDate = 0;
    private final List<Long> logEntryIDs = new ArrayList<>();
    private final List<String> logLevels = new ArrayList<>();

    public SearchEntry(String filename)
    {
        this.filename = filename;
    }

    public void addLogEntry(LogEntry logEntry)
    {
        logEntryIDs.add(logEntry.getEntryID());

        if(!logLevels.contains(logEntry.getLogLevel()))
        {
            logLevels.add(logEntry.getLogLevel());
        }

        if(logEntry.getTime() < firstDate)
        {
            firstDate = logEntry.getTime();
        }

        if(logEntry.getTime() > lastDate)
        {
            lastDate = logEntry.getTime();
        }
    }
}