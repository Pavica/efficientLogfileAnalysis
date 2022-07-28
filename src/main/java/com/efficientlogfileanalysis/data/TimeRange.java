package com.efficientlogfileanalysis.data;

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
}
