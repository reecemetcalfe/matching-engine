package com.gsa;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.PriorityQueue;

class OrderBook {
    //Priority Queue ordered by reverse price then entryTime, so head of queue is highest price/lowest time
    private PriorityQueue<OrderBookEntry> buyQueue = new PriorityQueue<>(
                    Comparator.comparing(OrderBookEntry::getLimitPrice).reversed()
                                .thenComparing(OrderBookEntry::getEntryTime));

    //Priority Queue ordered by price then entryTime, so head of queue is lowest price/lowest time
    private PriorityQueue<OrderBookEntry> sellQueue = new PriorityQueue<>(
                    Comparator.comparing(OrderBookEntry::getLimitPrice)
                                .thenComparing(OrderBookEntry::getEntryTime));

    void addOrder(Order order) {
        System.out.println("***Received Order: "+order+"****");  //TODO - remove
        if (order.side == 'B') {
            //Buy - check if matched anything on sellQueue
            int remainingQty = tryMatchBuyOrderToSellQueue(order);
            //Either no match or have remaining visibleQty so add to buyQueue
            if (remainingQty > 0) {
                int visibleQty = remainingQty;
                if (order instanceof IcebergOrder) {
                    visibleQty = Math.min(remainingQty, ((IcebergOrder) order).peakSize);
                }
                buyQueue.add(new OrderBookEntry(visibleQty, remainingQty, order));
            }
        } else {
            //Sell - check if matched anything on buyQueue
            int remainingQty = tryMatchSellOrderToBuyQueue(order);
            //Either no match or have remaining visibleQty so add to sellQueue
            if (remainingQty > 0) {
                int visibleQty = remainingQty;
                if (order instanceof IcebergOrder) {
                    visibleQty = Math.min(remainingQty, ((IcebergOrder) order).peakSize);
                }
                sellQueue.add(new OrderBookEntry(visibleQty, remainingQty, order));
            }
        }
        printOrderBook();
    }

    private int tryMatchBuyOrderToSellQueue(Order order) {
        short orderPrice = order.limitPrice;
        int orderQty = order.totalQty;

        OrderBookEntry topOfBook = sellQueue.peek();
        if (topOfBook == null || topOfBook.limitPrice > orderPrice) return order.totalQty; //No match

        //Work down the book to fill
        int qtyLeftToFill = orderQty;
        while (qtyLeftToFill > 0 && !sellQueue.isEmpty()) {

            if (sellQueue.peek().limitPrice > orderPrice) break;

            //Remove entry from top of book
            topOfBook = sellQueue.poll();

            //Save visible visibleQty in case it's an Iceberg
            int visiblePeakQty = topOfBook.visibleQty;

            int tradeQty = Math.min(topOfBook.totalQty, qtyLeftToFill); //include any hidden

            //Trade
            execute(order, topOfBook.order, topOfBook.limitPrice, tradeQty);
            //Update visibleQty left
            qtyLeftToFill = order.totalQty - order.filledQty;

            if (topOfBook.order.filledQty < topOfBook.order.totalQty) {
                //Taken part of liquidity, amend visibleQty and add back to queue as is [keeps time priority]
                //Set remaining visibleQty of order
                if (topOfBook.order instanceof IcebergOrder) {
                    //If totalQty left > peak size set to peak size
                    int peakSize = ((IcebergOrder) topOfBook.order).peakSize;
                    topOfBook.visibleQty = Math.min(topOfBook.totalQty - topOfBook.order.filledQty, peakSize);
                    //But amend peak size to reflect any part volume taken from next peak
                    int nextPeakQtyUsed = (tradeQty - visiblePeakQty) % peakSize; //Mod peaksize in case we've taken some complete peaks
                    if (nextPeakQtyUsed > 0)
                        topOfBook.visibleQty = peakSize - nextPeakQtyUsed;

                    if (tradeQty >= visiblePeakQty) {
                        //We cleaned out this peak - add as new order with renewed timestamp
                        topOfBook.refreshTimestamp();
                    }
                } else {
                    topOfBook.visibleQty = topOfBook.order.totalQty - topOfBook.order.filledQty;
                }
                sellQueue.add(topOfBook);
            }
        }
        return qtyLeftToFill;
    }

