package com.efficientlogfileanalysis.data.search;

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
        }

        public FilterBuilder addLogLevel(byte logLevel)
        {
            logLevels.add(logLevel);
            return this;
        }
    }
}
