package com.gsa;

// Holds top level Order details
class Order {
    char side;
    int orderId;
    int filledQty;
    int totalQty;
    long entryTime;
    short limitPrice;

    Order(char side, int orderId, int qty, short limitPrice) {
        this.side = side;
        this.orderId = orderId;
        this.totalQty = qty;
        this.filledQty = 0;
        this.entryTime = System.nanoTime();
        this.limitPrice = limitPrice;
    }

    @Override
    public String toString() {
        return "***ORDER [" + side + "|" +
                            orderId + "|" +
                            limitPrice + "|" +
                            totalQty + "|" +
                            limitPrice + "]***";
    }
}
