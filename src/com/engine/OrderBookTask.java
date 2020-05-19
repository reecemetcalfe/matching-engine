package com.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/*
 * Main class which receives input and creates Orders
 */
public class OrderBookTask {

    void run() {
        //Create order book
        OrderBook orderBook = new OrderBook();

        // Start reading input - assumed no exit conditions just kill app
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String line;
            System.out.println("Enter Order:");
            while ((line = in.readLine()) != null) {
                String[] inputArgs = line.split(",");
                if (inputArgs.length < 4) {
                    System.out.println("Ignoring input: " + Arrays.toString(inputArgs));
                    System.out.println("Enter Order:");
                    continue;
                }
                orderBook.addOrder(createOrder(inputArgs));
                System.out.println("Enter Order:");
            }
        } catch (IOException e) {
            System.err.println("Input Error: " + e.getMessage());
        }
    }

    private Order createOrder(String... input) throws IOException {
        //Basic input validation to ascertain which order type
        if (input.length == 4) {
            return new Order(input[0].charAt(0), Integer.parseInt(input[1]),
                             Short.parseShort(input[2]), Integer.parseInt(input[3]));
        } else if (input.length == 5) {
            return new IcebergOrder(input[0].charAt(0), Integer.parseInt(input[1]),
                             Short.parseShort(input[2]), Integer.parseInt(input[3]), Integer.parseInt(input[4]));
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
