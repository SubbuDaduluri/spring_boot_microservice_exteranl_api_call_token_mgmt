package com.subbu.dsrmtech.externalapicall.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class OAuth2ResponseErrorHandler implements ResponseErrorHandler {


    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        return (statusCode.is4xxClientError() || statusCode.is5xxServerError());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        String responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());

        if (statusCode.is4xxClientError()) {
            handle4xxError(statusCode, responseBody);
        } else if (statusCode.is5xxServerError()) {
            handle5xxError(statusCode, responseBody);
        } else {
            throw new OAuth2TokenException("Unexpected error during OAuth2 token request: " + statusCode);
        }
    }

    private void handle4xxError(HttpStatusCode statusCode, String responseBody) {
        log.error("4xx error occurred: {} - {}", statusCode, responseBody);

        if (statusCode == HttpStatus.UNAUTHORIZED) {
            throw new OAuth2TokenException("Invalid credentials provided. Response: " + responseBody);
        } else if (statusCode == HttpStatus.BAD_REQUEST) {
            throw new OAuth2TokenException("Bad request. Check the request parameters. Response: " + responseBody);
        } else {
            throw new OAuth2TokenException("Client error occurred: " + statusCode + " - " + responseBody);
        }
    }

    private void handle5xxError(HttpStatusCode statusCode, String responseBody) {
        log.error("5xx error occurred: {} - {}", statusCode, responseBody);
        throw new OAuth2TokenException("Server error occurred: " + statusCode + " - " + responseBody);
    }
}
