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

import com.efficientlogfileanalysis.index.data.*;
import lombok.Getter;

public class Index {

    /**
     * The directory where the index gets created.
     */
    public static final Path PATH_TO_INDEX = Paths.get("index");

    private static Index instance;

    /**
     * The current state of the index
     */
    @Getter
    private IndexState currentState;

    private final Lock stateLock = new ReentrantLock();
    private final Condition stateChanged = stateLock.newCondition();
    private List<IndexStateObserver> indexStateObservers = new ArrayList<>();

    //id manager
    SerializableBiMap<Short, String> fileIDManager;
    SerializableBiMap<Integer, String> moduleIDManager;
    SerializableBiMap<Integer, String> classIDManager;
    SerializableBiMap<Integer, String> exceptionIDManager;

    //additional managers for information that have nothing to do with Lucene
    SerializableMap<Short, Set<Byte>> logLevelIndexManager;
    SerializableMap<Short, TimeRange> logDateManager;
    SerializableMap<Short, Long> bytesRead;

    private IndexCreationWorker indexCreator;

    private Index(){
        fileIDManager           =   new SerializableBiMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        moduleIDManager         =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        classIDManager          =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);
        exceptionIDManager      =   new SerializableBiMap<>(I_TypeConverter.INTEGER_TYPE_CONVERTER, I_TypeConverter.STRING_TYPE_CONVERTER);

        logLevelIndexManager    =   new SerializableMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.setConverter(I_TypeConverter.BYTE_TYPE_CONVERTER));
        logDateManager          =   new SerializableMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.TIME_RANGE_CONVERTER);
        bytesRead               =   new SerializableMap<>(I_TypeConverter.SHORT_TYPE_CONVERTER, I_TypeConverter.LONG_TYPE_CONVERTER);

        setCurrentState(IndexState.NOT_READY);
    }

    public static synchronized Index getInstance() {
        if(instance == null) {
            instance = new Index();
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

    void setCurrentState(IndexState state)
    {
        stateLock.lock();
        if(currentState != state)
        {
            currentState = state;
            notifyIndexStateObservers(state);
            stateChanged.signalAll();
        }
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
     * Starts the Index Creation worker in a separate thread<br>
     * Does nothing if a IndexCreator is already active
     */
    public void startIndexCreationWorker()
    {
        if(indexCreator == null || !indexCreator.isAlive())
        {
            indexCreator = new IndexCreationWorker(this);
            indexCreator.start();
        }
    }

    public void stopIndexCreationWorker()
    {
        indexCreator.shutdown();
    }

    /**
     * Method that deletes the Index and recreates it in a separate thread
     */
    public void redoIndex()
    {
        startIndexCreationWorker();
        indexCreator.redoIndex();
    }


    //----- GETTER -----//


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
        return logDateManager.get(fileID);
    }

    //----- LogLevelIndexManager -----//
    public Set<Byte> getLogLevelsOfFile(short fileID) {
        return logLevelIndexManager.getOrDefault(fileID, new LinkedHashSet<>());
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
}
