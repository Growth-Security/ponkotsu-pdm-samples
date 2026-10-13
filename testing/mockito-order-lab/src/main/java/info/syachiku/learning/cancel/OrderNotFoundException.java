package info.syachiku.learning.cancel;

public class OrderNotFoundException extends RuntimeException {
    private final long orderId;
    public OrderNotFoundException(long orderId) {
        super("order not found: " + orderId);
        this.orderId = orderId;
    }
    public long getOrderId() { return orderId; }
}
