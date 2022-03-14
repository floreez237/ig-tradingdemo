package com.steven.igtradingdemo.controllers;

import com.steven.igtradingdemo.dto.PlaceTradeRequest;
import com.steven.igtradingdemo.services.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trade")
public class TradeController {
    private final TradeService tradeService;

    @PostMapping("/place")
    public void placeMarketOrder(@Valid @RequestBody PlaceTradeRequest placeTradeRequest) throws Exception {
        tradeService.placeOrder(placeTradeRequest);
    }
}
