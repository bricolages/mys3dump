package org.bricolages.mys3dump;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created by shimpei-kodama on 2016/03/10.
 */
class WorkerResult {
    private final String threadName;
    private Long processedRowCount = 0L;
    private Long processedTime;
    private final LocalDateTime startDateTime = LocalDateTime.now();

    public WorkerResult(String threadName) {
        this.threadName = threadName;
    }

    String getThreadName() {
        return threadName;
    }

    Long getProcessedRowCount() {
        return processedRowCount;
    }

    Long getProcessedTime() {
        if (processedTime == null) throw new IllegalStateException("Called before invoking Worker#finish()");
        return processedTime;
    }

    void addProcessedRowCount(Long count) {
        processedRowCount += count;
    }

    void finish() {
        processedTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - startDateTime.toEpochSecond(ZoneOffset.UTC);
    }
}
