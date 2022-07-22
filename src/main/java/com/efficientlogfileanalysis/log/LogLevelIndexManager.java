package com.efficientlogfileanalysis.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores which log levels are present inside which file and which file has which log levels
 * @author Jan Mandl
 */
public class LogLevelIndexManager extends IndexManager<Short, String> {

    private static LogLevelIndexManager instance;

    private LogLevelIndexManager() {
        super(
            IndexManager.I_TypeConverter.SHORT_TYPE_CONVERTER,
            IndexManager.I_TypeConverter.STRING_TYPE_CONVERTER
        );

        createIndex();
    }

    public static synchronized LogLevelIndexManager getInstance() {
        if(instance == null) {
            instance = new LogLevelIndexManager();
        }

        return instance;
    }

    /**
     * Goes through all the log files and stores which log levels are contained
     */
    @Override
    protected void createIndex() {
        super.createIndex();

        try (Search search = new Search()) {
            
            List<byte[]> files = search.searchForLogLevelsInFiles();

            for(short i = 0;i < files.size(); ++i) {
                values.putValue(i, new String(files.get(i)));
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    protected void print() {
        String output;
        LogLevelIDManager llidm = LogLevelIDManager.getInstance();
        FileIDManager fidm = FileIDManager.getInstance();

        //super.print();
        //System.out.println("\n\n");

        for(short fileID : values.getKeySet()) {
            
            output = "FileID: " + /*fidm.get(*/fileID/*)*/ + " Log levels: ";
            
            for(byte logLevel : values.getValue(fileID).getBytes()) {
                output += llidm.get(logLevel) + "; ";
            }

            System.out.println(output);
        }
    }

    public byte[] get(short fileID) {
        return values.getValue(fileID).getBytes();
    }

    public short[] get(byte logLevelID) {
        ArrayList<Short> filesContainingLogLevel = new ArrayList<>();
        
        for(Short fileID : values.getKeySet()) {
            for(byte logLevel : values.getValue(fileID).getBytes()) {
                if(logLevel == logLevelID) {
                    filesContainingLogLevel.add(fileID);
                    break;
                }
            }
        }

        short[] byteToByteConverter = new short[filesContainingLogLevel.size()];
        for(int i = 0;i < filesContainingLogLevel.size(); ++i) {
            byteToByteConverter[i] = Short.valueOf(filesContainingLogLevel.get(i));
        }

        return byteToByteConverter;
    }

    public static void main(String[] args) {
        LogLevelIndexManager.getInstance().print();

        String s = "";
        for(byte b : LogLevelIndexManager.getInstance().get((short)23)) {
            s += LogLevelIDManager.getInstance().get(b) + "; ";
        }
        System.out.println();
        System.out.println();
        System.out.println(s);

        s = "";
        for(short value : LogLevelIndexManager.getInstance().get(LogLevelIDManager.getInstance().get("FATAL"))) {
            s += value + "\n";
        }
        System.out.println();
        System.out.println();
        System.out.println(s);
    }
}
