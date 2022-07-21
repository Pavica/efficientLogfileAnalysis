package com.efficientlogfileanalysis.data.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileSearch {
    private String filename;
    private long firstDate = Long.MAX_VALUE;
    private long lastDate = 0;
    private final List<String> logLevels = new ArrayList<>();
}
