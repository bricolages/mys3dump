package org.bricolages.mys3dump;

/**
 * Created by shimpei-kodama on 2016/02/26.
 */
class CSVRowFormatter extends DelimitedRowFormatter {
    public CSVRowFormatter(int columnCount) {
        super(",", columnCount);
    }

    @Override
    public String getFormat() {
        return "csv";
    }
}
