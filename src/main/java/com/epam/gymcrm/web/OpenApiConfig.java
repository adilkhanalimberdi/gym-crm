package com.epam.gymcrm.web;

import com.epam.gymcrm.dto.Credentials;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Gym CRM API",
        version = "1.0",
        description = "Trainees, trainers and their trainings. Everything except registration and login "
                + "needs the username and password of the caller in an HTTP Basic Authorization header."))
@SecurityScheme(name = OpenApiConfig.BASIC_AUTH, type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {

    public static final String BASIC_AUTH = "basicAuth";

    static {
        // Credentials come from the Authorization header, not from request parameters
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(Credentials.class);
    }
}
