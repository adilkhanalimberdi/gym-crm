package com.epam.gymcrm.dto;

import com.epam.gymcrm.exception.InvalidRequestException;

/** Which part of a list to read; the page number starts at zero. */
public record PageQuery(int page, int size) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) {
            throw new InvalidRequestException("Page must not be negative");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidRequestException("Page size must be between 1 and " + MAX_SIZE);
        }
    }

    public static PageQuery firstPage() {
        return new PageQuery(0, DEFAULT_SIZE);
    }

    /** How many items come before this page. */
    public int offset() {
        return Math.multiplyExact(page, size);
    }
}
