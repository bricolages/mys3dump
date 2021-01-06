package org.bricolages.mys3dump;

import org.bricolages.mys3dump.exception.ApplicationException;
import org.bricolages.mys3dump.exception.EmptyTableException;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shimpei-kodama on 2016/03/01.
 */
class ScanQueryBuilder {
    static private final Logger logger = LoggerFactory.getLogger(ScanQueryBuilder.class);

    private final String tableName;
    private String query;
    private MySQLDataSource dataSource;
    private String partitionColumn;
    private int partitionNumber;

    public ScanQueryBuilder(MySQLDataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName;
    }

    public List<ScanQuery> buildScanQueries() throws SQLException, ApplicationException {
        ResultSetSchema schema = this.dataSource.getTableSchema(tableName);
        if (hasPartitionInfo()) {
            return newPartitionStream().map(part -> new ScanQuery(buildQueryWithPlaceHolder(schema), new Partition(partitionColumn, part.start, part.end))).collect(Collectors.toList());
        } else {
            return Collections.singletonList(new ScanQuery(buildQuery(schema)));
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

    String buildQueryWithPlaceHolder(ResultSetSchema schema) {
        if (this.query != null) {
            if (! this.query.contains(ScanQuery.PARTITION_CONDITION_PLACE_HOLDER)) {
                throw new IllegalArgumentException("No place holder in partition query: " + this.query);
            }
            return this.query;
        }
        else {
            StringBuilder qry = new StringBuilder();
            qry.append(buildQuery(schema))
                    .append(" WHERE ")
                    .append(ScanQuery.PARTITION_CONDITION_PLACE_HOLDER);
            this.query = qry.toString();
            return this.query;
        }
    }

    String buildQuery(ResultSetSchema schema) {
        if (this.query != null) {
            return this.query;
        }
        else {
            StringJoiner sel = new StringJoiner(",");
            schema.getColumns().forEach(c -> sel.add(c.sqlExpression()));
            StringBuilder qry = new StringBuilder();
            qry.append("SELECT ")
                    .append(sel.toString())
                    .append(" FROM ")
                    .append(quotedTableName());
            this.query = qry.toString();
            return this.query;
        }
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
