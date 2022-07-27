package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.Tuple;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Singleton class that contains a TimeRange (begin and end date) for each logFile
 * @author Andreas Kurz
 */
public class LogDateManager extends IndexManager<Short, LogDateManager.TimeRange>{

    private static final String FILE_NAME = "log_date_index";

    /**
     * Data class representing a range of time
     */
    @AllArgsConstructor
    @ToString
    public static class TimeRange
    {
        public long beginDate;
        public long endDate;

        public TimeRange()
        {
            beginDate = 0;
            endDate = Long.MAX_VALUE;
        }

        /**
         * Type Converter which can be used to convert a TimeRange object
         */
        private static final I_TypeConverter converter = new I_TypeConverter<TimeRange>(){
            @Override
            public Tuple<Integer, TimeRange> read(RandomAccessFile file) throws IOException {
                long beginDate = file.readLong();
                long endDate = file.readLong();
                return new Tuple(16, new TimeRange(beginDate, endDate));
            }

            @Override
            public int write(RandomAccessFile file, TimeRange value) throws IOException {
                file.writeLong(value.beginDate);
                file.writeLong(value.endDate);
                return 16;
            }
        };

    }

    public LogDateManager() {
        super(
            I_TypeConverter.SHORT_TYPE_CONVERTER,
            TimeRange.converter
        );
    }

    @Override
    protected void createIndex() {
        super.createIndex();

        try
        {
            File file = new File(Manager.PATH_TO_INDEX + "/" + FILE_NAME);

            if(file.exists())
            {
                super.readIndex(new RandomAccessFile(file, "rw"));
            }
        }
        catch (IOException e) {}
    }

    /**
     * Writes the Index to the default file location
     * @throws IOException if the indexFile can't be written to
     */
    public void writeIndex() throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(Manager.PATH_TO_INDEX + "/" + FILE_NAME, "rw");
        super.writeIndex(file);
    }

    /**
     * Returns the time range associated with the given fileIndex
     * @param key the fileIndex of the requested file
     * @return the time range or (if no data for the specified file is found) the whole unix time frame
     */
    public TimeRange get(short key){
        if(!values.containsKey(key)) {
            return new TimeRange();
        }

        return values.getValue(key);
    }

    /**
     * Returns an array with the file indices of the files that have a date in between the specified TimeRange
     * @param value The time range that gets checked
     * @return the files that have that time range inside them
     */
    public short[] get(TimeRange value) {return null;}

    /**
     * Sets the beginning date for the given file
     * @param fileID the ID of the specified file
     * @param date the new begin date
     */
    public void setBeginDate(short fileID, long date){
        if(!values.containsKey(fileID))
        {
            putValue(fileID, new TimeRange());
        }
        get(fileID).beginDate = date;
    }

    /**
     * Sets the end date for the given file
     * @param fileID the ID of the specified file
     * @param date the new end date
     */
    public void setEndDate(short fileID, long date){
        if(!values.containsKey(fileID))
        {
            putValue(fileID, new TimeRange());
        }
        get(fileID).endDate = date;
    }

    /**
     * Sets both the begin and end date for the specified file
     * @param fileID the ID of the specified file
     * @param beginDate the new beginDate
     * @param endDate  the new end date
     */
    public void setDateRange(short fileID, long beginDate, long endDate)
    {
        putValue(fileID, new TimeRange(beginDate, endDate));
    }

    @SneakyThrows
    public static void main(String[] args) {
        LogDateManager ldm = new LogDateManager();

        System.out.println(ldm.get((short) 0));

//        logDateManager.setDateRange((short)0, 50, 100);
//        logDateManager.setDateRange((short)1, 200, 300);
//        logDateManager.setDateRange((short)2, 500, 550);
//        logDateManager.setDateRange((short)3, 1000, 2000);
//
//        logDateManager.writeIndex(new RandomAccessFile(Manager.PATH_TO_INDEX + "/" + FILE_NAME, "rw"));

    }
}
