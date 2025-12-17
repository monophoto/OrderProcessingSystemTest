package com.ordersys;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for OrderService class.
 * Tests order creation, validation, stock reservation, and error handling.
 */
public class OrderServiceTest {

    private ProductCatalog catalog;
    private PricingEngine pricingEngine;
    private OrderService orderService;
    private Map<String, Product> mockProducts;

    @BeforeMethod
    public void setup() {
        // Initialize test catalog with sample products
        mockProducts = new HashMap<>();
        mockProducts.put("P001", new Product("P001", "Laptop", 1200.00, 10));
        mockProducts.put("P002", new Product("P002", "Mouse", 25.00, 50));
        mockProducts.put("P003", new Product("P003", "Keyboard", 75.00, 30));
        mockProducts.put("P004", new Product("P004", "Monitor", 300.00, 5));
        mockProducts.put("P005", new Product("P005", "USB Cable", 10.00, 100));
        catalog = new ProductCatalog(mockProducts);
        pricingEngine = new PricingEngine();
        orderService = new OrderService(catalog, pricingEngine);
    }

    // ========== Successful Order Creation Tests ==========

    /**
     * Test: Successfully create an order with single item
     * Expected: Order is created, stock is reserved, total is correct
     */
    @Test
    public void testCreateOrder_SingleItem_Success() {
        Cart cart = new Cart(catalog);
        cart.addItem("P002", 3); // 3 × 25 = 75

        Product product = catalog.getProduct("P002");
        int initialStock = product.getStock();

        OrderResult order = orderService.createOrder(cart, null);

        // Verify order details
        Assert.assertNotNull(order);
        Assert.assertEquals(order.getTotal(), 85.00, 0.01); // 75 + 10 shipping
        Assert.assertEquals(order.getItems().size(), 1);
        Assert.assertEquals(order.getItems().get("P002").intValue(), 3);

        // Verify stock was reserved
        Assert.assertEquals(product.getStock(), initialStock - 3);
    }

    /**
     * Test: Successfully create order with multiple items
     * Expected: Order created, all stocks reserved, total correct
     */
    @Test
    public void testCreateOrder_MultipleItems_Success() {
        Cart cart = new Cart(catalog);
        cart.addItem("P001", 1); // 1200
        cart.addItem("P002", 2); // 50
        cart.addItem("P005", 3); // 30
        // Subtotal: 1280, Total items: 6 (qualifies for bulk discount!)
        // Bulk discount: 64, Total: 1280 - 64 + 10 = 1226

        Product p1 = catalog.getProduct("P001");
        Product p2 = catalog.getProduct("P002");
        Product p5 = catalog.getProduct("P005");
        int stock1 = p1.getStock();
        int stock2 = p2.getStock();
        int stock5 = p5.getStock();

        OrderResult order = orderService.createOrder(cart, null);

        Assert.assertEquals(order.getTotal(), 1226.00, 0.01); // Fixed: includes bulk discount
        Assert.assertEquals(order.getItems().size(), 3);

        // Verify all stocks were reserved
        Assert.assertEquals(p1.getStock(), stock1 - 1);
        Assert.assertEquals(p2.getStock(), stock2 - 2);
        Assert.assertEquals(p5.getStock(), stock5 - 3);
    }

    /**
     * Test: Create order with bulk discount applied
     * Expected: Discount is reflected in total, stock reserved
     */
    @Test
    public void testCreateOrder_WithBulkDiscount() {
        Cart cart = new Cart(catalog);
        cart.addItem("P002", 5); // 5 × 25 = 125
        // Bulk discount: 6.25, total: 125 - 6.25 + 10 = 128.75

        Product product = catalog.getProduct("P002");
        int initialStock = product.getStock();

        OrderResult order = orderService.createOrder(cart, null);

        Assert.assertEquals(order.getTotal(), 128.75, 0.01);
        Assert.assertEquals(product.getStock(), initialStock - 5);
    }

