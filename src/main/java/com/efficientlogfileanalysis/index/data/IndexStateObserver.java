package com.efficientlogfileanalysis.index.data;

public interface IndexStateObserver {
    void update(IndexState newIndexState);
}
