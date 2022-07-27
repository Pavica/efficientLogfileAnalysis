package com.efficientlogfileanalysis.log;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;

public class ClassIDManager extends IndexManager<Integer, String>{

    private static final String FILE_NAME = "ClassIDIndex";

    @Override
    protected void createIndex() {
        super.createIndex();

        File file = new File(Manager.PATH_TO_INDEX + "/" + FILE_NAME);
        if(file.exists())
        {
            try
            {
                readIndex(new RandomAccessFile(file, "rw"));
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("Class Index cant be read");
            }
        }
    }

    public ClassIDManager()
    {
        super(
            I_TypeConverter.INTEGER_TYPE_CONVERTER,
            I_TypeConverter.STRING_TYPE_CONVERTER
        );
    }

    /**
     * Writes the Index to the default file path
     * @throws IOException if the index can't be written
     */
    public void writeIndex() throws IOException {
        super.writeIndex(new RandomAccessFile(FILE_NAME, "rw"));
    }

    /**
     * Reads the Index from the default file path
     * @throws IOException if the index can't be read
     */
    public void readIndex() throws IOException {
        super.readIndex(new RandomAccessFile(FILE_NAME, "rw"));
    }

    /**
     * Returns the Class name associated with the given key
     * @param key the value of the key
     * @return the class name associated with the given key
     */
    public String get(Integer key) {
        return values.getValue(key);
    }

    /**
     * Returns the Key name associated with the given className
     * @param value the className
     * @return the key associated with the className
     */
    public Integer get(String value) {
        return values.getKey(value);
    }

    /**
     * Returns all class names
     * @return a set of all classNames
     */
    public Set<String> getClassNames()
    {
        return values.getValueSet();
    }

    /**
     * Adds the className, if the specified className is not already contained in the list
     * @param className the classname which should be added
     * @return the new id of the classname (returns the previous key if the classname is already present)
     */
    public int addIfAbsent(String className)
    {
        if(values.containsValue(className))
        {
            return values.getKey(className);
        }

        int newID = values.size();
        values.putValue(newID, className);

        return newID;
    }

    @SneakyThrows
    public static void main(String[] args) {
        //System.out.println(ClassIDManager.getInstance());
    }
}
