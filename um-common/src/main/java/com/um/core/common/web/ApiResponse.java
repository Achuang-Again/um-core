package com.um.core.common.web;

import com.um.core.common.constant.ErrorCodes;
import com.um.core.common.trace.TraceContext;

import java.time.Instant;

/**
 * 统一 REST 响应体。
 */
public record ApiResponse<T>(int code, String message, T data, String traceId, Instant timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCodes.SUCCESS, "success", data, TraceContext.get(), Instant.now());
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, TraceContext.get(), Instant.now());
    }
}
