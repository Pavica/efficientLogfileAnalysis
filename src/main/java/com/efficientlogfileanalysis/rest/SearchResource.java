package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.logs.data.LogEntry;
import com.efficientlogfileanalysis.logs.data.LogLevel;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.data.Tuple;
import com.efficientlogfileanalysis.luceneSearch.data.Filter;
import com.efficientlogfileanalysis.luceneSearch.data.FilterData;
import com.efficientlogfileanalysis.logs.LogReader;
import com.efficientlogfileanalysis.index.IndexManager;
import com.efficientlogfileanalysis.luceneSearch.Search;
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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileData
    {
        private long firstDate = 0;
        private long lastDate = Long.MAX_VALUE;
        private String filename = null;
        private List<LogLevel> logLevels = new ArrayList<>();

        public void addLogLevel(LogLevel logLevel) {
            logLevels.add(logLevel);
        }
    }

    /**
     * Search for logFiles containing entries that match the given filter
     * @param filterData contains filter information
     * @return meta information about all logFiles that match the given criteria
     */
    @POST
    @Path("/files")
    @Produces("application/json")
    public Response searchForFiles(FilterData filterData)
    {
        Filter filter = filterData.parse();

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
                    fileData.addLogLevel(LogLevel.fromID(logLevelID));
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

    /**
     * Returns all entries matching the given filterData in a specific logfile
     * @param filterData contains filter information that every logEntry needs to match
     * @param fileName the name of the logfile to be searched
     * @return all matching entries without the logMessage
     */
    @POST
    @Path("/file/{fileName}")
    @Produces("application/json")
    public Response searchInFile(FilterData filterData, @PathParam("fileName") String fileName)
    {
        //parse filter data
        Filter filter = filterData.parse();

        //add the fileID to the filter
        short fileID = IndexManager.getInstance().getFileID(fileName);
        filter.setFileID(fileID);

        try (Search search = new Search())
        {
            List<LogEntry> result = new ArrayList<>();
            List<Long> entryIDs = search.searchForLogEntryIDs(filter);

            try (LogReader logReader = new LogReader(Settings.getInstance().getLogFilePath()))
            {
                for(long entryID : entryIDs)
                {
                    result.add(logReader.readLogEntryWithoutMessage(fileName, entryID));
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

    /**
     * Returns the matching LogEntries in a given file in pages
     * @param pageRequestData data identifying the previous page (if null returns the first page)
     * @param fileName the name of the file to be searched
     * @param amount the maximal amount of entries for each page
     * @return the matching logEntries including a pageRequestData object that needs to be specified to retrieve the next page
     */
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
        Filter filter = pageRequestData.filterData.parse();
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

            try (LogReader logReader = new LogReader(logPath))
            {
                for(long entryID : result.value1)
                {
                    //get preceding log entries
                    logEntries.add(logReader.readLogEntryWithoutMessage(fileName, entryID));
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