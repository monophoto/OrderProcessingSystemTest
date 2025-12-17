package com.ordersys;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for Cart class.
 * Tests adding items, quantity validation, stock checking, and subtotal calculation.
 */
public class CartTest {

    private ProductCatalog catalog;
    private Cart cart;

    @BeforeMethod
    public void setup() {
        // Initialize test catalog with sample products
        Map<String, Product> mockProducts = new HashMap<>();
        mockProducts.put("P001", new Product("P001", "Laptop", 1200.00, 10));
        mockProducts.put("P002", new Product("P002", "Mouse", 25.00, 50));
        mockProducts.put("P003", new Product("P003", "Keyboard", 75.00, 30));
        mockProducts.put("P004", new Product("P004", "Monitor", 300.00, 5));
        mockProducts.put("P005", new Product("P005", "USB Cable", 10.00, 100));
        catalog = new ProductCatalog(mockProducts);
        cart = new Cart(catalog);
    }

    // ========== Add Item Tests ==========

    /**
     * Test: Successfully add a single item to an empty cart
     * Expected: Item is added and total items count is correct
     */
    @Test
    public void testAddItem_SingleItem() {
        cart.addItem("P001", 2);

        Assert.assertEquals(cart.getTotalItems(), 2);
        Assert.assertTrue(cart.getItems().containsKey("P001"));
        Assert.assertEquals(cart.getItems().get("P001").intValue(), 2);
    }

    /**
     * Test: Add multiple different items to the cart
     * Expected: All items are added with correct quantities
     */
    @Test
    public void testAddItem_MultipleItems() {
        cart.addItem("P001", 1);
        cart.addItem("P002", 3);
        cart.addItem("P003", 2);

        Assert.assertEquals(cart.getTotalItems(), 6);
        Assert.assertEquals(cart.getItems().size(), 3);
        Assert.assertEquals(cart.getItems().get("P001").intValue(), 1);
        Assert.assertEquals(cart.getItems().get("P002").intValue(), 3);
        Assert.assertEquals(cart.getItems().get("P003").intValue(), 2);
    }

    /**
     * Test: Add the same item multiple times (incremental addition)
     * Expected: Quantities are accumulated correctly
     */
    @Test
    public void testAddItem_SameItemMultipleTimes() {
        cart.addItem("P001", 2);
        cart.addItem("P001", 3);

        Assert.assertEquals(cart.getTotalItems(), 5);
        Assert.assertEquals(cart.getItems().get("P001").intValue(), 5);
    }

    /**
     * Test: Add maximum available stock to cart (boundary case)
     * Expected: Item is added successfully
     */
    @Test
    public void testAddItem_MaximumStock() {
        Product product = catalog.getProduct("P004");
        int maxStock = product.getStock();

        cart.addItem("P004", maxStock);

        Assert.assertEquals(cart.getTotalItems(), maxStock);
        Assert.assertEquals(cart.getItems().get("P004").intValue(), maxStock);
    }

    // ========== Invalid Quantity Tests ==========

    /**
     * Test: Attempt to add item with zero quantity
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddItem_ZeroQuantity() {
        cart.addItem("P001", 0);
    }

    /**
     * Test: Attempt to add item with negative quantity
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddItem_NegativeQuantity() {
        cart.addItem("P001", -5);
    }

    /**
     * Test: Attempt to add item with very large negative quantity
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddItem_LargeNegativeQuantity() {
        cart.addItem("P002", -100);
    }

    // ========== Insufficient Stock Tests ==========

    /**
     * Test: Attempt to add more items than available in stock
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddItem_InsufficientStock() {
        cart.addItem("P001", 15); // Only 10 available
    }

    /**
     * Test: Attempt to add quantity that exceeds stock on second addition
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddItem_ExceedsStockOnSecondAdd() {
        cart.addItem("P004", 3); // Product has 5 in stock
        cart.addItem("P004", 5); // Total would be 8, exceeds 5 available
    }

    /**
     * Test: Add item with exact stock limit then try to add more
     * Expected: First addition succeeds, second throws exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddItem_BoundaryExceeded() {
        cart.addItem("P004", 5); // Exact stock
        cart.addItem("P004", 1); // Should fail
    }

    /**
     * Test: Attempt to add non-existent product
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddItem_InvalidProductId() {
        cart.addItem("P999", 1);
    }

    // ========== Subtotal Calculation Tests ==========

    /**
     * Test: Calculate subtotal for single item
     * Expected: Subtotal equals price × quantity
     */
    @Test
    public void testGetSubtotal_SingleItem() {
        cart.addItem("P001", 2); // 2 × 1200 = 2400

        Assert.assertEquals(cart.getSubtotal(), 2400.00, 0.01);
    }

