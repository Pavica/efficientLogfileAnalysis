package com.efficientlogfileanalysis.data;

import lombok.ToString;

import java.util.HashMap;
import java.util.Set;

/**
 * A data class that stores key value pairs where the keys can also be accessed via the value. Both the key and value need to be unique.
 * @author Jan Mandl
 */
@ToString
public class BiMap<K, V> {
    /**
     * Stores the (key | value)
     */
    private HashMap<K, V> map1;
    /**
     * Stores the (value | key)
     */
    private HashMap<V, K> map2;

    public BiMap() {
        map1 = new HashMap<>();
        map2 = new HashMap<>();
    }

    /**
     * Saves a value and key into the Bimap.
     */
    public void putValue(K key, V value) {
        map1.put(key, value);
        map2.put(value, key);
    }

    /**
     * Wrapper method for putValue(K, V)
     */
    public void putKey(V key, K value) {
        putValue(value, key);
    }

    /**
     * Gets the value with the key. If the requested key isnt inside the map null is returned.
     */
    public V getValue(K key) {
        return map1.getOrDefault(key, null);
    }

    /**
     * Gets the key with the value. If the requested value isnt inside the map null is returned.
     */
    public K getKey(V value) {
        return map2.getOrDefault(value, null);
    }

    /**
     * Returns the size of the map
     */
    public int size() {
        return map1.size();
    }

    /**
     * Returns if the key is present in the map
     * @param key the key-value to be tested
     * @return true if the map contains the specified key; false if not
     */
    public boolean containsKey(K key)
    {
        return map1.containsKey(key);
    }

    /**
     * Returns if the value is present in the map
     * @param value the value to be tested
     * @return true if the map contains the specified value; false if not
     */
    public boolean containsValue(V value)
    {
        return map2.containsKey(value);
    }

    /**
     * Returns a Set containing all the keys in the map
     * @return all the keys in the map
     */
    public Set<K> getKeySet()
    {
        return map1.keySet();
    }

    /**
     * Returns a Set containing all the values of the map
     * @return all the values
     */
    public Set<V> getValueSet()
    {
        return map2.keySet();
    }

    /**
     * Adds the element, if the specified element is not already contained in the map
     * @param key the key which should be added
     * @param value the other key which should be added
     * @return true when the new key value pair is inserted into the map otherwise false
     */
    public boolean addIfAbsent(K key, V value) {
        if(map1.containsKey(key) || map2.containsKey(value)) {
            return false;
        }

        map1.put(key,value);
        map2.put(value,key);

        return true;
    }
}