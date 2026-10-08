package mate.academy.bookservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.BookSearchParametersDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;
import mate.academy.bookservice.exception.EntityNotFoundException;
import mate.academy.bookservice.mapper.BookMapper;
import mate.academy.bookservice.model.Book;
import mate.academy.bookservice.model.Category;
import mate.academy.bookservice.repository.BookRepository;
import mate.academy.bookservice.repository.CategoryRepository;
import mate.academy.bookservice.repository.specification.BookSpecificationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;
    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookDto bookDto;
    private CreateBookRequestDto request;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("The Hobbit");
        bookDto = new BookDto(1L, "The Hobbit", "Tolkien", "9780000000001",
                new BigDecimal("20.00"), "A journey", "cover.jpg", Set.of(1L, 2L));
        request = new CreateBookRequestDto(bookDto.title(), bookDto.author(), bookDto.isbn(),
                bookDto.price(), bookDto.description(), bookDto.coverImage(),
                bookDto.categoryIds());
    }

    @Test
    @DisplayName("Creation resolves all requested categories before saving")
    void create_ExistingCategories_SavesBookWithCategories() {
        Category fiction = new Category();
        fiction.setId(1L);
        Category classics = new Category();
        classics.setId(2L);
        when(bookMapper.toEntity(request)).thenReturn(book);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(fiction));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(classics));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        BookDto result = bookService.create(request);

        assertThat(result).isEqualTo(bookDto);
        assertThat(book.getCategories()).containsExactlyInAnyOrder(fiction, classics);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Creation rejects a missing category without saving the book")
    void create_MissingCategory_ThrowsEntityNotFoundException() {
        CreateBookRequestDto invalidRequest = new CreateBookRequestDto(request.title(),
                request.author(), request.isbn(), request.price(), null, null, Set.of(99L));
        when(bookMapper.toEntity(invalidRequest)).thenReturn(book);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.create(invalidRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find category by id: 99");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Book listing maps results and preserves pagination")
    void getAll_ExistingBooks_ReturnsMappedPage() {
        PageRequest pageable = PageRequest.of(1, 2);
        when(bookRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(book), pageable, 3));
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        Page<BookDto> result = bookService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(bookDto);
        assertThat(result.getPageable()).isEqualTo(pageable);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("An existing book is returned as a DTO")
    void getById_ExistingBook_ReturnsBookDto() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        BookDto result = bookService.getById(1L);

        assertThat(result).isEqualTo(bookDto);
    }

    @Test
    @DisplayName("Looking up a missing book throws a not-found exception")
    void getById_MissingBook_ThrowsEntityNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find book by id: 99");
    }

    @Test
    @DisplayName("Search uses the built specification and maps matching books")
    void search_MatchingParameters_ReturnsMappedPage() {
        BookSearchParametersDto parameters = new BookSearchParametersDto("Hobbit", null, null);
        PageRequest pageable = PageRequest.of(0, 5);
        Specification<Book> specification = (root, query, builder) -> builder.conjunction();
        when(bookSpecificationBuilder.build(parameters)).thenReturn(specification);
        when(bookRepository.findAll(specification, pageable))
                .thenReturn(new PageImpl<>(List.of(book), pageable, 1));
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        Page<BookDto> result = bookService.search(parameters, pageable);

        assertThat(result.getContent()).containsExactly(bookDto);
        assertThat(result.getPageable()).isEqualTo(pageable);
    }

    @Test
    @DisplayName("Updating a book replaces its categories and saves the existing entity")
    void update_ExistingBook_ReplacesCategoriesAndReturnsDto() {
        Category oldCategory = new Category();
        oldCategory.setId(3L);
        book.setCategories(Set.of(oldCategory));
        Category fiction = new Category();
        fiction.setId(1L);
        Category classics = new Category();
        classics.setId(2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(fiction));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(classics));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        BookDto result = bookService.update(1L, request);

        assertThat(result).isEqualTo(bookDto);
        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getCategories()).containsExactlyInAnyOrder(fiction, classics);
        verify(bookMapper).updateBookFromDto(request, book);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Deleting an existing book delegates to the repository")
    void deleteById_ExistingBook_DeletesBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteById(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deleting a missing book throws without deleting anything")
    void deleteById_MissingBook_ThrowsEntityNotFoundException() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find book by id: 99");
        verify(bookRepository, never()).deleteById(any());
    }
}
