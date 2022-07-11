package com.efficientlogfileanalysis.bl;

import java.io.File;
import java.util.HashMap;

public class FileIDManager {

    private final HashMap<String, Integer> fileIDs;

    private static FileIDManager instance;

    public static synchronized FileIDManager getInstance()
    {
        if(instance == null)
        {
            instance = new FileIDManager();
        }

        return instance;
    }

    private FileIDManager()
    {
        fileIDs = new HashMap<>();

        for( File file : new File("test_logs").listFiles() )
        {
            fileIDs.put(file.getName(), fileIDs.size());
        }
    }

    public int getFileID(String filename)
    {
        return fileIDs.getOrDefault(filename, -1);
    }
}
