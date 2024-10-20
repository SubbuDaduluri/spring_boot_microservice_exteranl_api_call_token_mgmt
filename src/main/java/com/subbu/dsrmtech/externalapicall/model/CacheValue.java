package com.subbu.dsrmtech.externalapicall.model;

import java.time.Instant;

public class CacheValue {
    private final String token;
    private final Instant expiry;

    public CacheValue(String token, Instant expiry) {
        this.token = token;
        this.expiry = expiry;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiry);
    }
}
