package com.lyw.commonUtil.exception;

/**
 * 用户可感知的具体异常，派生该类的异常类 错误信息不会打印在日志中、message 直接返回给前端
 *
 */
public class UserPerceivableSpecificException extends RuntimeException {

    public UserPerceivableSpecificException() {
        super();
    }

    public UserPerceivableSpecificException(String message) {
        super(message);
    }


}
