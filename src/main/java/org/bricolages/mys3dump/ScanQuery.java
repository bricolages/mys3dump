package org.bricolages.mys3dump;

/**
 * Created by shimpei-kodama on 2016/03/01.
 */
class ScanQuery {
    public static final String PARTITION_CONDITION_PLACE_HOLDER = "@PARTITION_CONDITION@";
    private final String query;
    private final Partition partition;

    public ScanQuery(String query) {
        this.query = query;
        this.partition = null;
    }

    public ScanQuery(String query, Partition partition) {
        this.query = query;
        this.partition = partition;
    }

    @Override
    public String toString() {
        if (partition == null) return query;
        return query.replaceAll(PARTITION_CONDITION_PLACE_HOLDER, partition.toString());
    }
}
