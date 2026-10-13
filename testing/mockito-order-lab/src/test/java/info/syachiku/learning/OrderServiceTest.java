package info.syachiku.learning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private final CustomerDirectory customers = mock(CustomerDirectory.class);
    private final OrderRepository orders = mock(OrderRepository.class);
    private final OrderService service = new OrderService(customers, orders);

    @ParameterizedTest @ValueSource(ints = {1, 99})
    void acceptsQuantityBoundaryAndSavesExactDraft(int quantity) {
        when(customers.require(10L)).thenReturn(new Customer(10L, true));
        when(orders.save(new OrderDraft(10L, quantity))).thenReturn(501L);
        assertEquals(501L, service.create(10L, quantity));
        verify(orders).save(new OrderDraft(10L, quantity));
        verifyNoMoreInteractions(orders);
    }

    @ParameterizedTest @NullSource @ValueSource(longs = {0, -1})
    void rejectsInvalidCustomerIdBeforeCallingDependencies(Long customerId) {
        var error = assertThrows(IllegalArgumentException.class,
                () -> service.create(customerId, 2));
        assertEquals("customerId must be positive", error.getMessage());
        verifyNoInteractions(customers, orders);
    }

    @ParameterizedTest @NullSource @ValueSource(ints = {-1, 0, 100})
    void rejectsInvalidQuantityBeforeCallingDependencies(Integer quantity) {
        var error = assertThrows(IllegalArgumentException.class,
                () -> service.create(10L, quantity));
        assertEquals("quantity must be between 1 and 99", error.getMessage());
        verifyNoInteractions(customers, orders);
    }

    @Test
    void rejectsInactiveCustomerWithoutSaving() {
        when(customers.require(10L)).thenReturn(new Customer(10L, false));
        var error = assertThrows(IllegalArgumentException.class,
                () -> service.create(10L, 2));
        assertEquals("inactive customer", error.getMessage());
        verifyNoInteractions(orders);
    }

    @Test
    void propagatesMissingCustomerWithoutSaving() {
        var missing = new IllegalArgumentException("customer not found: 10");
        when(customers.require(10L)).thenThrow(missing);
        assertSame(missing, assertThrows(IllegalArgumentException.class,
                () -> service.create(10L, 2)));
        verifyNoInteractions(orders);
    }

    @Test
    void propagatesRepositoryFailureWithoutPretendingSuccess() {
        when(customers.require(10L)).thenReturn(new Customer(10L, true));
        var unavailable = new IllegalStateException("storage unavailable (teaching example)");
        when(orders.save(new OrderDraft(10L, 2))).thenThrow(unavailable);
        assertSame(unavailable, assertThrows(IllegalStateException.class,
                () -> service.create(10L, 2)));
        verify(orders).save(new OrderDraft(10L, 2));
        verifyNoMoreInteractions(orders);
    }
}
