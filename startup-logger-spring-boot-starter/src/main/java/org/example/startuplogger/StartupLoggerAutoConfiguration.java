package org.example.startuplogger;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(DataSourceProperties.class)
@ConditionalOnProperty(prefix = "app.startup-logger", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(StartupLoggerProperties.class)
public class StartupLoggerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    StartupInfoLogger startupInfoLogger(org.springframework.core.env.Environment environment, DataSourceProperties properties) {
        return new StartupInfoLogger(environment, properties);
    }
}
