package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.data.search.SearchEntry;
import com.efficientlogfileanalysis.log.FileIDManager;
import com.efficientlogfileanalysis.log.LogReader;
import com.efficientlogfileanalysis.log.Search;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
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

    private Filter parseFilterData(FilterData filterData)
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


        return filterBuilder.build();
    }

    @Deprecated
    @POST
    @Path("/filter")
    @Produces("application/json")
    public Response filteredSearch(FilterData filterData)
    {
        Filter filter = parseFilterData(filterData);

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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileData
    {
        private long firstDate = 0;
        private long lastDate = Long.MAX_VALUE;
        private String filename = null;
        private List<String> logLevels = new ArrayList<>();

        public void addLogLevel(String logLevel) {
            logLevels.add(logLevel);
        }
    }

    @POST
    @Path("/files")
    @Produces("application/json")
    public Response search(FilterData filterData)
    {
        Filter filter = parseFilterData(filterData);

        try
        {
            Search search = new Search();

            List<Short> fileIDs = search.searchForFiles(filter);
            List<FileData> affectedFiles = new ArrayList<>(fileIDs.size());

            for(short fileID : fileIDs)
            {
                FileData fileData = new FileData();
                fileData.setFirstDate(0);
                fileData.setLastDate(Long.MAX_VALUE);
                fileData.addLogLevel("INFO");
                fileData.setFilename(FileIDManager.getInstance().get(fileID));

                affectedFiles.add(fileData);
            }

            return Response.ok(affectedFiles).build();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/file/{fileName}")
    @Produces("application/json")
    public Response search(FilterData filterData, @PathParam("fileName") String fileName)
    {
        Filter filter = parseFilterData(filterData);
        short fileID = FileIDManager.getInstance().get(fileName);

        try
        {
            Search search = new Search();

            List<Long> entryIDs = search.searchInFile(filter, fileID);

            LogReader logReader = new LogReader();
            String logPath = Settings.getInstance().getLogFilePath();

            List<LogEntry> result = new ArrayList<>();

            for(long entryID : entryIDs)
            {
                result.add(logReader.readLogEntryWithoutMessage(logPath, fileID, entryID));
            }

            logReader.close();

            return Response.ok(result).build();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}