package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.LogFile;
import lombok.SneakyThrows;

import java.io.*;
import java.util.HashMap;

public class LogReader {

    private static final String REGEX_BEGINNING_OF_LOG_ENTRY = "^\\d{2} \\w{3} \\d{4}.*";

    /**
     * Reads all the files in a directory into a Logfile array.
     * @param path The path to the folder
     * @return A list containing all Logentries grouped by the file they are in
     */
    @SneakyThrows
    public static LogFile[] readAllLogFiles(String path)
    {
        File[] logFolderFileList = new File(path).listFiles();
        LogFile[] logFiles = new LogFile[logFolderFileList.length];
        short currentFileIndex = 0;

        for (File file : logFolderFileList) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            String currentLine;
            long currentByteCount = 0;

            logFiles[currentFileIndex] = new LogFile(file.getName());

            while ((currentLine = br.readLine()) != null) {

                if (line.equals("") || !currentLine.matches(REGEX_BEGINNING_OF_LOG_ENTRY)) {
                    line += currentLine + "\n";
                    continue;
                }

                LogEntry newLogEntry = new LogEntry(line);

                //add a \n for the exact bytecount
                line += "\n";
                newLogEntry.setLogFileStartOfBytes(currentByteCount);
                currentByteCount += line.getBytes().length;

                line = currentLine;
                logFiles[currentFileIndex].addEntry(newLogEntry);

            }

            ++currentFileIndex;
        }

        return logFiles;
    }

    /**
     * Creates a LogEntry object from a FileIndex and a logEntryID
     * @param path The path to the folder containing the log files
     * @param fileIndex The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log entry that has the id of the variable fileIndex inside the file with the id in logEntryID
     */
    //TODO this method is incredibly slow
    public static LogEntry getLogEntry(String path, short fileIndex, long logEntryID) {
        File logFile = new File(path + "/" + FileIDManager.getInstance().get(fileIndex));
        String line = "";

        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r"))
        {
            String tempLine = "";

            line = raf.readLine();
            raf.seek(logEntryID);

            //while there is no date at the beginning of the line
            while(
                (tempLine = raf.readLine()) != null &&
                !tempLine.matches(REGEX_BEGINNING_OF_LOG_ENTRY)
            ) {
                line += tempLine;
            }
        }
        catch (IOException ioe) {
            System.out.println(ioe);
        }

        return new LogEntry(line, logEntryID);
    }

}
