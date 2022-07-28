package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.data.Tuple;
import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.data.search.SearchEntry;
import com.efficientlogfileanalysis.log.*;
import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.lucene.search.ScoreDoc;

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

    public static Filter parseFilterData(FilterData filterData)
    {
        Filter.FilterBuilder filterBuilder = Filter.builder();

        filterBuilder
                .beginDate(filterData.beginDate)
                .endDate(filterData.endDate);

        filterData.logLevels.stream().map(LogLevelIDManager.getInstance()::get).forEach(filterBuilder::addLogLevel);

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
            List<SearchEntry> result;

            Search search = new Search();

            result = search.search(filter);

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

                fileData.setFirstDate(LogDateManager.getInstance().get(fileID).beginDate);
                fileData.setLastDate(LogDateManager.getInstance().get(fileID).endDate);

                for(byte logLevelID : LogLevelIndexManager.getInstance().get(fileID)){
                    fileData.addLogLevel(LogLevelIDManager.getInstance().get(logLevelID));
                }

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

        filter.setFileID(fileID);

        try
        {
            Search search = new Search();

            List<Long> entryIDs = search.searchForLogEntryIDs(filter);

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageRequestData
    {
        FilterData filterData;
        LuceneSearchEntry lastSearchEntry;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LuceneSearchEntry
    {
        public int docNumber;
        public float docScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultPageResponseData
    {
        LuceneSearchEntry lastSearchEntry;
        List<LogEntry> logEntries;
    }

    @POST
    @Path("/file/{fileName}/{amount}")
    @Produces("application/json")
    public Response getResultPage(
        PageRequestData pageRequestData,
        @PathParam("fileName") String fileName,
        @PathParam("amount") int amount
    )
    {
        Filter filter = parseFilterData(pageRequestData.filterData);
        LuceneSearchEntry lastSearchData = pageRequestData.lastSearchEntry;

        short fileID = FileIDManager.getInstance().get(fileName);

        filter.setFileID(fileID);

        try
        {
            Search search = new Search();

            ScoreDoc lastSearchEntry = lastSearchData == null ? null : new ScoreDoc(lastSearchData.docNumber, lastSearchData.docScore);

            Tuple<List<Long>, ScoreDoc> result = search.searchForLogEntryIDsWithPagination(filter, amount, lastSearchEntry);

            ResultPageResponseData responseData = new ResultPageResponseData();
            responseData.setLastSearchEntry(new LuceneSearchEntry(result.value2.doc, result.value2.score));

            LogReader logReader = new LogReader();
            String logPath = Settings.getInstance().getLogFilePath();

            List<LogEntry> logEntries = new ArrayList<>();
            for(long entryID : result.value1)
            {
                logEntries.add(logReader.readLogEntryWithoutMessage(logPath, fileID, entryID));
            }

            logReader.close();

            responseData.setLogEntries(logEntries);

            return Response.ok(responseData).build();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}