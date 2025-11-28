package com.lyw.commonUtil.exception;

public class DuplicateMessageException extends RuntimeException {

    public DuplicateMessageException(String message) {
        super(message);
    }

    public DuplicateMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
