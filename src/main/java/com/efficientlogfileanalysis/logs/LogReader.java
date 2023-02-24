package com.efficientlogfileanalysis.logs;

import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.index.data.TimeRange;
import com.efficientlogfileanalysis.logs.data.LogEntry;
import com.efficientlogfileanalysis.logs.data.LogFile;
import com.efficientlogfileanalysis.logs.data.LogFileData;
import com.efficientlogfileanalysis.logs.data.LogLevel;
import com.efficientlogfileanalysis.util.DateConverter;
import com.efficientlogfileanalysis.util.Timer;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A class with methods for reading information out of logfiles.
 * @author Andreas Kurz, Jan Mandl
 */
public class LogReader implements Closeable {

    private static final String REGEX_START_OF_LOG_ENTRY = "^\\d{2} \\w{3} \\d{4}[\\s\\S]*";

    /**
     * A functional interface containing an action used by the <code>forEachLogFile</code> method
     * @param <T> an Optional Exception thrown by the performAction method
     */
    public interface LogFileAction<T extends Exception>{
        void performAction(Path logFile) throws T, IOException;
    }

    /**
     * Performs a given operation on all logFiles within the logFolder
     * @param folder the path to the logFolder
     * @param handler the Action that should be performed
     * @param <E> the type of the exception which is thrown by performAction (optional)
     * @throws E if the given action throws an Exception
     * @throws IOException if the logFolder can't be accessed
     */
    public static <E extends Exception> void forEachLogFile(String folder, LogFileAction<E> handler) throws E, IOException {
        try(Stream<Path> fileStream = getAllLogFilesAsStream(folder))
        {
            Iterator<Path> iterator = fileStream.iterator();

            while(iterator.hasNext()){
                handler.performAction(iterator.next());
            }
        }
    }

    /**
     * Returns a Stream containing all logFiles in a folder<br>
     * Important! Has to be closed after being used (ideally within a try with resources block)
     * @param logFolder the path to the logFolder
     * @return a Stream of all LogFiles within the given folder
     * @throws IOException if the logFolder can't be accessed
     */
    public static Stream<Path> getAllLogFilesAsStream(String logFolder) throws IOException {
        return Files.walk(Paths.get(logFolder))
            .filter(Files::isRegularFile)
            .filter(file -> file.getFileName().toString().contains(".log"));
    }

    /**
     * Returns all logFiles ina given folder as an File[]
     * @param logFolder the path to the logFolder
     * @return an array containing File objects
     * @throws IOException if the folder can't be accessed
     */
    public static File[] getAllLogFiles(String logFolder) throws IOException
    {
        try(Stream<Path> files = getAllLogFilesAsStream(logFolder))
        {
            return files.map(Path::toFile).toArray(File[]::new);
        }
    }

    /**
     * Reads all the files in a directory into a Logfile array.
     * @param path The path to the folder
     * @return A list containing all Logentries grouped by the file they are in
     */
    @SneakyThrows
    public static LogFile[] readAllLogFiles(String path)
    {
        return Arrays.stream(getAllLogFiles(path))
                .map(file -> new LogFile(file.getName(), readSingleFile(file.getAbsolutePath()).getEntries()))
                .toArray(LogFile[]::new);
    }

    /**
     * Reads all logEntries from a single file
     * @param path The path to the file
     * @return A list containing all Logentries
     */
    @SneakyThrows
    public static LogFileData readSingleFile(String path)
    {
        return readSingleFile(path, 0);
    }

    private static boolean stringContainsOnly(String string, char... values){
        for(int i = 0; i < string.length(); i++){
            char currentCharacter = string.charAt(0);
            boolean characterIsPresent = false;
            for(char value : values){
                if(currentCharacter == value){
                    characterIsPresent = true;
                    break;
                }
            }
            if(!characterIsPresent){
                return false;
            }
        }
        return true;
    }

