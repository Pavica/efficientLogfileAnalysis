package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.LogFile;
import com.efficientlogfileanalysis.test.Timer;
import lombok.SneakyThrows;

import java.io.*;
import java.util.HashMap;

public class LogReader implements Closeable {

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
            long currentByteCount = -1;

            logFiles[currentFileIndex] = new LogFile(file.getName());

            while ((currentLine = br.readLine()) != null) {

                if (line.equals("") || !currentLine.matches(REGEX_BEGINNING_OF_LOG_ENTRY)) {
                    line += currentLine + "\n";
                    continue;
                }

                LogEntry newLogEntry = new LogEntry(line, currentByteCount == -1 ? 0 : currentByteCount);

                //add a \n for the exact bytecount
                line += "\n";
                currentByteCount += line.getBytes().length;

                line = currentLine;
                logFiles[currentFileIndex].addEntry(newLogEntry);

            }

            logFiles[currentFileIndex].addEntry(new LogEntry(line, currentByteCount == -1 ? 0 : currentByteCount));

            ++currentFileIndex;
        }

        return logFiles;
    }

    private HashMap<Short, RandomAccessFile> openFiles;

    public LogReader()
    {
        openFiles = new HashMap<>();
    }

    public void close() throws IOException
    {
        for( RandomAccessFile randomAccessFile : openFiles.values() )
        {
            randomAccessFile.close();
        }
    }

    /**
     * Creates a LogEntry object from a FileIndex and a logEntryID
     * @param path The path to the folder containing the log files
     * @param fileIndex The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log entry that has the id of the variable fileIndex inside the file with the id in logEntryID
     */
    //TODO this method is incredibly slow
    public LogEntry getLogEntry(String path, short fileIndex, long logEntryID) throws IOException {
        if(!openFiles.containsKey(fileIndex))
        {
            openFiles.put(fileIndex, new RandomAccessFile(path + "/" + FileIDManager.getInstance().get(fileIndex), "r"));
        }

        RandomAccessFile file = openFiles.get(fileIndex);

        String line = "";
        String tempLine = "";

        file.seek(logEntryID);
        line = file.readLine();

        //while there is no date at the beginning of the line
        while(
            (tempLine = file.readLine()) != null &&
            !tempLine.matches(REGEX_BEGINNING_OF_LOG_ENTRY)
        )
        {
            line += tempLine;
        }

        return new LogEntry(line, logEntryID);
    }

    @SneakyThrows
    public static void main(String[] args) {

        Timer timer = new Timer();

        LogReader logReader = new LogReader();

        Timer.Time time = timer.timeExecutionSpeed(() -> {
            try
            {
                logReader.getLogEntry("test_logs", (short)1, 0);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 100_000);

        System.out.println(time);
    }

}
