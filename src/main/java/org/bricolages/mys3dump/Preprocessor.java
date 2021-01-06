package org.bricolages.mys3dump;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shimpei-kodama on 2016/11/07.
 */
class Preprocessor {
    static private final Logger logger = LoggerFactory.getLogger(Preprocessor.class);

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
        for (int i = 0; i < rsSchema.getColumnCount(); i++) {
            int type = rsSchema.getColumnTypes().get(i);
            List<PreprocessOperation> applicable_ops = ops.stream().filter(op -> op.isApplicableTo(type)).filter(op -> op.isValid()).collect(Collectors.toList());
            if (!applicable_ops.isEmpty()) {
                colOps.put(i, applicable_ops);
                logger.info(applicable_ops.stream().map(op -> op.toString()).collect(Collectors.joining(", ", "Set preproc for " + rsSchema.getColumnNames().get(i) + ": ", "")));
            }
        }
        return colOps;
    }
}
