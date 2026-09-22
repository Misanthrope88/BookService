package mate.academy.bookservice.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateBookRequestDto(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
        @NotBlank(message = "Author must not be blank")
        @Size(max = 255, message = "Author must not exceed 255 characters")
        String author,
        @NotBlank(message = "ISBN must not be blank")
        @Size(max = 255, message = "ISBN must not exceed 255 characters")
        String isbn,
        @NotNull(message = "Price must not be null")
        @Positive(message = "Price must be greater than zero")
        @Digits(
                integer = 36,
                fraction = 2,
                message = "Price must have up to 36 integer digits and 2 decimal places"
        )
        BigDecimal price,
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,
        @Size(max = 255, message = "Cover image must not exceed 255 characters")
        String coverImage
) {
}
