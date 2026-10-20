package info.syachiku.books;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(@NotNull @Min(1) Integer quantity) {
}
