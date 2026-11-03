package com.epam.gymcrm.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Logs one line per REST call at INFO: the endpoint, the status and the duration. The request and response
 * bodies hold personal data, so they are only written at DEBUG, with passwords masked. The Authorization
 * header is never logged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RestCallLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RestCallLoggingFilter.class);

    private static final int MAX_BODY_LENGTH = 2000;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var cachedRequest = new ContentCachingRequestWrapper(request);
        var cachedResponse = new ContentCachingResponseWrapper(response);
        long start = System.nanoTime();
        try {
            chain.doFilter(cachedRequest, cachedResponse);
        } finally {
            logCall(cachedRequest, cachedResponse, (System.nanoTime() - start) / 1_000_000);
            cachedResponse.copyBodyToResponse();
        }
    }

    private void logCall(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long millis) {
        int status = response.getStatus();
        String message = "{} {} -> {} in {} ms";
        Object[] arguments = {request.getMethod(), endpoint(request), status, millis};
        if (status >= 500) {
            log.error(message, arguments);
        } else if (status >= 400) {
            log.warn(message, arguments);
        } else {
            log.info(message, arguments);
        }
        logBodies(request, response);
    }

    private void logBodies(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("{} {} request body: {}, response body: {}",
                request.getMethod(),
                request.getRequestURI(),
                body(request.getContentAsByteArray(), request.getCharacterEncoding()),
                body(response.getContentAsByteArray(), response.getCharacterEncoding()));
    }

    private String endpoint(HttpServletRequest request) {
        String query = request.getQueryString();
        if (query == null) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + SensitiveDataMasker.maskQuery(query);
    }

    private String body(byte[] content, String encoding) {
        if (content.length == 0) {
            return "<empty>";
        }
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        String text = SensitiveDataMasker.maskJson(new String(content, charset));
        if (text.length() > MAX_BODY_LENGTH) {
            return text.substring(0, MAX_BODY_LENGTH) + "...";
        }
        return text;
    }
}
