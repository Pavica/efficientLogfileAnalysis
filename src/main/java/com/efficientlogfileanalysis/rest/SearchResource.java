package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.data.Tuple;
import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.data.search.SearchEntry;
import com.efficientlogfileanalysis.log.LogReader;
import com.efficientlogfileanalysis.log.IndexManager;
import com.efficientlogfileanalysis.log.Search;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
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
        private Long beginDate;
        private Long endDate;
        private List<String> logLevels;
        private String module;
        private String className;
        private String exception;
        private String message;
    }

    public static Filter parseFilterData(FilterData filterData)
    {
        Filter.FilterBuilder filterBuilder = Filter.builder();

        if(filterData.beginDate != null)
        {
            filterBuilder.beginDate(filterData.beginDate);
        }

        if(filterData.endDate != null)
        {
            filterBuilder.endDate(filterData.endDate);
        }

        //filterData.logLevels.stream().map(LogLevelIDManager.getInstance()::get).forEach(filterBuilder::addLogLevel);
        filterData.logLevels.stream().map(IndexManager.getInstance()::getLogLevelID).forEach(filterBuilder::addLogLevel);


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

        if(filterData.message != null && !filterData.message.isEmpty())
        {
            filterBuilder.message(filterData.message);
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

        try (Search search = new Search())
        {
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

        try (Search search = new Search())
        {
            List<Short> fileIDs = search.searchForFiles(filter);
            List<FileData> affectedFiles = new ArrayList<>(fileIDs.size());
            IndexManager mgr = IndexManager.getInstance();

            for(short fileID : fileIDs)
            {
                FileData fileData = new FileData();

                fileData.setFirstDate(mgr.getLogFileDateRange(fileID).beginDate);
                fileData.setLastDate(mgr.getLogFileDateRange(fileID).endDate);

                for(byte logLevelID : mgr.getLogLevelsOfFile(fileID)) {
                    fileData.addLogLevel(mgr.getLogLevelName(logLevelID));
                }

                fileData.setFilename(mgr.getFileName(fileID));

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
        short fileID = IndexManager.getInstance().getFileID(fileName);

        filter.setFileID(fileID);

        try (Search search = new Search())
        {
            List<Long> entryIDs = search.searchForLogEntryIDs(filter);

            String logPath = Settings.getInstance().getLogFilePath();

            List<LogEntry> result = new ArrayList<>();

            try (LogReader logReader = new LogReader())
            {
                for(long entryID : entryIDs)
                {
                    result.add(logReader.readLogEntryWithoutMessage(logPath, fileID, entryID));
                }
            }

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
        //Get LastSearchEntry Data
        LuceneSearchEntry lastSearchData = pageRequestData.lastSearchEntry;
        ScoreDoc lastSearchEntry = lastSearchData == null ? null : new ScoreDoc(lastSearchData.docNumber, lastSearchData.docScore);

        //Get the ID of the requested file
        Filter filter = parseFilterData(pageRequestData.filterData);
        short fileID = IndexManager.getInstance().getFileID(fileName);

        filter.setFileID(fileID);

        try (Search search = new Search())
        {
            Tuple<List<Long>, ScoreDoc> result = search.searchForLogEntryIDsWithPagination(filter, amount, lastSearchEntry);

            if(result.value1.isEmpty())
            {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            ResultPageResponseData responseData = new ResultPageResponseData();
            responseData.setLastSearchEntry(new LuceneSearchEntry(result.value2.doc, result.value2.score));

            String logPath = Settings.getInstance().getLogFilePath();
            List<LogEntry> logEntries = new ArrayList<>();

            try (LogReader logReader = new LogReader())
            {
                for(long entryID : result.value1)
                {
                    logEntries.add(logReader.readLogEntryWithoutMessage(logPath, fileID, entryID));
                }
            }

            responseData.setLogEntries(logEntries);

            return Response.ok(responseData).build();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}