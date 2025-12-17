package com.ordersys;

/**
 * Represents the result of a pricing calculation.
 */
public class PricingResult {
    private double subtotal;
    private double bulkDiscount;
    private double couponDiscount;
    private double shipping;
    private double total;

    public PricingResult(double subtotal, double bulkDiscount, double couponDiscount,
                         double shipping, double total) {
        this.subtotal = Math.round(subtotal * 100.0) / 100.0;
        this.bulkDiscount = Math.round(bulkDiscount * 100.0) / 100.0;
        this.couponDiscount = Math.round(couponDiscount * 100.0) / 100.0;
        this.shipping = shipping;
        this.total = Math.round(total * 100.0) / 100.0;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getBulkDiscount() {
        return bulkDiscount;
    }

    public double getCouponDiscount() {
        return couponDiscount;
    }

    public double getShipping() {
        return shipping;
    }

    public double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return String.format("PricingResult{subtotal=%.2f, bulkDiscount=%.2f, couponDiscount=%.2f, shipping=%.2f, total=%.2f}",
                subtotal, bulkDiscount, couponDiscount, shipping, total);
    }
}