    /**
     * Reads all logEntries after a specific location in the file
     * @param path the path to the logFile
     * @param offset how many bytes should be skipped
     * @return a logFileData object with all read logEntries as well as how many bytes have been read
     * @throws IOException If an IOError occurs
     */
    public static LogFileData readSingleFile(String path, long offset) throws IOException {
        Matcher startOfLogEntry = Pattern.compile(REGEX_START_OF_LOG_ENTRY).matcher("");
        List<LogEntry> entries = new ArrayList<>();

        FileChannel fileChannel = FileChannel.open(Paths.get(path));
        fileChannel.position(offset);
        Scanner scanner = new Scanner(fileChannel);
        scanner.useDelimiter("\n");

        long bytesRead = offset;

        String line;
        String currentEntry = "";

        while (currentEntry != null) {

            //read a single line
            line = !scanner.hasNext() ? null : scanner.next() + "\n";

            //skip empty lines
            if(currentEntry.equals("") && line != null && stringContainsOnly(line, '\r', ' ', '\n', '\t')){
                bytesRead += line.getBytes().length;
                continue;
            }

            //if the current entry is empty or the line is part of it (does not start with a date) add it to the entry
            if (line != null && (currentEntry.isEmpty() || !startOfLogEntry.reset(line).matches())) {
                currentEntry += line;
                continue;
            }

            try
            {
                //try to add the new entry
                LogEntry newLogEntry = new LogEntry(currentEntry, bytesRead);
                entries.add(newLogEntry);

                //add how many bytes have been read
                bytesRead += currentEntry.getBytes().length;

                //move on to the next line
                currentEntry = line;
            }
            catch(IndexOutOfBoundsException | DateTimeParseException | NumberFormatException somethingWentWrong) {
                //if the entry can't be passed stop reading the file
                break;
            }
        }

        //check if bytes read got out of bounds
        //happens if the last entry was missing a \n
        if(bytesRead > fileChannel.size())
        {
            bytesRead = fileChannel.size();
        }

        scanner.close();
        return new LogFileData(entries, bytesRead - offset);
    }

    @Getter
    private final String logFolderPath;
    private final Matcher startOfLogEntry;
    private final HashMap<String, RandomAccessFile> openFiles;

    public LogReader(String logFolderPath)
    {
        this.logFolderPath = logFolderPath;
        startOfLogEntry = Pattern.compile(REGEX_START_OF_LOG_ENTRY).matcher("");
        openFiles = new HashMap<>();
    }

    /**
     * Should always be called when the LogReader is no longer needed
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
     * @param fileName The name of the file that should get checked.
     * @throws IOException
     */
    private void prepareFile(String fileName) throws IOException {
        if(!openFiles.containsKey(fileName))
        {
            openFiles.put(
                fileName,
                new RandomAccessFile(logFolderPath + "/" + fileName,"r")
            );
        }
    }

    /**
     * Convenience method. Prepares a RandomAccessFile object to be ready to be read from.
     * @param fileName The name of the file
     * @param logEntryID The nth log entry inside a file
     * @return a RandomAccessFile whose FilePointer is right before the logEntry
     * @throws IOException if the log directory can't be accessed
     */
    private RandomAccessFile prepareRandomAccessFile(
        String fileName,
        long logEntryID
    ) throws IOException {

        prepareFile(fileName);
        
        RandomAccessFile file = openFiles.get(fileName);
        file.seek(logEntryID);
        
        return file;
    }

    /**
     * Creates a LogEntry object from a FileIndex and a logEntryID
     * @param fileName The name of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log entry that has the id of the variable fileIndex inside the file with the id in logEntryID
     * @throws IOException if the log directory can't be accessed
     */
    public LogEntry getLogEntry(String fileName, long logEntryID) throws IOException {
        RandomAccessFile file = prepareRandomAccessFile(fileName, logEntryID);

        String line = "";
        String tempLine = "";

        line = file.readLine();

        //while there is no date at the beginning of the line
        while(
            (tempLine = file.readLine()) != null &&
            !startOfLogEntry.reset(tempLine).matches()
        )
        {
            line += tempLine;
        }

        return new LogEntry(line, logEntryID);
    }

