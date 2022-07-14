package com.efficientlogfileanalysis;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.log.Filter;
import com.efficientlogfileanalysis.log.Search;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Path("/search")
public class LogfileResource {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterData
    {
        private long beginDate;
        private long endDate;
        private List<String> logLevels;
        private String module;
        private String className;
        private String exception;
    }

    @POST
    @Path("/filter")
    @Produces("application/json")
    public Response filteredSearch(FilterData filterData)
    {
        //empty list in js gets converted into null for some reason
        if(filterData.logLevels == null) {
            filterData.logLevels = new ArrayList<>();
        }

        Filter.FilterBuilder filterBuilder = Filter.builder();

        filterBuilder
                .beginDate(filterData.beginDate)
                .endDate(filterData.endDate);

        filterData.logLevels.forEach(filterBuilder::addLogLevel);

        if(filterData.module != null)
        {
            filterBuilder.module(filterData.module);
        }

        if(filterData.className != null)
        {
            filterBuilder.className(filterData.className);
        }

        if(filterData.exception != null)
        {
            filterBuilder.exception(filterData.exception);
        }


        Filter filter = filterBuilder.build();

        System.out.println();

        try
        {
            Search search = new Search();
            List<LogEntry> result = search.search(filter);

            return Response.ok(result).build();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_GATEWAY).build();
        }
    }
}