package com.efficientlogfileanalysis.luceneSearch.data;

import com.efficientlogfileanalysis.logs.data.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterData
{
    private Long beginDate;
    private Long endDate;
    private List<String> logLevels;
    private String module;
    private String className;
    private String exception;
    private String message;

    /**
     * Parses the filter Data
     * @return a Filter based on this filterData object
     */
    public Filter parse()
    {
        Filter.FilterBuilder filterBuilder = Filter.builder();

        if(this.beginDate != null)
        {
            filterBuilder.beginDate(this.beginDate);
        }

        if(this.endDate != null)
        {
            filterBuilder.endDate(this.endDate);
        }

        //this.logLevels.stream().map(LogLevelIDManager.getInstance()::get).forEach(filterBuilder::addLogLevel);
        this.logLevels.stream().map(LogLevel::valueOf).map(LogLevel::getId).forEach(filterBuilder::addLogLevel);


        if(this.module != null)
        {
            filterBuilder.module(this.module);
        }

        if(this.className != null)
        {
            filterBuilder.className(this.className);
        }

        if(this.exception != null)
        {
            filterBuilder.exception(this.exception);
        }

        if(this.message != null && !this.message.isEmpty())
        {
            filterBuilder.message(this.message);
        }

        return filterBuilder.build();
    }
}