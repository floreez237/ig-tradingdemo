package com.steven.igtradingdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountBalanceInfo {
    private BigDecimal pnl;
    private BigDecimal availableCashForTrade;
    private BigDecimal amountDue;
    private BigDecimal funds;
    private BigDecimal margin;
    private BigDecimal availableToDeal;
    private BigDecimal equity;
    private BigDecimal equityUsed;

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


}
