package com.example.touchtyped.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class PDFCache {
    private final int maxSize;
    private final LinkedHashMap<String, byte[]> cache;

    public PDFCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<String,byte[]>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                return size() > maxSize;
            }
        };
    }

    public synchronized byte[] getPDF(String key) {
        return cache.get(key);
    }

    public synchronized void putPDF(String key, byte[] pdf) {
        if (!cache.containsKey(key)) {
            cache.put(key, pdf);
        }
    }

    public synchronized void clear() {
        cache.clear();
    }

    private static final PDFCache instance = new PDFCache(50);

    public static PDFCache getInstance() {
        return instance;
    }

}
