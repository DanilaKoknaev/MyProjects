package org.example.startuplogger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.startup-logger")
public class StartupLoggerProperties {
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
