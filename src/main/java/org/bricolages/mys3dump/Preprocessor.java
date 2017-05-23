package org.bricolages.mys3dump;

import org.apache.log4j.Logger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by shimpei-kodama on 2016/11/07.
 */
class Preprocessor {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final Map<Integer, List<PreprocessOperation>> columnOperations;
    private final Boolean hasApplicableOperation;

    public Preprocessor(ResultSetSchema rsSchema, PreprocessOperation... ops) {
        columnOperations = getColumnOperations(Arrays.asList(ops), rsSchema);
        hasApplicableOperation = !columnOperations.isEmpty();
    }

    public char[][] process(char[][] row) {
        if (!hasApplicableOperation) {
            return row;
        }
        for (Map.Entry<Integer,List<PreprocessOperation>> entry : columnOperations.entrySet()) {
            int idx = entry.getKey().intValue();
            if (isNullValue(row[idx])) {
                continue;
            }
            for (PreprocessOperation op : entry.getValue()) {
                // Modify passed value directly since primitive types are "passed by value"
                row[idx] = op.apply(String.valueOf(row[idx])).toCharArray();
            }
        }
        return row;
    }

    boolean isNullValue(char[] cs) {
        return (cs.length > 0 && cs[0] == '\0');
    }

    Map<Integer, List<PreprocessOperation>> getColumnOperations(List<PreprocessOperation> ops, ResultSetSchema rsSchema) {
        Map<Integer, List<PreprocessOperation>> colOps = new HashMap<>();
        for (int i = 0; i < rsSchema.columnCount; i++) {
            int type = rsSchema.columnTypes[i];
            List<PreprocessOperation> applicable_ops = ops.stream().filter(op -> op.isApplicableTo(type)).filter(op -> op.isValid()).collect(Collectors.toList());
            if (!applicable_ops.isEmpty()) {
                colOps.put(i, applicable_ops);
                logger.info(applicable_ops.stream().map(op -> op.toString()).collect(Collectors.joining(", ", "Set preproc for " + rsSchema.columnNames[i] + ": ", "")));
            }
        }
        return colOps;
    }
}