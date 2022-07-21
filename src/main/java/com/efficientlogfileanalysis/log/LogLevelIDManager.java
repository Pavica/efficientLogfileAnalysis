package com.efficientlogfileanalysis.log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Singleton class that manages the log levels and their ids.
 * @author Jan Mandl 
 */
public class LogLevelIDManager extends IndexManager<Byte, String> {
    
    private static LogLevelIDManager instance;
    
    private LogLevelIDManager() {
        super(
            IndexManager.I_TypeConverter.BYTE_TYPE_CONVERTER,
            IndexManager.I_TypeConverter.STRING_TYPE_CONVERTER
        );

        //createIndex();
    }

    public static synchronized LogLevelIDManager getInstance() {
        if(instance == null) {
            instance = new LogLevelIDManager();
        }
        
        return instance;
    }
    
    protected void createIndex() {
        super.createIndex();

        for(String s : Search.allLogLevels) {
            values.putValue((byte)values.size(), s);
        }
    }

    public String get(Byte key) {
        return values.getValue(key);
    }

    public Byte get(String value) {
        return values.getKey(value);
    }

    public static void main(String[] args) {
        try {

            File d = new File("d");
            d.createNewFile();
            RandomAccessFile file = new RandomAccessFile(d, "rw");
            
            //new LogLevelIDManager().writeIndex(file);
            new LogLevelIDManager().readIndex(file);
            new LogLevelIDManager().print();

            System.out.println(
                new LogLevelIDManager().get(Byte.valueOf((byte)2))
            );
            System.out.println(
                new LogLevelIDManager().get("WARN")
            );

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}