package org.example.startuplogger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StartupLoggerAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(StartupLoggerAutoConfiguration.class)).withBean(DataSourceProperties.class, DataSourceProperties::new);

    @Test
    void enabledByDefault() {
        runner.run(c -> assertThat(c).
                hasSingleBean(StartupInfoLogger.class));
    }

    @Test
    void canBeDisabled() {
        runner.withPropertyValues("app.startup-logger.enabled=false")
                .run(c -> assertThat(c)
                        .doesNotHaveBean(StartupInfoLogger.class));
    }
}
