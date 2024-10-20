package com.subbu.dsrmtech.externalapicall.service;

import org.springframework.http.ResponseEntity;

public interface ExternalApiService {

    ResponseEntity<String> callExternalApi();
}
