package org.bricolages.mys3dump;

/**
 * Created by shimpei-kodama on 2016/02/22.
 */
class EmptyTableException extends RuntimeException {
    public EmptyTableException(String table) {
        super(table + " is empty.");
    }
}
