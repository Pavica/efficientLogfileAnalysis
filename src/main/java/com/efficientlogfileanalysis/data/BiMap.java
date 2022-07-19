package com.efficientlogfileanalysis.data;

import java.util.HashMap;

/**
 * A data class that stores key value pairs where the keys can also be accessed via the value. Both the key and value need to be unique.
 * @author Jan Mandl
 */
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

    public int size() {
        return map1.size();
    }
}