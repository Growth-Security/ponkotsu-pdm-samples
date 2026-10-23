public final class Main {
    public static void main(String[] args) {
        System.out.println("整理前：" + BeforeReceipt.format(3_000, 2));
        OrderPrice price = new OrderPrice(3_000, 2);
        System.out.println("整理後：" + ReceiptText.format(price));
        System.out.println("見積額：" + price.total() + "円");
        System.out.println("小計：" + price.subtotal() + "円／送料：" + price.shipping() + "円");
    }
}
