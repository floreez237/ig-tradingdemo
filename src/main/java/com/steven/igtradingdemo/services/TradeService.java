package com.steven.igtradingdemo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.steven.igtradingdemo.utils.CustomCache;
import com.steven.igtradingdemo.utils.MapUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {
    private final static int MAX_NUMBER_OF_RETRIES = 5;
    private final RestAPI restApi;
    private final AuthenticationResponseAndConversationContext authenticationContext;
    private final ObjectMapper objectMapper;
    private final StreamingAPI streamingAPI;
    private List<HandyTableListenerAdapter> listeners = new ArrayList<>();
    private AccountBalanceInfo accountBalanceInfo;

    public File placeOrder(PlaceTradeRequest placeTradeRequest) throws Exception {
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
        log.info("Account info Map {}", accountBalanceInfo.convertToMap());
        String dealReference = otcPositionV1.getDealReference();
        Map<String, String> tradeUpdate = receiveTradeUpdate(dealReference);
        log.info("Trade Update Map {}",tradeUpdate);
        unsubscribeFromStreams();
        File accountSummaryFile = MapUtils.convertMapToCsv(accountBalanceInfo.convertToMap(), "account ".concat(dealReference));
        File orderStatusFile = MapUtils.convertMapToCsv(tradeUpdate, "orderStatus ".concat(dealReference));

        return zipFiles(Arrays.asList(accountSummaryFile,orderStatusFile),"Trade summary ".concat(dealReference));
    }

    private File zipFiles(Collection<File> fileCollection, String fileName) {
        File zipFile = new File(fileName.concat(".zip"));
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            for (File file : fileCollection) {
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
//                FileUtils.delete(file);
                FileInputStream fileInputStream = new FileInputStream(file);

                byte[] bytes = new byte[1024];
                int length;
                while((length = fileInputStream.read(bytes)) >= 0) {
                    zipOutputStream.write(bytes, 0, length);
                }
                fileInputStream.close();
                FileUtils.delete(file);
            }
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create zip file");
        }
        return zipFile;
    }

    private AccountBalanceInfo receiveAccountBalanceInfo() {
        int numberOfRetries = 0;
        while (numberOfRetries < MAX_NUMBER_OF_RETRIES && accountBalanceInfo == null) {
            delay(500);
            numberOfRetries++;
        }
        return accountBalanceInfo;
    }

    private Map<String, String> receiveTradeUpdate(String dealReference) {
        int numberOfRetries = 0;
        Map<String, String> tradeUpdateMap = null;
        while (numberOfRetries < MAX_NUMBER_OF_RETRIES && tradeUpdateMap == null) {
            delay(500);
            tradeUpdateMap = CustomCache.dealReferenceTradeUpdateMap.get(dealReference);
            numberOfRetries++;
        }
        if (tradeUpdateMap != null) {
            CustomCache.dealReferenceTradeUpdateMap.remove(dealReference);
        }
        return tradeUpdateMap;
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
        listeners.add(streamingAPI.subscribeForConfirms(authenticationContext.getAccountId(), new HandyTableListenerAdapter() {
            @Override
            public void onUpdate(int i, String s, UpdateInfo updateInfo) {
                log.info("Trade confirm update i {} s {} data {}", i, s, updateInfo);
                try {
                    JsonNode orderStatusNode = objectMapper.readTree(updateInfo.toString()).get(0);
                    orderStatusNode = ((ObjectNode) orderStatusNode).remove(Arrays.asList("affectedDeals", "channel"));
                    Map<String, String> map = new HashMap<>();
                    for (Iterator<String> it = orderStatusNode.fieldNames(); it.hasNext(); ) {
                        String fieldName = it.next();
                        map.put(fieldName, orderStatusNode.get(fieldName).asText());
                    }
                    String dealReference = map.get("dealReference");
                    CustomCache.dealReferenceTradeUpdateMap.put(dealReference, map);
                } catch (JsonProcessingException e) {
                    log.error("Error While Parsing Trade Update");
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to parse Trade Update");
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
        listeners.clear();
        streamingAPI.disconnect();
    }

    private void delay(long millisecondsToWait) {
        try {
            Thread.sleep(millisecondsToWait);
        } catch (InterruptedException ignored) {
        }

    }
}
