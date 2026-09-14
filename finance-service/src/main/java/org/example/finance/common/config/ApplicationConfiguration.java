package org.example.finance.common.config;

import org.example.finance.user.BootstrapAdminProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BootstrapAdminProperties.class)
public class ApplicationConfiguration {
}
