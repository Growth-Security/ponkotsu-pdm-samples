package info.syachiku.learning;

import java.util.HashMap;
import java.util.Map;

/** 教材用のメモリ内顧客台帳。DBや外部サービスへは接続しない。 */
public class CustomerDirectory {
    private final Map<Long, Customer> customers = new HashMap<>();
    private int lookupCalls;

    public void add(Customer customer) {
        customers.put(customer.id(), customer);
    }

    public Customer require(long customerId) {
        lookupCalls++;
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("customer not found: " + customerId);
        }
        return customer;
    }

    public int lookupCalls() {
        return lookupCalls;
    }
}
