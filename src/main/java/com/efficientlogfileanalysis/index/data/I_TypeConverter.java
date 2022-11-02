package com.efficientlogfileanalysis.index.data;

import com.efficientlogfileanalysis.data.Tuple;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

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

    static<T> I_TypeConverter<List<T>> listConverter(I_TypeConverter<T> typeConverter){
        return new CollectionTypeConverter(typeConverter, ArrayList::new);
    }

    static<T> I_TypeConverter<Set<T>> setConverter(I_TypeConverter<T> typeConverter){
        return new CollectionTypeConverter(typeConverter, LinkedHashSet::new);
    }

    class CollectionTypeConverter<T, L extends Collection<T>> implements I_TypeConverter<L>
    {
        private interface CollectionCreator<L>
        {
            L createCollection(int expectedSize);
        }

        private I_TypeConverter<T> typeConverter;
        private CollectionCreator<L> collectionCreator;

        public CollectionTypeConverter(I_TypeConverter<T> typeConverter, CollectionCreator collectionCreator)
        {
            this.typeConverter = typeConverter;
            this.collectionCreator = collectionCreator;
        }

        @Override
        public Tuple<Integer, L> read(RandomAccessFile file) throws IOException {
            int lengthRead = 4;
            int listLength = file.readInt();

            L list = collectionCreator.createCollection(listLength);

            for(int i = 0; i < listLength; i++)
            {
                Tuple<Integer, T> listEntry = typeConverter.read(file);
                lengthRead += listEntry.value1;
                list.add(listEntry.value2);
            }

            return new Tuple(lengthRead, list);
        }

        @Override
        public int write(RandomAccessFile file, L value) throws IOException {
            int lengthWritten = 4;

            file.writeInt(value.size());

            for(T element : value)
            {
                lengthWritten += typeConverter.write(file, element);
            }

            return lengthWritten;
        }
    }

    /**
     * Converter for Long objects.
     */
    I_TypeConverter<Long> LONG_TYPE_CONVERTER = new I_TypeConverter<Long>() {
        /**
         * Reads a Long object from a RandomAccessFile.
         * @param file A RandomAccessFile that is going to be written to
         * @return A Tuple object that with the amount of bytes read and a Long object
         * @throws IOException
         */
        @Override
        public Tuple<Integer, Long> read(RandomAccessFile file) throws IOException {
            return new Tuple<>(8, file.readLong());
        }

        /**
         * Writes a Long object into a given RandomAccessFile and returns the amount of bytes that were written.
         * @param file A RandomAccessFile that is going to be written to
         * @param value The Long object that is written to the file
         * @return The amount of bytes that were written to the file
         * @throws IOException
         */
        @Override
        public int write(RandomAccessFile file, Long value) throws IOException {
            file.writeLong(value);
            return 2;
        }
    };

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

    /**
     * Type Converter which can be used to convert a TimeRange object
     */
    I_TypeConverter<TimeRange> TIME_RANGE_CONVERTER = new I_TypeConverter<TimeRange>(){
        @Override
        public Tuple<Integer, TimeRange> read(RandomAccessFile file) throws IOException {
            long beginDate = file.readLong();
            long endDate = file.readLong();
            return new Tuple(16, new TimeRange(beginDate, endDate));
        }

        @Override
        public int write(RandomAccessFile file, TimeRange value) throws IOException {
            file.writeLong(value.beginDate);
            file.writeLong(value.endDate);
            return 16;
        }
    };
}
