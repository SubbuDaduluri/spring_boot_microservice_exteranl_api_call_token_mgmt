package com.subbu.dsrmtech.externalapicall.service.impl;

import com.subbu.dsrmtech.externalapicall.service.ExternalApiService;
import com.subbu.dsrmtech.externalapicall.service.OAuthTokenService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalApiServiceImpl implements ExternalApiService {

    @Autowired
    private OAuthTokenService tokenService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    @CircuitBreaker(name = "externalApiService", fallbackMethod = "fallbackGetExternalApiData")
    public ResponseEntity<String> callExternalApi() {
        String accessToken = tokenService.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "https://external-api.com/endpoint",
            HttpMethod.GET,
            entity,
            String.class);

        return response;
    }

    // Fallback method in case of failure
    public ResponseEntity<String> fallbackGetExternalApiData(Throwable throwable) {
        // Log the error or handle it accordingly
        return new ResponseEntity<String>("Default data - External API service is currently unavailable.",
            HttpStatus.OK);
    }

    
}
