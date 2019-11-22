package com.gsa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class OrderBookTask {

    void run() {

        OrderBook orderBook = new OrderBook();

        // -----TESTING------

        Order b1 = new Order('B', 12347, 50000, (short)99);
        orderBook.addOrder(b1);

        Order b2 = new Order('B', 12346, 25500, (short)98);
        orderBook.addOrder(b2);

        Order s1 = new Order('S', 12350, 10000, (short)100);
        orderBook.addOrder(s1);

        Order s2 = new Order('S', 12351, 7500, (short)100);
        orderBook.addOrder(s2);

        Order s3 = new Order('S', 12352, 20000, (short)101);
        orderBook.addOrder(s3);

        Order t1 = new IcebergOrder('B', 99999, 100000, (short)100, 10000);
        orderBook.addOrder(t1);

        Order t2 = new IcebergOrder('S', 99998, 12000, (short)100, 8000);
        orderBook.addOrder(t2);

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

    /* Assumed all orders have a limit price */
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
