package mate.academy.bookservice.dto;

import jakarta.validation.constraints.Size;

public record BookSearchParametersDto(
        @Size(max = 255, message = "Title must not exceed 255 characters") String title,
        @Size(max = 255, message = "Author must not exceed 255 characters") String author,
        @Size(max = 255, message = "ISBN must not exceed 255 characters") String isbn
) {
}
