package com.efficientlogfileanalysis.luceneSearch.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
    private long beginDate;
    private long endDate;
    private List<Byte> logLevels;
    private String module;
    private String className;
    private String exception;
    private short fileID;
    private String message;

    public static class FilterBuilder
    {
        private FilterBuilder()
        {
            beginDate   =   0;
            endDate     =   Long.MAX_VALUE;
            logLevels   =   new ArrayList<>();
            module      =   null;
            className   =   null;
            exception   =   null;
            fileID      =   -1;
            message     =   null;
        }

        public FilterBuilder addLogLevel(byte logLevel)
        {
            logLevels.add(logLevel);
            return this;
        }
    }
}