    /**
     * Creates a LogEntry object without the message from a FileIndex and a logEntryID
     * @param fileName The name of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log entry that has the id of the variable fileIndex inside the file with the id in logEntryID<br>The message is null.
     * @throws IOException if the log directory can't be accessed
     */
    public LogEntry readLogEntryWithoutMessage(String fileName, long logEntryID) throws IOException {
        RandomAccessFile file = prepareRandomAccessFile(fileName, logEntryID);
        StringBuilder stringBuilder = new StringBuilder("");

        LogEntry logEntry = new LogEntry();
        logEntry.setEntryID(logEntryID);

        //Read the date
        byte[] bytes = new byte[24];
        file.read(bytes);
        logEntry.setDateFromString(new String(bytes));

        //Read the log level
        file.readFully(bytes, 0, 7);
        logEntry.setLogLevel(LogLevel.valueOf(new String(bytes, 0, 7).trim()));

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
     * Reads all Files which start less than x bytes before or after a given entry
     * @param fileName the name of the file
     * @param logEntryID the position of the original logEntry
     * @param byteRange the range of bytes
     * @return a list of logEntries
     * @throws IOException
     */
    public List<LogEntry> getNearbyEntries(String fileName, long logEntryID, long byteRange) throws IOException
    {
        List<LogEntry> entries = new ArrayList<>();
        RandomAccessFile file = prepareRandomAccessFile(fileName, logEntryID);

        long startPosition = logEntryID - byteRange;
        long maxPosition = logEntryID + byteRange;

        if(startPosition < 0) {
            startPosition = 0;
        }

        if(maxPosition > file.length()){
            maxPosition = file.length();
        }

        file.seek(startPosition);

        String currentLine = "";
        long nextEntryID = startPosition;
        while(!startOfLogEntry.reset(currentLine).matches()){
            nextEntryID = file.getFilePointer();
            currentLine = file.readLine();
        }

        String entry;
        long entryID;
        do
        {
            entry = currentLine;
            entryID = nextEntryID;
            nextEntryID = file.getFilePointer();
            while(
                    (currentLine = file.readLine()) != null &&
                            !startOfLogEntry.reset(currentLine).matches()
            )
            {
                nextEntryID = file.getFilePointer();
                entry += currentLine + "\n";
            }
            entries.add(new LogEntry(entry, entryID));
        }while(file.getFilePointer() < maxPosition);

        if(currentLine != null && !currentLine.isEmpty() && nextEntryID < maxPosition){
            entries.add(new LogEntry(currentLine, nextEntryID));
        }

        return entries;
    }


    /**
     * Reads the date of the specified entry and returns it in milliseconds.
     * @param fileName The index of the file
     * @param logEntryID The nth log entry inside a file
     * @return The time at which the entry was logged
     * @throws IOException if the log directory can't be accessed
     */
    public long readDateOfEntry(String fileName, long logEntryID) throws IOException {
        RandomAccessFile file = prepareRandomAccessFile(fileName, logEntryID);
        
        byte[] bytes = new byte[24];
        file.read(bytes);

        return DateConverter.toLong(
            LocalDateTime.parse(
                new String(bytes),
                LogEntry.DTF
            )
        );
    }

    /**
     * Reads the log level of the specified entry and returns it.
     * @param fileName The name of the file
     * @param logEntryID The nth log entry inside a file
     * @return The log level of the entry as a String object
     * @throws IOException if the log directory can't be accessed
     */
    public LogLevel readLogLevelOfEntry(String fileName, long logEntryID) throws IOException {

        RandomAccessFile file = prepareRandomAccessFile(fileName, logEntryID);
        byte[] bytes = new byte[7];
        
        file.seek(logEntryID + 24);
        file.read(bytes);

        return LogLevel.valueOf(new String(bytes).trim());
    }

    /**
     * Finds the position (ID) of the last entry in a logfile
     * @param fileName the name of the file
     * @return the ID of the last logEntry, or 0 if the file contains no entries
     */
    private long getIDOfLastLogEntry(String fileName) throws IOException
    {
        RandomAccessFile file = prepareRandomAccessFile(fileName, 0);

        for(long position = file.length() - 1; position >= 0; --position)
        {
            file.seek(position);
            byte character = file.readByte();

            if(character == '\n'){

                byte[] bytes = new byte[24];
                file.read(bytes);

                String startOfEntry = new String(bytes);

                //check if the line starts with a date
                if(startOfLogEntry.reset(startOfEntry).matches()){
                    return ++position;
                }
            }
        }

        return 0;
    }

    /**
     * Retrieves the time range in which the logfile has been used
     * @param fileName the name of the file
     * @return a TimeRange object containing the dates for the first and last logEntry
     */
    public TimeRange getTimeRangeOfFile(String fileName) throws IOException
    {
        TimeRange result = new TimeRange();
        result.endDate = Files.getLastModifiedTime(Paths.get(logFolderPath, fileName)).toMillis();
        try {
            result.beginDate = readDateOfEntry(fileName, 0);
        }
        catch(NumberFormatException | DateTimeParseException | IndexOutOfBoundsException ex) {
            //LogEntry can not be read
            //File has invalid format
            result.beginDate = result.endDate;
        }
        return result;
    }

    @SneakyThrows
    public static void main(String[] args) {
        try(LogReader reader = new LogReader(Settings.getInstance().getLogFilePath()))
        {
            TimeRange range = new TimeRange(
                DateConverter.toLong(LocalDateTime.parse("2022-07-05T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                DateConverter.toLong(LocalDateTime.parse("2022-07-06T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            );

            Timer.timeIt(() -> {
                LogReader.forEachLogFile(reader.logFolderPath, path -> {
                    TimeRange fileRange = reader.getTimeRangeOfFile(path.getFileName().toString());
                    range.overlaps(fileRange);
                });
            }, 1_000);


//            String path = Settings.getInstance().getLogFilePath();
//            Timer.timeIt(() -> getAllLogFiles(path), 1_000);

//            LogReader.getAllLogFiles()
//            File[] files = LogReader.getAllLogFiles(reader.path);
//            Timer.timeIt(() -> {
//                for(File f : files){
//                    TimeRange t = reader.getTimeRangeOfFile(f.getName());
//                }
//            }, 1_000);
        }
    }
}
