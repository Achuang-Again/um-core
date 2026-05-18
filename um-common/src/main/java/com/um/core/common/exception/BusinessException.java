package com.um.core.common.exception;

/**
 * 可预期的业务异常，由全局异常处理器转换为 ApiResponse。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
