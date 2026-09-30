package mate.academy.bookservice.dto;

import jakarta.validation.constraints.NotNull;
import mate.academy.bookservice.model.Status;

public record UpdateOrderStatusRequestDto(
        @NotNull(message = "Status must not be null")
        Status status
) {
}
