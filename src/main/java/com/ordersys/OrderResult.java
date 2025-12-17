package com.ordersys;

import java.util.Map;

/**
 * Represents the result of creating an order.
 */
public class OrderResult {
    private double total;
    private Map<String, Integer> items;

    public OrderResult(double total, Map<String, Integer> items) {
        this.total = total;
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return String.format("OrderResult{total=%.2f, items=%s}", total, items);
    }
}