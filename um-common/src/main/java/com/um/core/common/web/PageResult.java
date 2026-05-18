package com.um.core.common.web;

import java.util.List;

public record PageResult<T>(List<T> items, long page, long size, long total, long totalPages) {

    public static <T> PageResult<T> of(List<T> items, long page, long size, long total) {
        long totalPages = size <= 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(items, page, size, total, totalPages);
    }
}
