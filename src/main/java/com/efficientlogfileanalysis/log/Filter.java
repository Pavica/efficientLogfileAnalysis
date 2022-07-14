package com.efficientlogfileanalysis.log;

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
    private List<String> logLevels;
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

        public FilterBuilder addLogLevel(String logLevel)
        {
            logLevels.add(logLevel);
            return this;
        }
    }

    public static void main(String[] args) {
        Filter filter = Filter
                .builder()
                .addLogLevel("miauser")
                .addLogLevel("miauser 2")
                .build();

        List list = Filter.builder().logLevels;
        list.forEach(System.out::println);

        System.out.println(filter);
    }
}
