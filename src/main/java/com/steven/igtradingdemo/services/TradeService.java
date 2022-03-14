package com.steven.igtradingdemo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.StreamingAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.CurrenciesItem;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.GetMarketDetailsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.CreateOTCPositionV1Request;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.CreateOTCPositionV1Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.OrderType;
import com.iggroup.webapi.samples.client.streaming.HandyTableListenerAdapter;
import com.lightstreamer.ls_client.UpdateInfo;
import com.steven.igtradingdemo.dto.AccountBalanceInfo;
import com.steven.igtradingdemo.dto.PlaceTradeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {
    private final RestAPI restApi;
    private final AuthenticationResponseAndConversationContext authenticationContext;
    private final ObjectMapper objectMapper;
    private final StreamingAPI streamingAPI;
    private List<HandyTableListenerAdapter> listeners = new ArrayList<>();
    private AccountBalanceInfo accountBalanceInfo;
    private final static int MAX_NUMBER_OF_RETRIES = 5;
    public void placeOrder(PlaceTradeRequest placeTradeRequest) throws Exception {
        String tradeableEpic = placeTradeRequest.getMarketEpic();
        GetMarketDetailsV2Response marketDetails = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
        BigDecimal minPositionSize = BigDecimal.valueOf(marketDetails.getDealingRules().getMinDealSize().getValue());
        BigDecimal positionSize = placeTradeRequest.getPositionSize();
        if (positionSize.compareTo(minPositionSize) < 0) {
            log.error("Position size {} is less than minimum of {}", positionSize, minPositionSize);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position Size is less than minimum");
        }
        CreateOTCPositionV1Request createPositionRequest = new CreateOTCPositionV1Request();
        createPositionRequest.setEpic(tradeableEpic);
        createPositionRequest.setExpiry(marketDetails.getInstrument().getExpiry());
        createPositionRequest.setDirection(placeTradeRequest.getTradeDirection());
        createPositionRequest.setOrderType(OrderType.MARKET);

        List<CurrenciesItem> currencies = marketDetails.getInstrument().getCurrencies();
        createPositionRequest.setCurrencyCode(currencies.size() > 0 ? currencies.get(0).getCode() : "GBP");
        createPositionRequest.setSize(positionSize);
        createPositionRequest.setGuaranteedStop(false);
        createPositionRequest.setForceOpen(true);

        log.info(">>> Creating long position epic={}, expiry={} size={} orderType={} level={} currency={}", tradeableEpic, createPositionRequest.getExpiry(),
                createPositionRequest.getSize(), createPositionRequest.getOrderType(), createPositionRequest.getLevel(), createPositionRequest.getCurrencyCode());
        CreateOTCPositionV1Response otcPositionV1 = restApi.createOTCPositionV1(authenticationContext.getConversationContext(), createPositionRequest);

        subscribeToStreams();
        AccountBalanceInfo accountBalanceInfo = receiveAccountBalanceInfo();
        log.info("Map {}",accountBalanceInfo.convertToMap());
        unsubscribeFromStreams();
    }

    private AccountBalanceInfo receiveAccountBalanceInfo() {
        int numberOfRetries = 0;
        while (numberOfRetries < MAX_NUMBER_OF_RETRIES && accountBalanceInfo== null) {
            delay(1000);
            numberOfRetries++;
        }
        return accountBalanceInfo;
    }

    private void subscribeToStreams() throws Exception {
        log.info("Subscribing to Lightstreamer account updates");
        streamingAPI.connect(authenticationContext.getAccountId(), authenticationContext.getConversationContext(), authenticationContext.getLightstreamerEndpoint());
        listeners.add(streamingAPI.subscribeForAccountBalanceFullInfo(authenticationContext.getAccountId(), new HandyTableListenerAdapter() {
            @Override
            public void onUpdate(int i, String s, UpdateInfo updateInfo) {
                try {
                    log.info("Received Account Info");
                    JsonNode jsonNode = objectMapper.readTree(updateInfo.toString());
                    if (jsonNode.isArray()) {
                        accountBalanceInfo = convertToAccountBalanceInfo(jsonNode);
                        log.info("Account Balancing Info parsed");
                    } else {
                        log.error("Error While Parsing Account balance info. Array Expected");
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to parse Account Balance info");
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error While Parsing Account balance info");
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to parse Account Balance info");
                }
            }
        }));
    }

    private AccountBalanceInfo convertToAccountBalanceInfo(JsonNode jsonNode) {
        return AccountBalanceInfo.builder()
                .pnl(BigDecimal.valueOf(jsonNode.get(0).asDouble()))
                .margin(BigDecimal.valueOf(jsonNode.get(2).asDouble()))
                .amountDue(BigDecimal.valueOf(jsonNode.get(3).asDouble()))
                .availableCashForTrade(BigDecimal.valueOf(jsonNode.get(4).asDouble()))
                .funds(BigDecimal.valueOf(jsonNode.get(5).asDouble()))
                .availableToDeal(BigDecimal.valueOf(jsonNode.get(6).asDouble()))
                .equity(BigDecimal.valueOf(jsonNode.get(7).asDouble()))
                .equityUsed(BigDecimal.valueOf(jsonNode.get(8).asDouble()))
                .build();
    }

    private void unsubscribeFromStreams() {
        listeners.forEach(handyTableListenerAdapter -> {
            try {
                streamingAPI.unsubscribe(handyTableListenerAdapter.getSubscribedTableKey());
            } catch (Exception e) {
                log.error("Error During Unsubscription");
                e.printStackTrace();
            }
        });
        streamingAPI.disconnect();
    }

    private void delay(long millisecondsToWait) {
        try {
            Thread.sleep(millisecondsToWait);
        } catch (InterruptedException ignored) {
        }

    }
}
