package com.ordersys;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a shopping cart that holds items and calculates totals.
 */
public class Cart {
    private ProductCatalog catalog;
    private Map<String, Integer> items;

    /**
     * Creates a new Cart with access to the product catalog.
     * @param catalog The ProductCatalog to use for product lookups
     */
    public Cart(ProductCatalog catalog) {
        this.catalog = catalog;
        this.items = new HashMap<>();
    }

    /**
     * Adds an item to the cart.
     * @param productId The product ID to add
     * @param quantity The quantity to add (must be positive)
     * @throws IllegalArgumentException if quantity is not positive, product not found, or insufficient stock
     */
    public void addItem(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Product product = catalog.getProduct(productId);

        // Check if there's enough stock available
        if (quantity > product.getStock()) {
            throw new IllegalArgumentException("Not enough stock for product: " + productId);
        }

        items.put(productId, items.getOrDefault(productId, 0) + quantity);
    }

    /**
     * Gets the total number of items in the cart.
     * @return Total count of all items
     */
    public int getTotalItems() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Calculates the subtotal (sum of all item prices × quantities).
     * @return The subtotal amount
     */
    public double getSubtotal() {
        double total = 0.0;
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            Product product = catalog.getProduct(entry.getKey());
            total += product.getPrice() * entry.getValue();
        }
        return total;
    }

    /**
     * Gets all items in the cart.
     * @return Map of product ID to quantity
     */
    public Map<String, Integer> getItems() {
        return items;
    }

    /**
     * Checks if the cart is empty.
     * @return true if cart has no items
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
}