    /**
     * Test: Calculate subtotal for multiple different items
     * Expected: Subtotal is sum of all item costs
     */
    @Test
    public void testGetSubtotal_MultipleItems() {
        cart.addItem("P001", 1); // 1 × 1200 = 1200
        cart.addItem("P002", 4); // 4 × 25 = 100
        cart.addItem("P003", 2); // 2 × 75 = 150
        // Total: 1200 + 100 + 150 = 1450

        Assert.assertEquals(cart.getSubtotal(), 1450.00, 0.01);
    }

    /**
     * Test: Subtotal for empty cart
     * Expected: Subtotal is 0.00
     */
    @Test
    public void testGetSubtotal_EmptyCart() {
        Assert.assertEquals(cart.getSubtotal(), 0.00, 0.01);
    }

    /**
     * Test: Subtotal calculation with low-cost items
     * Expected: Correct calculation for small amounts
     */
    @Test
    public void testGetSubtotal_LowCostItems() {
        cart.addItem("P005", 10); // 10 × 10 = 100

        Assert.assertEquals(cart.getSubtotal(), 100.00, 0.01);
    }

    /**
     * Test: Subtotal accuracy with decimal prices
     * Expected: Accurate calculation without rounding errors
     */
    @Test
    public void testGetSubtotal_DecimalPrices() {
        cart.addItem("P002", 3); // 3 × 25.00 = 75.00
        cart.addItem("P005", 7); // 7 × 10.00 = 70.00
        // Total: 145.00

        Assert.assertEquals(cart.getSubtotal(), 145.00, 0.01);
    }

    // ========== Total Items Tests ==========

    /**
     * Test: Get total items count for empty cart
     * Expected: Returns 0
     */
    @Test
    public void testGetTotalItems_EmptyCart() {
        Assert.assertEquals(cart.getTotalItems(), 0);
    }

    /**
     * Test: Get total items count with various quantities
     * Expected: Returns sum of all quantities
     */
    @Test
    public void testGetTotalItems_VariousQuantities() {
        cart.addItem("P001", 2);
        cart.addItem("P002", 5);
        cart.addItem("P003", 3);

        Assert.assertEquals(cart.getTotalItems(), 10);
    }

    // ========== Cart State Tests ==========

    /**
     * Test: Check if new cart is empty
     * Expected: isEmpty() returns true
     */
    @Test
    public void testIsEmpty_NewCart() {
        Assert.assertTrue(cart.isEmpty());
    }

    /**
     * Test: Check if cart with items is not empty
     * Expected: isEmpty() returns false
     */
    @Test
    public void testIsEmpty_CartWithItems() {
        cart.addItem("P001", 1);
        Assert.assertFalse(cart.isEmpty());
    }

    // ========== Edge Cases ==========

    /**
     * Test: Add minimum quantity (1) to cart
     * Expected: Item is added successfully
     */
    @Test
    public void testAddItem_MinimumValidQuantity() {
        cart.addItem("P001", 1);

        Assert.assertEquals(cart.getTotalItems(), 1);
        Assert.assertEquals(cart.getItems().get("P001").intValue(), 1);
    }

    /**
     * Test: Add large quantity within stock limits
     * Expected: Item is added successfully with correct subtotal
     */
    @Test
    public void testAddItem_LargeQuantity() {
        cart.addItem("P005", 50); // 50 × 10 = 500

        Assert.assertEquals(cart.getTotalItems(), 50);
        Assert.assertEquals(cart.getSubtotal(), 500.00, 0.01);
    }

    /**
     * Test: Complex cart scenario with multiple operations
     * Expected: All calculations are correct
     */
    @Test
    public void testComplexCartScenario() {
        cart.addItem("P001", 1); // 1200
        cart.addItem("P002", 2); // 50
        cart.addItem("P003", 1); // 75
        cart.addItem("P005", 5); // 50
        // Total: 1375, Items: 9

        Assert.assertEquals(cart.getTotalItems(), 9);
        Assert.assertEquals(cart.getSubtotal(), 1375.00, 0.01);
        Assert.assertEquals(cart.getItems().size(), 4);
    }
}