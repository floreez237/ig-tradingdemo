package com.steven.igtradingdemo.dto;


import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

public class PlaceTradeRequest {
    private String marketEpic;
    private BigDecimal positionSize;
    private Direction tradeDirection;

    public PlaceTradeRequest(String marketEpic, BigDecimal positionSize, Direction tradeDirection) {
        this.marketEpic = marketEpic;
        this.positionSize = positionSize;
        this.tradeDirection = tradeDirection;
    }

    public PlaceTradeRequest() {
    }

    public static PlaceTradeRequestBuilder builder() {
        return new PlaceTradeRequestBuilder();
    }

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

    public String getMarketEpic() {
        return this.marketEpic;
    }

    public void setMarketEpic(String marketEpic) {
        this.marketEpic = marketEpic;
    }

    public BigDecimal getPositionSize() {
        return this.positionSize;
    }

    public void setPositionSize(BigDecimal positionSize) {
        this.positionSize = positionSize;
    }

    public Direction getTradeDirection() {
        return this.tradeDirection;
    }

    public void setTradeDirection(Direction tradeDirection) {
        this.tradeDirection = tradeDirection;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PlaceTradeRequest)) return false;
        final PlaceTradeRequest other = (PlaceTradeRequest) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$marketEpic = this.getMarketEpic();
        final Object other$marketEpic = other.getMarketEpic();
        if (this$marketEpic == null ? other$marketEpic != null : !this$marketEpic.equals(other$marketEpic))
            return false;
        final Object this$positionSize = this.getPositionSize();
        final Object other$positionSize = other.getPositionSize();
        if (this$positionSize == null ? other$positionSize != null : !this$positionSize.equals(other$positionSize))
            return false;
        final Object this$tradeDirection = this.getTradeDirection();
        final Object other$tradeDirection = other.getTradeDirection();
        if (this$tradeDirection == null ? other$tradeDirection != null : !this$tradeDirection.equals(other$tradeDirection))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PlaceTradeRequest;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $marketEpic = this.getMarketEpic();
        result = result * PRIME + ($marketEpic == null ? 43 : $marketEpic.hashCode());
        final Object $positionSize = this.getPositionSize();
        result = result * PRIME + ($positionSize == null ? 43 : $positionSize.hashCode());
        final Object $tradeDirection = this.getTradeDirection();
        result = result * PRIME + ($tradeDirection == null ? 43 : $tradeDirection.hashCode());
        return result;
    }

    public String toString() {
        return "PlaceTradeRequest(marketEpic=" + this.getMarketEpic() + ", positionSize=" + this.getPositionSize() + ", tradeDirection=" + this.getTradeDirection() + ")";
    }

    public static class PlaceTradeRequestBuilder {
        private String marketEpic;
        private BigDecimal positionSize;
        private Direction tradeDirection;

        PlaceTradeRequestBuilder() {
        }

        public PlaceTradeRequestBuilder marketEpic(String marketEpic) {
            this.marketEpic = marketEpic;
            return this;
        }

        public PlaceTradeRequestBuilder positionSize(BigDecimal positionSize) {
            this.positionSize = positionSize;
            return this;
        }

        public PlaceTradeRequestBuilder tradeDirection(Direction tradeDirection) {
            this.tradeDirection = tradeDirection;
            return this;
        }

        public PlaceTradeRequest build() {
            return new PlaceTradeRequest(marketEpic, positionSize, tradeDirection);
        }

        public String toString() {
            return "PlaceTradeRequest.PlaceTradeRequestBuilder(marketEpic=" + this.marketEpic + ", positionSize=" + this.positionSize + ", tradeDirection=" + this.tradeDirection + ")";
        }
    }
}
