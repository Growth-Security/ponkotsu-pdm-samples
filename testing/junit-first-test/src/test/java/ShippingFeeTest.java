import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShippingFeeTest {
    private final ShippingFee shippingFee = new ShippingFee();

    @Test
    void subtotal9999Costs500() {
        assertEquals(500, shippingFee.calculate(9_999));
    }

    @Test
    void subtotal10000IsFree() {
        assertEquals(0, shippingFee.calculate(10_000));
    }

    @Test
    void negativeSubtotalIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> shippingFee.calculate(-1));
    }
}
