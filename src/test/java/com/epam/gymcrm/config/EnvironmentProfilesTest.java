package com.epam.gymcrm.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentProfilesTest {

    private static final String POOL_SIZE = "spring.datasource.hikari.maximum-pool-size";

    private static Properties load(String profile) throws IOException {
        return PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-" + profile + ".properties"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"dev", "stg", "prod"})
    void deployedEnvironments_takeCredentialsFromTheEnvironment(String profile) throws IOException {
        Properties properties = load(profile);

        assertEquals("${DB_USERNAME}", properties.getProperty("spring.datasource.username"));
        assertEquals("${DB_PASSWORD}", properties.getProperty("spring.datasource.password"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"dev", "stg", "prod"})
    void deployedEnvironments_manageTheSchemaWithMigrations(String profile) throws IOException {
        Properties properties = load(profile);

        assertEquals("true", properties.getProperty("spring.flyway.enabled"));
        assertEquals("validate", properties.getProperty("spring.jpa.hibernate.ddl-auto"));
    }

    @Test
    void local_usesInMemoryDatabase() throws IOException {
        Properties properties = load("local");

        assertTrue(properties.getProperty("spring.datasource.url").startsWith("jdbc:h2:mem:"));
        assertEquals("create-drop", properties.getProperty("spring.jpa.hibernate.ddl-auto"));
    }

    @Test
    void stagingAndProduction_haveDifferentPoolsFromDevelopment() throws IOException {
        Set<String> poolSizes = Stream.of(load("dev"), load("stg"), load("prod"))
                .map(properties -> properties.getProperty(POOL_SIZE))
                .collect(Collectors.toSet());

        assertEquals(Set.of("5", "10", "20"), poolSizes);
    }
}
