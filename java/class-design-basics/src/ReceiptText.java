public final class ReceiptText {
    private ReceiptText() {}

    public static String format(OrderPrice price) {
        return "請求額：" + price.total() + "円";
    }
}
