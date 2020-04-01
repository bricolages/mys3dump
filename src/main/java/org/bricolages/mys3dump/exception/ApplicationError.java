package org.bricolages.mys3dump.exception;

public class ApplicationError extends RuntimeException {
    public ApplicationError(String message) {
        super(message);
    }

    public ApplicationError(Exception cause) {
        super(cause);
    }
}
