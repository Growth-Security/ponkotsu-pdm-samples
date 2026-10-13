package info.syachiku.learning;

import org.junit.jupiter.api.Test;
import org.mockito.exceptions.misusing.WrongTypeOfReturnValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MockitoBehaviorTest {
    @Test
    void mockWhenDoesNotExecuteRealMethod() {
        CustomerDirectory directory = mock(CustomerDirectory.class);
        when(directory.require(10L)).thenReturn(new Customer(10L, true));
        assertEquals(new Customer(10L, true), directory.require(10L));
        assertEquals(0, directory.lookupCalls());
        System.out.println("OBS mock + when: real lookup not executed; configured customer returned");
    }

    @Test
    void unstubbedSpyExecutesRealMethod() {
        CustomerDirectory directory = spy(new CustomerDirectory());
        directory.add(new Customer(10L, true));
        assertEquals(new Customer(10L, true), directory.require(10L));
        assertEquals(1, directory.lookupCalls());
        System.out.println("OBS unstubbed spy: real lookup executed once");
    }

    @Test
    void spyWhenFailsBeforeThenReturnForUnknownCustomer() {
        CustomerDirectory directory = spy(new CustomerDirectory());
        var error = assertThrows(IllegalArgumentException.class,
                () -> when(directory.require(10L)).thenReturn(new Customer(10L, true)));
        assertEquals("customer not found: 10", error.getMessage());
        assertEquals(1, directory.lookupCalls());
        System.out.println("OBS spy + when: customer not found: 10 thrown DURING stubbing; real lookup=1");
    }

    @Test
    void spyDoReturnAvoidsRealMethodDuringStubbingAndInvocation() {
        CustomerDirectory directory = spy(new CustomerDirectory());
        doReturn(new Customer(10L, true)).when(directory).require(10L);
        assertEquals(0, directory.lookupCalls());
        assertEquals(new Customer(10L, true), directory.require(10L));
        assertEquals(0, directory.lookupCalls());
        System.out.println("OBS spy + doReturn: configured customer returned; real lookup=0");
    }

    @Test
    void successfulSpyWhenStillExecutesRealMethodDuringStubbing() {
        CustomerDirectory directory = spy(new CustomerDirectory());
        directory.add(new Customer(10L, true));
        when(directory.require(10L)).thenReturn(new Customer(10L, false));
        assertEquals(1, directory.lookupCalls());
        assertFalse(directory.require(10L).active());
        assertEquals(1, directory.lookupCalls());
        System.out.println("OBS spy + when with existing data: stubbing succeeds BUT real lookup already executed once");
    }

    @Test
    void doReturnAcceptsWrongTypeAtCompileTimeButRejectsItAtRuntime() {
        CustomerDirectory directory = mock(CustomerDirectory.class);
        assertThrows(WrongTypeOfReturnValue.class,
                () -> doReturn("not a Customer").when(directory).require(10L));
        System.out.println("OBS wrong doReturn type: WrongTypeOfReturnValue during stubbing");
    }
}
