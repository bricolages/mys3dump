package org.bricolages.mys3dump.exception;

/**
 * Created by shimpei-kodama on 2016/02/22.
 */
public class EmptyTableException extends RuntimeException {
    public EmptyTableException(String table) {
        super(table + " is empty.");
    }
}
