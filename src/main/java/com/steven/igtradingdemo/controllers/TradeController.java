package com.steven.igtradingdemo.controllers;

import com.steven.igtradingdemo.dto.PlaceTradeRequest;
import com.steven.igtradingdemo.services.TradeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trade")
public class TradeController {
    private final TradeService tradeService;

    @GetMapping(value = "/place")
    public ResponseEntity<Resource> placeMarketOrder(PlaceTradeRequest placeTradeRequest) throws Exception {
        placeTradeRequest.validateObject();
        File file = tradeService.placeOrderAndReturnZip(placeTradeRequest);
        ByteArrayResource byteArrayResource = new ByteArrayResource(FileUtils.readFileToByteArray(file));
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(file.getName()));
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        try {
            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(byteArrayResource);
        } finally {
            FileUtils.delete(file);
        }
    }
}
