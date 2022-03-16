package com.steven.igtradingdemo.dto;

import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV1.Direction;

import java.math.BigDecimal;

public class TradeUpdate {
    private Direction direction;
    private String dealId;
    private String expiry;
    private BigDecimal positionSize;

    public TradeUpdate(Direction direction, String dealId, String expiry, BigDecimal positionSize) {
        this.direction = direction;
        this.dealId = dealId;
        this.expiry = expiry;
        this.positionSize = positionSize;
    }

    public TradeUpdate() {
    }

    public static TradeUpdateBuilder builder() {
        return new TradeUpdateBuilder();
    }

    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getDealId() {
        return this.dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getExpiry() {
        return this.expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public BigDecimal getPositionSize() {
        return this.positionSize;
    }

    public void setPositionSize(BigDecimal positionSize) {
        this.positionSize = positionSize;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TradeUpdate)) return false;
        final TradeUpdate other = (TradeUpdate) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$direction = this.getDirection();
        final Object other$direction = other.getDirection();
        if (this$direction == null ? other$direction != null : !this$direction.equals(other$direction)) return false;
        final Object this$dealId = this.getDealId();
        final Object other$dealId = other.getDealId();
        if (this$dealId == null ? other$dealId != null : !this$dealId.equals(other$dealId)) return false;
        final Object this$expiry = this.getExpiry();
        final Object other$expiry = other.getExpiry();
        if (this$expiry == null ? other$expiry != null : !this$expiry.equals(other$expiry)) return false;
        final Object this$positionSize = this.getPositionSize();
        final Object other$positionSize = other.getPositionSize();
        if (this$positionSize == null ? other$positionSize != null : !this$positionSize.equals(other$positionSize))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TradeUpdate;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $direction = this.getDirection();
        result = result * PRIME + ($direction == null ? 43 : $direction.hashCode());
        final Object $dealId = this.getDealId();
        result = result * PRIME + ($dealId == null ? 43 : $dealId.hashCode());
        final Object $expiry = this.getExpiry();
        result = result * PRIME + ($expiry == null ? 43 : $expiry.hashCode());
        final Object $positionSize = this.getPositionSize();
        result = result * PRIME + ($positionSize == null ? 43 : $positionSize.hashCode());
        return result;
    }

    public String toString() {
        return "TradeUpdate(direction=" + this.getDirection() + ", dealId=" + this.getDealId() + ", expiry=" + this.getExpiry() + ", positionSize=" + this.getPositionSize() + ")";
    }

    public static class TradeUpdateBuilder {
        private Direction direction;
        private String dealId;
        private String expiry;
        private BigDecimal positionSize;

        TradeUpdateBuilder() {
        }

        public TradeUpdateBuilder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        public TradeUpdateBuilder dealId(String dealId) {
            this.dealId = dealId;
            return this;
        }

        public TradeUpdateBuilder expiry(String expiry) {
            this.expiry = expiry;
            return this;
        }

        public TradeUpdateBuilder positionSize(BigDecimal positionSize) {
            this.positionSize = positionSize;
            return this;
        }

        public TradeUpdate build() {
            return new TradeUpdate(direction, dealId, expiry, positionSize);
        }

        public String toString() {
            return "TradeUpdate.TradeUpdateBuilder(direction=" + this.direction + ", dealId=" + this.dealId + ", expiry=" + this.expiry + ", positionSize=" + this.positionSize + ")";
        }
    }
}
