package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.BiMap;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class that manages the names and ids of the logfiles.
 * @author Andreas Kurz, Jan Mandl 
 */
public class FileIDManager {

    private static FileIDManager instance;
    
    private BiMap<Short, String> fileInformations;

    public static synchronized FileIDManager getInstance() {
        if(instance == null) {
            instance = new FileIDManager();
        }
        return instance;
    }

    private FileIDManager() {
        fileInformations = new BiMap<>();

        for(File file : new File("test_logs").listFiles()) {
            fileInformations.putKey(file.getName(), (short) fileInformations.size());
        }
    }

    /**
     * Looks for new logfiles and give them an ID. Also deletes old IDs of deleted logfiles.
     */
    public void update() {
        new IOException("Not implemented yet");
    }

    /**
     * Gets the ID of the logfile.
     * @return key The name of the log file
     */
    public short get(String key) {
        Short value = fileInformations.getKey(key);
        return value == null ? -1 : value.shortValue();
    }

    /**
     * Gets the name of the logfile.
     * @return key The ID of the log file
     */
    public String get(short key) {
        return fileInformations.getValue(Short.valueOf(key));
    }

    @Data
    @AllArgsConstructor
    public static class FileData
    {
        private short id;
        private String name;
    }

    public List<FileData> getLogFileData(){
        List<FileData> data = new ArrayList<>(fileInformations.size());

        for(short fileID : fileInformations.getKeySet())
        {
            data.add(new FileData(fileID, fileInformations.getValue(fileID)));
        }

        return data;
    }
}
