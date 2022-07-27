package com.efficientlogfileanalysis.log;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;

public class ModuleIDManager extends IndexManager<Integer, String>{

    private static final String FILE_NAME = "ModuleIDIndex";

    private static ModuleIDManager instance;

    public static synchronized ModuleIDManager getInstance()
    {
        if(instance == null)
        {
            instance = new ModuleIDManager();
        }

        return instance;
    }

    private ModuleIDManager()
    {
        super(
            I_TypeConverter.INTEGER_TYPE_CONVERTER,
            I_TypeConverter.STRING_TYPE_CONVERTER
        );
    }

    /**
     * Writes the Index to the default File path
     * @throws IOException if the index can't be accessed
     */
    public void writeIndex() throws IOException {
        super.writeIndex(new RandomAccessFile(FILE_NAME, "rw"));
    }

    /**
     * Reads the Index from the default File path
     * @throws IOException if the index can't be accessed
     */
    public void readIndex() throws IOException {
        super.readIndex(new RandomAccessFile(FILE_NAME, "rw"));
    }

    /**
     * Returns the module name associated with the given key
     * @param key the value of the key
     * @return the class name associated with the given key
     */
    public String get(Integer key) {
        return values.getValue(key);
    }

    /**
     * Returns the Key name associated with the given module name
     * @param value the module name
     * @return the key associated with the module name
     */
    public Integer get(String value) {
        return values.getKey(value);
    }

    /**
     * Returns all module names
     * @return a set of all module names
     */
    public Set<String> getModuleNames()
    {
        return values.getValueSet();
    }

    /**
     * Adds the moduleName, if the specified moduleName is not already contained in the list
     * @param moduleName the moduleName which should be added
     * @return the new id of the moduleName (returns the previous key if the moduleName is already present)
     */
    public int addIfAbsent(String moduleName)
    {
        if(values.containsValue(moduleName))
        {
            return values.getKey(moduleName);
        }

        int newID = values.size();
        values.putValue(newID, moduleName);

        return newID;
    }

    @Override
    protected void createIndex() {
        super.createIndex();

        File file = new File(FILE_NAME);
        if(file.exists())
        {
            try
            {
                readIndex(new RandomAccessFile(file, "rw"));
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("Module Index cant be read");
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        ModuleIDManager.getInstance().readIndex();
        System.out.println(ModuleIDManager.getInstance());
    }
}
