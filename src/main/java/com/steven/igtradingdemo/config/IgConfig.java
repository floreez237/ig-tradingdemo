package com.steven.igtradingdemo.config;

import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.dto.session.createSessionV2.CreateSessionV2Request;
import com.steven.igtradingdemo.services.TradeService;
import com.steven.igtradingdemo.utils.Constants;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = "com.iggroup.webapi.samples.client")
public class IgConfig {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(IgConfig.class);
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

    @Bean
    public CommandLineRunner addTradesEveryMinute(TradeService tradeService) {
        return args -> {
            int numberOfTrades = 0;
            int maxNumberOfTrades = Constants.NUMBER_OF_RANDOM_TRADES;
            while (numberOfTrades < maxNumberOfTrades) {
                tradeService.randomlyPlaceAndOrder();
                numberOfTrades++;
                Thread.sleep(Constants.INTERVAL_BETWEEN_TRADES_IN_MILLISECONDS);
            }
            log.info("Randomly Opened trades");
        };
    }
}
