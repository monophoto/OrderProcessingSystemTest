package com.ordersys;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for PricingEngine class.
 * Tests pricing calculations including discounts, coupons, and shipping.
 */
public class PricingEngineTest {

    private ProductCatalog catalog;
    private PricingEngine pricingEngine;
    private Cart cart;

    @BeforeMethod
    public void setup() {
        // Initialize test catalog with sample products
        Map<String, Product> mockProducts = new HashMap<>();
        mockProducts.put("P001", new Product("P001", "Laptop", 1200.00, 10));
        mockProducts.put("P002", new Product("P002", "Mouse", 25.00, 50));
        mockProducts.put("P003", new Product("P003", "Keyboard", 75.00, 30));
        mockProducts.put("P004", new Product("P004", "Monitor", 300.00, 15));
        mockProducts.put("P005", new Product("P005", "USB Cable", 10.00, 100));
        catalog = new ProductCatalog(mockProducts);
        pricingEngine = new PricingEngine();
        cart = new Cart(catalog);
    }

    // ========== No Discount Tests ==========

    /**
     * Test: Calculate price with no discounts or coupons (base case)
     * Expected: Total = subtotal + shipping (no discounts applied)
     */
    @Test
    public void testCalculate_NoDiscounts() {
        cart.addItem("P002", 2); // 2 × 25 = 50

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getSubtotal(), 50.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 0.00, 0.01);
        Assert.assertEquals(result.getCouponDiscount(), 0.00, 0.01);
        Assert.assertEquals(result.getShipping(), 10.00, 0.01);
        Assert.assertEquals(result.getTotal(), 60.00, 0.01); // 50 + 10
    }

    /**
     * Test: Calculate price for single low-value item
     * Expected: Only shipping is added to subtotal
     */
    @Test
    public void testCalculate_SingleItem() {
        cart.addItem("P005", 1); // 1 × 10 = 10

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getSubtotal(), 10.00, 0.01);
        Assert.assertEquals(result.getTotal(), 20.00, 0.01); // 10 + 10 shipping
    }

    /**
     * Test: Calculate price for expensive item without discounts
     * Expected: Correct calculation with standard shipping
     */
    @Test
    public void testCalculate_ExpensiveItem() {
        cart.addItem("P001", 1); // 1 × 1200 = 1200

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getSubtotal(), 1200.00, 0.01);
        Assert.assertEquals(result.getTotal(), 1210.00, 0.01); // 1200 + 10 shipping
    }

    // ========== Bulk Discount Tests ==========

    /**
     * Test: Bulk discount applied for exactly 5 items (boundary case)
     * Expected: 5% discount on subtotal
     */
    @Test
    public void testCalculate_BulkDiscount_ExactlyFiveItems() {
        cart.addItem("P005", 5); // 5 × 10 = 50

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getSubtotal(), 50.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 2.50, 0.01); // 5% of 50
        Assert.assertEquals(result.getTotal(), 57.50, 0.01); // 50 - 2.50 + 10
    }

    /**
     * Test: Bulk discount applied for more than 5 items
     * Expected: 5% discount on subtotal
     */
    @Test
    public void testCalculate_BulkDiscount_MoreThanFiveItems() {
        cart.addItem("P002", 3); // 3 × 25 = 75
        cart.addItem("P005", 3); // 3 × 10 = 30
        // Total: 6 items, subtotal = 105

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getSubtotal(), 105.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 5.25, 0.01); // 5% of 105
        Assert.assertEquals(result.getTotal(), 109.75, 0.01); // 105 - 5.25 + 10
    }

    /**
     * Test: No bulk discount for 4 items (below threshold)
     * Expected: No bulk discount applied
     */
    @Test
    public void testCalculate_BulkDiscount_BelowThreshold() {
        cart.addItem("P002", 4); // 4 × 25 = 100

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getSubtotal(), 100.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 0.00, 0.01);
        Assert.assertEquals(result.getTotal(), 110.00, 0.01); // 100 + 10
    }

    /**
     * Test: Bulk discount with high-value items
     * Expected: Correct 5% discount calculation
     */
    @Test
    public void testCalculate_BulkDiscount_HighValue() {
        cart.addItem("P001", 2); // 2 × 1200 = 2400
        cart.addItem("P004", 3); // 3 × 300 = 900
        // Total: 5 items, subtotal = 3300

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getSubtotal(), 3300.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 165.00, 0.01); // 5% of 3300
        Assert.assertEquals(result.getTotal(), 3145.00, 0.01); // 3300 - 165 + 10
    }

    // ========== SAVE10 Coupon Tests ==========

    /**
     * Test: Apply SAVE10 coupon (10% off)
     * Expected: 10% discount on subtotal, no bulk discount
     */
    @Test
    public void testCalculate_Save10Coupon() {
        cart.addItem("P003", 2); // 2 × 75 = 150

        PricingResult result = pricingEngine.calculate(cart, "SAVE10");

        Assert.assertEquals(result.getSubtotal(), 150.00, 0.01);
        Assert.assertEquals(result.getCouponDiscount(), 15.00, 0.01); // 10% of 150
        Assert.assertEquals(result.getShipping(), 10.00, 0.01);
        Assert.assertEquals(result.getTotal(), 145.00, 0.01); // 150 - 15 + 10
    }

    /**
     * Test: SAVE10 coupon with single item
     * Expected: Correct 10% discount applied
     */
    @Test
    public void testCalculate_Save10Coupon_SingleItem() {
        cart.addItem("P004", 1); // 1 × 300 = 300

        PricingResult result = pricingEngine.calculate(cart, "SAVE10");

        Assert.assertEquals(result.getSubtotal(), 300.00, 0.01);
        Assert.assertEquals(result.getCouponDiscount(), 30.00, 0.01); // 10% of 300
        Assert.assertEquals(result.getTotal(), 280.00, 0.01); // 300 - 30 + 10
    }

    /**
     * Test: SAVE10 coupon with expensive items
     * Expected: Significant discount applied correctly
     */
    @Test
    public void testCalculate_Save10Coupon_ExpensiveItems() {
        cart.addItem("P001", 2); // 2 × 1200 = 2400

        PricingResult result = pricingEngine.calculate(cart, "SAVE10");

        Assert.assertEquals(result.getSubtotal(), 2400.00, 0.01);
        Assert.assertEquals(result.getCouponDiscount(), 240.00, 0.01); // 10% of 2400
        Assert.assertEquals(result.getTotal(), 2170.00, 0.01); // 2400 - 240 + 10
    }

    // ========== FREESHIP Coupon Tests ==========

    /**
     * Test: Apply FREESHIP coupon
     * Expected: Shipping is 0, no other discounts
     */
    @Test
    public void testCalculate_FreeShipCoupon() {
        cart.addItem("P002", 3); // 3 × 25 = 75

        PricingResult result = pricingEngine.calculate(cart, "FREESHIP");

        Assert.assertEquals(result.getSubtotal(), 75.00, 0.01);
        Assert.assertEquals(result.getCouponDiscount(), 0.00, 0.01);
        Assert.assertEquals(result.getShipping(), 0.00, 0.01);
        Assert.assertEquals(result.getTotal(), 75.00, 0.01); // 75 + 0 shipping
    }

    /**
     * Test: FREESHIP coupon with single expensive item
     * Expected: No shipping charge
     */
    @Test
    public void testCalculate_FreeShipCoupon_ExpensiveItem() {
        cart.addItem("P001", 1); // 1 × 1200 = 1200

        PricingResult result = pricingEngine.calculate(cart, "FREESHIP");

        Assert.assertEquals(result.getSubtotal(), 1200.00, 0.01);
        Assert.assertEquals(result.getShipping(), 0.00, 0.01);
        Assert.assertEquals(result.getTotal(), 1200.00, 0.01);
    }

    // ========== Combined Discount Tests ==========

    /**
     * Test: Bulk discount + SAVE10 coupon combination
     * Expected: Both discounts applied (bulk 5% + coupon 10%)
     */
    @Test
    public void testCalculate_BulkAndSave10Coupon() {
        cart.addItem("P002", 5); // 5 × 25 = 125

        PricingResult result = pricingEngine.calculate(cart, "SAVE10");

        Assert.assertEquals(result.getSubtotal(), 125.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 6.25, 0.01); // 5% of 125
        Assert.assertEquals(result.getCouponDiscount(), 12.50, 0.01); // 10% of 125
        Assert.assertEquals(result.getShipping(), 10.00, 0.01);
        Assert.assertEquals(result.getTotal(), 116.25, 0.01); // 125 - 6.25 - 12.50 + 10
    }

    /**
     * Test: Bulk discount + FREESHIP coupon combination
     * Expected: Bulk discount + free shipping
     */
    @Test
    public void testCalculate_BulkAndFreeShipCoupon() {
        cart.addItem("P003", 6); // 6 × 75 = 450

        PricingResult result = pricingEngine.calculate(cart, "FREESHIP");

        Assert.assertEquals(result.getSubtotal(), 450.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 22.50, 0.01); // 5% of 450
        Assert.assertEquals(result.getCouponDiscount(), 0.00, 0.01);
        Assert.assertEquals(result.getShipping(), 0.00, 0.01);
        Assert.assertEquals(result.getTotal(), 427.50, 0.01); // 450 - 22.50
    }

    /**
     * Test: Complex combination - bulk discount with SAVE10 coupon and multiple items
     * Expected: Maximum discounts applied correctly
     */
    @Test
    public void testCalculate_ComplexCombination() {
        cart.addItem("P001", 1); // 1200
        cart.addItem("P002", 2); // 50
        cart.addItem("P003", 1); // 75
        cart.addItem("P005", 2); // 20
        // Total: 6 items, subtotal = 1345

        PricingResult result = pricingEngine.calculate(cart, "SAVE10");

        Assert.assertEquals(result.getSubtotal(), 1345.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 67.25, 0.01); // 5% of 1345
        Assert.assertEquals(result.getCouponDiscount(), 134.50, 0.01); // 10% of 1345
        Assert.assertEquals(result.getShipping(), 10.00, 0.01);
        Assert.assertEquals(result.getTotal(), 1153.25, 0.01); // 1345 - 67.25 - 134.50 + 10
    }

    // ========== Invalid/Unknown Coupon Tests ==========

    /**
     * Test: Unknown coupon code
     * Expected: Treated as no coupon, standard pricing
     */
    @Test
    public void testCalculate_InvalidCoupon() {
        cart.addItem("P002", 2); // 2 × 25 = 50

        PricingResult result = pricingEngine.calculate(cart, "INVALID");

        Assert.assertEquals(result.getSubtotal(), 50.00, 0.01);
        Assert.assertEquals(result.getCouponDiscount(), 0.00, 0.01);
        Assert.assertEquals(result.getShipping(), 10.00, 0.01);
        Assert.assertEquals(result.getTotal(), 60.00, 0.01);
    }

    /**
     * Test: Empty string as coupon
     * Expected: Treated as no coupon
     */
    @Test
    public void testCalculate_EmptyCoupon() {
        cart.addItem("P003", 1); // 1 × 75 = 75

        PricingResult result = pricingEngine.calculate(cart, "");

        Assert.assertEquals(result.getSubtotal(), 75.00, 0.01);
        Assert.assertEquals(result.getCouponDiscount(), 0.00, 0.01);
        Assert.assertEquals(result.getTotal(), 85.00, 0.01); // 75 + 10
    }

    // ========== Edge Cases ==========

    /**
     * Test: Minimum cart value (1 item at lowest price)
     * Expected: Correct calculation with standard shipping
     */
    @Test
    public void testCalculate_MinimumValue() {
        cart.addItem("P005", 1); // 1 × 10 = 10

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getSubtotal(), 10.00, 0.01);
        Assert.assertEquals(result.getTotal(), 20.00, 0.01);
    }

    /**
     * Test: Large order with multiple high-value items
     * Expected: Accurate calculations for large amounts
     */
    @Test
    public void testCalculate_LargeOrder() {
        cart.addItem("P001", 5); // 5 × 1200 = 6000
        cart.addItem("P004", 5); // 5 × 300 = 1500
        // Total: 10 items, subtotal = 7500

        PricingResult result = pricingEngine.calculate(cart, "SAVE10");

        Assert.assertEquals(result.getSubtotal(), 7500.00, 0.01);
        Assert.assertEquals(result.getBulkDiscount(), 375.00, 0.01); // 5% of 7500
        Assert.assertEquals(result.getCouponDiscount(), 750.00, 0.01); // 10% of 7500
        Assert.assertEquals(result.getTotal(), 6385.00, 0.01); // 7500 - 375 - 750 + 10
    }

    /**
     * Test: Boundary case - exactly 4 items (no bulk discount)
     * Expected: No bulk discount triggered
     */
    @Test
    public void testCalculate_BoundaryFourItems() {
        cart.addItem("P002", 4); // 4 × 25 = 100

        PricingResult result = pricingEngine.calculate(cart, null);

        Assert.assertEquals(result.getBulkDiscount(), 0.00, 0.01);
        Assert.assertEquals(result.getTotal(), 110.00, 0.01);
    }

    /**
     * Test: Pricing result rounding accuracy
     * Expected: Results rounded to 2 decimal places
     */
    @Test
    public void testCalculate_RoundingAccuracy() {
        cart.addItem("P002", 7); // 7 × 25 = 175

        PricingResult result = pricingEngine.calculate(cart, "SAVE10");

        // 175 - 8.75 (bulk 5%) - 17.50 (coupon 10%) + 10 = 158.75
        Assert.assertEquals(result.getBulkDiscount(), 8.75, 0.01);
        Assert.assertEquals(result.getCouponDiscount(), 17.50, 0.01);
        Assert.assertEquals(result.getTotal(), 158.75, 0.01);
    }
}