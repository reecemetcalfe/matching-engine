package com.gsa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class OrderBookTask {

    void run() {

        OrderBook orderBook = new OrderBook();

        // -----TESTING------
        System.out.println("Test 4.2.2");
        Order b1 = new Order('B', 12347, 50000, (short)99);
        orderBook.addOrder(b1);
        Order b2 = new Order('B', 12346, 25500, (short)98);
        orderBook.addOrder(b2);
        Order s1 = new Order('S', 12350, 500, (short)100);
        orderBook.addOrder(s1);
        Order s2 = new IcebergOrder('S', 12351, 100000, (short)100, 10000);
        orderBook.addOrder(s2);
        Order s3 = new Order('S', 12352, 100, (short)103);
        orderBook.addOrder(s3);
        Order s4 = new Order('S', 12354, 20000, (short)105);
        orderBook.addOrder(s4);
        Order t1 = new Order('B', 99999, 16000, (short)100);
        orderBook.addOrder(t1);
        orderBook.clear();
        // -----------------
        System.out.println("Test 4.2.3");
        b1 = new Order('B', 12347, 50000, (short)99);
        orderBook.addOrder(b1);
        b2 = new Order('B', 12346, 25500, (short)98);
        orderBook.addOrder(b2);
        s1 = new Order('S', 12352, 10000, (short)100);
        orderBook.addOrder(s1);
        s2 = new Order('S', 12354, 7500, (short)100);
        orderBook.addOrder(s2);
        s3 = new Order('S', 12355, 20000, (short)101);
        orderBook.addOrder(s3);
        t1 = new IcebergOrder('B', 99999, 100000, (short)100, 10000);
        orderBook.addOrder(t1);
        Order t2 = new Order('S', 99998, 10000, (short)100);
        orderBook.addOrder(t2);
        Order t3 = new Order('S', 99997, 11000, (short)100);
        orderBook.addOrder(t3);
        Order t4 = new IcebergOrder('B', 99995, 50000, (short)100, 20000);
        orderBook.addOrder(t4);
        Order t5 = new Order('S', 99994, 35000, (short)100);
        orderBook.addOrder(t5);
        // -------------------

        // Start reading input - assumed no exit conditions just kill app
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                String[] inputArgs = line.split(",");
                if (inputArgs.length < 4) {
                    System.out.println("Ignoring input: " + Arrays.toString(inputArgs));
                    continue;
                }
                orderBook.addOrder(createOrder(inputArgs));
            }
        } catch (IOException e) {
            System.err.println("Input Error: " + e.getMessage());
        }
    }

    /* Assumed all orders have a limit price i.e. no 'at best' */
    private Order createOrder(String... input) throws IOException {
        //Basic input validation to ascertain which order type
        if (input.length == 4) {
            return new Order(input[0].charAt(0), Integer.parseInt(input[1]),
                             Integer.parseInt(input[2]), Short.parseShort(input[3]));
        } else if (input.length == 5) {
            return new IcebergOrder(input[0].charAt(0), Integer.parseInt(input[1]), Integer.parseInt(input[2]),
                                    Short.parseShort(input[3]), Integer.parseInt(input[4]));
        } else {
            throw new IOException("Error creating order with input: " + Arrays.toString(input));
        }
    }

    //Entry point
    public static void main(String[] args) {
        try {
            OrderBookTask obt = new OrderBookTask();
            obt.run();
        } catch (Exception e) {
            System.err.println("Error running Order Book Task: "+e.getMessage());
        }
    }
}
