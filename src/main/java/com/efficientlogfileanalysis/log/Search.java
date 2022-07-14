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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Search {

    public static final String PATH_TO_INDEX = "index";

    private IndexSearcher searcher;

    public Search() throws IOException
    {
        //Open the Index
        Directory indexDirectory = FSDirectory.open(Paths.get("index"));
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
            for(String notInlcuded : filter.getLogLevels())
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
                new TermQuery(new Term("className", filter.getClassName())),
                BooleanClause.Occur.MUST
            );
        }

        Query query = queryBuilder.build();

        ScoreDoc[] hits = searcher.search(query, Integer.MAX_VALUE).scoreDocs;

        List<LogEntry> results = new ArrayList<>();
        for(ScoreDoc hit : hits)
        {
            Document document = searcher.doc(hit.doc);

            results.add(new LogEntry(
                //value.get("date").numericValue().longValue() , existiert nicht wegen longpoint
                1,
                document.getField("logLevel").stringValue(),
                document.getField("module").stringValue(),
                document.getField("classname").stringValue(),
                document.getField("message").stringValue()
            ));
        }

        return results;
    }

    public static void main(String[] args) throws IOException {
        Search search = new Search();

        Filter f = Filter.builder().build();
        search.search(f).forEach(System.out::println);
    }

}
