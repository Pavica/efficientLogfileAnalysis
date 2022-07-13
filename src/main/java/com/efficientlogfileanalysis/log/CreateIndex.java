package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.bl.FileIDManager;
import com.efficientlogfileanalysis.data.LogEntry;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with main method which indexes all log files.
 */
public class CreateIndex {

    /**
     * Reads all the Logentries from a directory.
     * @param path The path to the folder
     * @return A list containing all LogEntries from a directory with Logfiles
     */
    @SneakyThrows
    public static List<LogEntry> readAllLogEntries(String path)
    {
        List<LogEntry> logEntries = new ArrayList<>();

        File logFolder = new File(path);

        for (File file : logFolder.listFiles())
        {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line = "";
            String currentLine;

            while ((currentLine = br.readLine()) != null) {

                if (line.equals("") || !currentLine.matches("\\d{2} \\w{3} \\d{4}.*")) {
                    line += currentLine + "\n";
                    continue;
                }

                LogEntry newLogEntry = new LogEntry(line);
                line = currentLine;

                //temporary
                logEntries.add(newLogEntry);
            }
        }

        return logEntries;
    }

    public static void main(String[] args) throws IOException {
        //Delete the previous index
        new File("index").delete();

        //Create path object
        Path indexPath = Paths.get("index");

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
        List<LogEntry> logEntries = readAllLogEntries("test_logs");
        FileIDManager mgr = FileIDManager.getInstance();

        for(LogEntry logEntry : logEntries)
        {
            Document document = new Document();
            
            document.add(new LongPoint("date", logEntry.getTime()));
            document.add(new StringField("logLevel", logEntry.getLogLevel(), Field.Store.YES));
            document.add(new TextField("message", logEntry.getMessage(), Field.Store.YES));
            document.add(new StringField("classname", logEntry.getClassName(), Field.Store.YES));
            document.add(new StringField("module", logEntry.getModule(), Field.Store.YES));
            //machen das referenz azf fiel gmeacht wird
            //document.add();
            
            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }
}