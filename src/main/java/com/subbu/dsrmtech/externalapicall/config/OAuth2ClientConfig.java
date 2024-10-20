package com.subbu.dsrmtech.externalapicall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientId;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OAuth2ClientConfig {

    @Autowired
    private OAuth2Properties oAuth2Properties;

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        Map<OAuth2AuthorizedClientId, OAuth2AuthorizedClient> authorizedClients = new HashMap<>();

        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        // Load the external configuration
        Map<String, ClientRegistration> registrations = new HashMap<>();
        // You would parse your YAML file and populate registrations here
        registrations.put("external-api-client", loadClientRegistration());

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration loadClientRegistration() {
        return ClientRegistration.withRegistrationId("external-api-client")
            .clientId(oAuth2Properties.getClientId())
            .clientSecret(oAuth2Properties.getClientSecret())
            .scope("read")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri(oAuth2Properties.getTokenUrl()) // Replace with your token URI
            .build();
    }
}

