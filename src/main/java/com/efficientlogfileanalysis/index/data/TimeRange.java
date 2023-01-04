package com.efficientlogfileanalysis.index.data;

import com.efficientlogfileanalysis.util.DateConverter;
import lombok.AllArgsConstructor;

/**
 * Data class representing a range of time
 */
@AllArgsConstructor
public class TimeRange
{
    public long beginDate;
    public long endDate;

    public TimeRange()
    {
        beginDate = 0;
        endDate = Long.MAX_VALUE;
    }

    /**
     * Returns true if this timeRange contains the other one or vice versa
     * @param timeRange another TimeRange object
     * @return true if the time ranges overlap
     */
    public boolean overlaps(TimeRange timeRange)
    {
        return this.contains(timeRange) || timeRange.contains(this);
    }

    /**
     * Returns true if one of the given dates are contained within the timeRange
     * @param anotherTimeRange the other two dates
     * @return true if the other TimeRange object is contained within this object
     */
    public boolean contains(TimeRange anotherTimeRange)
    {
        return
            (beginDate <= anotherTimeRange.beginDate && anotherTimeRange.beginDate <= endDate) ||
            (beginDate <= anotherTimeRange.endDate && anotherTimeRange.endDate <= endDate);
    }

    @Override
    public String toString()
    {
        return String.format("TimeRange(%s - %s)",
            DateConverter.toDateTime(beginDate),
            DateConverter.toDateTime(endDate)
        );
    }

    public static void main(String[] args) {
        TimeRange tr1 = new TimeRange(10, 20);
        TimeRange tr2 = new TimeRange(22, 25);
        System.out.println(tr1.overlaps(tr2));
    }
}
