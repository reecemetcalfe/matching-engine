package com.gsa;

class Trade {
    private int buyOrderId;
    private int sellOrderId;
    private short price;
    private int qty;

    Trade(int buyOrderId, int sellOrderId, short price, int qty) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.qty = qty;
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d,%d", buyOrderId, sellOrderId, price, qty);
    }
}
