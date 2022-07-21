package com.efficientlogfileanalysis.util;

/**
 * Class containing several methods to convert dataTypes to and from byte[]
 */
public class ByteConverter {

    /**
     * Converts a short value into a byte[]
     * @param value the value to be converted
     * @return a byte[] with length 2 representing the short value
     */
    public static byte[] shortToByte(short value)
    {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value >> 8);
        bytes[1] = (byte) value;
        return bytes;
    }

    /**
     * Converts a byte[] back into a usable short value<br>
     * If the length of the array is not 2 a IndexOutOfBoundsException is thrown!
     * @param x a byte[] with length 2
     * @return the short value
     */
    public static short byteToShort(byte[] x)
    {
        return (short)(((x[0] & 0xFF) << 8) | (x[1] & 0xFF));
    }

    public static void main(String[] args) {
        System.out.printf(Short.valueOf(byteToShort(shortToByte((short)-250))).toString());
    }

}