    private int tryMatchSellOrderToBuyQueue(Order order) {
        short orderPrice = order.limitPrice;
        int orderQty = order.totalQty;

        OrderBookEntry topOfBook = buyQueue.peek();
        if (topOfBook == null || topOfBook.limitPrice < orderPrice) return order.totalQty; //No match

        //Work down the book to fill
        int qtyLeftToFill = orderQty;
        while (qtyLeftToFill > 0 && !buyQueue.isEmpty()) {

            if (buyQueue.peek().limitPrice < orderPrice) break;

            //Remove entry from top of book
            topOfBook = buyQueue.poll();

            //Save visible visibleQty in case it's an Iceberg
            int visiblePeakQty = topOfBook.visibleQty;

            int tradeQty = Math.min(topOfBook.totalQty, qtyLeftToFill); //include any hidden

            //Trade
            execute(order, topOfBook.order, topOfBook.limitPrice, tradeQty);
            //Update visibleQty left
            qtyLeftToFill = order.totalQty - order.filledQty;

            if (topOfBook.order.filledQty < topOfBook.order.totalQty) {
                //Taken part of liquidity, amend visibleQty and add back to queue as is [keeps time priority]
                //Set remaining visibleQty of order
                if (topOfBook.order instanceof IcebergOrder) {
                    //If visibleQty left > peak size set to peak size
                    int peakSize = ((IcebergOrder) topOfBook.order).peakSize;
                    topOfBook.visibleQty = Math.min(topOfBook.totalQty - topOfBook.order.filledQty, peakSize);
                    //But amend peak size to reflect any part volume taken from next peak
                    int nextPeakQtyUsed = (tradeQty - visiblePeakQty) % peakSize; //Mod peaksize in case we've taken some complete peaks
                    if (nextPeakQtyUsed > 0)
                        topOfBook.visibleQty = peakSize - nextPeakQtyUsed;

                    if (tradeQty >= visiblePeakQty) {
                        //We cleaned out this peak - add as new order with renewed timestamp
                        topOfBook.refreshTimestamp();
                    }
                } else {
                    topOfBook.visibleQty = topOfBook.order.totalQty - topOfBook.order.filledQty;
                }
                buyQueue.add(topOfBook);
            }
        }
        return qtyLeftToFill;
    }

    private void execute(Order buy, Order sell, short price, int qty) {
        //Amend order qtys
        buy.filledQty += qty;
        sell.filledQty += qty;
        //Generate trade
        Trade trade = new Trade(buy.orderId, sell.orderId, price, qty);
        System.out.println(trade);
    }

    private void printOrderBook() {
        //Need to copy queues as only get correct priority ordering by removing everything,
        //calling toString just prints the tree left to right
        PriorityQueue<OrderBookEntry> buyCopy = new PriorityQueue<>(buyQueue);
        PriorityQueue<OrderBookEntry> sellCopy = new PriorityQueue<>(sellQueue);

        System.out.println("+-----------------------------------------------------------------+");
        System.out.println("| BUY                            | SELL                           |");
        System.out.println("| Id       | Volume      | Price | Price | Volume      | Id       |");
        System.out.println("+----------+-------------+-------+-------+-------------+----------+");

        while (!buyCopy.isEmpty() || !sellCopy.isEmpty()) {
            String bId = "";
            String bVol = "";
            String bPrc = "";
            OrderBookEntry buyEntry = buyCopy.poll();
            if (buyEntry != null) {
                bId = String.format("%s", buyEntry.order.orderId);
                bVol = String.format("%s", NumberFormat.getNumberInstance(Locale.UK).format(buyEntry.visibleQty));
                bPrc = String.format("%s", NumberFormat.getNumberInstance(Locale.UK).format(buyEntry.limitPrice));
            }

            String sId = "";
            String sVol = "";
            String sPrc = "";
            OrderBookEntry sellEntry = sellCopy.poll();
            if (sellEntry != null) {
                sId = String.format("%s", sellEntry.order.orderId);
                sVol = String.format("%s", NumberFormat.getNumberInstance(Locale.UK).format(sellEntry.visibleQty));
                sPrc = String.format("%s", NumberFormat.getNumberInstance(Locale.UK).format(sellEntry.limitPrice));
            }
            System.out.printf("|%10s|%13s|%7s|%7s|%13s|%10s|\n", bId, bVol, bPrc, sPrc, sVol, sId);
        }
        System.out.println("+-----------------------------------------------------------------+");
    }
}
