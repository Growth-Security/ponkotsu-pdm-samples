package info.syachiku.learning.cancel;

public class OrderCannotBeCanceledException extends RuntimeException {
    public OrderCannotBeCanceledException(long orderId) {
        super("order cannot be canceled: " + orderId);
    }
}
