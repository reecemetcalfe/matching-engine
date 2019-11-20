package com.gsa;

// Used to model an entry in the order book - linked to an Order
class OrderBookEntry {
    private long entryTime;     //Time created
    int visibleQty;             //visibleQty
    int totalQty;               //visibleQty + hidden qty
    short limitPrice;           //price
    Order order;                //order which generated this entry

    // Basic Order Entry - default to full order visibleQty & price
    private OrderBookEntry(Order order) {
        this.visibleQty = order.totalQty;
        this.totalQty   = visibleQty;
        this.limitPrice = order.limitPrice;
        this.order      = order;
        this.entryTime  = System.nanoTime();
    }

    //Option to specify visible & hidden visibleQty
    OrderBookEntry(int visibleQty, int totalQty, Order order) {
        this(order);
        this.visibleQty = visibleQty;
        this.totalQty = totalQty;
    }

    short getLimitPrice() {
        return limitPrice;
    }

    long getEntryTime() {
        return entryTime;
    }

    //Used for re-entering next Peak of iceberg orders
    void refreshTimestamp() {
        this.entryTime = System.nanoTime();
    }
}
