package mate.academy.bookservice.dto;

public record BookSearchParametersDto(
        String title,
        String author,
        String isbn
) {
}
