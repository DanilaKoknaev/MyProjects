package org.example.finance.auth.dto;

import java.time.Instant;

public record TokenResponse(String tokenType, String accessToken, String refreshToken, Instant accessExpiresAt,
                            Instant refreshExpiresAt) {
}
