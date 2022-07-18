package com.efficientlogfileanalysis.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class that stores server side configurations.
 * @author Jan Mandl
 */
public class Settings {
    private static final String configFileName = "efficientLogFileAnalysis.conf";
    private static Settings instance;
    
    /**
     * Stores the path to the logfile directory.
     */
    private String logFilePath;

    /**
     * The approximated max size the cache file should have. 0 means unlimited size.
     */
    private long maxSizeOfCacheFile; 

    private Settings() throws IOException {
        File confFile = new File(configFileName);
        
        if(confFile.createNewFile()) {
            setDefaultValues();
            writeConfigFile();
        } else {
            readConfigFile();
        }
    }

    /**
     * Sets default values for the configurations.
     */
    private void setDefaultValues() {
        logFilePath = 
            System.getProperty("user.home") +
            (System.getProperty("os.name").contains("win") ? "/" : "\\") + "logs";
        maxSizeOfCacheFile = 0;
    }

    /**
     * Writes the config file to the directory inside configFilePath. The format is just [variable name]=[value].
     * @throws IOException if the config file couldnt be written.
     */
    private void writeConfigFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(configFileName));

        bw.write("# lines marked with a # are comments");
        bw.write("\n");
        bw.write("# Delete this file to generate a default conf");
        bw.write("\n\n");
        bw.write("# logFilePath is the path to the folder containing the logfiles");
        bw.write("\n");
        bw.write("path_to_log_files=" + logFilePath);
        bw.write("\n");
        bw.write("# The maximum size of the created index in bytes. 0 means unlimited size.");
        bw.write("\n");
        bw.write("maxiumum_cache_file_size=" + maxSizeOfCacheFile);
        
        bw.flush();
        bw.close();
    }

    /**
     * Reads the config file from the directory inside configFilePath. The format is just [variable name]=[value]. Lines that begin with a # get skipped.
     * @throws IOException if the config file couldnt be read.
     */
    private void readConfigFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(configFileName));
        String line = "";
        String[] keyValuePair;

        setDefaultValues();

        while((line = br.readLine()) != null) {
            if(line.startsWith("#")) {
                continue;
            }

            keyValuePair = line.split("=");
            if(keyValuePair[0].equals("path_to_log_files")) {

                logFilePath = keyValuePair[1];

            } else if(keyValuePair[0].equals("maxiumum_cache_file_size")) {

                maxSizeOfCacheFile = Long.parseLong(keyValuePair[1]);

            }
        }

        br.close();
    }

    public static synchronized Settings getInstance() throws IOException {
        if(instance == null) {
            instance = new Settings();
        }

        return instance;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public long getMaxSizeOfCache() {
        return maxSizeOfCacheFile;
    }

    public void setLogFilePath(String path) throws IOException {
        logFilePath = path;
        writeConfigFile();
    }

    public void setMaxSizeOfCacheFile(long size) throws IOException {
        maxSizeOfCacheFile = size;
        writeConfigFile();
    }
}