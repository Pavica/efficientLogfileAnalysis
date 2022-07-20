package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.data.search.SearchEntry;
import com.efficientlogfileanalysis.test.Timer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class Search {

    private IndexSearcher searcher;

    public Search() throws IOException
    {
        //Open the Index
        Directory indexDirectory = FSDirectory.open(Paths.get(LuceneIndexManager.PATH_TO_INDEX));
        DirectoryReader directoryReader = DirectoryReader.open(indexDirectory);
        searcher = new IndexSearcher(directoryReader);
    }

    public List<SearchEntry> search(Filter filter) throws IOException
    {
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        //Add the time range
        queryBuilder.add(
            LongPoint.newRangeQuery("date",
                filter.getBeginDate(),
                filter.getEndDate()
            ), BooleanClause.Occur.MUST
        );

        if(!filter.getLogLevels().isEmpty())
        {
            //Apply all logLevel filters
            //TODO improve that
            String[] allLogLevels = {"INFO", "DEBUG", "WARN", "ERROR", "TRACE", "FATAL"};

            for(String notInlcuded : Arrays.stream(allLogLevels).filter(s -> !filter.getLogLevels().contains(s)).collect(Collectors.toList()))
            {
                queryBuilder.add(
                    new TermQuery(new Term("logLevel", notInlcuded)),
                    BooleanClause.Occur.MUST_NOT
                );
            }
        }

        //Apply module filter
        if(filter.getModule() != null)
        {
            queryBuilder.add(
                new TermQuery(new Term("module", filter.getModule())),
                BooleanClause.Occur.MUST
            );
        }

        //Apply className filter
        if(filter.getClassName() != null)
        {
            queryBuilder.add(
                new TermQuery(new Term("classname", filter.getClassName())),
                BooleanClause.Occur.MUST
            );
        }

        Query query = queryBuilder.build();

        System.out.println("Lucene start:");
        ScoreDoc[] hits = searcher.search(query, Integer.MAX_VALUE).scoreDocs;
        System.out.println("Lucene end:");

        System.out.println("stupid start");
        HashMap<Short, SearchEntry> logFiles = new HashMap<>();

        LogReader logReader = new LogReader();
        String path = Settings.getInstance().getLogFilePath();

        for(ScoreDoc hit : hits)
        {
            Document document = searcher.doc(hit.doc);

            short fileIndex = document.getField("fileIndex").numericValue().shortValue();
            long entryIndex = document.getField("logEntryID").numericValue().longValue();

            LogEntry result = new LogEntry();
            result.setEntryID(entryIndex);
            result.setTime(
                logReader.readDateOfEntry(
                    path,
                    fileIndex,
                    entryIndex
                )
            );
            result.setLogLevel(
                logReader.readLogLevelOfEntry(
                    path, 
                    fileIndex, 
                    entryIndex
                )
            );

            //slower version
            /*
            LogEntry result = logReader.getLogEntry(
                path,
                fileIndex,
                entryIndex
            );
            */

            logFiles.putIfAbsent(fileIndex, new SearchEntry(FileIDManager.getInstance().get(fileIndex)));
            logFiles.get(fileIndex).addLogEntry(result);
        }
        logReader.close();

        System.out.println("stupid end");

        return new ArrayList<>(logFiles.values());
    }

    public static void main(String[] args) throws IOException {
        Timer timer = new Timer();

        Timer.Time time = timer.timeExecutionSpeed(() -> {

            try {
                Search search = new Search();
        
                Filter f = Filter.builder().build();
                search.search(f);
            } 
            catch (Exception e) {
                e.printStackTrace();
            }

        }, 1);

        System.out.println(time);

        /*
        Timer timer = new Timer();

        Timer.Time time = timer.timeExecutionSpeed(() -> {
            LogEntry result = new LogEntry("04 Jul 2022 14:27:28,743 DEBUG [key] AbstractDialog:? - hide end: MAP036 timestamp: 1656937674040");
        }, 100_000);

        System.out.println(time);*/
    }

}
