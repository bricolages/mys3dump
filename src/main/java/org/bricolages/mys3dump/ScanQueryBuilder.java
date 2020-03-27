package org.bricolages.mys3dump;

import org.apache.log4j.Logger;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by shimpei-kodama on 2016/03/01.
 */
class ScanQueryBuilder {
    private final Logger logger = Logger.getLogger(this.getClass());

    private final String table;
    private String query;
    private MySQLDataSource ds;
    private String partitionColumn;
    private int partitionNumber;

    public ScanQueryBuilder(MySQLDataSource ds, String table) {
        this.ds = ds;
        this.table = table;
    }

    public List<ScanQuery> getScanQueries() throws SQLException {
        ResultSetSchema schema = this.ds.getTableSchema(table);
        if (hasPartitionInfo()) {
            return newPartitionStream().map(part -> new ScanQuery(getQuery(schema, true), new Partition(partitionColumn, part.start, part.end))).collect(Collectors.toList());
        } else {
            return Collections.singletonList(new ScanQuery(getQuery(schema, false)));
        }
    }

    public ScanQueryBuilder setQuery(String query) {
        this.query = query;
        return this;
    }

    public ScanQueryBuilder setPartitionInfo(String partitionColumn, int partitionNumber) {
        this.partitionColumn = partitionColumn;
        this.partitionNumber = partitionNumber > 0 ? partitionNumber : 1;
        return this;
    }

    boolean hasPartitionInfo() {
        return partitionColumn != null;
    }

    String getQuery(ResultSetSchema schema, boolean withPlaceHolder) {
        if (query != null) {
            if (withPlaceHolder && !query.contains(ScanQuery.PLACE_HOLDER)) {
                throw new IllegalArgumentException("No place holder in partition query: " + query);
            }
            return query;
        }
        StringJoiner sel = new StringJoiner(",");
        schema.getColumns().forEach(c -> sel.add(c.sqlExpression()));
        StringBuilder qry = new StringBuilder();
        qry.append("SELECT ").append(sel.toString()).append(" FROM ").append(table);
        if (withPlaceHolder) qry.append(" WHERE ").append(ScanQuery.PLACE_HOLDER);
        query = qry.toString();
        return query;
    }

    Stream<Partition> newPartitionStream() throws SQLException {
        String mxQuery = "SELECT min(" + partitionColumn + "), " + "max(" + partitionColumn + ") FROM " + table;
        List<List<Long>> l = ds.execute(mxQuery, Long.class);
        Long min = l.get(0).get(0);
        Long max = l.get(0).get(1);
        if (min == null && max == null) throw new EmptyTableException(table);
        logger.info("Min " + partitionColumn + ": " + min);
        logger.info("Max " + partitionColumn + ": " + max);
        return new Partition(partitionColumn, min, max).split(partitionNumber).stream();
    }
}
