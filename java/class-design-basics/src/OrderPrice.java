public final class OrderPrice {
    private static final int FREE_SHIPPING_FROM = 10_000;
    private static final int SHIPPING_FEE = 500;
    private final int subtotal;
    private final int shipping;
    private final int total;

    public OrderPrice(int unitPrice, int quantity) {
        if (unitPrice < 0 || quantity < 1) {
            throw new IllegalArgumentException("単価は0円以上、数量は1個以上です");
        }
        subtotal = Math.multiplyExact(unitPrice, quantity);
        shipping = subtotal >= FREE_SHIPPING_FROM ? 0 : SHIPPING_FEE;
        total = Math.addExact(subtotal, shipping);
    }

    public int subtotal() { return subtotal; }
    public int shipping() { return shipping; }
    public int total() { return total; }
}
