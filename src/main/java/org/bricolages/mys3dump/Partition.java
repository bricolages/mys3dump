package org.bricolages.mys3dump;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shimpei-kodama on 2016/02/29.
 */
class Partition {
    public final String columnName;
    public final Long start;
    public final Long end;

    Partition(String columnName, Long start, Long end) {
        this.columnName = columnName;
        this.start = start;
        this.end = end;
    }

    List<Partition> split(int n) {
        Long estRowCount = end - start + 1; // Estimated row count
        List<Partition> pts = new ArrayList<>();
        Long partitionSize;
        if (estRowCount < n) {
            partitionSize = estRowCount;
        } else {
            partitionSize = (long) Math.ceil(estRowCount / (double) n);
        }
        for (Long pStart = start, pEnd; pStart <= end; pStart = pEnd + 1) {
            pEnd = pStart + partitionSize - 1;
            if (pEnd >= end) pEnd = end;
            pts.add(new Partition(columnName, pStart, pEnd));
        }
        return pts;
    }

    @Override
    public String toString() {
        return columnName + " BETWEEN " + start + " AND " + end;
    }
}
