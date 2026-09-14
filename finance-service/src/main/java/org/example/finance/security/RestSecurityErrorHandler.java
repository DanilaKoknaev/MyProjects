package org.example.finance.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         AuthenticationException ex) throws IOException {
        write(res, req, 401, "AUTHENTICATION_REQUIRED", "Authentication is required");
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
                       AccessDeniedException ex) throws IOException {
        write(res, req, 403, "ACCESS_DENIED", "Access is denied");
    }

    private void write(HttpServletResponse res, HttpServletRequest req, int status,
                       String code, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().printf("{\"timestamp\":\"%s\",\"status\":%d,\"code\":" +
                "\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"fieldErrors\":[]}",
                Instant.now(), status, code, message, req.getRequestURI());
    }
}
