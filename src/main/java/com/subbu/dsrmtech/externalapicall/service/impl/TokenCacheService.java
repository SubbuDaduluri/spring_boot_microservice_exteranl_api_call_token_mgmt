package com.subbu.dsrmtech.externalapicall.service.impl;

import com.subbu.dsrmtech.externalapicall.model.OAuth2Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final CacheManager cacheManager;

    @Autowired
    public TokenCacheService(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;

    }

    public void storeToken(String key, OAuth2Token cacheValue) {
        Cache cache = cacheManager.getCache("oauth2TokenCache");
        if (cache != null) {
            cache.put(key, cacheValue);
         }
    }

    public OAuth2Token getToken(String key) {
        Cache cache = cacheManager.getCache("oauth2TokenCache");

        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            return valueWrapper != null ? (OAuth2Token) valueWrapper.get() : null;
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
