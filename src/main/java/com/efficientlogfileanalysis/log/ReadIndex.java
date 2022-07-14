package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.Timer;
import com.efficientlogfileanalysis.bl.FileIDManager;
import com.efficientlogfileanalysis.data.LogEntry;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /**
     * Creates a Logentry object from a FileIndex and a logentryID
     * @param path The path to the folder containing the log files
     * @param fileIndex The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log entry that has the id of the variable fileIndex inside the file with the id in logEntryID
     */
    private static LogEntry getLogEntry(String path, short fileIndex, long logEntryID) {
        File logFile = new File(path + "/" + FileIDManager.getInstance().get(fileIndex));
        String line = "";

        try {
            RandomAccessFile raf = new RandomAccessFile(logFile, "r");
            //todo: use Stringbuilder
            String tempLine = "";

            line = raf.readLine();
            raf.seek(logEntryID);
            
            //while there is no date at the beginning of the line
            while(
                (tempLine = raf.readLine()) != null &&
                !tempLine.matches("\\d{2} \\w{3} \\d{4}.*")
            ) {
                line += tempLine;
            }

        } catch (IOException ioe) {
            System.out.println(ioe);
        }
        
        return new LogEntry(line);
    }
    
    public static void main(String[] args) {        
        Timer timer = new Timer();

        //Create path object
        Path indexPath = Paths.get("index");

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

        Query query = LongPoint.newRangeQuery("date",
            convertToLong(2022, 7, 5, 12, 53, 58),
            convertToLong(2022, 7, 5, 12, 54, 0)
        );

        ScoreDoc[] hits = null;
        try {
            hits = indexSearcher.search(query, Integer.MAX_VALUE).scoreDocs;
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }

        //print hit amount
        System.out.println(hits.length);

        FileIDManager mgr = FileIDManager.getInstance();

        //Iterate through the search results
        try {
            for(ScoreDoc hit : hits) {
                Document value = indexSearcher.doc(hit.doc);

                LogEntry result = new LogEntry(
                    0,
                    value.getField("logLevel").stringValue(),
                    value.getField("module").stringValue(),
                    value.getField("classname").stringValue(),
                    value.getField("message").stringValue(),
                    Long.parseLong(value.getField("logEntryID").stringValue())
                );

                result.setLocalDateTime(getLogEntry(
                        "test_logs",
                        Short.parseShort(value.getField("fileIndex").stringValue()),
                        result.getLogFileStartOfBytes()
                    ).getLocalDateTime()
                );

                System.out.println(mgr.get(Short.parseShort(value.getField("fileIndex").stringValue())));
                System.out.println(result + "\n");
            }

        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }

        System.out.println("Time elapsed: " + timer.time() + "ms");
    }

}