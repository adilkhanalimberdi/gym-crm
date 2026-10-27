package com.epam.gymcrm.web.error;

import java.time.Instant;

/** Body of every error answer; the transaction id lets a caller find the matching log lines. */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path,
                            String transactionId) {
}
