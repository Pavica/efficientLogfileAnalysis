package com.efficientlogfileanalysis.data;

/**
 * Is used to get a sequence of non repeating numbers
 *
 * <br><br>
 * Last changed: 2022-06-03
 * @author Andreas Kurz
 */
public class Sequence {

    /** contains the current value of the sequence **/
    private int sequentialNumber;

    /**
     * Creates a new Sequence starting with the value 0
     */
    public Sequence(){
        this.sequentialNumber = -1;
    }

    /**
     * Returns the next value in the sequence
     * @return int representing the next value in the sequence
     */
    public synchronized int nextValue(){
        return ++sequentialNumber;
    }

    /**
     * Gets the current value of the sequence
     * @return the current value
     */
    public synchronized int currentValue(){
        return sequentialNumber;
    }
}