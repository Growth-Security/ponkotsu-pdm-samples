package info.syachiku.learning.cancel;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancelOrderServiceTest {
    private final OrderRepository repository = mock(OrderRepository.class);
    private final OrderService service = new OrderService(repository);

    @Test
    void waitingOrderIsCanceledAndSaved() {
        Order order = Order.waitingForShipment(100L);
        when(repository.findById(100L)).thenReturn(Optional.of(order));
        service.cancel(100L);
        assertEquals(Order.Status.CANCELED, order.getStatus());
        verify(repository).save(order);
    }

    @Test
    void missingOrderIsRejectedWithoutSaving() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        var error = assertThrows(OrderNotFoundException.class, () -> service.cancel(999L));
        assertEquals(999L, error.getOrderId());
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shippedOrderIsRejectedWithoutStateChangeOrSaving() {
        Order order = Order.shipped(200L);
        when(repository.findById(200L)).thenReturn(Optional.of(order));
        assertThrows(OrderCannotBeCanceledException.class, () -> service.cancel(200L));
        assertEquals(Order.Status.SHIPPED, order.getStatus());
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void alreadyCanceledOrderIsRejectedWithoutSavingAgain() {
        Order order = Order.waitingForShipment(300L);
        order.cancel();
        when(repository.findById(300L)).thenReturn(Optional.of(order));
        assertThrows(OrderCannotBeCanceledException.class, () -> service.cancel(300L));
        assertEquals(Order.Status.CANCELED, order.getStatus());
        verify(repository, never()).save(any(Order.class));
    }
}
