package com.efficientlogfileanalysis.index.data;

import com.efficientlogfileanalysis.util.DateConverter;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Data class representing a range of time
 */
@AllArgsConstructor
@ToString
public class TimeRange
{
    public long beginDate;
    public long endDate;

    public TimeRange()
    {
        beginDate = 0;
        endDate = Long.MAX_VALUE;
    }

    @Override
    public String toString()
    {
        return String.format("TimeRange(%s - %s)",
            DateConverter.toDateTime(beginDate),
            DateConverter.toDateTime(endDate)
        );
    }
}
