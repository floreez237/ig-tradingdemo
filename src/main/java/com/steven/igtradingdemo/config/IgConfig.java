package com.steven.igtradingdemo.config;

import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.dto.session.createSessionV2.CreateSessionV2Request;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = "com.iggroup.webapi.samples.client")
@Slf4j
public class IgConfig {
    @Value("${ig.username}")
    private String userName;
    @Value("${ig.password}")
    private String password;
    @Value("${ig.api-key}")
    private String apiKey;

    @Bean
    public HttpClient httpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public AuthenticationResponseAndConversationContext authenticationContext(RestAPI restApi) {
        log.info("Connecting as {}", userName);

        boolean encrypt = Boolean.TRUE;

        CreateSessionV2Request authRequest = new CreateSessionV2Request();
        authRequest.setIdentifier(userName);
        authRequest.setPassword(password);
        authRequest.setEncryptedPassword(encrypt);
        return restApi.createSession(authRequest, apiKey, encrypt);
    }
}
