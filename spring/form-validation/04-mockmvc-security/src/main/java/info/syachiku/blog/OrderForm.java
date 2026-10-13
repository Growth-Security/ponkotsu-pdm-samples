package info.syachiku.blog;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderForm {
    @NotNull
    private Long customerId;
    @NotNull
    @Min(1)
    private Integer quantity;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
