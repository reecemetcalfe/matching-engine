package com.gsa;

import java.text.NumberFormat;
import java.util.*;

/*
 * Holds Order Book as two Priority Queues, buy and sell.
 * When Orders are added they are attempted to be matched to the book,
 * otherwise they are added themselves if not fully filled
 */
class OrderBook {
    //Priority Queue ordered by reverse price then entryTime, so head of queue is highest price/lowest time
    private PriorityQueue<OrderBookEntry> buyQueue = new PriorityQueue<>(
                    Comparator.comparing(OrderBookEntry::getLimitPrice).reversed()
                                .thenComparing(OrderBookEntry::getEntryTime));

    //Priority Queue ordered by price then entryTime, so head of queue is lowest price/lowest time
    private PriorityQueue<OrderBookEntry> sellQueue = new PriorityQueue<>(
                    Comparator.comparing(OrderBookEntry::getLimitPrice)
                                .thenComparing(OrderBookEntry::getEntryTime));

    /*
     * Add @order to the book.
     * Tries to match then adds to book itself
     */
    void addOrder(Order order) {
        try {
            if (order.side == 'B') {
                //Buy - check if matched anything on sellQueue
                int remainingQty = tryMatchOrderToBook(order, sellQueue);
                //Either no match or have remaining visibleQty so add to buyQueue
                if (remainingQty > 0) {
                    int visibleQty = remainingQty;
                    if (order instanceof IcebergOrder) {
                        visibleQty = Math.min(remainingQty, ((IcebergOrder) order).peakSize);
                    }
                    buyQueue.add(new OrderBookEntry(visibleQty, order));
                }
            } else {
                //Sell - check if matched anything on buyQueue
                int remainingQty = tryMatchOrderToBook(order, buyQueue);
                //Either no match or have remaining visibleQty so add to sellQueue
                if (remainingQty > 0) {
                    int visibleQty = remainingQty;
                    if (order instanceof IcebergOrder) {
                        visibleQty = Math.min(remainingQty, ((IcebergOrder) order).peakSize);
                    }
                    sellQueue.add(new OrderBookEntry(visibleQty, order));
                }
            }
        } catch (Exception e) {
            System.err.println("Something went wrong: "+e.getMessage());
        }
        printOrderBook();
    }

    //Check if @topOfBookOrder price doesn't satisfy the limit price of the order @orderPrice
    private boolean hasBrokenLimitPrice(Order topOfBookOrder, short orderPrice) {
        if (topOfBookOrder.side == 'B') {
            return topOfBookOrder.limitPrice < orderPrice;
        }
        //S
        return topOfBookOrder.limitPrice > orderPrice;
    }

    //Matching logic. Try match @order to existing Order Entries in @orderBookEntries
    private int tryMatchOrderToBook(Order order, PriorityQueue<OrderBookEntry> orderBookEntries) throws Exception {
        short orderPrice = order.limitPrice;
        int qtyLeftToFill = order.totalQty;
        OrderBookEntry topOfBook;
        //Holds Iceberg orders that have had their peak executed but still have hidden volume.
        Queue<IcebergOrder> icebergQueue = new LinkedList<>();
        HashMap<TradeId, Trade> tradeMap = new HashMap<>();

        //Work down the book to fill - execute loop at least once
        do {
            //Re-add any used icebergs to the book [new timestamps]
            if (!icebergQueue.isEmpty()) {
                IcebergOrder io = icebergQueue.poll();
                int visibleQty = Math.min(io.peakSize, io.totalQty - io.filledQty);
                orderBookEntries.add(new OrderBookEntry(visibleQty, io));
            }

            //Start sweeping down the book
            while (qtyLeftToFill > 0 && !orderBookEntries.isEmpty()) {

                if (hasBrokenLimitPrice(orderBookEntries.peek().order, orderPrice)) break;    //Gone below price point of order

                //Remove entry from top of book
                topOfBook = orderBookEntries.poll();

                //Save visible visibleQty in case it's an Iceberg
                int visiblePeakQty = topOfBook.visibleQty;

                int tradeQty = Math.min(topOfBook.visibleQty, qtyLeftToFill);                   //Execute all visible qty
                execute(order, topOfBook.order, topOfBook.limitPrice, tradeQty, tradeMap);      //Updates order qtys

                //Update visibleQty left
                qtyLeftToFill = order.totalQty - order.filledQty;

                if (tradeQty < visiblePeakQty) {
                    //Taken part liquidity, amend visibleQty and add back to book as is [keep time priority]
                    topOfBook.visibleQty = visiblePeakQty - tradeQty;
                    orderBookEntries.add(topOfBook);
                } else {
                    //Taken full visibleQty, check if it's an iceberg with further hidden qty
                    if (topOfBook.order.filledQty < topOfBook.order.totalQty) {
                        //Must be an iceberg otherwise we'd still have peak left
                        if (!(topOfBook.order instanceof IcebergOrder))
                            throw new Exception("Not an iceberg, something went wrong");
                        //Add to the queue to refresh peak once we've gone down the current available orders
                        icebergQueue.add((IcebergOrder) topOfBook.order);
                    }
                }
            }
        } while (!icebergQueue.isEmpty());  //Re-add to the book and try again

        tradeMap.values().forEach(Trade::printTrade);   //Print trades

        return qtyLeftToFill;
    }

    /*
     * Perform an exection between Order @o1 and Order @o2 at @qty and @price.
     * Adds entry to map @tradeMap in case multiple executions happen between Iceberg peaks
     */
    private void execute(Order o1, Order o2, short price, int qty, HashMap<TradeId, Trade> tradeMap) {
        //Amend order qtys
        o1.filledQty += qty;
        o2.filledQty += qty;

        //Generate trade Id
        TradeId tradeId;
        if (o1.side == 'B') {
            tradeId = new TradeId(o1.orderId, o2.orderId);
        } else {
            tradeId = new TradeId(o2.orderId, o1.orderId);
        }

        //Check if already traded before
        Trade trade = tradeMap.get(tradeId);
        if (trade == null) {
            //Create new entry
            trade = new Trade(tradeId, price, qty);
            tradeMap.put(tradeId, trade);
        } else {
            //Already done a trade between these two parties (multiple iceberg peaks) so just add to Qty
            trade.qty += qty;
        }
    }

    //Print Order book in table format
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
