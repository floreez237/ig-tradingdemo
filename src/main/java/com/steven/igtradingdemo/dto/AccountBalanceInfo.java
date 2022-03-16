package com.steven.igtradingdemo.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class AccountBalanceInfo {
    private BigDecimal pnl;
    private BigDecimal availableCashForTrade;
    private BigDecimal amountDue;
    private BigDecimal funds;
    private BigDecimal margin;
    private BigDecimal availableToDeal;
    private BigDecimal equity;
    private BigDecimal equityUsed;

    public AccountBalanceInfo(BigDecimal pnl, BigDecimal availableCashForTrade, BigDecimal amountDue,
                              BigDecimal funds, BigDecimal margin, BigDecimal availableToDeal, BigDecimal equity, BigDecimal equityUsed) {
        this.pnl = pnl;
        this.availableCashForTrade = availableCashForTrade;
        this.amountDue = amountDue;
        this.funds = funds;
        this.margin = margin;
        this.availableToDeal = availableToDeal;
        this.equity = equity;
        this.equityUsed = equityUsed;
    }

    public AccountBalanceInfo() {
    }

    public static AccountBalanceInfoBuilder builder() {
        return new AccountBalanceInfoBuilder();
    }

    public Map<String, String> convertToMap() {
        Map<String, String> map = new HashMap<>();
        map.put("PNL", convertBigDecimalToString(pnl));
        map.put("AMOUNT_DUE", convertBigDecimalToString(amountDue));
        map.put("AVAILABLE_CASH", convertBigDecimalToString(availableCashForTrade));
        map.put("FUNDS", convertBigDecimalToString(funds));
        map.put("MARGIN", convertBigDecimalToString(margin));
        map.put("AVAILABLE_TO_DEAL", convertBigDecimalToString(availableToDeal));
        map.put("EQUITY", convertBigDecimalToString(equity));
        map.put("EQUITY_USED", convertBigDecimalToString(equityUsed));
        return map;
    }

    private String convertBigDecimalToString(BigDecimal bigDecimal) {
        return bigDecimal.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }


    public BigDecimal getPnl() {
        return this.pnl;
    }

    public void setPnl(BigDecimal pnl) {
        this.pnl = pnl;
    }

    public BigDecimal getAvailableCashForTrade() {
        return this.availableCashForTrade;
    }

    public void setAvailableCashForTrade(BigDecimal availableCashForTrade) {
        this.availableCashForTrade = availableCashForTrade;
    }

    public BigDecimal getAmountDue() {
        return this.amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public BigDecimal getFunds() {
        return this.funds;
    }

    public void setFunds(BigDecimal funds) {
        this.funds = funds;
    }

    public BigDecimal getMargin() {
        return this.margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public BigDecimal getAvailableToDeal() {
        return this.availableToDeal;
    }

    public void setAvailableToDeal(BigDecimal availableToDeal) {
        this.availableToDeal = availableToDeal;
    }

    public BigDecimal getEquity() {
        return this.equity;
    }

    public void setEquity(BigDecimal equity) {
        this.equity = equity;
    }

    public BigDecimal getEquityUsed() {
        return this.equityUsed;
    }

    public void setEquityUsed(BigDecimal equityUsed) {
        this.equityUsed = equityUsed;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AccountBalanceInfo)) return false;
        final AccountBalanceInfo other = (AccountBalanceInfo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$pnl = this.getPnl();
        final Object other$pnl = other.getPnl();
        if (this$pnl == null ? other$pnl != null : !this$pnl.equals(other$pnl)) return false;
        final Object this$availableCashForTrade = this.getAvailableCashForTrade();
        final Object other$availableCashForTrade = other.getAvailableCashForTrade();
        if (this$availableCashForTrade == null ? other$availableCashForTrade != null : !this$availableCashForTrade.equals(other$availableCashForTrade))
            return false;
        final Object this$amountDue = this.getAmountDue();
        final Object other$amountDue = other.getAmountDue();
        if (this$amountDue == null ? other$amountDue != null : !this$amountDue.equals(other$amountDue)) return false;
        final Object this$funds = this.getFunds();
        final Object other$funds = other.getFunds();
        if (this$funds == null ? other$funds != null : !this$funds.equals(other$funds)) return false;
        final Object this$margin = this.getMargin();
        final Object other$margin = other.getMargin();
        if (this$margin == null ? other$margin != null : !this$margin.equals(other$margin)) return false;
        final Object this$availableToDeal = this.getAvailableToDeal();
        final Object other$availableToDeal = other.getAvailableToDeal();
        if (this$availableToDeal == null ? other$availableToDeal != null : !this$availableToDeal.equals(other$availableToDeal))
            return false;
        final Object this$equity = this.getEquity();
        final Object other$equity = other.getEquity();
        if (this$equity == null ? other$equity != null : !this$equity.equals(other$equity)) return false;
        final Object this$equityUsed = this.getEquityUsed();
        final Object other$equityUsed = other.getEquityUsed();
        if (this$equityUsed == null ? other$equityUsed != null : !this$equityUsed.equals(other$equityUsed))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AccountBalanceInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $pnl = this.getPnl();
        result = result * PRIME + ($pnl == null ? 43 : $pnl.hashCode());
        final Object $availableCashForTrade = this.getAvailableCashForTrade();
        result = result * PRIME + ($availableCashForTrade == null ? 43 : $availableCashForTrade.hashCode());
        final Object $amountDue = this.getAmountDue();
        result = result * PRIME + ($amountDue == null ? 43 : $amountDue.hashCode());
        final Object $funds = this.getFunds();
        result = result * PRIME + ($funds == null ? 43 : $funds.hashCode());
        final Object $margin = this.getMargin();
        result = result * PRIME + ($margin == null ? 43 : $margin.hashCode());
        final Object $availableToDeal = this.getAvailableToDeal();
        result = result * PRIME + ($availableToDeal == null ? 43 : $availableToDeal.hashCode());
        final Object $equity = this.getEquity();
        result = result * PRIME + ($equity == null ? 43 : $equity.hashCode());
        final Object $equityUsed = this.getEquityUsed();
        result = result * PRIME + ($equityUsed == null ? 43 : $equityUsed.hashCode());
        return result;
    }

    public String toString() {
        return "AccountBalanceInfo(pnl=" + this.getPnl() + ", availableCashForTrade=" + this.getAvailableCashForTrade() + ", amountDue=" + this.getAmountDue() + ", funds=" + this.getFunds() + ", margin=" + this.getMargin() + ", availableToDeal=" + this.getAvailableToDeal() + ", equity=" + this.getEquity() + ", equityUsed=" + this.getEquityUsed() + ")";
    }

    public static class AccountBalanceInfoBuilder {
        private BigDecimal pnl;
        private BigDecimal availableCashForTrade;
        private BigDecimal amountDue;
        private BigDecimal funds;
        private BigDecimal margin;
        private BigDecimal availableToDeal;
        private BigDecimal equity;
        private BigDecimal equityUsed;

        AccountBalanceInfoBuilder() {
        }

        public AccountBalanceInfoBuilder pnl(BigDecimal pnl) {
            this.pnl = pnl;
            return this;
        }

        public AccountBalanceInfoBuilder availableCashForTrade(BigDecimal availableCashForTrade) {
            this.availableCashForTrade = availableCashForTrade;
            return this;
        }

        public AccountBalanceInfoBuilder amountDue(BigDecimal amountDue) {
            this.amountDue = amountDue;
            return this;
        }

        public AccountBalanceInfoBuilder funds(BigDecimal funds) {
            this.funds = funds;
            return this;
        }

        public AccountBalanceInfoBuilder margin(BigDecimal margin) {
            this.margin = margin;
            return this;
        }

        public AccountBalanceInfoBuilder availableToDeal(BigDecimal availableToDeal) {
            this.availableToDeal = availableToDeal;
            return this;
        }

        public AccountBalanceInfoBuilder equity(BigDecimal equity) {
            this.equity = equity;
            return this;
        }

        public AccountBalanceInfoBuilder equityUsed(BigDecimal equityUsed) {
            this.equityUsed = equityUsed;
            return this;
        }

        public AccountBalanceInfo build() {
            return new AccountBalanceInfo(pnl, availableCashForTrade, amountDue, funds, margin, availableToDeal, equity, equityUsed);
        }

        public String toString() {
            return "AccountBalanceInfo.AccountBalanceInfoBuilder(pnl=" + this.pnl + ", availableCashForTrade=" + this.availableCashForTrade + ", amountDue=" + this.amountDue + ", funds=" + this.funds + ", margin=" + this.margin + ", availableToDeal=" + this.availableToDeal + ", equity=" + this.equity + ", equityUsed=" + this.equityUsed + ")";
        }
    }
}
