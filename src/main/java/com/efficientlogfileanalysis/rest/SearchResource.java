package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.data.search.SearchEntry;
import com.efficientlogfileanalysis.log.Search;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;

@Path("/search")
public class SearchResource {

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
            List<SearchEntry> result = search.search(filter);

            return Response.ok(result).build();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}