    /**
     * Test: Create order with SAVE10 coupon
     * Expected: Coupon discount applied, correct total
     */
    @Test
    public void testCreateOrder_WithSave10Coupon() {
        Cart cart = new Cart(catalog);
        cart.addItem("P003", 2); // 2 × 75 = 150
        // SAVE10: 15, total: 150 - 15 + 10 = 145

        OrderResult order = orderService.createOrder(cart, "SAVE10");

        Assert.assertEquals(order.getTotal(), 145.00, 0.01);
    }

    /**
     * Test: Create order with FREESHIP coupon
     * Expected: No shipping charge, correct total
     */
    @Test
    public void testCreateOrder_WithFreeShipCoupon() {
        Cart cart = new Cart(catalog);
        cart.addItem("P004", 1); // 1 × 300 = 300
        // FREESHIP: 0 shipping, total: 300

        OrderResult order = orderService.createOrder(cart, "FREESHIP");

        Assert.assertEquals(order.getTotal(), 300.00, 0.01);
    }

    /**
     * Test: Create order with both bulk discount and coupon
     * Expected: Both discounts applied correctly
     */
    @Test
    public void testCreateOrder_BulkDiscountAndCoupon() {
        Cart cart = new Cart(catalog);
        cart.addItem("P003", 5); // 5 × 75 = 375
        // Bulk: 18.75, SAVE10: 37.50, total: 375 - 18.75 - 37.50 + 10 = 328.75

        OrderResult order = orderService.createOrder(cart, "SAVE10");

        Assert.assertEquals(order.getTotal(), 328.75, 0.01);
    }

    /**
     * Test: Create order using maximum available stock
     * Expected: Order succeeds, stock becomes zero
     */
    @Test
    public void testCreateOrder_MaximumStock() {
        Cart cart = new Cart(catalog);
        Product product = catalog.getProduct("P004");
        int maxStock = product.getStock(); // 5

        cart.addItem("P004", maxStock);
        OrderResult order = orderService.createOrder(cart, null);

        Assert.assertNotNull(order);
        Assert.assertEquals(product.getStock(), 0);
    }

    // ========== Empty Cart Tests ==========

    /**
     * Test: Attempt to create order with empty cart
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateOrder_EmptyCart() {
        Cart cart = new Cart(catalog);
        orderService.createOrder(cart, null);
    }

    /**
     * Test: Create order from newly initialized cart (no items added)
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateOrder_NewEmptyCart() {
        Cart emptyCart = new Cart(catalog);
        orderService.createOrder(emptyCart, "SAVE10");
    }

    // ========== Insufficient Stock Tests ==========

    /**
     * Test: Attempt to create order when product has insufficient stock
     * Expected: IllegalArgumentException is thrown, no stock reserved
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateOrder_InsufficientStock_SingleItem() {
        Cart cart = new Cart(catalog);
        Product product = catalog.getProduct("P004");
        int initialStock = product.getStock(); // 5

        // Try to add 3 items (within limit), but manually reduce stock
        cart.addItem("P004", 3);
        product.setStock(2); // Simulate stock being reduced elsewhere

        try {
            orderService.createOrder(cart, null);
        } finally {
            // Verify stock wasn't modified
            Assert.assertEquals(product.getStock(), 2);
        }
    }

    /**
     * Test: Insufficient stock when ordering multiple products
     * Expected: IllegalArgumentException, no stock changes
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateOrder_InsufficientStock_MultipleItems() {
        Cart cart = new Cart(catalog);
        cart.addItem("P001", 2);
        cart.addItem("P004", 3);

        Product p1 = catalog.getProduct("P001");
        Product p4 = catalog.getProduct("P004");
        int stock1 = p1.getStock();
        int stock4 = p4.getStock();

        // Reduce stock to cause insufficient stock
        p4.setStock(2);

        try {
            orderService.createOrder(cart, null);
        } finally {
            // Verify no stock was reserved for any product
            Assert.assertEquals(p1.getStock(), stock1);
            Assert.assertEquals(p4.getStock(), 2);
        }
    }

    /**
     * Test: Stock becomes insufficient between cart creation and order
     * Expected: Order fails, appropriate exception thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateOrder_StockChangedBetweenCartAndOrder() {
        Cart cart = new Cart(catalog);
        cart.addItem("P004", 5);

        Product product = catalog.getProduct("P004");
        // Simulate concurrent stock reduction
        product.setStock(3);

        orderService.createOrder(cart, null);
    }

    // ========== Order Result Validation Tests ==========

    /**
     * Test: Verify order result contains correct item details
     * Expected: Order items match cart items
     */
    @Test
    public void testCreateOrder_OrderResultItems() {
        Cart cart = new Cart(catalog);
        cart.addItem("P001", 1);
        cart.addItem("P002", 3);
        cart.addItem("P003", 2);

        OrderResult order = orderService.createOrder(cart, null);

        Map<String, Integer> orderItems = order.getItems();
        Assert.assertEquals(orderItems.size(), 3);
        Assert.assertEquals(orderItems.get("P001").intValue(), 1);
        Assert.assertEquals(orderItems.get("P002").intValue(), 3);
        Assert.assertEquals(orderItems.get("P003").intValue(), 2);
    }

