package com.efficientlogfileanalysis.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import com.efficientlogfileanalysis.data.ConcurrentQueue;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.index.data.*;
import com.efficientlogfileanalysis.logs.data.LogEntry;
import com.efficientlogfileanalysis.logs.LogReader;
import com.efficientlogfileanalysis.logs.data.LogFileData;
import com.efficientlogfileanalysis.util.ByteConverter;
import lombok.SneakyThrows;
import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;

public class IndexManager {

    /**
     * The current state of the index
     */
    private IndexState currentState;

    private Lock stateLock = new ReentrantLock();
    private Condition stateChanged = stateLock.newCondition();
    private List<IndexStateObserver> indexStateObservers = new ArrayList<>();

    /**
     * The directory where the index gets created.
     */
    public static final Path PATH_TO_INDEX = Paths.get("index");

    private static IndexManager instance;

    //id manager
    private SerializableBiMap<Short, String> fileIDManager;
    private SerializableBiMap<Integer, String> moduleIDManager;
    private SerializableBiMap<Integer, String> classIDManager;
    private SerializableBiMap<Integer, String> exceptionIDManager;

    //additional managers for information that have nothing to do with Lucene
    private SerializableBiMap<Short, Set<Byte>> logLevelIndexManager;
    private SerializableBiMap<Short, TimeRange> logDateManager;
    private SerializableMap<Short, Long> bytesRead;

    private IndexCreator indexCreator;

    private IndexManager(){
        fileIDManager           =   new SerializableBiMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        moduleIDManager         =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        classIDManager          =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        exceptionIDManager      =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);

