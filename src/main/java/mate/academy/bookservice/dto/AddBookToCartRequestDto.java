package mate.academy.bookservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddBookToCartRequestDto(
        @NotNull(message = "Book ID must not be null")
        @Positive(message = "Book ID must be greater than zero")
        Long bookId,
        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {
}
