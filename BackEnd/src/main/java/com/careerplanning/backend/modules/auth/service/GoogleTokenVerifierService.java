package com.careerplanning.backend.modules.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Locale;
import java.util.Set;

@Service
public class GoogleTokenVerifierService {

    private static final Set<String> VALID_ISSUERS = Set.of(
            "accounts.google.com",
            "https://accounts.google.com"
    );

    private final String googleClientId;
    private final RestClient restClient;

    public GoogleTokenVerifierService(@Value("${google.auth.client-id:}") String googleClientId) {
        this.googleClientId = googleClientId == null ? "" : googleClientId.trim();
        this.restClient = RestClient.builder().build();
    }

    public VerifiedGoogleUser verifyIdToken(String idToken) {
        String normalizedToken = idToken == null ? "" : idToken.trim();
        if (normalizedToken.isBlank()) {
            throw new IllegalArgumentException("Google ID token is required");
        }

        if (googleClientId.isBlank()) {
            throw new IllegalArgumentException("Google sign-in is not configured on server");
        }

        GoogleTokenInfo tokenInfo;
        try {
            tokenInfo = restClient.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={idToken}", normalizedToken)
                    .retrieve()
                    .body(GoogleTokenInfo.class);
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Invalid Google token");
        }

        if (tokenInfo == null) {
            throw new IllegalArgumentException("Invalid Google token");
        }

        if (!googleClientId.equals(tokenInfo.aud())) {
            throw new IllegalArgumentException("Google token audience mismatch");
        }

        if (!VALID_ISSUERS.contains(tokenInfo.iss())) {
            throw new IllegalArgumentException("Google token issuer mismatch");
        }

        if (!Boolean.parseBoolean(tokenInfo.emailVerified())) {
            throw new IllegalArgumentException("Google account email is not verified");
        }

        String email = tokenInfo.email() == null ? "" : tokenInfo.email().trim().toLowerCase(Locale.ROOT);
        if (email.isBlank()) {
            throw new IllegalArgumentException("Google account email is missing");
        }

        String subject = tokenInfo.sub() == null ? "" : tokenInfo.sub().trim();
        if (subject.isBlank()) {
            throw new IllegalArgumentException("Google account identity is missing");
        }

        String fullName = tokenInfo.name() == null ? "" : tokenInfo.name().trim();
        if (fullName.isBlank()) {
            fullName = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
        }

        return new VerifiedGoogleUser(email, fullName, subject);
    }

    private record GoogleTokenInfo(
            String aud,
            String iss,
            String email,
            @JsonProperty("email_verified") String emailVerified,
            String name,
            String sub
    ) {
    }

    public record VerifiedGoogleUser(String email, String fullName, String subject) {
    }
}
