package com.engine;

//Models a trade
class Trade {
    private final TradeId tradeId;
    private final short price;
    int qty;

    Trade(TradeId tradeId, short price, int qty) {
        this.tradeId = tradeId;
        this.price = price;
        this.qty = qty;
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d,%d", tradeId.buyOrderId, tradeId.sellOrderId, price, qty);
    }

    void printTrade() {
        System.out.println(toString());
    }
}
