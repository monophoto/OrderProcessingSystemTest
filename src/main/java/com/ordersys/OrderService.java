package com.ordersys;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for creating and managing orders.
 */
public class OrderService {
    private ProductCatalog catalog;
    private PricingEngine pricingEngine;

    /**
     * Creates a new OrderService.
     * @param catalog The ProductCatalog for stock management
     * @param pricingEngine The PricingEngine for price calculations
     */
    public OrderService(ProductCatalog catalog, PricingEngine pricingEngine) {
        this.catalog = catalog;
        this.pricingEngine = pricingEngine;
    }

    /**
     * Creates an order from a cart, validates stock, calculates pricing, and reserves stock.
     * @param cart The shopping cart
     * @param coupon Optional coupon code (can be null)
     * @return OrderResult with total and items
     * @throws IllegalArgumentException if cart is empty or insufficient stock
     */
    public OrderResult createOrder(Cart cart, String coupon) {
        // Validate cart is not empty
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate stock availability for all items
        for (Map.Entry<String, Integer> entry : cart.getItems().entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = catalog.getProduct(productId);

            if (quantity > product.getStock()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + productId);
            }
        }

        // Calculate pricing
        PricingResult pricing = pricingEngine.calculate(cart, coupon);

        // Reserve stock for all items
        for (Map.Entry<String, Integer> entry : cart.getItems().entrySet()) {
            catalog.reserveStock(entry.getKey(), entry.getValue());
        }

        // Create a copy of the items map for the order result
        Map<String, Integer> orderItems = new HashMap<>(cart.getItems());

        return new OrderResult(pricing.getTotal(), orderItems);
    }
}