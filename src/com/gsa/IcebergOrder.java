package com.gsa;

// Used for IcebergOrders with addition of a peakSize
class IcebergOrder extends Order {
    int peakSize;

    IcebergOrder(char side, int orderId, int qty, short limitPrice, int peakSize) {
        super(side, orderId, qty, limitPrice);
        this.peakSize = peakSize;
    }

    @Override
    public String toString() {
        return "***ORDER [" + side + "|" +
                            orderId + "|" +
                            limitPrice + "|" +
                            totalQty + "|" +
                            limitPrice + "|" +
                            peakSize + "]***";
    }
}
