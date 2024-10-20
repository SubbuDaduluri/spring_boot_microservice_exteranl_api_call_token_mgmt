package com.subbu.dsrmtech.externalapicall.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;


public class OAuth2Token implements Serializable {
    private String token;
    private Instant expiry;

    public OAuth2Token() {}

    public OAuth2Token(String token, Instant expiry) {
        this.token = token;
        this.expiry = expiry;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiry() {
        return expiry;
    }

    @JsonIgnore
    public boolean isExpired() {
        return Instant.now().isAfter(expiry);
    }
}
