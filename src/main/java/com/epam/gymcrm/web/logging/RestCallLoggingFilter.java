package com.epam.gymcrm.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Logs one line per REST call: the endpoint, the request, the status and the response (the error message
 * for failed calls). Passwords are masked and the Authorization header is never logged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RestCallLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("rest.calls");
    private static final int MAX_BODY_LENGTH = 2000;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
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
        String message = "{} {} -> {} in {} ms, request body: {}, response body: {}";
        Object[] arguments = {request.getMethod(), endpoint(request), status, millis,
                body(request.getContentAsByteArray(), request.getCharacterEncoding()),
                body(response.getContentAsByteArray(), response.getCharacterEncoding())};
        if (status >= 500) {
            log.error(message, arguments);
        } else if (status >= 400) {
            log.warn(message, arguments);
        } else {
            log.info(message, arguments);
        }
    }

    private String endpoint(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null ? request.getRequestURI()
                : request.getRequestURI() + "?" + SensitiveDataMasker.maskQuery(query);
    }

    private String body(byte[] content, String encoding) {
        if (content.length == 0) {
            return "<empty>";
        }
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        String text = SensitiveDataMasker.maskJson(new String(content, charset));
        return text.length() > MAX_BODY_LENGTH ? text.substring(0, MAX_BODY_LENGTH) + "..." : text;
    }
}
