/** 金額の単位は円。小計は0円から1,000,000円まで。 */
public final class ShippingFee {
    public int calculate(int subtotal) {
        if (subtotal < 0 || subtotal > 1_000_000) {
            throw new IllegalArgumentException("小計は0円以上100万円以下です");
        }
        return subtotal >= 10_000 ? 0 : 500;
    }
}
