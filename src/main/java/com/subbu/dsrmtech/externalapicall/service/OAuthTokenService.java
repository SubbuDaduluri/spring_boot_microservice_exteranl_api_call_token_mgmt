package com.subbu.dsrmtech.externalapicall.service;

import reactor.core.publisher.Mono;

public interface OAuthTokenService {

    String getAccessToken();
}
