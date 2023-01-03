package com.efficientlogfileanalysis.test;

import com.efficientlogfileanalysis.logs.data.LogEntry;
import com.efficientlogfileanalysis.logs.data.LogLevel;
import com.efficientlogfileanalysis.logs.LogReader;
import com.efficientlogfileanalysis.index.IndexManager;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.util.Timer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Class with main method which tests different search queries.
 */
public class ReadIndex {

    /**
     * Convenience function. Converts date an time to a long value in milliseconds.
     * @return The current date and time in miliseconds with precision of seconds.
     */
    public static long convertToLong(int year, int month, int dayOfMonth, int hour, int minute, int second)
    {
        LocalDateTime time = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    public static void main(String[] args) {        
        Timer timer = new Timer();

        //Create path object
        Path indexPath = IndexManager.PATH_TO_INDEX;

        //Open the index directory (creates the directory if it doesn't exist)
        IndexSearcher indexSearcher = null;
        try {
            //Query the index
            Directory indexDirectory = FSDirectory.open(indexPath);
            DirectoryReader directoryReader = DirectoryReader.open(indexDirectory);

            indexSearcher = new IndexSearcher(directoryReader);
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }

        Query dateQuery = LongPoint.newRangeQuery("date",
            convertToLong(2022, 7, 5, 12, 53, 58),
            convertToLong(2022, 7, 5, 12, 54, 0)
        );

        Query moduleQuery = new TermQuery(new Term("module", "NGKP-RCV Chl: 1, Port: 2012"));

        Query query = new BooleanQuery.Builder()
                .add(dateQuery, BooleanClause.Occur.MUST)
                .add(moduleQuery, BooleanClause.Occur.MUST)
                .build();

        ScoreDoc[] hits = null;
        try {
            hits = indexSearcher.search(query, Integer.MAX_VALUE).scoreDocs;
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }

        //print hit amount
        System.out.println(hits.length);

        //FileIDManager mgr = FileIDManager.getInstance();
        IndexManager mgr = IndexManager.getInstance();
        
        //Iterate through the search results
        try {
            for(ScoreDoc hit : hits) {
                Document value = indexSearcher.doc(hit.doc);

                LogEntry result = new LogEntry(
                    0,
                    LogLevel.valueOf(value.getField("logLevel").stringValue()),
                    value.getField("module").stringValue(),
                    value.getField("classname").stringValue(),
                    value.getField("message").stringValue(),
                    Long.parseLong(value.getField("logEntryID").stringValue())
                );

                try (LogReader logReader = new LogReader(Settings.getInstance().getLogFilePath()))
                {
                    result.setDateAsLocalDateTime(logReader.getLogEntry(
                            IndexManager.getInstance().getFileName(Short.parseShort(value.getField("fileIndex").stringValue())),
                            result.getEntryID()
                        ).retrieveDateAsLocalDateTime()
                    );
                }

                //System.out.println(mgr.get(Short.parseShort(value.getField("fileIndex").stringValue())));
                System.out.println(mgr.getFileName(Short.parseShort(value.getField("fileIndex").stringValue())));
                System.out.println(result + "\n");
            }

        } catch (IOException ioe) {
            System.out.println(ioe);
        }

        System.out.println("Time elapsed: " + timer.time() + "ms");
    }

}