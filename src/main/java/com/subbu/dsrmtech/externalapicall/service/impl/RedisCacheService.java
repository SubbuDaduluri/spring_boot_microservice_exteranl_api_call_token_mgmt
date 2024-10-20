package com.subbu.dsrmtech.externalapicall.service.impl;

import com.subbu.dsrmtech.externalapicall.model.OAuth2Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "ACCESS_TOKEN_";

    public void storeToken(String key, OAuth2Token oAuth2Token) {
        redisTemplate.opsForValue().set(TOKEN_PREFIX + key, oAuth2Token);
    }

    public OAuth2Token getToken(String key) {
        Object tokenObj = redisTemplate.opsForValue().get(TOKEN_PREFIX + key);
        // Cast the retrieved object to AccessToken safely
        if (tokenObj instanceof OAuth2Token oAuth2Token) {
            return oAuth2Token;
        } else {
            throw new IllegalStateException("Unexpected value type found in Redis");
        }
    }

    public void removeToken(String key) {
        redisTemplate.delete(TOKEN_PREFIX + key);
    }
}
