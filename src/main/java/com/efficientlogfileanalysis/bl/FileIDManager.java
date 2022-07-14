package com.efficientlogfileanalysis.bl;

import com.efficientlogfileanalysis.data.BiMap;

import java.io.File;

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

        for(File file : new File("test_logs").listFiles()) {
            fileInformations.putKey(file.getName(), fileInformations.size());
        }
    }

    /**
     * Looks for new logfiles and give them an ID. Also deletes old IDs of deleted logfiles.
     */
    public void update() {
        new Exception("Not implemented yet");
    }

    /**
     * Gets the ID of the logfile.
     */
    public short get(String key) {
        Integer i = fileInformations.getKey(key);
        return i == null ? null : i.shortValue();
    }

    /**
     * Gets the name of the logfile.
     */
    public String get(short key) {
        String s = fileInformations.getValue(new Integer(key));
        return s;
    }
}
