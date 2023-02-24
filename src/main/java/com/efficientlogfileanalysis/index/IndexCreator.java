package com.efficientlogfileanalysis.index;

import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.index.data.TimeRange;
import com.efficientlogfileanalysis.logs.LogReader;
import com.efficientlogfileanalysis.logs.data.LogEntry;
import com.efficientlogfileanalysis.logs.data.LogFileData;
import com.efficientlogfileanalysis.util.ByteConverter;
import com.efficientlogfileanalysis.util.Timer;
import lombok.Getter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
public class IndexCreator implements Closeable {

    private static final Path LUCENE_DIRECTORY = Index.PATH_TO_INDEX.resolve("lucene");

    private Index index;

    private Directory indexDirectory;
    private Analyzer analyzer;
    private IndexWriterConfig indexWriterConfig;
    private IndexWriter indexWriter;

    public IndexCreator(Index index) throws IOException
    {
        this.index = index;

        //Open the index directory (creates the directory if it doesn't exist)
        indexDirectory = FSDirectory.open(LUCENE_DIRECTORY);

        //Create Analyzer object
        //The analyzer removes useless tokens ( words like a, an is etc.)
        analyzer = new StandardAnalyzer();

        //The IndexWriter is used to create an Index
        indexWriterConfig = new IndexWriterConfig(analyzer);

        //Specify the Index to be written to and the config
        indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
    }

    @Override
    public void close() throws IOException
    {
        indexWriter.close();
        analyzer.close();
        indexDirectory.close();
    }

    /**
     * Repeatably tries updating the file<br>
     *
     * @param file
     * @throws IOException
     * @throws InterruptedException
     */
    public void repeatablyTryAndUpdateFile(File file) throws IOException, InterruptedException
    {
        final int MAX_NUMBER_OF_RETRIES = 100;
        for(int i = 0; i < MAX_NUMBER_OF_RETRIES; i++)
        {
            try
            {
                if(!file.exists()){
                    break;
                }

                indexSingleLogFile(file.getName());
                break;
            }
            catch(FileSystemException ex) {
                System.out.println("File is currently being read by another process\nRetrying in 250ms");
                Thread.sleep(100);
            }
        }
    }

    public void indexSingleLogFile(String filename) throws IOException
    {
        //add the file id to the index
        index.fileIDManager.addIfAbsent((short)index.fileIDManager.size(), filename);
        short fileID = index.fileIDManager.getKey(filename);

        index.bytesRead.putIfAbsent(fileID, 0L);
        long bytesIndexed = index.bytesRead.get(fileID);

        LogFileData fileData = LogReader.readSingleFile(
                Settings.getInstance().getLogFilePath() + File.separator + filename,
                bytesIndexed
        );

        if(fileData.getEntries().size() <= 0){
            return;
        }

        index.bytesRead.put(fileID, bytesIndexed + fileData.getBytesRead());

        TimeRange previousTimeRange = index.logDateManager.get(fileID);

        if(previousTimeRange == null){
            previousTimeRange = new TimeRange();
            previousTimeRange.beginDate = fileData.getEntries().get(0).getTime();
        }
        previousTimeRange.endDate = fileData.getEntries().get(fileData.getEntries().size() - 1).getTime();

        //save the beginning and end date of each file
        index.logDateManager.put(
                fileID,
                previousTimeRange
        );

        for(LogEntry logEntry : fileData.getEntries()) {
            indexLogEntry(logEntry, fileID);
        }
    }

    public void indexLogEntry(LogEntry logEntry, short fileID) throws IOException
    {
        Document document = new Document();

        index.logLevelIndexManager.putIfAbsent(fileID, new LinkedHashSet<>());
        index.logLevelIndexManager.get(fileID).add(logEntry.getLogLevel().getId());

        //add the classname to the classname index
        index.classIDManager.addIfAbsent(index.classIDManager.size(), logEntry.getClassName());
        int classID = index.classIDManager.getKey(logEntry.getClassName());

        //add the module to the module index
        index.moduleIDManager.addIfAbsent(index.moduleIDManager.size(), logEntry.getModule());
        int moduleID = index.moduleIDManager.getKey(logEntry.getModule());

        Optional<String> exception = logEntry.findException();
        if(exception.isPresent())
        {
            index.exceptionIDManager.addIfAbsent(index.exceptionIDManager.size(), exception.get());
            int exceptionID = index.exceptionIDManager.getKey(exception.get());

            document.add(new IntPoint("exception", exceptionID));
        }

        document.add(new NumericDocValuesField("date", logEntry.getTime()));
        document.add(new LongPoint("date", logEntry.getTime()));
        document.add(new StoredField("logEntryID", logEntry.getEntryID()));
        document.add(new LongPoint("logLevel", logEntry.getLogLevel().getId()));
        document.add(new StoredField("logLevel", logEntry.getLogLevel().getId()));
        document.add(new TextField("message", logEntry.getMessage(), Field.Store.NO));
        document.add(new IntPoint("classname", classID));
        document.add(new IntPoint("module", moduleID));
        document.add(new StoredField("fileIndex", fileID));

        //add the fileIndex as a IntPoint so that lucene can search for entries in a specific file
        document.add(new IntPoint("fileIndex", fileID));

        //add the file index as a sortedField so that lucene can group by it
        document.add(new SortedDocValuesField(
                "fileIndex",
                new BytesRef(ByteConverter.shortToByte(fileID)))
        );
        document.add(new SortedDocValuesField(
                "logLevel",
                new BytesRef(new byte[] {
                        logEntry.getLogLevel().getId()
                })
        ));

        indexWriter.addDocument(document);
    }

    public static void main(String[] args) throws IOException {

        LogFileData file = LogReader.readSingleFile("C:\\Users\\AndiK\\3D Objects\\Diplomarbeit\\log-files\\test_logs\\DesktopClient-DEGFF-N-0165.haribo.dom.log");
        List<LogEntry> entries = file.getEntries();


        IndexCreator indexCreator = new IndexCreator(Index.getInstance());
        Timer.timeIt(() -> {
            for (LogEntry entry : entries) {
                indexCreator.indexLogEntry(entry, (short)0);
            }
        }, 20);
    }

}
