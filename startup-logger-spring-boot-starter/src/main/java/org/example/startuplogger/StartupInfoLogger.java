package org.example.startuplogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Arrays;

public class StartupInfoLogger implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger log = LoggerFactory.getLogger(StartupInfoLogger.class);
    private final Environment environment;
    private final DataSourceProperties datasource;

    public StartupInfoLogger(Environment e, DataSourceProperties d) {
        environment = e;
        datasource = d;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        String packageVersion = event.getSpringApplication().getMainApplicationClass() == null ? null : event.getSpringApplication().getMainApplicationClass().getPackage().getImplementationVersion();
        String version = packageVersion == null ? environment.getProperty("app.version", "unknown") : packageVersion;
        String url = datasource.getUrl() == null ? "not configured" : datasource.getUrl().replaceAll("(?i)(password|user)=[^&;]+", "$1=***");
        log.info("Application started: profiles={}, version={}, databaseUrl={}, databaseUser={}, contextStartup={}", Arrays.toString(environment.getActiveProfiles()), version, url, datasource.getUsername() == null ? "not configured" : datasource.getUsername(), event.getTimeTaken());
    }
}
