package com.epam.gymcrm.web.logging;

import org.slf4j.MDC;

/**
 * The id that ties together everything that happens while one request is handled. It is kept in the
 * logging context (so every log line carries it) and echoed in a response header. Pass {@link #current()}
 * in the {@link #HEADER} header when calling downstream services.
 */
public final class TransactionId {

    public static final String HEADER = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";

    private TransactionId() {
    }

    /** The id of the request being handled on this thread, or null outside of a request. */
    public static String current() {
        return MDC.get(MDC_KEY);
    }
}
