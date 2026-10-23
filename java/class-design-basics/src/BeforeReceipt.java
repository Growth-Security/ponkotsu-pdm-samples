public final class BeforeReceipt {
    private BeforeReceipt() {}

    public static String format(int unitPrice, int quantity) {
        if (unitPrice < 0 || quantity < 1) {
            throw new IllegalArgumentException("単価は0円以上、数量は1個以上です");
        }
        int subtotal = Math.multiplyExact(unitPrice, quantity);
        int shipping = subtotal >= 10_000 ? 0 : 500;
        int total = Math.addExact(subtotal, shipping);
        return "請求額：" + total + "円";
    }
}
