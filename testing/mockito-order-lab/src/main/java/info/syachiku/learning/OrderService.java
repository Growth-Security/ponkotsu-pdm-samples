package info.syachiku.learning;

public class OrderService {
    private final CustomerDirectory customers;
    private final OrderRepository orders;

    public OrderService(CustomerDirectory customers, OrderRepository orders) {
        this.customers = customers;
        this.orders = orders;
    }

    public long create(Long customerId, Integer quantity) {
        if (customerId == null || customerId < 1) {
            throw new IllegalArgumentException("customerId must be positive");
        }
        if (quantity == null || quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("quantity must be between 1 and 99");
        }
        Customer customer = customers.require(customerId);
        if (!customer.active()) {
            throw new IllegalArgumentException("inactive customer");
        }
        return orders.save(new OrderDraft(customer.id(), quantity));
    }
}
