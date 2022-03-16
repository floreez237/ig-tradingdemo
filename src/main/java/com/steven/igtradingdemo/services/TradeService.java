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
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.Direction;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.OrderType;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.GetWatchlistByWatchlistIdV1Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.InstrumentType;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.MarketStatus;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.MarketsItem;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistsV1.GetWatchlistsV1Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistsV1.WatchlistsItem;
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

import javax.annotation.PostConstruct;
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
    private final List<HandyTableListenerAdapter> listeners = new ArrayList<>();
    private AccountBalanceInfo accountBalanceInfo;
    private List<String> tradeableEpics;

    @PostConstruct
    public void init() {
        tradeableEpics = getTradeableEpics();
    }

    public void randomlyPlaceAndOrder() {
        if (tradeableEpics.isEmpty()) {
            log.info("Cannot Randomly place order as there are no available epics");
            return;
        }
        try {
            Random random = new Random();
            String randomEpic = tradeableEpics.get(random.nextInt(tradeableEpics.size()));
            GetMarketDetailsV2Response marketDetails = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), randomEpic);
            int minimumSize = (int) (marketDetails.getDealingRules().getMinDealSize().getValue() * 100);
            int MAX_SIZE = 110;
            int randomDelta = random.nextInt(MAX_SIZE - minimumSize);
            CreateOTCPositionV1Response createOTCPositionV1Response = placeOrder(PlaceTradeRequest.builder()
                    .marketEpic(randomEpic)
                    .positionSize(BigDecimal.valueOf((minimumSize + randomDelta) / 100.0))
                    .tradeDirection(random.nextInt(100) < 50 ? Direction.BUY : Direction.SELL)
                    .build());
            subscribeToStreams();
            AccountBalanceInfo accountBalanceInfo = receiveAccountBalanceInfo();
            String dealReference = createOTCPositionV1Response.getDealReference();
            Map<String, String> tradeUpdate = receiveTradeUpdate(dealReference);
            unsubscribeFromStreams();
            writeMapValuesToFile(accountBalanceInfo.convertToMap(), "account.csv");
            writeMapValuesToFile(tradeUpdate, "order status.csv");
        } catch (Exception e) {
            log.error("An Error occurred while placing random trades");
            e.printStackTrace();
        }
    }

    private void writeMapValuesToFile(Map<String, String> map, String fileName) throws IOException {
        File file = new File(fileName);
        FileWriter fileWriter = new FileWriter(file, true);
        if (FileUtils.sizeOf(file) == 0) {
            fileWriter.write(String.join(",", map.keySet()).concat("\n"));
        }
        fileWriter.write(String.join(",", map.values()).concat("\n"));
        fileWriter.close();
    }

    private List<String> getTradeableEpics() {
        try {
            List<String> epics = new ArrayList<>();
            GetWatchlistsV1Response watchlistsResponse = restApi.getWatchlistsV1(authenticationContext.getConversationContext());
            log.info("Watchlists: {}", watchlistsResponse.getWatchlists().size());
            WatchlistsItem indicesWatchList = watchlistsResponse.getWatchlists().stream()
                    .filter(watchlistsItem -> watchlistsItem.getId().equals("Major FX"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid WatchList ID"));
            GetWatchlistByWatchlistIdV1Response watchlistInstrumentsResponse = restApi.getWatchlistByWatchlistIdV1(authenticationContext.getConversationContext(),
                    indicesWatchList.getId());
            for (MarketsItem market : watchlistInstrumentsResponse.getMarkets()) {
                if (market.getStreamingPricesAvailable() && market.getMarketStatus() == MarketStatus.TRADEABLE
                        && market.getInstrumentType().equals(InstrumentType.CURRENCIES)) {
                    epics.add(market.getEpic());
                }
            }
            /*for (WatchlistsItem watchlist : watchlistsResponse.getWatchlists()) {
                log.info(watchlist.getName() + " : ");
                if (watchlist.getId().equals("Major Indices")) {
                    GetWatchlistByWatchlistIdV1Response watchlistInstrumentsResponse = restApi.getWatchlistByWatchlistIdV1(authenticationContext.getConversationContext(), watchlist.getId());
                    for (MarketsItem market : watchlistInstrumentsResponse.getMarkets()) {
                        if (market.getStreamingPricesAvailable() && market.getMarketStatus() == MarketStatus.TRADEABLE
                                && market.getInstrumentType().equals(InstrumentType.CURRENCIES)) {
                            epics.add(market.getEpic());
                        }
                    }
                }

            }*/
            return epics;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File placeOrderAndReturnZip(PlaceTradeRequest placeTradeRequest) throws Exception {
        CreateOTCPositionV1Response otcPositionV1 = placeOrder(placeTradeRequest);

        subscribeToStreams();
        AccountBalanceInfo accountBalanceInfo = receiveAccountBalanceInfo();
        String dealReference = otcPositionV1.getDealReference();
        Map<String, String> tradeUpdate = receiveTradeUpdate(dealReference);
        unsubscribeFromStreams();
        File accountSummaryFile = MapUtils.convertMapToCsv(accountBalanceInfo.convertToMap(), "account ".concat(dealReference));
        File orderStatusFile = MapUtils.convertMapToCsv(tradeUpdate, "orderStatus ".concat(dealReference));

        return zipFiles(Arrays.asList(accountSummaryFile, orderStatusFile), "Trade summary ".concat(dealReference));
    }

    private CreateOTCPositionV1Response placeOrder(PlaceTradeRequest placeTradeRequest) throws Exception {
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
        Direction tradeDirection = placeTradeRequest.getTradeDirection();
        createPositionRequest.setDirection(tradeDirection);
        createPositionRequest.setOrderType(OrderType.MARKET);
        List<CurrenciesItem> currencies = marketDetails.getInstrument().getCurrencies();
        createPositionRequest.setCurrencyCode(currencies.size() > 0 ? currencies.get(0).getCode() : "GBP");
        createPositionRequest.setSize(positionSize);
        createPositionRequest.setGuaranteedStop(false);
        createPositionRequest.setForceOpen(true);

        log.info(">>> Creating {} position epic={}, expiry={} size={} orderType={} level={} currency={}",
                tradeDirection==Direction.BUY?"long":"short",
                tradeableEpic, createPositionRequest.getExpiry(),
                createPositionRequest.getSize(), createPositionRequest.getOrderType(), createPositionRequest.getLevel(), createPositionRequest.getCurrencyCode());
        return restApi.createOTCPositionV1(authenticationContext.getConversationContext(), createPositionRequest);
    }

    private File zipFiles(Collection<File> fileCollection, String fileName) {
        File zipFile = new File(fileName.concat(".zip"));
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            for (File file : fileCollection) {
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream fileInputStream = new FileInputStream(file);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fileInputStream.read(bytes)) >= 0) {
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
            delay(1000);
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
                    log.error("Invalid Account Info Received from IG: {}",updateInfo);
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
