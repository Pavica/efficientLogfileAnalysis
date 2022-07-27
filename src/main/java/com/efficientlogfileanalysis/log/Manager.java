package com.efficientlogfileanalysis.log;

import java.io.IOException;
import java.util.List;

public class Manager {
    /**
     * The directory where the index gets created.
     */
    public static final String PATH_TO_INDEX = "./index";

    private static Manager instance;
    
    private FileIDManager fidm;
    private LogDateManager ldm;
    private LogLevelIDManager llidm;
    private LogLevelIndexManager llim;

    private Manager(){
        fidm = new FileIDManager();
        ldm = new LogDateManager();
        llidm = new LogLevelIDManager();
        llim = new LogLevelIndexManager();
    }

    public static synchronized Manager getInstance() {
        if(instance == null) {
            instance = new Manager();
        }

        return instance;
    }

    public void createIndices() throws IOException {
        LuceneIndexManager.createIndex();
        fidm.createIndex();
        ldm.createIndex();
        llidm.createIndex();
        //muss hinter llidm sein
        llim.createIndex();
    }

    public short getFileID(String fileName) {
        return fidm.get(fileName);
    }

    public String getFileName(short fileID) {
        //TODO: does ./index work on windows?
        return fidm.get(fileID);
    }
    
    public LogDateManager.TimeRange getLogFileDateRange(short fileID) {
        return ldm.get(fileID);
    }

    public void setDateRange(short fileID, long begin, long end) {
        ldm.setDateRange(fileID, begin, end);
    }

    public byte getLogLevelID(String logLevel) {
        return llidm.get(logLevel);
    }
    
    public String getLogLevelName(byte logLevelID) {
        return llidm.get(logLevelID);
    }

    public byte[] getLogLevel(short fileID) {
        return llim.get(fileID);
    }
    
    public short[] getLogLevelFiles(byte logLevelID) {
        return llim.get(logLevelID);
    }

    public FileIDManager getFileIDManager() {
        return fidm;
    }

    public List<FileIDManager.FileData> getFileIDLogFileData() {
        return fidm.getLogFileData();
    }

    public static void main(String[] args) {

        Manager mgr = new Manager();
        try {
            mgr.createIndices();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
