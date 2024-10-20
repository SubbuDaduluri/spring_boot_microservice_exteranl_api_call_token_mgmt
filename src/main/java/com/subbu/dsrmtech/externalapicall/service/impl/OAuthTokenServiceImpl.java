package com.subbu.dsrmtech.externalapicall.service.impl;

import com.subbu.dsrmtech.externalapicall.exception.OAuth2TokenException;
import com.subbu.dsrmtech.externalapicall.model.CacheValue;
import com.subbu.dsrmtech.externalapicall.service.OAuthTokenService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class OAuthTokenServiceImpl implements OAuthTokenService {

    @Value("${oauth2.client.id}")
    private String clientId;

    @Value("${oauth2.client.secret}")
    private String clientSecret;

    @Value("${oauth2.token.url}")
    private String tokenUrl;
    private long tokenExpiryTime;
    private final ReentrantLock lock = new ReentrantLock();  // Thread-safe caching
    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate;
    private final TokenCacheService tokenCacheService;

    @Autowired
    public OAuthTokenServiceImpl(RestTemplate restTemplate, TokenCacheService tokenCacheService) {
        this.restTemplate = restTemplate;
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public String getAccessToken() {
        if (isTokenExpired()) {
            // Using ReentrantLock to avoid multiple threads trying to refresh the token at the same time
            lock.lock();
            try {
                // Double-check locking
                if (isTokenExpired()) {
                    tokenCacheService.removeToken(clientId);
                    fetchNewToken();
                }
            } finally {
                lock.unlock();
            }
        }
        CacheValue cacheValue = tokenCacheService.getToken(clientId);
        return Objects.nonNull(cacheValue) ? cacheValue.getToken() : null;
    }


    private boolean isTokenExpired() {
        CacheValue cacheValue = tokenCacheService.getToken(clientId);
        return cacheValue == null || cacheValue.isExpired();
    }

    @CircuitBreaker(name = "oauth2TokenApi", fallbackMethod = "fallbackCallExternalTokenApi")
    private void fetchNewToken() {

        try {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "client_credentials");
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForObject(tokenUrl, requestEntity, Map.class);

            String accessToken = (String) response.get("access_token");
            int expiresIn = (int) response.get("expires_in");
            // Subtract 60 seconds for buffer
            Instant expiryTime = Instant.now().plusSeconds(expiresIn * 1000).minusSeconds(6000);
            CacheValue cacheValue = new CacheValue(accessToken, expiryTime);
            tokenCacheService.storeToken(clientId, cacheValue);
        } catch (OAuth2TokenException ex) {
            throw new OAuth2TokenException("Failed to retrieve OAuth2 token: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            throw new OAuth2TokenException("Error while connecting to the OAuth2 token endpoint: " + ex.getMessage(), ex);
        }
    }

    private String fallbackCallExternalTokenApi(Throwable throwable) {
        log.error("Fallback called due to: " + throwable.getMessage());
        return null;
    }
}
