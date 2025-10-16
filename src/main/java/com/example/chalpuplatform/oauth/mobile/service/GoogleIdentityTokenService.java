package com.example.chalpuplatform.oauth.mobile.service;

import com.example.chalpuplatform.common.exception.AuthException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GoogleIdentityTokenService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityTokenService(@Value("${google.mobile.client-ids}") List<String> mobileClientIds) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(mobileClientIds)
                .build();
        log.info("GoogleIdTokenVerifier initialized for mobile client IDs: {}", mobileClientIds);
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.error("Invalid Google ID token. The token couldn't be verified.");
                throw new AuthException(ErrorMessage.AUTH_INVALID_TOKEN);
            }
            log.info("Google ID Token verification successful for email: {}", idToken.getPayload().getEmail());
            return idToken.getPayload();
        } catch (Exception e) {
            log.error("Google ID Token verification failed: {}", e.getMessage(), e);
            throw new AuthException(ErrorMessage.AUTH_INVALID_TOKEN);
        }
    }
} 