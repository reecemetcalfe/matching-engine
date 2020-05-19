package com.engine;

// Holds top level Order details
class Order {
    char side;
    int orderId;
    int filledQty;
    int totalQty;
    short limitPrice;

    /* Assumed all orders have a limit price i.e. no 'at best' */
    Order(char side, int orderId, short limitPrice, int qty) {
        this.side = side;
        this.orderId = orderId;
        this.totalQty = qty;
        this.filledQty = 0;
        this.limitPrice = limitPrice;
    }
}
