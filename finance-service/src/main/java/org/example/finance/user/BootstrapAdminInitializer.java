package org.example.finance.user;

import org.example.finance.user.model.Role;
import org.example.finance.user.model.User;
import org.example.finance.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap-admin", name = {"email", "password"})
public class BootstrapAdminInitializer implements ApplicationRunner {
    private final BootstrapAdminProperties properties;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public BootstrapAdminInitializer(BootstrapAdminProperties p, UserRepository u, PasswordEncoder e) {
        properties = p;
        users = u;
        encoder = e;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (properties.email() == null || properties.email().isBlank() || properties.password() == null || properties.password().isBlank())
            return;
        users.findByEmailIgnoreCase(properties.email()).orElseGet(() -> users.save(new User(properties.email(), encoder.encode(properties.password()), properties.fullName() == null ? "System Administrator" : properties.fullName(), Role.ADMIN, null)));
    }
}
