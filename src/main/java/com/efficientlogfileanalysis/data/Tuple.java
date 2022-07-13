package com.efficientlogfileanalysis.data;

import lombok.AllArgsConstructor;

/**
 * Data class containing a value pair.
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 */
@AllArgsConstructor
public class Tuple<T1, T2> {
    public T1 value1;
    public T2 value2;
}