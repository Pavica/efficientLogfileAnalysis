package com.efficientlogfileanalysis.data;

import com.efficientlogfileanalysis.data.BiMap;
import com.efficientlogfileanalysis.data.Tuple;
import com.efficientlogfileanalysis.log.I_TypeConverter;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class that creates an Index based on a key and value pair.<br>
 * The Index can be saved and read from a file.
 * @param <K> The type of the key
 * @param <V> The type of the value
 * @author Andreas Kurz
 */
public class SerializableBiMap<K, V> extends BiMap<K, V> {

    private I_TypeConverter<K> keyConverter;
    private I_TypeConverter<V> valueConverter;

    private long indexLength;

    public SerializableBiMap(I_TypeConverter<K> keyConverter, I_TypeConverter<V> valueConverter)
    {
        super();
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    /**
     * Reads the Index to a file with the specified name<br>
     * The same as <code>readIndex(new RandomAccessFile(fileName, "r"))</code> with the additional benefit of actually closing the file
     * @param fileName the name of the file
     * @throws IOException if the file can't be read
     */
    public void readIndex(String fileName) throws IOException
    {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        readIndex(raf);
        raf.close();
    }

    /**
     * Reads the index from a file and puts it into the values HashMap.
     * @param file A RandomAccessFile containing an index
     * @throws IOException
     */
    public void readIndex(RandomAccessFile file) throws IOException
    {
        if(file.getFilePointer() + 8 >= file.length()){
            return;
        }

        long indexSize = indexLength = file.readLong();
        while(indexSize > 0)
        {
            Tuple<Integer, K> key = keyConverter.read(file);
            Tuple<Integer, V> value = valueConverter.read(file);

            indexSize -= key.value1;
            indexSize -= value.value1;

            putValue(key.value2, value.value2);
        }
    }

    /**
     * Writes the Index to a file with the specified name<br>
     * The same as <code>writeIndex(new RandomAccessFile(fileName, "rw"))</code> with the additional benefit of actually closing the file
     * @param fileName the name of the file
     * @throws IOException if the file can't be written to
     */
    public void writeIndex(String fileName) throws IOException
    {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        writeIndex(raf);
        raf.close();
    }

    /**
     * Writes the Index to a RandomAccessFile with a binary format. Serialization is done by the TypeConverter class. 
     * @param file A RandomAccessFile the index is being written to
     * @throws IOException
     */
    public void writeIndex(RandomAccessFile file) throws IOException
    {
        long startLocation = file.getFilePointer();
        long newIndexLength = 0;

        file.seek(startLocation + 8);
        for(K key : getKeySet())
        {
            V value = getValue(key);

            newIndexLength += keyConverter.write(file, key);
            newIndexLength += valueConverter.write(file, value);
        }

        file.seek(startLocation);
        file.writeLong(newIndexLength);

        indexLength = newIndexLength;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("");

        for(K key : getKeySet())
        {
            output.append(String.format(" %12s\t|\t%s \n", key, getValue(key)));
        }

        return output.toString();
    }
}
