package com.efficientlogfileanalysis.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.efficientlogfileanalysis.data.*;
import com.efficientlogfileanalysis.test.Timer;
import com.efficientlogfileanalysis.util.ByteConverter;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class IndexManager {
    private enum IndexState {
        // index creation wasnt started yet or interrupted
        NOT_READY,
        // the index is ready for use
        READY,
        // when a file couldnt be read
        ERROR,
        // the index is currently being build
        INDEXING,
        // index creation was interrupted but is still running
        INTERRUPTED
    }
    

    /**
     * The current state of the index
     */
    private IndexState currentState;
    private Lock indexCreation;
    private Condition indexCreationCondition;
    private boolean isThreadWaiting;
    
    /**
     * The directory where the index gets created.
     */
    public static final String PATH_TO_INDEX = "index";

    private static IndexManager instance;

    //id manager
    private SerializableBiMap<Short, String> fileIDManager;
    private SerializableBiMap<Integer, String> moduleIDManager;
    private SerializableBiMap<Integer, String> classIDManager;
    private SerializableBiMap<Integer, String> exceptionIDManager;
    private SerializableBiMap<Byte, String> logLevelIDManager;

    //additional managers for informations that have nothing to do with Lucene
    private SerializableBiMap<Short, List<Byte>> logLevelIndexManager;
    private SerializableBiMap<Short, TimeRange> logDateManager;

    private IndexManager(){
        fileIDManager           =   new SerializableBiMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        moduleIDManager         =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        classIDManager          =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        exceptionIDManager      =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        logLevelIDManager       =   new SerializableBiMap<>(I_TypeConverter.BYTE_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);

        logLevelIndexManager    =   new SerializableBiMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.listConverter(I_TypeConverter.BYTE_TYPE_CONVERTER));
        logDateManager          =   new SerializableBiMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.TIME_RANGE_CONVERTER);

        currentState = IndexState.NOT_READY;
        indexCreation = new ReentrantLock();
        indexCreationCondition = indexCreation.newCondition();
        isThreadWaiting = false;
    }

    public static synchronized IndexManager getInstance() {
        if(instance == null) {
            instance = new IndexManager();
        }

        return instance;
    }

    /**
     * Builds all of the required indices
     * @throws IOException When the files cant be created or written to
     */
    public void createIndices() throws IOException {
        indexCreation.lock();

        if(currentState == IndexState.INTERRUPTED) {
            indexCreation.unlock();
            return;
        }

        //waits for the indexing procedure to end (only 1 thread waits here)
        if(currentState == IndexState.INDEXING) {
            currentState = IndexState.INTERRUPTED;
            try {
                indexCreationCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        indexCreationWorker();

        if(currentState == IndexState.ERROR) {
            throw new IOException();
        }

        //restarts the index creation (if there is already a thread waiting)
        if(currentState == IndexState.INTERRUPTED) {
            indexCreationCondition.signal();
        }
        indexCreation.unlock();
    }

    private void indexCreationWorker() throws IOException {
        currentState = IndexState.INDEXING;
        indexCreation.unlock();
         
        Timer t = new Timer();

        //Check if the logfile directory exists
        if(!new File(Settings.getInstance().getLogFilePath()).exists()) {
            System.out.println("Specified log directory " + Settings.getInstance().getLogFilePath() + " does not exist");
            indexCreation.lock();
            currentState = IndexState.ERROR;
            return;
        }

        //create log level ID index
        for(String s : Search.allLogLevels) {
            logLevelIDManager.putValue((byte)logLevelIDManager.size(), s);
        }
        
        indexCreation.lock();
        //restart indexing if interrupted
        if(currentState == IndexState.INTERRUPTED) {
            return;
        }
        indexCreation.unlock();

        createLuceneIndex();

        indexCreation.lock();
        //restart indexing if interrupted
        if(currentState == IndexState.INTERRUPTED) {
            return;
        }
        indexCreation.unlock();

        //create logLevel index
        try (Search search = new Search())
        {
            //create log level index
            List<List<Byte>> files = search.searchForLogLevelsInFiles();
            for(short i = 0;i < files.size(); ++i)
            {
                logLevelIndexManager.putValue(i, files.get(i));
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            indexCreation.lock();
            currentState = IndexState.ERROR;
            return;
        }

        System.out.println(t);
        indexCreation.lock();
        currentState = IndexState.READY;
        return;
    }

    public void saveIndices() throws IOException
    {
        String path = "index" + File.separator;
        fileIDManager.writeIndex(path + "file_id_manager");
        moduleIDManager.writeIndex(path + "module_id_manager");
        classIDManager.writeIndex(path + "class_id_manager");
        logLevelIDManager.writeIndex(path + "log_level_id_manager");
        exceptionIDManager.writeIndex(path + "exception_id_manager");
        logLevelIndexManager.writeIndex(path + "logLevel_index_manager");
        logDateManager.writeIndex(path + "log_date_manager");
    }

    public void readIndices() throws IOException
    {
        String path = "index" + File.separator;
        fileIDManager.readIndex(path + "file_id_manager");
        moduleIDManager.readIndex(path + "module_id_manager");
        classIDManager.readIndex(path + "class_id_manager");
        logLevelIDManager.readIndex(path + "log_level_id_manager");
        exceptionIDManager.readIndex(path + "exception_id_manager");
        logLevelIndexManager.readIndex(path + "logLevel_index_manager");
        logDateManager.readIndex(path + "log_date_manager");
    }


    /**
     * Creates the Lucene index, the logDate index, the module index, the file index and the class index.
     * @throws IOException
     */
    private void createLuceneIndex() throws IOException
    {
        //Create path object
        Path indexPath = Paths.get(IndexManager.PATH_TO_INDEX + "/" + "lucene");

        //Delete previous directory
        if(indexPath.toFile().exists())
        {
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

        //Read all the log entries from all the files into a list
        LogFile[] logFiles = LogReader.readAllLogFiles(Settings.getInstance().getLogFilePath());

        for(LogFile logfile : logFiles)
        {
            //add the file id to the index
            fileIDManager.addIfAbsent((short)fileIDManager.size(), logfile.filename);
            short fileID = fileIDManager.getKey(logfile.filename);

            //save the begin and end date of each file
            logDateManager.putValue(
                fileID,
                new TimeRange(
                    logfile.getEntries().get(0).getTime(),
                    logfile.getEntries().get(logfile.getEntries().size()-1).getTime()
                )
            );

            for(LogEntry logEntry : logfile.getEntries()) {
                Document document = new Document();

                //add the classname to the classname index
                classIDManager.addIfAbsent(classIDManager.size(), logEntry.getClassName());
                int classID = classIDManager.getKey(logEntry.getClassName());

                //add the module to the module index
                moduleIDManager.addIfAbsent(moduleIDManager.size(), logEntry.getModule());
                int moduleID = moduleIDManager.getKey(logEntry.getModule());

                Optional<String> exception = logEntry.findException();
                if(exception.isPresent())
                {
                    exceptionIDManager.addIfAbsent(exceptionIDManager.size(), exception.get());
                    int exceptionID = exceptionIDManager.getKey(exception.get());

                    document.add(new IntPoint("exception", exceptionID));
                }

                document.add(new NumericDocValuesField("date", logEntry.getTime()));
                document.add(new LongPoint("date", logEntry.getTime()));
                document.add(new StoredField("logEntryID", logEntry.getEntryID()));
                document.add(new LongPoint("logLevel", logLevelIDManager.getKey(logEntry.getLogLevel())));
                document.add(new StoredField("logLevel", logLevelIDManager.getKey(logEntry.getLogLevel())));
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
                            logLevelIDManager.getKey(logEntry.getLogLevel())
                        })
                ));

                indexWriter.addDocument(document);

                if(currentState == IndexState.INTERRUPTED) {
                    indexWriter.close();
                    return;
                }
            }

        }

        indexWriter.close();
    }

    public IndexState getIndexState() {
        return currentState;
    }

    public boolean exists() {
        return currentState == IndexState.READY;
    }

    //----- fileIDManager ----//
    public String getFileName(short fileIndex) {
        return fileIDManager.getValue(fileIndex);
    }

    public short getFileID(String fileName) {
        return fileIDManager.getKey(fileName);
    }

    public Set<Short> getFileIDs() {
        return fileIDManager.getKeySet();
    }

    public BiMap<Short, String> getFileData() {
        return fileIDManager;
    }
    //----- fileIDManager ----//


    public TimeRange getLogFileDateRange(short fileID) {
        return logDateManager.getValue(fileID);
    }

    //----- LogLevelIndexManager -----//
    public List<Byte> getLogLevelsOfFile(short fileID) {
        return logLevelIndexManager.getValue(fileID);
    }
    //----- LogLevelIndexManager -----//


    //----- LogLevelIDManager -----//
    public String getLogLevelName(byte logLevelID) {
        return logLevelIDManager.getValue(logLevelID);
    }

    public byte getLogLevelID(String s) {
        return logLevelIDManager.getKey(s);
    }
    //----- LogLevelIDManager -----//


    //----- ModuleIDManager -----//
    public int getModuleID(String module) {
        return moduleIDManager.getKey(module);
    }

    public Set<String> getModuleNames() {
        return moduleIDManager.getValueSet();
    }
    //----- ModuleIDManager -----//


    //----- ClassIDManager -----//
    public int getExceptionID(String exceptionName) {
        return exceptionIDManager.getKey(exceptionName);
    }

    public Set<String> getExceptionNames() {
        return exceptionIDManager.getValueSet();
    }
    //----- ClassIDManager -----//


    //----- ClassIDManager -----//
    public int getClassID(String className) {
        return classIDManager.getKey(className);
    }

    public Set<String> getClassNames() {
        return classIDManager.getValueSet();
    }
    //----- ClassIDManager -----//

    @SneakyThrows
    public static void main(String[] args)
    {
        Random rand = new Random(1000);
        IndexManager mgr = IndexManager.getInstance();
        //mgr.readIndices();
        //mgr.createIndices();
        //mgr.saveIndices();

        //mgr.getExceptionNames().forEach(System.out::println);

        //mgr.readIndices();
        //System.out.println(mgr.moduleIDManager);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0;i < 2048; ++i) {
                    System.out.println(currentState);
                    try {
                        Thread.sleep(rand.nextInt(1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        for(int i = 0;i < 128; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try { 
                        mgr.createIndices();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            Thread.sleep(rand.nextInt(1000));
        }
    }
}
