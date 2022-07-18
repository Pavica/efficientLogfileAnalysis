package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.LogEntry;
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

    public List<LogEntry> search(Filter filter) throws IOException
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

        ScoreDoc[] hits = searcher.search(query, Integer.MAX_VALUE).scoreDocs;

        List<LogEntry> results = new ArrayList<>();
        for(ScoreDoc hit : hits)
        {
            Document document = searcher.doc(hit.doc);

            short fileIndex = document.getField("fileIndex").numericValue().shortValue();
            long entryIndex = document.getField("logEntryID").numericValue().longValue();

            LogEntry result = LogReader.getLogEntry("test_logs", fileIndex, entryIndex);

            results.add(result);
        }

        return results;
    }

    public static void main(String[] args) throws IOException {
        Search search = new Search();

        Filter f = Filter.builder().build();
        search.search(f).forEach(System.out::println);
    }

}
