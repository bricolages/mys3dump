package org.bricolages.mys3dump;

/**
 * Created by shimpei-kodama on 2016/02/26.
 */
class TSVRowFormatter extends DelimitedRowFormatter {
    public TSVRowFormatter(int columnCount) {
        super("\t", columnCount);
    }

    @Override
    public String getFormat() {
        return "tsv";
    }
}
