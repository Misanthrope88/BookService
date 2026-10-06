package mate.academy.bookservice.util;

import java.math.BigDecimal;
import java.util.Set;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.CategoryDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;
import mate.academy.bookservice.dto.CreateCategoryDto;

public final class TestUtil {
    private TestUtil() {
    }

    public static BookDto hobbitDto() {
        return new BookDto(1L, "The Hobbit", "J. R. R. Tolkien", "9780000000001",
                new BigDecimal("20.00"), null, null, Set.of(1L, 2L));
    }

    public static CreateBookRequestDto createBookRequest() {
        return new CreateBookRequestDto("Dune", "Frank Herbert", "9780000000005",
                new BigDecimal("40.00"), "A desert planet", "dune.jpg", Set.of(1L, 2L));
    }

    public static BookDto bookDto(Long id, CreateBookRequestDto request) {
        return new BookDto(id, request.title(), request.author(), request.isbn(),
                request.price(), request.description(), request.coverImage(),
                request.categoryIds());
    }

    public static CreateCategoryDto createCategoryRequest() {
        return new CreateCategoryDto("Fantasy", "Fantasy books");
    }

    public static CategoryDto fictionCategoryDto() {
        return new CategoryDto(1L, "Fiction", "Fiction books");
    }

    public static CategoryDto categoryDto(Long id, CreateCategoryDto request) {
        return new CategoryDto(id, request.name(), request.description());
    }
}
