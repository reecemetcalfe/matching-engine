package com.gsa;

import java.util.Objects;

//Holds two parties involved in a Trade
class TradeId {
    int buyOrderId;
    int sellOrderId;

    TradeId(int buyOrderId, int sellOrderId) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof TradeId)) return false;
        TradeId id = (TradeId)o;
        return buyOrderId == id.buyOrderId && sellOrderId == id.sellOrderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(buyOrderId, sellOrderId);
    }
}
