package info.syachiku.blog;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public record OrderRequest(@NotNull Long customerId, @NotNull @Min(1) Integer quantity) {}
