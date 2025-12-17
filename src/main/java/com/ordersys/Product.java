package com.ordersys;

/**
 * Represents a product in the catalog with id, name, price, and stock quantity.
 */
public class Product {
    private String id;
    private String name;
    private double price;
    private int stock;

    /**
     * Creates a new Product.
     * @param id Product identifier
     * @param name Product name
     * @param price Product price (must be non-negative)
     * @param stock Available stock quantity (must be non-negative)
     * @throws IllegalArgumentException if price or stock is negative
     */
    public Product(String id, String name, double price, int stock) {
        if (price < 0 || stock < 0) {
            throw new IllegalArgumentException("Invalid product data: price and stock must be non-negative");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f, stock=%d}",
                id, name, price, stock);
    }
}