        logLevelIndexManager    =   new SerializableBiMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.setConverter(I_TypeConverter.BYTE_TYPE_CONVERTER));
        logDateManager          =   new SerializableBiMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.TIME_RANGE_CONVERTER);
        bytesRead               =   new SerializableMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.LONG_TYPE_CONVERTER);

        setCurrentState(IndexState.NOT_READY);
    }

    public static synchronized IndexManager getInstance() {
        if(instance == null) {
            instance = new IndexManager();
        }

        return instance;
    }

    public void saveIndices() throws IOException
    {
        String path = "index" + File.separator;
        fileIDManager.writeIndex(path + "file_id_manager");
        moduleIDManager.writeIndex(path + "module_id_manager");
        classIDManager.writeIndex(path + "class_id_manager");
        exceptionIDManager.writeIndex(path + "exception_id_manager");
        logLevelIndexManager.writeIndex(path + "logLevel_index_manager");
        logDateManager.writeIndex(path + "log_date_manager");
        bytesRead.writeIndex(path + "bytes_read");
    }

    public void readIndices() throws IOException
    {
        File indexDirectory = new File("index" + File.separator);
        File luceneDirectory = new File(indexDirectory, "lucene");

        if(!luceneDirectory.exists()) {
            throw new IOException();
        }

        String path = indexDirectory.getAbsolutePath() + File.separator;
        fileIDManager.readIndex(path + "file_id_manager");
        moduleIDManager.readIndex(path + "module_id_manager");
        classIDManager.readIndex(path + "class_id_manager");
        exceptionIDManager.readIndex(path + "exception_id_manager");
        logLevelIndexManager.readIndex(path + "logLevel_index_manager");
        logDateManager.readIndex(path + "log_date_manager");
        bytesRead.readIndex(path + "bytes_read");

        setCurrentState(IndexState.READY);
    }

    public void deleteIndex() throws IOException
    {
        //Delete previous directory
        if(PATH_TO_INDEX.toFile().exists())
        {
            try(Stream<Path> paths = Files.walk(PATH_TO_INDEX))
            {
                paths.map(Path::toFile)
                    .sorted(Comparator.comparing(File::isDirectory)) //sort so that files are deleted before their directories are
                    .forEach(File::delete);
            }
        }

        fileIDManager.clear();
        moduleIDManager.clear();
        classIDManager.clear();
        exceptionIDManager.clear();
        logLevelIndexManager.clear();
        logDateManager.clear();
        bytesRead.clear();
    }

    private synchronized void setCurrentState(IndexState state)
    {
        stateLock.lock();
        currentState = state;
        notifyIndexStateObservers(state);
        stateChanged.signalAll();
        stateLock.unlock();
    }

    /**
     * Waits until the IndexState changes and returns the new State
     * @param timeout the max amount of seconds to wait
     * @return the new IndexState or null if the timeout was reached
     * @throws InterruptedException if the threads gets interrupted during waiting
     */
    public IndexState waitForIndexStateChange(long timeout) throws InterruptedException
    {
        stateLock.lock();
        boolean timeOutReached = !stateChanged.await(timeout, TimeUnit.SECONDS);
        stateLock.unlock();

        return timeOutReached ? null : currentState;
    }

    public void attachIndexStateObserver(IndexStateObserver newObserver)
    {
        indexStateObservers.add(newObserver);
    }

    private void notifyIndexStateObservers(IndexState newState)
    {
        for(IndexStateObserver observer : indexStateObservers){
            observer.update(newState);
        }
    }

    /**
     * Convenience method that creates the lucene index in a separate thread
     */
    public void startIndexCreationWorker()
    {
        try
        {
            readIndices();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if(indexCreator == null || !indexCreator.isAlive())
        {
            indexCreator = new IndexCreator();
            indexCreator.start();
        }
        else
        {
            indexCreator.redoIndex();
        }
    }

    public void stopIndexCreationWorker()
    {
        indexCreator.shutdown();
    }

    private class IndexCreator extends Thread{

        private ConcurrentQueue<IndexCreatorTask> tasks = new ConcurrentQueue<>();
        private DirectoryWatcher fileChangeChecker;

        private boolean directoryChanged = false;
        private boolean interrupted = false;

        public void run()
        {
            fileChangeChecker = new DirectoryWatcher(Settings.getInstance().getLogFilePath(), tasks::push);
            fileChangeChecker.start();

            try
            {
                setCurrentState(IndexState.INDEXING);
                checkAllFilesForUpdates();
                setCurrentState(IndexState.READY);
            }
            catch (InterruptedException e) {interrupted = true;}
            catch (IOException e) {e.printStackTrace();}

            while(!interrupted || directoryChanged)
            {
                try
                {
                    while(directoryChanged){
                        interrupted = false;
                        System.out.println("Recreating Index");
                        directoryChanged = false;
                        setCurrentState(IndexState.INDEXING);
                        createNewIndex();
                        setCurrentState(IndexState.READY);
                    }

                    IndexCreatorTask task = tasks.pop();

                    setCurrentState(IndexState.INDEXING);
                    switch(task.getTaskType())
                    {
                        case FILE_CREATED:
                            fileCreated(task.getFilename());
                            break;
                        case FILE_APPENDED:
                            fileChanged(task.getFilename());
                            break;
                    }

                    if(tasks.isEmpty()){
                        setCurrentState(IndexState.READY);
                    }
                }
                catch (InterruptedException e) {
                    setCurrentState(IndexState.INTERRUPTED);
                    interrupted = true;
                }
                catch (IOException e){
                    //--- An error occurred --//
                    System.err.println("Everything died");
                    e.printStackTrace();
                    setCurrentState(IndexState.ERROR);
                    return;
                }
            }
        }

        private void fileCreated(String filename) throws IOException, InterruptedException {
//            System.out.println("Indexing new file: " + filename);
            updateFile(filename);
        }

        private void fileChanged(String filename) throws IOException, InterruptedException {
//            System.out.println("Indexing changes in file: " + filename);
            updateFile(filename);
        }

        public void redoIndex()
        {
            directoryChanged = true;
            this.interrupt();
        }

        public void shutdown()
        {
            this.interrupt();
            fileChangeChecker.interrupt();
        }

        private void createNewIndex()
        {
            try
            {
                deleteIndex();

                try(LuceneIndexCreator indexCreator = new LuceneIndexCreator())
                {
                    fileChangeChecker = fileChangeChecker.switchDirectory(
                            Settings.getInstance().getLogFilePath()
                    );

                    String logFolder = Settings.getInstance().getLogFilePath();
                    File[] files = LogReader.getAllLogFiles(logFolder);

                    for(File file : files)
                    {
                        indexSingleLogFile(indexCreator, file.getName());

                        if(directoryChanged){
                            return;
                        }
                    }

                    saveIndices();
                }
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }

        private void checkAllFilesForUpdates() throws IOException, InterruptedException
        {
            for(File file : LogReader.getAllLogFiles(Settings.getInstance().getLogFilePath()))
            {
                updateFile(file.getName());
            }
        }

        private void updateFile(String filename) throws IOException, InterruptedException
        {
            File file = new File(Settings.getInstance().getLogFilePath() + File.separator + filename);

            //ignore all files that don't end in .log
            if(!file.exists() || file.isDirectory() || !file.getName().endsWith(".log")){
                return;
            }

            try(LuceneIndexCreator indexCreator = new LuceneIndexCreator())
            {
                boolean shouldRetry = true;
                while(shouldRetry)
                {
                    try
                    {
                        if(!file.exists()){
                            shouldRetry = false;
                        }

                        indexSingleLogFile(indexCreator, filename);
                        shouldRetry = false;
                    }
                    catch(FileSystemException ex) {
                        System.out.println("File is currently being read by another process\nRetrying in 250ms");
                        Thread.sleep(100);
                    }
                }
            }

            saveIndices();
        }

        private void indexSingleLogFile(LuceneIndexCreator indexCreator, String filename) throws IOException
        {
            //add the file id to the index
            fileIDManager.addIfAbsent((short)fileIDManager.size(), filename);
            short fileID = fileIDManager.getKey(filename);

            bytesRead.putIfAbsent(fileID, 0L);
            long bytesIndexed = bytesRead.get(fileID);

            LogFileData fileData = LogReader.readSingleFile(
            Settings.getInstance().getLogFilePath() + File.separator + filename,
                bytesIndexed
            );

            if(fileData.getEntries().size() <= 0){
                return;
            }

            bytesRead.put(fileID, bytesIndexed + fileData.getBytesRead());

            TimeRange previousTimeRange = logDateManager.getValue(fileID);

            if(previousTimeRange == null){
                previousTimeRange = new TimeRange();
                previousTimeRange.beginDate = fileData.getEntries().get(0).getTime();
            }
            previousTimeRange.endDate = fileData.getEntries().get(fileData.getEntries().size() - 1).getTime();

            //save the beginning and end date of each file
            logDateManager.putValue(
                fileID,
                previousTimeRange
            );

            for(LogEntry logEntry : fileData.getEntries()) {
                indexLogEntry(indexCreator, logEntry, fileID);

                if(this.directoryChanged){
                    return;
                }
            }
        }

        private void indexLogEntry(LuceneIndexCreator indexCreator, LogEntry logEntry, short fileID) throws IOException
        {
            Document document = new Document();

            logLevelIndexManager.addIfAbsent(fileID, new LinkedHashSet<>());
            logLevelIndexManager.getValue(fileID).add(logEntry.getLogLevel().getId());

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

            indexCreator.getIndexWriter().addDocument(document);
        }
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
    public Set<Byte> getLogLevelsOfFile(short fileID) {
        return logLevelIndexManager.getValueOrDefault(fileID, new LinkedHashSet<>());
    }
    //----- LogLevelIndexManager -----//


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
////        IndexManager.getInstance().createLuceneIndex();
//        IndexManager.getInstance().deleteIndex();
//
//        if(true){
//            return;
//        }
//
//        Settings.getInstance().setLogFilePath("C:\\Users\\AndiK\\OneDrive\\Dokumente\\HTL 5. Jahr\\Diplomarbeit\\efficientLogfileAnalysis\\test_logs");
//        IndexManager mgr = IndexManager.getInstance();
//
//        Thread printStatus = new Thread(() -> {
//            while(true){
//                System.out.println(mgr.currentState);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//        printStatus.start();
//
//        Thread creationWorker = new Thread(mgr::createIndices);
//        creationWorker.start();
//
//        Random r = new Random();
//
//        while(true){
//            Thread.sleep(r.nextInt(8000));
//            System.out.println("Another Thread starts");
//
//            Thread newCreationWorker = new Thread(mgr::createIndices);
//            newCreationWorker.start();
//        }
    }
}
