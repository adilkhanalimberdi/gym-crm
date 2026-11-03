package com.epam.gymcrm.web.error;

import com.epam.gymcrm.dto.response.ErrorResponse;
import com.epam.gymcrm.exception.ApiException;
import com.epam.gymcrm.web.logging.TransactionId;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Turns every failure into an {@link ErrorResponse} with a suitable status; internals are never exposed.
 * Domain exceptions carry their own status, Spring MVC exceptions are handled by the base class, so a new
 * exception needs no new handler method.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String BASIC_CHALLENGE = "Basic realm=\"gym-crm\"";
    private static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected error, please contact support with the transaction id";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException exception, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (exception.getStatus() == HttpStatus.UNAUTHORIZED) {
            headers.set(HttpHeaders.WWW_AUTHENTICATE, BASIC_CHALLENGE);
        }
        return handleExceptionInternal(
                exception,
                null,
                headers,
                exception.getStatus(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception exception, WebRequest request) {
        HttpServletRequest servletRequest = servletRequestOf(request);
        log.error(
                "Unexpected error while handling {} {}",
                servletRequest.getMethod(),
                servletRequest.getRequestURI(),
                exception
        );
        return handleExceptionInternal(
                exception,
                null,
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return handleExceptionInternal(
                exception,
                problemOf(status, "Validation failed: " + details),
                headers,
                status,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return handleExceptionInternal(
                exception,
                problemOf(status, "Request body is missing or malformed"),
                headers,
                status,
                request
        );
    }

    /** The default message would echo the rejected value back to the caller. */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String message = "Parameter '" + exception.getPropertyName() + "' has an invalid value";
        return handleExceptionInternal(
                exception,
                problemOf(status, message),
                headers,
                status,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                statusCode.value(),
                HttpStatus.valueOf(statusCode.value()).getReasonPhrase(),
                messageOf(exception, body),
                servletRequestOf(request).getRequestURI(),
                TransactionId.current()
        );
        return ResponseEntity.status(statusCode)
                .headers(headers)
                .body(response);
    }

    private String messageOf(Exception exception, Object body) {
        if (exception instanceof ApiException) {
            return exception.getMessage();
        }
        if (body instanceof ProblemDetail problem && problem.getDetail() != null) {
            return problem.getDetail();
        }
        if (exception instanceof org.springframework.web.ErrorResponse springError
                && springError.getBody().getDetail() != null) {
            return springError.getBody().getDetail();
        }
        return UNEXPECTED_ERROR_MESSAGE;
    }

    private ProblemDetail problemOf(HttpStatusCode status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }

    private HttpServletRequest servletRequestOf(WebRequest request) {
        return ((ServletWebRequest) request).getRequest();
    }
}
