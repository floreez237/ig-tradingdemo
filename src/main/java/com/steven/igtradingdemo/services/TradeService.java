package com.steven.igtradingdemo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.CurrenciesItem;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.GetMarketDetailsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.CreateOTCPositionV1Request;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.CreateOTCPositionV1Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.Direction;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.OrderType;
import com.steven.igtradingdemo.dto.PlaceTradeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {
    private final RestAPI restApi;
    private final AuthenticationResponseAndConversationContext authenticationContext;
    private final ObjectMapper objectMapper;

    public void placeOrder(PlaceTradeRequest placeTradeRequest) throws Exception {
        String tradeableEpic = placeTradeRequest.getMarketEpic();
        GetMarketDetailsV2Response marketDetails = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
        BigDecimal minPositionSize = BigDecimal.valueOf(marketDetails.getDealingRules().getMinDealSize().getValue());
        
        CreateOTCPositionV1Request createPositionRequest = new CreateOTCPositionV1Request();
        createPositionRequest.setEpic(tradeableEpic);
        createPositionRequest.setExpiry(marketDetails.getInstrument().getExpiry());
        createPositionRequest.setDirection(placeTradeRequest.getTradeDirection());
        createPositionRequest.setOrderType(OrderType.MARKET);

        List<CurrenciesItem> currencies = marketDetails.getInstrument().getCurrencies();
        createPositionRequest.setCurrencyCode(currencies.size() > 0 ? currencies.get(0).getCode() : "GBP");
        createPositionRequest.setSize(minPositionSize);
        createPositionRequest.setGuaranteedStop(false);
        createPositionRequest.setForceOpen(true);

        log.info(">>> Creating long position epic={}, expiry={} size={} orderType={} level={} currency={}", tradeableEpic, createPositionRequest.getExpiry(),
                createPositionRequest.getSize(), createPositionRequest.getOrderType(), createPositionRequest.getLevel(), createPositionRequest.getCurrencyCode());
        CreateOTCPositionV1Response otcPositionV1 = restApi.createOTCPositionV1(authenticationContext.getConversationContext(), createPositionRequest);
        log.info("Response: {}", objectMapper.writeValueAsString(otcPositionV1));
    }
}
