package com.epam.gymcrm.config;

import com.epam.gymcrm.storage.StorageInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan("com.epam.gymcrm")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /** Static because it is a BeanPostProcessor and must be available before regular beans are created. */
    @Bean
    public static StorageInitializer storageInitializer(@Value("${storage.init.file:}") String seedFile) {
        return new StorageInitializer(seedFile);
    }
}
