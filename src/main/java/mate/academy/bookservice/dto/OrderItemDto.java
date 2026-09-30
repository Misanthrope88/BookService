package mate.academy.bookservice.dto;

public record OrderItemDto(
        Long id,
        Long bookId,
        int quantity
) {
}
