package com.efficientlogfileanalysis.index.data;

public enum IndexState {
    // index creation wasn't started yet or interrupted
    NOT_READY,
    // the index is ready for use
    READY,
    // when a file couldn't be read
    ERROR,
    // the index is currently being build
    INDEXING,
    // index creation was interrupted but is still running
    INTERRUPTED
}