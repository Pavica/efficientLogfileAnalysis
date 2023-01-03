package com.efficientlogfileanalysis.logs.data;

import com.efficientlogfileanalysis.logs.data.LogEntry;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LogFileData{
    private List<LogEntry> entries;
    private long bytesRead;
}