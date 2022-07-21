package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.BiMap;
import com.efficientlogfileanalysis.data.Tuple;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class that creates an Index based on a key and value pair.<br>
 * The Index can be saved and read from a file.
 * @param <K> The type of the key
 * @param <V> The type of the value
 * @author Andreas Kurz
 */
public class IndexManager<K, V>{

    /**
     * Interface that serializes the given type<br>
     * Some usual type converters are predefined
     * @param <T> the type to be serialized
     */
    public interface I_TypeConverter<T>{

        /**
         * Function that gets called when reading from a file.
         * @param file The file to read from
         * @return A Tuple object that where the Integer objects is the amount of read bytes and T is the returned value
         * @throws IOException
         */
        Tuple<Integer, T> read(RandomAccessFile file) throws IOException;
        /**
         * Function that gets called when writing a value to a file.
         * @param file The file to write to
         * @param value The value that is being written to the file 
         * @return The amount of written bytes
         * @throws IOException
         */
        int write(RandomAccessFile file, T value) throws IOException;

        /**
         * Converter for Integer objects.
         */
        I_TypeConverter<Integer> INTEGER_TYPE_CONVERTER = new I_TypeConverter<Integer>() {
            /**
             * Reads an Integer object from a RandomAccessFile.
             * @param file A RandomAccessFile that is going to be written to
             * @return A Tuple object that with the amount of bytes read and an Integer object
             * @throws IOException 
             */
            @Override
            public Tuple<Integer, Integer> read(RandomAccessFile file) throws IOException {
                return new Tuple<>(4, file.readInt());
            }
            
            /**
             * Writes an Integer object into a given RandomAccessFile and returns the amount of bytes that were written.
             * @param file A RandomAccessFile that is going to be written to
             * @param value The Integer object that is written to the file
             * @return The amount of bytes that were written to the file
             * @throws IOException 
             */
            @Override
            public int write(RandomAccessFile file, Integer value) throws IOException {
                file.writeInt(value);
                return 4;
            }
        };

        /**
         * Converter for Short objects.
         */
        I_TypeConverter<Short> SHORT_TYPE_CONVERTER = new I_TypeConverter<Short>() {
            /**
             * Reads a Short object from a RandomAccessFile.
             * @param file A RandomAccessFile that is going to be written to
             * @return A Tuple object that with the amount of bytes read and an Shorts object
             * @throws IOException 
             */
            @Override
            public Tuple<Integer, Short> read(RandomAccessFile file) throws IOException {
                return new Tuple<>(2, file.readShort());
            }
            
            /**
             * Writes a Short object into a given RandomAccessFile and returns the amount of bytes that were written.
             * @param file A RandomAccessFile that is going to be written to
             * @param value The Short object that is written to the file
             * @return The amount of bytes that were written to the file
             * @throws IOException 
             */
            @Override
            public int write(RandomAccessFile file, Short value) throws IOException {
                file.writeShort(value);
                return 2;
            }
        };

        /**
         * Converter for Byte objects.
         */
        I_TypeConverter<Byte> BYTE_TYPE_CONVERTER = new I_TypeConverter<Byte>() {
            /**
             * Reads a Byte object from a RandomAccessFile.
             * @param file A RandomAccessFile that is going to be written to
             * @return A Tuple object that with the amount of bytes read and an Byte object
             * @throws IOException 
             */
            @Override
            public Tuple<Integer, Byte> read(RandomAccessFile file) throws IOException {
                return new Tuple<>(1, file.readByte());
            }
            
            /**
             * Writes a Byte object into a given RandomAccessFile and returns the amount of bytes that were written.
             * @param file A RandomAccessFile that is going to be written to
             * @param value The Byte object that is written to the file
             * @return The amount of bytes that were written to the file
             * @throws IOException 
             */
            @Override
            public int write(RandomAccessFile file, Byte value) throws IOException {
                file.writeByte(value);
                return 1;
            }
        };

        /**
         * Converter for String objects.
         */
        I_TypeConverter<String> STRING_TYPE_CONVERTER = new I_TypeConverter<String>() {
            /**
             * Reads an Integer object representing the length of the following String object and the String object from a RandomAccessFile.
             * @param file A RandomAccessFile that is going to be written to
             * @return A Tuple object that with the amount of bytes read and the String object 
             * @throws IOException 
             */
            @Override
            public Tuple<Integer, String> read(RandomAccessFile file) throws IOException {
                int size = file.readInt();
                byte[] bytes = new byte[size];
                file.read(bytes);
                return new Tuple<>(size + 4, new String(bytes));
            }
            
            /**
             * Writes a String object into a given RandomAccessFile and returns the amount of bytes that were written.
             * @param file A RandomAccessFile that is going to be written to
             * @param value The String object that is written to the file
             * @return The amount of bytes that were written to the file
             * @throws IOException 
             */
            @Override
            public int write(RandomAccessFile file, String value) throws IOException {
                byte[] data = value.getBytes();
                file.writeInt(data.length);
                file.write(data);
                return 4 + data.length;
            }
        };
    }

    private I_TypeConverter<K> keyConverter;
    private I_TypeConverter<V> valueConverter;

    protected BiMap<K, V> values;

    private long indexLength;

    public IndexManager(I_TypeConverter<K> keyConverter, I_TypeConverter<V> valueConverter)
    {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;

        createIndex();
    }

    /**
     * Method that gets called when the index should get created.
     */
    protected void createIndex() {
        values = new BiMap<>();
    }

    /**
     * Reads the index from a file and puts it into the values HashMap.
     * @param file A RandomAccessFile containing an index
     * @throws IOException
     */
    protected void readIndex(RandomAccessFile file) throws IOException
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

            values.putValue(key.value2, value.value2);
        }
    }

    /**
     * Writes the Index to a RandomAccessFile with a binary format. Serialization is done by the TypeConverter class. 
     * @param file A RandomAccessFile the index is being written to
     * @throws IOException
     */
    protected void writeIndex(RandomAccessFile file) throws IOException
    {
        long startLocation = file.getFilePointer();
        long newIndexLength = 0;

        file.seek(startLocation + 8);
        for(K key : values.getKeySet())
        {
            V value = values.getValue(key);

            newIndexLength += keyConverter.write(file, key);
            newIndexLength += valueConverter.write(file, value);
        }

        file.seek(startLocation);
        file.writeLong(newIndexLength);

        if(newIndexLength != indexLength)
        {
            indexLength = newIndexLength;
            System.err.println("Index Length changed!");
        }
    }

    /**
     * Prints the values inside the BiMap
     */
    protected void print() {
        for (K key : values.getKeySet()) {
            System.out.println("Key: " + key + " Value: " + values.getValue(key));
        }
    }

    public int getSize(){
        return values.size();
    }

    public boolean hasKey(K key){
        return values.containsKey(key);
    }

    public void putValue(K key, V value){
        values.putValue(key, value);
    }

    public void putKey(V value, K key){
        values.putKey(value, key);
    }
}