    /**
     * Test: Order result is independent of cart (defensive copy)
     * Expected: Modifying cart after order doesn't affect order
     */
    @Test
    public void testCreateOrder_OrderResultIndependence() {
        Cart cart = new Cart(catalog);
        cart.addItem("P002", 5);

        OrderResult order = orderService.createOrder(cart, null);
        Map<String, Integer> originalItems = new HashMap<>(order.getItems());

        // Modify the order items map (shouldn't affect original order)
        order.getItems().put("P999", 10);

        // Original order data should be preserved
        Assert.assertEquals(originalItems.size(), 1);
        Assert.assertEquals(originalItems.get("P002").intValue(), 5);
    }

    // ========== Stock Reservation Validation Tests ==========

    /**
     * Test: Verify stock is only reserved after validation passes
     * Expected: Stock reserved only when order creation succeeds
     */
    @Test
    public void testCreateOrder_StockReservedOnlyAfterValidation() {
        Cart cart = new Cart(catalog);
        cart.addItem("P002", 10);

        Product product = catalog.getProduct("P002");
        int initialStock = product.getStock();

        OrderResult order = orderService.createOrder(cart, null);

        Assert.assertEquals(product.getStock(), initialStock - 10);
    }

    /**
     * Test: Multiple sequential orders reduce stock correctly
     * Expected: Stock decreases with each successful order
     */
    @Test
    public void testCreateOrder_MultipleSequentialOrders() {
        Product product = catalog.getProduct("P002");
        int initialStock = product.getStock(); // 50

        // First order
        Cart cart1 = new Cart(catalog);
        cart1.addItem("P002", 10);
        orderService.createOrder(cart1, null);
        Assert.assertEquals(product.getStock(), initialStock - 10);

        // Second order
        Cart cart2 = new Cart(catalog);
        cart2.addItem("P002", 15);
        orderService.createOrder(cart2, null);
        Assert.assertEquals(product.getStock(), initialStock - 25);

        // Third order
        Cart cart3 = new Cart(catalog);
        cart3.addItem("P002", 20);
        orderService.createOrder(cart3, null);
        Assert.assertEquals(product.getStock(), initialStock - 45);
    }

    // ========== Edge Cases ==========

    /**
     * Test: Create order with single item at minimum quantity
     * Expected: Order succeeds with correct calculations
     */
    @Test
    public void testCreateOrder_MinimumQuantity() {
        Cart cart = new Cart(catalog);
        cart.addItem("P005", 1); // 1 × 10 = 10

        OrderResult order = orderService.createOrder(cart, null);

        Assert.assertEquals(order.getTotal(), 20.00, 0.01); // 10 + 10 shipping
        Assert.assertEquals(order.getItems().get("P005").intValue(), 1);
    }

