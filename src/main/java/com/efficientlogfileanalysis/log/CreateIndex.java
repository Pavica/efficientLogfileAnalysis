package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.Timer;
import com.efficientlogfileanalysis.bl.FileIDManager;
import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.Logfile;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class with main method which indexes all log files.
 */
public class CreateIndex {

    /**
     * Reads all the files in a dirctory into a Logfile array.
     * @param path The path to the folder
     * @return A list containing all Logentries grouped by the file they are in
     */
    @SneakyThrows
    public static Logfile[] readAllLogEntries(String path)
    {
        File[] logFolderFileList = new File(path).listFiles();
        Logfile[] logfiles = new Logfile[logFolderFileList.length];
        short currentFileIndex = 0;

        for (File file : logFolderFileList) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            String currentLine;
            long currentByteCount = 0;

            logfiles[currentFileIndex] = new Logfile(file.getName());

            while ((currentLine = br.readLine()) != null) {

                if (line.equals("") || !currentLine.matches("\\d{2} \\w{3} \\d{4}.*")) {
                    line += currentLine + "\n";
                    continue;
                }

                LogEntry newLogEntry = new LogEntry(line);
                
                //add a \n for the exact bytecount
                line += "\n";
                newLogEntry.setLogFileStartOfBytes(currentByteCount);
                currentByteCount += line.getBytes().length;
                
                line = currentLine;
                logfiles[currentFileIndex].addEntry(newLogEntry);

            }

            ++currentFileIndex;
        }

        return logfiles;
    }

    public static void main(String[] args) throws IOException {

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
        Logfile[] logfiles = readAllLogEntries("test_logs");
        FileIDManager mgr = FileIDManager.getInstance();

        for(Logfile logfile : logfiles) {

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