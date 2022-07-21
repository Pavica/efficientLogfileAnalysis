package com.efficientlogfileanalysis.log;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.efficientlogfileanalysis.data.Settings;

/**
 * Singleton class that manages the names and ids of the logfiles.
 * @author Andreas Kurz, Jan Mandl 
 */
public class FileIDManager extends IndexManager<Short, String> {

    private static FileIDManager instance;

    public static synchronized FileIDManager getInstance() {
        if(instance == null) {
            instance = new FileIDManager();
        }
        return instance;
    }

    private FileIDManager() {
        super(
            IndexManager.I_TypeConverter.SHORT_TYPE_CONVERTER,
            IndexManager.I_TypeConverter.STRING_TYPE_CONVERTER
        );
        
        createIndex();
    }

    public void createIndex() {
        super.createIndex();
        
        try {
            for(File file : new File(Settings.getInstance().getLogFilePath()).listFiles()) {
                values.putKey(file.getName(), (short)values.size());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Gets the ID of the logfile.
     * @return key The name of the log file
     */
    public short get(String key) {
        Short value = values.getKey(key);
        return value == null ? -1 : value.shortValue();
    }

    /**
     * Gets the name of the logfile.
     * @return key The ID of the log file
     */
    public String get(short key) {
        return values.getValue(Short.valueOf(key));
    }

    @Data
    @AllArgsConstructor
    public static class FileData
    {
        private short id;
        private String name;
    }

    public List<FileData> getLogFileData(){
        List<FileData> data = new ArrayList<>(values.size());
        
        for(short fileID : values.getKeySet())
        {
            data.add(new FileData(fileID, values.getValue(fileID)));
        }

        return data;
    }

    public static void main(String[] args) {
        File f = new File("d");
        try {
            f.createNewFile();
            try (RandomAccessFile file = new RandomAccessFile(f, "rw")) {

                //FileIDManager.getInstance().getLogFileData().forEach(System.out::println);
                //FileIDManager.getInstance().writeIndex(file);
                FileIDManager.getInstance().readIndex(file);
                FileIDManager.getInstance().getLogFileData().forEach(System.out::println);
            
            } catch (IOException e) {
                e.printStackTrace();
            }        
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
