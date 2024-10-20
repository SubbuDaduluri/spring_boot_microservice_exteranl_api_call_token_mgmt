package com.subbu.dsrmtech.externalapicall.controller;

import com.subbu.dsrmtech.externalapicall.service.ExternalApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

    @Autowired
    private ExternalApiService externalApiService;


    @GetMapping("/call")
    public ResponseEntity<String> callExternalApi() {
        return externalApiService.callExternalApi();
    }
}
