package org.bricolages.mys3dump;

import java.io.IOException;

/**
 * Created by shimpei-kodama on 2016/02/08.
 */
interface RowWriterFactory {
    RowWriter newRowWriter() throws IOException;
}
