package com.ordersys;

/**
 * Calculates pricing with discounts and shipping.
 */
public class PricingEngine {

    /**
     * Calculates the total price for a cart with optional coupon.
     *
     * Pricing rules:
     * - Bulk discount: 5% off subtotal if 5 or more items
     * - Coupon "SAVE10": 10% off subtotal
     * - Coupon "FREESHIP": Free shipping
     * - Default shipping: $10
     *
     * @param cart The shopping cart
     * @param coupon Optional coupon code (can be null)
     * @return PricingResult with breakdown of charges
     */
    public PricingResult calculate(Cart cart, String coupon) {
        double subtotal = cart.getSubtotal();
        int totalItems = cart.getTotalItems();

        // Calculate bulk discount (5% off if 5 or more items)
        double bulkDiscount = totalItems >= 5 ? 0.05 * subtotal : 0.0;

        // Calculate coupon discount
        double couponDiscount = 0.0;
        double shipping = 10.0;

        if (coupon != null) {
            switch (coupon) {
                case "SAVE10":
                    couponDiscount = 0.10 * subtotal;
                    break;
                case "FREESHIP":
                    shipping = 0.0;
                    break;
            }
        }

        // Calculate final total
        double total = subtotal - bulkDiscount - couponDiscount + shipping;

        return new PricingResult(subtotal, bulkDiscount, couponDiscount, shipping, total);
    }
}