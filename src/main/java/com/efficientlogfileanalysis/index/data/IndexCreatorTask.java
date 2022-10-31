package com.efficientlogfileanalysis.index.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class IndexCreatorTask
{
    private String filename;
    private TaskType taskType;

    public enum TaskType {
        FILE_CREATED,
        FILE_APPENDED,
        FILE_DELETED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexCreatorTask that = (IndexCreatorTask) o;
        return Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename);
    }
}
