package info.syachiku.learning.cancel;

import java.util.Optional;

/** 教材で必要な契約だけを定義。実際の JPA 保存動作は検証しない。 */
public interface OrderRepository {
    Optional<Order> findById(long orderId);
    Order save(Order order);
}
