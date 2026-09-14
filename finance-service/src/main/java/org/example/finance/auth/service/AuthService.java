package org.example.finance.auth.service;

import org.example.finance.auth.dto.TokenResponse;
import org.example.finance.auth.model.RefreshToken;
import org.example.finance.auth.repository.RefreshTokenRepository;
import org.example.finance.security.JwtProperties;
import org.example.finance.security.JwtService;
import org.example.finance.user.model.User;
import org.example.finance.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@Transactional
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository tokens;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final JwtProperties properties;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository u, RefreshTokenRepository t, PasswordEncoder p, JwtService j, JwtProperties props) {
        users = u;
        tokens = t;
        passwords = p;
        jwt = j;
        properties = props;
    }

    public TokenResponse login(String email, String password) {
        var user = active(email);
        if (user.getPasswordHash() == null || !passwords.matches(password, user.getPasswordHash()))
            throw new BadCredentialsException("Invalid credentials");
        return issue(user);
    }

    public TokenResponse issueForOidcEmail(String email) {
        return issue(active(email));
    }

    public TokenResponse refresh(String raw) {
        var stored = tokens.findByTokenHash(hash(raw)).orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now()))
            throw new BadCredentialsException("Expired refresh token");
        stored.revoke();
        return issue(stored.getUser());
    }

    public void logout(String raw) {
        tokens.findByTokenHash(hash(raw)).ifPresent(RefreshToken::revoke);
    }

    private User active(String email) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.isEnabled()) throw new BadCredentialsException("User is disabled");
        return user;
    }

    private TokenResponse issue(User user) {
        Instant now = Instant.now();
        String raw = randomToken();
        tokens.save(new RefreshToken(hash(raw), user, now.plus(properties.refreshTtl())));
        return new TokenResponse("Bearer", jwt.createAccessToken(user), raw, now.plus(properties.accessTtl()), now.plus(properties.refreshTtl()));
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
