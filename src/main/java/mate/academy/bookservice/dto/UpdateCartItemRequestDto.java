package mate.academy.bookservice.dto;

import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequestDto(
        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {
}
