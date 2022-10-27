package com.efficientlogfileanalysis.logs.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class. Stores the name of a file and the Logentries it contains.
 * @author Jan Mandl
 */
@Data
public class LogFile {
    public String filename;
    private List<LogEntry> entries;

    public LogFile(String filename) {
        this.filename = filename;
        entries = new ArrayList<>();
    }

    public void addEntry(LogEntry le) {
        entries.add(le);
    }
}