package com.ordersys;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for ProductCatalog class.
 * Tests product retrieval, stock reservation, and stock release functionality.
 */
public class ProductCatalogTest {

    private Map<String, Product> mockProducts;
    private ProductCatalog catalog;

    @BeforeMethod
    public void setup() {
        // Initialize test data with sample products
        mockProducts = new HashMap<>();
        mockProducts.put("P001", new Product("P001", "Laptop", 1200.00, 10));
        mockProducts.put("P002", new Product("P002", "Mouse", 25.00, 50));
        mockProducts.put("P003", new Product("P003", "Keyboard", 75.00, 30));
        mockProducts.put("P004", new Product("P004", "Monitor", 300.00, 15));
        mockProducts.put("P005", new Product("P005", "USB Cable", 10.00, 100));
        catalog = new ProductCatalog(mockProducts);
    }

    // ========== Product Retrieval Tests ==========

    /**
     * Test: Successfully retrieve a product with a valid ID
     * Expected: Product object is returned with correct attributes
     */
    @Test
    public void testGetProduct_ValidId() {
        Product product = catalog.getProduct("P001");
        Assert.assertNotNull(product);
        Assert.assertEquals(product.getName(), "Laptop");
        Assert.assertEquals(product.getPrice(), 1200.00);
        Assert.assertEquals(product.getStock(), 10);
    }

    /**
     * Test: Attempt to retrieve a product with invalid/non-existent ID
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetProduct_InvalidId() {
        catalog.getProduct("P999");
    }

    /**
     * Test: Retrieve multiple different products
     * Expected: Each product is correctly retrieved
     */
    @Test
    public void testGetProduct_MultipleProducts() {
        Product p1 = catalog.getProduct("P001");
        Product p2 = catalog.getProduct("P002");
        Product p3 = catalog.getProduct("P003");

        Assert.assertEquals(p1.getName(), "Laptop");
        Assert.assertEquals(p2.getName(), "Mouse");
        Assert.assertEquals(p3.getName(), "Keyboard");
    }

    // ========== Stock Reservation Tests ==========

    /**
     * Test: Successfully reserve stock when sufficient quantity is available
     * Expected: Stock is reduced by the reserved quantity
     */
    @Test
    public void testReserveStock_Success() {
        Product product = catalog.getProduct("P001");
        int initialStock = product.getStock();

        catalog.reserveStock("P001", 3);

        Assert.assertEquals(product.getStock(), initialStock - 3);
    }

    /**
     * Test: Reserve exact available stock (boundary case)
     * Expected: Stock becomes zero
     */
    @Test
    public void testReserveStock_ExactAmount() {
        Product product = catalog.getProduct("P002");
        int exactStock = product.getStock();

        catalog.reserveStock("P002", exactStock);

        Assert.assertEquals(product.getStock(), 0);
    }

    /**
     * Test: Attempt to reserve more stock than available
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReserveStock_InsufficientStock() {
        catalog.reserveStock("P001", 15); // Only 10 available
    }

    /**
     * Test: Attempt to reserve stock for invalid product ID
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReserveStock_InvalidProductId() {
        catalog.reserveStock("P999", 5);
    }

    /**
     * Test: Multiple sequential stock reservations
     * Expected: Stock is correctly reduced after each reservation
     */
    @Test
    public void testReserveStock_MultipleReservations() {
        Product product = catalog.getProduct("P003");
        int initialStock = product.getStock();

        catalog.reserveStock("P003", 5);
        Assert.assertEquals(product.getStock(), initialStock - 5);

        catalog.reserveStock("P003", 10);
        Assert.assertEquals(product.getStock(), initialStock - 15);
    }

    // ========== Stock Release Tests ==========

    /**
     * Test: Successfully release stock back to inventory
     * Expected: Stock is increased by the released quantity
     */
    @Test
    public void testReleaseStock_Success() {
        Product product = catalog.getProduct("P001");
        int initialStock = product.getStock();

        // First reserve some stock
        catalog.reserveStock("P001", 5);
        Assert.assertEquals(product.getStock(), initialStock - 5);

        // Then release it back
        catalog.releaseStock("P001", 5);
        Assert.assertEquals(product.getStock(), initialStock);
    }

    /**
     * Test: Release stock for a product (can exceed original stock)
     * Expected: Stock is increased correctly
     */
    @Test
    public void testReleaseStock_MultipleReleases() {
        Product product = catalog.getProduct("P004");
        int initialStock = product.getStock();

        catalog.releaseStock("P004", 10);
        Assert.assertEquals(product.getStock(), initialStock + 10);

        catalog.releaseStock("P004", 5);
        Assert.assertEquals(product.getStock(), initialStock + 15);
    }

    /**
     * Test: Attempt to release stock for invalid product ID
     * Expected: IllegalArgumentException is thrown
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReleaseStock_InvalidProductId() {
        catalog.releaseStock("P999", 5);
    }

    // ========== Edge Case Tests ==========

    /**
     * Test: Reserve minimum valid quantity (1 item)
     * Expected: Stock is reduced by 1
     */
    @Test
    public void testReserveStock_MinimumQuantity() {
        Product product = catalog.getProduct("P005");
        int initialStock = product.getStock();

        catalog.reserveStock("P005", 1);

        Assert.assertEquals(product.getStock(), initialStock - 1);
    }

    /**
     * Test: Product with low stock (boundary value)
     * Expected: Can retrieve and reserve within available limit
     */
    @Test
    public void testLowStockProduct() {
        mockProducts.put("P006", new Product("P006", "Limited Item", 50.00, 1));
        catalog = new ProductCatalog(mockProducts);

        Product product = catalog.getProduct("P006");
        Assert.assertEquals(product.getStock(), 1);

        catalog.reserveStock("P006", 1);
        Assert.assertEquals(product.getStock(), 0);
    }
}