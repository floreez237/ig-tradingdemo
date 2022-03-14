package com.steven.igtradingdemo.dto;


import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.Direction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceTradeRequest {
    @NotNull(message = "Market epic cannot be Null")
    private String marketEpic;
    @NotNull(message = "Position Size cannot be Null")
    private BigDecimal positionSize;
    @NotNull(message = "Trade Direction cannot be Null")
    private Direction tradeDirection;

    public void validateObject() {
        if (marketEpic == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Market Epic cannot be null");
        }
        if (positionSize == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position size cannot be null");
        }
        if (tradeDirection == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trade direction cannot be null");
        }
    }
}
