package com.efficientlogfileanalysis.bl;

import com.efficientlogfileanalysis.data.Tuple;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * Class that creates an Index based on a key and value pair.<br>
 * The Index can be saved and read from a file.
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public class IndexManager<K, V>{

    /**
     * Interface that serializes the given type<br>
     * Some usual type converters are predefined
     * @param <T> the type to be serialized
     */
    public interface I_TypeConverter<T>{

        Tuple<T, Integer> read(RandomAccessFile file) throws IOException;
        int write(RandomAccessFile file, T value) throws IOException;

        I_TypeConverter<Integer> INTEGER_TYPE_CONVERTER = new I_TypeConverter<Integer>() {
            @Override
            public Tuple<Integer, Integer> read(RandomAccessFile file) throws IOException {
                return new Tuple<>(4, file.readInt());
            }

            @Override
            public int write(RandomAccessFile file, Integer value) throws IOException {
                file.writeInt(value);
                return 4;
            }
        };
        I_TypeConverter<String> STRING_TYPE_CONVERTER = new I_TypeConverter<String>() {
            @Override
            public Tuple<String, Integer> read(RandomAccessFile file) throws IOException {
                int size = file.readInt();
                byte[] bytes = new byte[size];
                return new Tuple<>(new String(bytes), size + 4);
            }

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

    private HashMap<K, V> values;

    private long indexLength;

    public IndexManager(I_TypeConverter<K> keyConverter, I_TypeConverter<V> valueConverter)
    {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;

        values = new HashMap<>();
    }

    public void readIndex(RandomAccessFile file) throws IOException
    {
        if(file.getFilePointer() + 8 >= file.length()){
            return;
        }

        indexLength = file.readLong();

        long indexSize = indexLength;
        while(indexSize > 0)
        {
            Tuple<K, Integer> key = keyConverter.read(file);
            Tuple<V, Integer> value = valueConverter.read(file);

            indexSize -= key.value2;
            indexSize -= value.value2;

            values.put(key.value1, value.value1);
        }
    }

    public void writeIndex(RandomAccessFile file) throws IOException
    {
        long startLocation = file.getFilePointer();
        long newIndexLength = 0;

        file.seek(startLocation + 8);
        for(K key : values.keySet())
        {
            V value = values.get(key);

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

    public int getSize(){
        return values.size();
    }

    public boolean hasKey(K key){
        return values.containsKey(key);
    }

    public V getValue(K key){
        return values.get(key);
    }

    public void putValue(K key, V value){
        values.put(key, value);
    }

}
