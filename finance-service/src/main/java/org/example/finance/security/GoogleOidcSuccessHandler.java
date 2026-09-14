package org.example.finance.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.finance.auth.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOidcSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService auth;

    public GoogleOidcSuccessHandler(AuthService auth) {
        this.auth = auth;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var t = auth.issueForOidcEmail(((OidcUser) authentication.getPrincipal()).getEmail());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().printf("{\"tokenType\":\"Bearer\",\"accessToken\":\"%s\",\"refreshToken\":\"%s\",\"accessExpiresAt\":\"%s\",\"refreshExpiresAt\":\"%s\"}", t.accessToken(), t.refreshToken(), t.accessExpiresAt(), t.refreshExpiresAt());
    }
}
