package mate.academy.bookservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.BookSearchParametersDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;
import mate.academy.bookservice.service.BookService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book catalog operations")
public class BookController {
    private final BookService bookService;

    @Operation(summary = "Get all books")
    @ApiResponse(responseCode = "200", description = "Page of books returned")
    @GetMapping
    public Page<BookDto> getAll(@ParameterObject Pageable pageable) {
        return bookService.getAll(pageable);
    }

    @Operation(summary = "Search books")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of matching books returned"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    @GetMapping("/search")
    public Page<BookDto> searchBooks(@ParameterObject @Valid
                                     BookSearchParametersDto searchParameters,
                                     @ParameterObject Pageable pageable) {
        return bookService.search(searchParameters, pageable);
    }

    @Operation(summary = "Get a book by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book returned"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public BookDto getBookById(@PathVariable Long id) {
        return bookService.getById(id);
    }

    @Operation(summary = "Create a book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created"),
            @ApiResponse(responseCode = "400", description = "Invalid book data")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto createBook(@RequestBody @Valid CreateBookRequestDto bookDto) {
        return bookService.create(bookDto);
    }

    @Operation(summary = "Update a book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated"),
            @ApiResponse(responseCode = "400", description = "Invalid book data"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @PutMapping("/{id}")
    public BookDto updateBook(@PathVariable Long id,
                              @RequestBody @Valid CreateBookRequestDto bookDto) {
        return bookService.update(id, bookDto);
    }

    @Operation(summary = "Delete a book")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);
    }
}
