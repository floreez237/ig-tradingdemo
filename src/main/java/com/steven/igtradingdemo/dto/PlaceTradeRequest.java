package com.steven.igtradingdemo.dto;


import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.Direction;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PlaceTradeRequest {
    @NotNull(message = "Market epic cannot be Null")
    private String marketEpic;
    @NotNull(message = "Position Size cannot be Null")
    private BigDecimal positionSize;
    @NotNull(message = "Trade Direction cannot be Null")
    private Direction tradeDirection;
}
