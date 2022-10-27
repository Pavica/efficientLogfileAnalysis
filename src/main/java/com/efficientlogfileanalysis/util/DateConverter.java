package com.efficientlogfileanalysis.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Class providing methods to convert a Date between the LocalDateTime class and a simple long value
 */
public class DateConverter {

    /**
     * Converts a LocalDateTime Object into a long value
     * @param date The Date Object which is going to be converted
     * @return The date and time in milliseconds.
     */
    public static long toLong(LocalDateTime date) {
        return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Converts a long value into a LocalDateTime Object
     * @param timeValue The long value which is going to be converted (in milliseconds since 1970)
     * @return The millisecond value as a DateTime object.
     */
    public static LocalDateTime toDateTime(long timeValue)
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeValue), ZoneId.systemDefault());
    }

}
