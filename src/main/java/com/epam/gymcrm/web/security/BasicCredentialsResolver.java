package com.epam.gymcrm.web.security;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Supplies {@link Credentials} parameters of controller methods from the HTTP Basic
 * {@code Authorization} header. Whether the credentials are correct is checked by the services.
 */
public class BasicCredentialsResolver implements HandlerMethodArgumentResolver {

    private static final String BASIC_PREFIX = "Basic ";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Credentials.class.equals(parameter.getParameterType());
    }

    @Override
    public Credentials resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String header = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
            throw new AuthenticationFailedException();
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(header.substring(BASIC_PREFIX.length()).trim()),
                    StandardCharsets.UTF_8);
            int separator = decoded.indexOf(':');
            if (separator <= 0) {
                throw new AuthenticationFailedException();
            }
            return new Credentials(decoded.substring(0, separator), decoded.substring(separator + 1));
        } catch (IllegalArgumentException e) {
            throw new AuthenticationFailedException();
        }
    }
}
