package org.bricolages.mys3dump;

/**
 * Created by shimpei-kodama on 2016/11/07.
 */
public interface PreprocessOperation {
    String apply(String value);
    Boolean isApplicableTo(int type);
    Boolean isValid();
}