package com.subbu.dsrmtech.externalapicall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OAuth2TokenException.class)
    public ResponseEntity<String> handleOAuth2TokenException(OAuth2TokenException ex) {
        return new ResponseEntity<>("OAuth2 Token Error: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleRestClientException(RestClientException ex) {
        return new ResponseEntity<>("API connection issue: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler({HttpClientErrorException.class})
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException ex) {
        return new ResponseEntity<>("Error from external API: " + ex.getMessage(), ex.getStatusCode());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
