package com.gsa;

// Model IcebergOrders with addition of a peakSize
class IcebergOrder extends Order {
    int peakSize;

    IcebergOrder(char side, int orderId, short limitPrice, int qty, int peakSize) {
        super(side, orderId, limitPrice, qty);
        this.peakSize = peakSize;
    }
}
