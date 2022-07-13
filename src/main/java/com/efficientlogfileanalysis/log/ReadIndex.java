package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.LogEntry;
import lombok.SneakyThrows;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Class with main method which tests different search queries
 */
public class ReadIndex {

    public static long convertToLong(int year, int month, int dayOfMonth, int hour, int minute, int second)
    {
        LocalDateTime time = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @SneakyThrows
    public static void main(String[] args) {

        //Create path object
        Path indexPath = Paths.get("index");

        //Open the index directory (creates the directory if it doesn't exist)
        Directory indexDirectory = FSDirectory.open(indexPath);


        //Query the index
        DirectoryReader directoryReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

//        QueryParser parser = new QueryParser("name", analyzer);
//        Query query = parser.parse("Clark");

        Query query = LongPoint.newRangeQuery("date",
            convertToLong(2022, 7, 5, 12, 53, 58),
            convertToLong(2022, 7, 5, 12, 54, 0)
        );

        //Iterate through results


        ScoreDoc[] hits = indexSearcher.search(query, Integer.MAX_VALUE).scoreDocs;

        System.out.println(hits.length);

        for(ScoreDoc hit : hits)
        {
            Document value = indexSearcher.doc(hit.doc);

//            for(IndexableField field : value.getFields())
//            {
//                System.out.println(field.name());
//            }

            LogEntry result = new LogEntry(
                //value.get("date").numericValue().longValue() , existiert nicht wegen longpoint
                1,
                value.getField("logLevel").stringValue(),
                value.getField("module").stringValue(),
                value.getField("classname").stringValue(),
                value.getField("message").stringValue()
            );


            System.out.println(result);
        }
    }

}
