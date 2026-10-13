package info.syachiku.learning.cancel;

/** 記事の注文取消例を動かすための教材。JPA Entity ではない。 */
public class Order {
    public enum Status { WAITING, SHIPPED, CANCELED }
    private final long id;
    private Status status;

    private Order(long id, Status status) { this.id = id; this.status = status; }
    public static Order waitingForShipment(long id) { return new Order(id, Status.WAITING); }
    public static Order shipped(long id) { return new Order(id, Status.SHIPPED); }
    public long getId() { return id; }
    public Status getStatus() { return status; }

    public void cancel() {
        if (status != Status.WAITING) throw new OrderCannotBeCanceledException(id);
        status = Status.CANCELED;
    }
}
