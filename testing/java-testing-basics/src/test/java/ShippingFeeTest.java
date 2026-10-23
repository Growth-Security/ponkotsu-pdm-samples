import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShippingFeeTest {
    private final ShippingFee shippingFee = new ShippingFee();

    @Test
    void 小計0円を受け付け送料500円を返す() {
        assertEquals(500, shippingFee.calculate(0));
    }

    @Test
    void 小計1円の送料は500円() {
        assertEquals(500, shippingFee.calculate(1));
    }

    @Test
    void 小計9999円の送料は500円() {
        assertEquals(500, shippingFee.calculate(9_999));
    }

    @Test
    void 小計10000円の送料は0円() {
        assertEquals(0, shippingFee.calculate(10_000));
    }

    @Test
    void 小計10001円の送料は0円() {
        assertEquals(0, shippingFee.calculate(10_001));
    }

    @Test
    void 小計999999円の送料は0円() {
        assertEquals(0, shippingFee.calculate(999_999));
    }

    @Test
    void 小計100万円を受け付け送料0円を返す() {
        assertEquals(0, shippingFee.calculate(1_000_000));
    }

    @Test
    void 負の小計は受け付けない() {
        assertThrows(IllegalArgumentException.class,
                () -> shippingFee.calculate(-1));
    }

    @Test
    void 小計100万円を超えると受け付けない() {
        assertThrows(IllegalArgumentException.class,
                () -> shippingFee.calculate(1_000_001));
    }
}
