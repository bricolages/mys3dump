package org.bricolages.mys3dump;

import org.apache.log4j.Logger;
import org.bricolages.mys3dump.exception.ApplicationException;
import org.bricolages.mys3dump.exception.EmptyTableException;

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

    private final String tableName;
    private String query;
    private MySQLDataSource dataSource;
    private String partitionColumn;
    private int partitionNumber;

    public ScanQueryBuilder(MySQLDataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName;
    }

    public List<ScanQuery> getScanQueries() throws SQLException, ApplicationException {
        ResultSetSchema schema = this.dataSource.getTableSchema(tableName);
        if (hasPartitionInfo()) {
            return newPartitionStream().map(part -> new ScanQuery(getQueryWithPlaceHolder(schema), new Partition(partitionColumn, part.start, part.end))).collect(Collectors.toList());
        } else {
            return Collections.singletonList(new ScanQuery(getQuery(schema)));
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

    String getQueryWithPlaceHolder(ResultSetSchema schema) {
        if (query != null) {
            if (!query.contains(ScanQuery.PLACE_HOLDER)) {
                throw new IllegalArgumentException("No place holder in partition query: " + query);
            }
            return query;
        }
        StringBuilder qry = new StringBuilder();
        qry.append(getQuery(schema))
                .append(" WHERE ")
                .append(ScanQuery.PLACE_HOLDER);
        query = qry.toString();
        return query;
    }

    String getQuery(ResultSetSchema schema) {
        if (query != null) {
            return query;
        }
        StringJoiner sel = new StringJoiner(",");
        schema.getColumns().forEach(c -> sel.add(c.sqlExpression()));
        StringBuilder qry = new StringBuilder();
        qry.append("SELECT ")
                .append(sel.toString())
                .append(" FROM ")
                .append(quotedTableName());
        query = qry.toString();
        return query;
    }

    Stream<Partition> newPartitionStream() throws SQLException {
        String mxQuery = "SELECT min(" + partitionColumn + "), " + "max(" + partitionColumn + ") FROM " + quotedTableName();
        List<List<Long>> l = dataSource.execute(mxQuery, Long.class);
        Long min = l.get(0).get(0);
        Long max = l.get(0).get(1);
        if (min == null && max == null) throw new EmptyTableException(tableName);
        logger.info("Min " + partitionColumn + ": " + min);
        logger.info("Max " + partitionColumn + ": " + max);
        return new Partition(partitionColumn, min, max).split(partitionNumber).stream();
    }

    String quotedTableName() {
        return String.format("`%s`", tableName);
    }
}
