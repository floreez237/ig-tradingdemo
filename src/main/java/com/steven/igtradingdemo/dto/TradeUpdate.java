package com.steven.igtradingdemo.dto;

import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.Direction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeUpdate {
    private Direction direction;
    private String dealId;
    private String expiry;
    private BigDecimal positionSize;
}
