package mate.academy.bookservice.dto;

public record CartItemDto(
        Long id,
        Long bookId,
        String bookTitle,
        int quantity
) {
}
