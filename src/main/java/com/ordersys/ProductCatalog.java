package com.ordersys;

import java.util.Map;

/**
 * Manages the product catalog and stock operations.
 */
public class ProductCatalog {
    private Map<String, Product> products;

    /**
     * Creates a new ProductCatalog with the given products.
     * @param products Map of product ID to Product objects
     */
    public ProductCatalog(Map<String, Product> products) {
        this.products = products;
    }

    /**
     * Retrieves a product by its ID.
     * @param productId The product ID to look up
     * @return The Product object
     * @throws IllegalArgumentException if product is not found
     */
    public Product getProduct(String productId) {
        if (!products.containsKey(productId)) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        return products.get(productId);
    }

    /**
     * Reserves stock for a product by reducing its available quantity.
     * @param productId The product ID
     * @param quantity The quantity to reserve
     * @throws IllegalArgumentException if product not found or insufficient stock
     */
    public void reserveStock(String productId, int quantity) {
        Product product = getProduct(productId);
        if (quantity > product.getStock()) {
            throw new IllegalArgumentException("Not enough stock for product: " + productId);
        }
        product.setStock(product.getStock() - quantity);
    }

    /**
     * Releases stock for a product by increasing its available quantity.
     * @param productId The product ID
     * @param quantity The quantity to release
     * @throws IllegalArgumentException if product not found
     */
    public void releaseStock(String productId, int quantity) {
        Product product = getProduct(productId);
        product.setStock(product.getStock() + quantity);
    }

    /**
     * Gets all products in the catalog.
     * @return Map of product ID to Product objects
     */
    public Map<String, Product> getProducts() {
        return products;
    }
}