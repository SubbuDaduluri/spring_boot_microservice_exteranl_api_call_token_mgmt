package com.subbu.dsrmtech.externalapicall.service.impl;

import com.subbu.dsrmtech.externalapicall.config.OAuth2Properties;
import com.subbu.dsrmtech.externalapicall.exception.OAuth2TokenException;
import com.subbu.dsrmtech.externalapicall.model.CacheValue;
import com.subbu.dsrmtech.externalapicall.service.OAuthTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class OAuthTokenServiceImpl implements OAuthTokenService {

    private long tokenExpiryTime;
    private final ReentrantLock lock = new ReentrantLock();  // Thread-safe caching
    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate;
    private final TokenCacheService tokenCacheService;

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    private final OAuth2Properties oAuth2Properties;

    @Autowired
    public OAuthTokenServiceImpl(RestTemplate restTemplate, TokenCacheService tokenCacheService,
                                 OAuth2AuthorizedClientManager authorizedClientManager,
                                 OAuth2Properties oAuth2Properties) {
        this.restTemplate = restTemplate;
        this.tokenCacheService = tokenCacheService;
        this.authorizedClientManager = authorizedClientManager;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public String getAccessToken() {
        if (isTokenExpired()) {
            // Using ReentrantLock to avoid multiple threads trying to refresh the token at the same time
            lock.lock();
            try {
                // Double-check locking
                if (isTokenExpired()) {
                    tokenCacheService.removeToken(oAuth2Properties.getClientId());
                    fetchNewToken();
                }
            } finally {
                lock.unlock();
            }
        }
        CacheValue cacheValue = tokenCacheService.getToken(oAuth2Properties.getClientId());
        return Objects.nonNull(cacheValue) ? cacheValue.getToken() : null;
    }


    private boolean isTokenExpired() {
        CacheValue cacheValue = tokenCacheService.getToken(oAuth2Properties.getClientId());
        return cacheValue == null || cacheValue.isExpired();
    }

    private void fetchNewToken() {
        try {

            OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId("external-api-client")
                .principal("clientCredentials")
                .build();
            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(request);
            if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
                throw new OAuth2TokenException("Failed to obtain OAuth2 token");
            }
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            // Subtract 60 seconds for buffer
            long issuedAtSec = authorizedClient.getAccessToken().getIssuedAt().getLong(ChronoField.INSTANT_SECONDS);
            long expiredAtSec = authorizedClient.getAccessToken().getExpiresAt().getLong(ChronoField.INSTANT_SECONDS);
            Instant expiryTime = Instant.now().plusSeconds((expiredAtSec - issuedAtSec) * 1000).minusSeconds(6000);
            CacheValue cacheValue = new CacheValue(accessToken, expiryTime);
            tokenCacheService.storeToken(oAuth2Properties.getClientId(), cacheValue);
        } catch (OAuth2AuthorizationException ex) {
            // Handle different types of OAuth2 errors here (e.g., invalid credentials, etc.)
            throw new OAuth2TokenException("OAuth2 Token API authorization failed: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            // Handle connection issues, timeouts, etc.
            throw new OAuth2TokenException("Error while connecting to the OAuth2 Token API: " + ex.getMessage(), ex);
        }

    }

    private void fetchNewTokenRestTemplate() {

        try {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "client_credentials");
            requestBody.add("client_id", oAuth2Properties.getClientId());
            requestBody.add("client_secret", oAuth2Properties.getClientSecret());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForObject(oAuth2Properties.getTokenUrl(), requestEntity, Map.class);

            String accessToken = (String) response.get("access_token");
            int expiresIn = (int) response.get("expires_in");
            // Subtract 60 seconds for buffer
            Instant expiryTime = Instant.now().plusSeconds(expiresIn * 1000).minusSeconds(6000);
            CacheValue cacheValue = new CacheValue(accessToken, expiryTime);
            tokenCacheService.storeToken(oAuth2Properties.getClientId(), cacheValue);
        } catch (OAuth2TokenException ex) {
            throw new OAuth2TokenException("Failed to retrieve OAuth2 token: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            throw new OAuth2TokenException("Error while connecting to the OAuth2 token endpoint: " + ex.getMessage(), ex);
        }
    }

}
