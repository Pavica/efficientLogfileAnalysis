package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.BiMap;
import com.efficientlogfileanalysis.data.Settings;

import java.io.File;
import java.io.IOException;

/**
 * Singleton class that manages the names and ids of the logfiles.
 * @author Andreas Kurz, Jan Mandl 
 */
public class FileIDManager {

    private static FileIDManager instance;
    
    private BiMap<Integer, String> fileInformations;

    public static synchronized FileIDManager getInstance() {
        if(instance == null) {
            instance = new FileIDManager();
        }
        return instance;
    }

    private FileIDManager() {
        fileInformations = new BiMap<>();

        try {
            for(File file : new File(Settings.getInstance().getLogFilePath()).listFiles()) {
                fileInformations.putKey(file.getName(), fileInformations.size());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
        Integer i = fileInformations.getKey(key);
        return i == null ? null : i.shortValue();
    }

    /**
     * Gets the name of the logfile.
     * @return key The ID of the log file
     */
    public String get(short key) {
        return fileInformations.getValue(Integer.valueOf(key));
    }
}
