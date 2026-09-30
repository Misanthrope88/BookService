package mate.academy.bookservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequestDto(
        @NotBlank(message = "Shipping address must not be blank")
        @Size(max = 255, message = "Shipping address must not exceed 255 characters")
        String shippingAddress
) {
}
