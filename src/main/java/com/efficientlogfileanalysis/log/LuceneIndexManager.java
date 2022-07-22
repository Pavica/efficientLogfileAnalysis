package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.test.Timer;
import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.LogFile;
import com.efficientlogfileanalysis.data.Settings;

import com.efficientlogfileanalysis.util.ByteConverter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class with main method which indexes all log files.
 */
public class LuceneIndexManager {

    /**
     * The directory where the index gets created.
     */
    public static final String PATH_TO_INDEX = "index";

    /**
     * Creates the Index that is needed for Lucene to search through the log files.
     * @throws IOException
     */
    public static void createIndex() throws IOException {
        Timer timer = new Timer();

        //Create path object
        Path indexPath = Paths.get(PATH_TO_INDEX);

        //Delete previous directory
        if(indexPath.toFile().exists()) {
            Files.walk(indexPath).map(Path::toFile).forEach(File::delete);
        }

        //Open the index directory (creates the directory if it doesn't exist)
        Directory indexDirectory = FSDirectory.open(indexPath);

        //Create Analyzer object
        //The analyzer removes useless tokens ( words like a, an is etc.)
        Analyzer analyzer = new StandardAnalyzer();

        //The IndexWriter is used to create an Index
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        //Specify the Index to be written to and the config
        IndexWriter indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);

        //Check if the logfile directory exists
        if(!new File(Settings.getInstance().getLogFilePath()).exists()) {
            System.out.println("Specified log directory " + Settings.getInstance().getLogFilePath() + " does not exist");
            System.exit(-1);
        }

        //Read all the log entries from all the files into a list
        LogFile[] logFiles = LogReader.readAllLogFiles(Settings.getInstance().getLogFilePath());
        FileIDManager mgr = FileIDManager.getInstance();

        for(LogFile logfile : logFiles) {

            //save the begin and end date of each file
            short fileID = FileIDManager.getInstance().get(logfile.filename);
            LogDateManager.getInstance().setDateRange(
                fileID,
                logfile.getEntries().get(0).getTime(),
                logfile.getEntries().get(logfile.getEntries().size()-1).getTime()
            );

            for(LogEntry logEntry : logfile.getEntries()) {
                Document document = new Document();

                document.add(new LongPoint("date", logEntry.getTime()));
                document.add(new StoredField("logEntryID", logEntry.getEntryID()));
                document.add(new StringField("logLevel", logEntry.getLogLevel(), Field.Store.YES));
                document.add(new TextField("message", logEntry.getMessage(), Field.Store.YES));
                document.add(new StringField("classname", logEntry.getClassName(), Field.Store.YES));
                document.add(new StringField("module", logEntry.getModule(), Field.Store.YES));
                document.add(new StoredField("fileIndex", mgr.get(logfile.filename)));

                //add the fileIndex as a IntPoint so that lucene can search for entries in a specific file
                document.add(new IntPoint("fileIndex", mgr.get(logfile.filename)));

                //add the file index as a sortedField so that lucene can group by it
                document.add(new SortedDocValuesField("fileIndex",
                    new BytesRef(ByteConverter.shortToByte(mgr.get(logfile.filename))))
                );

                indexWriter.addDocument(document);
            }

        }

        indexWriter.close();
        LogDateManager.getInstance().writeIndex();

        System.out.println("Time elapsed: " + timer.time() + "ms");
    }

    public static void main(String[] args) throws IOException {
        createIndex();
    }
}