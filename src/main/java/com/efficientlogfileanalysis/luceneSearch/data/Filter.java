package com.efficientlogfileanalysis.luceneSearch.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
    private long beginDate;
    private long endDate;
    private Set<Byte> logLevels;
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
            logLevels   =   new HashSet();
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
