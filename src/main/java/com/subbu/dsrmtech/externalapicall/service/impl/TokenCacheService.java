package com.subbu.dsrmtech.externalapicall.service.impl;

import com.subbu.dsrmtech.externalapicall.model.CacheValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class TokenCacheService {

    private final CacheManager cacheManager;

    @Autowired
    public TokenCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void storeToken(String key, CacheValue cacheValue) {
        Cache cache = cacheManager.getCache("oauth2TokenCache");
        if (cache != null) {
            cache.put(key, cacheValue);
        }
    }

    public CacheValue getToken(String key) {
        Cache cache = cacheManager.getCache("oauth2TokenCache");
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            return valueWrapper != null ? (CacheValue) valueWrapper.get() : null;
        }
        return null;
    }

    public void removeToken(String key) {
        Cache cache = cacheManager.getCache("oauth2TokenCache");
        if (cache != null) {
            cache.evict(key);
        }
    }
}
