package com.engine;

// Used to model an entry in the order book - linked to an Order
class OrderBookEntry {
    private long entryTime;     //Time created
    int visibleQty;             //visibleQty
    short limitPrice;           //price
    Order order;                //order which generated this entry

    // Basic Order Entry - default to full order visibleQty & price
    private OrderBookEntry(Order order) {
        this.visibleQty = order.totalQty;
        this.limitPrice = order.limitPrice;
        this.order      = order;
        this.entryTime  = System.nanoTime();
    }

    //Option to specify visible & hidden visibleQty
    OrderBookEntry(int visibleQty, Order order) {
        this(order);
        this.visibleQty = visibleQty;
    }

    //Getters needed for PriorityQueue comparator
    short getLimitPrice() {
        return limitPrice;
    }
    long getEntryTime() {
        return entryTime;
    }
}