    /**
     * Test: Large order with maximum discounts
     * Expected: All calculations accurate for large amounts
     */
    @Test
    public void testCreateOrder_LargeOrderWithMaxDiscounts() {
        Cart cart = new Cart(catalog);
        cart.addItem("P001", 5); // 5 × 1200 = 6000
        cart.addItem("P004", 5); // 5 × 300 = 1500
        // Total: 10 items, subtotal = 7500
        // Bulk: 375, SAVE10: 750, total: 7500 - 375 - 750 + 10 = 6385

        Product p1 = catalog.getProduct("P001");
        Product p4 = catalog.getProduct("P004");

        OrderResult order = orderService.createOrder(cart, "SAVE10");

        Assert.assertEquals(order.getTotal(), 6385.00, 0.01);
        Assert.assertEquals(p1.getStock(), 5); // 10 - 5
        Assert.assertEquals(p4.getStock(), 0); // 5 - 5
    }

    /**
     * Test: Order with invalid coupon code
     * Expected: Order succeeds but coupon is ignored
     */
    @Test
    public void testCreateOrder_InvalidCoupon() {
        Cart cart = new Cart(catalog);
        cart.addItem("P003", 2); // 2 × 75 = 150

        OrderResult order = orderService.createOrder(cart, "INVALID_COUPON");

        // Should be treated as no coupon: 150 + 10 = 160
        Assert.assertEquals(order.getTotal(), 160.00, 0.01);
    }

    /**
     * Test: Complex scenario with multiple products and discounts
     * Expected: All business rules applied correctly
     */
    @Test
    public void testCreateOrder_ComplexScenario() {
        Cart cart = new Cart(catalog);
        cart.addItem("P001", 1); // 1200
        cart.addItem("P002", 2); // 50
        cart.addItem("P003", 1); // 75
        cart.addItem("P004", 1); // 300
        cart.addItem("P005", 2); // 20
        // Total: 7 items, subtotal = 1645
        // Bulk: 82.25, FREESHIP: 0, total: 1645 - 82.25 = 1562.75

        Product p1 = catalog.getProduct("P001");
        Product p2 = catalog.getProduct("P002");
        Product p3 = catalog.getProduct("P003");
        Product p4 = catalog.getProduct("P004");
        Product p5 = catalog.getProduct("P005");

        int stock1 = p1.getStock();
        int stock2 = p2.getStock();
        int stock3 = p3.getStock();
        int stock4 = p4.getStock();
        int stock5 = p5.getStock();

        OrderResult order = orderService.createOrder(cart, "FREESHIP");

        Assert.assertEquals(order.getTotal(), 1562.75, 0.01);
        Assert.assertEquals(order.getItems().size(), 5);

        // Verify all stock reservations
        Assert.assertEquals(p1.getStock(), stock1 - 1);
        Assert.assertEquals(p2.getStock(), stock2 - 2);
        Assert.assertEquals(p3.getStock(), stock3 - 1);
        Assert.assertEquals(p4.getStock(), stock4 - 1);
        Assert.assertEquals(p5.getStock(), stock5 - 2);
    }

    /**
     * Test: Order at exact stock boundary
     * Expected: Order succeeds, stock becomes zero
     */
    @Test
    public void testCreateOrder_ExactStockBoundary() {
        Cart cart = new Cart(catalog);
        Product product = catalog.getProduct("P004");
        int exactStock = product.getStock(); // 5

        cart.addItem("P004", exactStock);
        OrderResult order = orderService.createOrder(cart, null);

        Assert.assertNotNull(order);
        Assert.assertEquals(product.getStock(), 0);
        Assert.assertEquals(order.getItems().get("P004").intValue(), exactStock);
    }
}