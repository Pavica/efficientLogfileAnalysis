package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.test.Timer;
import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.LogFile;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class with main method which indexes all log files.
 */
public class LuceneIndexManager {

    public static void createIndex() throws IOException {
        Timer timer = new Timer();

        //Create path object
        Path indexPath = Paths.get("index");

        //Delete previous directory
        Files.walk(indexPath).map(Path::toFile).forEach(File::delete);

        //Open the index directory (creates the directory if it doesn't exist)
        Directory indexDirectory = FSDirectory.open(indexPath);

        //Create Analyzer object
        //The analyzer removes useless tokens ( words like a, an is etc.)
        Analyzer analyzer = new StandardAnalyzer();

        //The IndexWriter is used to create an Index
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        //Specify the Index to be written to and the config
        IndexWriter indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);

        //Read all the log entries from all the files into a list
        LogFile[] logFiles = LogReader.readAllLogFiles("test_logs");
        FileIDManager mgr = FileIDManager.getInstance();

        for(LogFile logfile : logFiles) {

            for(LogEntry logEntry : logfile.getEntries()) {
                Document document = new Document();

                document.add(new LongPoint("date", logEntry.getTime()));
                document.add(new StoredField("logEntryID", logEntry.getLogFileStartOfBytes()));
                document.add(new StringField("logLevel", logEntry.getLogLevel(), Field.Store.YES));
                document.add(new TextField("message", logEntry.getMessage(), Field.Store.YES));
                document.add(new StringField("classname", logEntry.getClassName(), Field.Store.YES));
                document.add(new StringField("module", logEntry.getModule(), Field.Store.YES));
                document.add(new StoredField("fileIndex", mgr.get(logfile.filename)));

                indexWriter.addDocument(document);
            }

        }

        indexWriter.close();

        System.out.println("Time elapsed: " + timer.time() + "ms");
    }
}