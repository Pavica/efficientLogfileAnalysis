package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.LogFile;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.test.Timer;
import lombok.SneakyThrows;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class with methods for reading informations out of logfiles.
 * @author Andreas Kurz, Jan Mandl
 */
public class LogReader implements Closeable {

    //TODO recompiling the regex should speed up indexing time
    private static final String REGEX_BEGINNING_OF_LOG_ENTRY = "^\\d{2} \\w{3} \\d{4}.*";
    private static final Matcher REGEX_MATCHER_BEGINNING_OF_LOG_ENTRY = Pattern.compile(REGEX_BEGINNING_OF_LOG_ENTRY).matcher("");

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

                if (line.equals("") || !REGEX_MATCHER_BEGINNING_OF_LOG_ENTRY.reset(currentLine).matches()) {
//                if (line.equals("") || !currentLine.matches(REGEX_BEGINNING_OF_LOG_ENTRY)) {
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

    /**
     * Should be called after using a lot of methods inside this class for multiple times. This is so that the openend files can be closed which improves performance.
     * @throws IOException
     */
    public void close() throws IOException
    {
        for( RandomAccessFile randomAccessFile : openFiles.values() )
        {
            randomAccessFile.close();
        }

        openFiles.clear();
    }

    /**
     * Convenience method. If the file isnt opened inside a RandomAccessFile Object yet, it gets openend.
     * @param path The path to the log files 
     * @param fileIndex The index of the file that should get checked.
     * @throws IOException
     */
    private void prepareFile(String path, short fileIndex) throws IOException {
        if(!openFiles.containsKey(fileIndex))
        {
            openFiles.put(
                fileIndex,
                new RandomAccessFile(path + "/" + FileIDManager.getInstance().get(fileIndex),"r")
            );
        }
    }

    /**
     * Convenience method. Prepares a RandomAccessFile object to be ready to be read from.
     * @param path The path to the folder containing the log files
     * @param fileIndex The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return a RandomAccessFile whose FilePointer is right before the logEntry
     * @throws IOException if the log directory can't be accessed
     */
    private RandomAccessFile prepareRandomAccessFile(
        String path,
        short fileIndex,
        long logEntryID
    ) throws IOException {

        prepareFile(path, fileIndex);
        
        RandomAccessFile file = openFiles.get(fileIndex);
        file.seek(logEntryID);
        
        return file;
    }

    /**
     * Creates a LogEntry object from a FileIndex and a logEntryID
     * @param path The path to the folder containing the log files
     * @param fileIndex The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log entry that has the id of the variable fileIndex inside the file with the id in logEntryID
     * @throws IOException if the log directory can't be accessed
     */
    public LogEntry getLogEntry(String path, short fileIndex, long logEntryID) throws IOException {
        RandomAccessFile file = prepareRandomAccessFile(path, fileIndex, logEntryID);

        String line = "";
        String tempLine = "";

        line = file.readLine();

        //while there is no date at the beginning of the line
        while(
            (tempLine = file.readLine()) != null &&
            !REGEX_MATCHER_BEGINNING_OF_LOG_ENTRY.reset(tempLine).matches()
        )
        {
            line += tempLine;
        }

        return new LogEntry(line, logEntryID);
    }

    /**
     * Creates a LogEntry object without the message from a FileIndex and a logEntryID
     * @param path The path to the folder containing the log files
     * @param fileIndex The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log entry that has the id of the variable fileIndex inside the file with the id in logEntryID<br>The message is null.
     * @throws IOException if the log directory can't be accessed
     */
    public LogEntry readLogEntryWithoutMessage(String path, short fileIndex, long logEntryID) throws IOException {
        RandomAccessFile file = prepareRandomAccessFile(path, fileIndex, logEntryID);
        StringBuilder stringBuilder = new StringBuilder("");

        LogEntry logEntry = new LogEntry();
        logEntry.setEntryID(logEntryID);

        //Read the date
        byte[] bytes = new byte[24];
        file.read(bytes);
        logEntry.setDateFromString(new String(bytes));

        //Read the log level
        file.readFully(bytes, 0, 7);
        logEntry.setLogLevel(new String(bytes, 0, 7).trim());

        //--- Read the module ---//
        //skip the first square bracket
        file.skipBytes(1);

        //read the contents of the square bracket (the module)
        boolean stillReading = true;
        int character;
        while(stillReading)
        {
            character = file.read();

            switch(character)
            {
                case ']':
                case -1:
                    stillReading = false;
                    break;

                default:
                    stringBuilder.append((char)character);
            }
        }

        logEntry.setModule(stringBuilder.toString());

        //--- Read the class ---//
        //skip the whitespace after the bracket
        file.skipBytes(1);
        //clear the old string buffer (is faster than newStringBuilder)
        stringBuilder.setLength(0);

        int previousCharacter = ' ';
        stillReading = true;
        while(stillReading)
        {
            character = file.read();

            switch (character)
            {
                case -1:
                    stillReading = false;
                    break;

                case '?':
                    if(previousCharacter == ':')
                    {
                        stillReading = false;
                        break;
                    }
                default :
                    previousCharacter = character;
                    stringBuilder.append((char)character);
                    break;
            }
        }

        //set the class name as the stringBuilder content without the last character (the last character is always :)
        logEntry.setClassName(stringBuilder.substring(0, stringBuilder.length()-1));

        return logEntry;
    }
    
    /**
     * Reads the date of the specified entry and returns it in miliseconds.
     * @param path The path to the folder containing the log files
     * @param fileIndex The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return The time at which the entry was logged
     * @throws IOException if the log directory can't be accessed
     */
    public long readDateOfEntry(String path, short fileIndex, long logEntryID) throws IOException {
        RandomAccessFile file = prepareRandomAccessFile(path, fileIndex, logEntryID);
        
        byte[] bytes = new byte[24];
        file.read(bytes);

        long milliseconds = LocalDateTime.parse(
            new String(bytes),
            LogEntry.DTF
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return milliseconds;
    }

    /**
     * Reads the log level of the specified entry and returns it.
     * @param path The path to the folder containing the log files
     * @param fileIndex The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log level of the entry as a String object
     * @throws IOException if the log directory can't be accessed
     */
    public String readLogLevelOfEntry(String path, short fileIndex, long logEntryID) throws IOException {
        prepareFile(path, fileIndex);
        
        RandomAccessFile file = openFiles.get(fileIndex);
        byte[] bytes = new byte[7];
        
        file.seek(logEntryID + 24);
        file.read(bytes);

        return new String(bytes).trim();
    }

    @SneakyThrows
    public static void main(String[] args) {

//        List<String> result = new ArrayList<>();
//        for(LogFile logFile : LogReader.readAllLogFiles("test_logs"))
//        {
//            logFile.getEntries().stream().map(LogEntry::getLogLevel).forEach(level -> {
//                if(!result.contains(level))
//                {
//                    result.add(level);
//                }
//            });
//        }
//
//        result.forEach(System.out::println);

//        LogReader reader = new LogReader();
//
//        System.out.println(reader.readLogEntryWithoutMessage(Settings.getInstance().getLogFilePath(), (short) 0, 142_566l));
//
//        Timer timer = new Timer();
//
//        short fileID = FileIDManager.getInstance().get("DesktopClient-My-User-PC.mshome.net.log");
//        String path = Settings.getInstance().getLogFilePath();
//
//        Timer.Time time = timer.timeExecutionSpeed(() -> {
//            try
//            {
//                reader.getLogEntry(path, fileID, 0l);
//            }
//            catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }, 100_000);
//
//        System.out.println(time);


//        ArrayList<LogEntry> sno = new ArrayList<>();
//        ArrayList<LogEntry> sho = new ArrayList<>();
//
//        int N_TIMES = 2;
//        sno.ensureCapacity(N_TIMES);
//        sho.ensureCapacity(N_TIMES);
        
        
        
        /*
        Timer timer = new Timer();

        LogReader logReader = new LogReader();
        Timer.Time time = timer.timeExecutionSpeed(() -> {
            
            try {
                long date = logReader.readDateOfEntry(
                    Settings.getInstance().getLogFilePath(),
                    (short)1,
                    0
                );
                String loglevel = logReader.readLogLevelOfEntry(
                    Settings.getInstance().getLogFilePath(),
                    (short)1,
                    0
                );

                LogEntry l = new LogEntry();
                l.setTime(date);
                l.setLogLevel(loglevel);
                sno.add(l);

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }, N_TIMES);
        logReader.close();
        System.out.println("My function: " + time);
        time = timer.timeExecutionSpeed(() -> {
            
            try {
                LogEntry le = logReader.getLogEntry(
                    Settings.getInstance().getLogFilePath(),
                    (short)1,
                    0
                );
                long date = le.getTime();
                String loglevel = le.getLogLevel();

                LogEntry l = new LogEntry();
                l.setTime(date);
                l.setLogLevel(loglevel);
                sho.add(l);

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }, N_TIMES);
        logReader.close();

        System.out.println("Done");
        for (int i = 0;i < N_TIMES; ++i) {
            if(
                !sno.get(i).getLogLevel().equals(sho.get(i).getLogLevel()) ||
                sno.get(i).getTime() != sho.get(i).getTime()
            ) {
                System.out.println(sno.get(i).getLogLevel() + "\n" + sho.get(i).getLogLevel());
                System.out.println(sno.get(i).getTime() + "\n" + sho.get(i).getTime());
                System.out.println(":(");
                break;
            }
        }

        System.out.println("Shorts fast function: " + time);
        */
    }
}
