package com.efficientlogfileanalysis.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class. Stores the name of a file and the Logentries it contains.
 * @author Jan Mandl
 */
public class Logfile {
    public String filename;
    private List<LogEntry> entries;

    public Logfile(String filename) {
        this.filename = filename;
        entries = new ArrayList<>();
    }

    public void addEntry(LogEntry le) {
        entries.add(le);
    }

    public List<LogEntry> getEntries() {
        return entries;
    }
}