package mate.academy.bookservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import mate.academy.bookservice.AbstractIntegrationTest;
import mate.academy.bookservice.dto.BookSearchParametersDto;
import mate.academy.bookservice.model.Book;
import mate.academy.bookservice.model.Category;
import mate.academy.bookservice.repository.specification.BookSpecificationBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Category filtering paginates matching active books")
    void findAllByCategoriesId_ExistingCategory_ReturnsMatchingActiveBooks() {
        PageRequest pageable = PageRequest.of(0, 1, Sort.by("title"));

        Page<Book> result = bookRepository.findAllByCategoriesId(1L, pageable);

        assertThat(result.getContent()).extracting(Book::getId).containsExactly(1L);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(bookRepository.findAllByCategoriesId(1L, pageable.next()).getContent())
                .extracting(Book::getId).containsExactly(2L);
    }

    @Test
    @DisplayName("An unknown category has no books")
    void findAllByCategoriesId_MissingCategory_ReturnsEmptyPage() {
        Page<Book> result = bookRepository.findAllByCategoriesId(99L, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Search combines trimmed title, author and exact ISBN filters")
    void findAll_CombinedSearchParameters_ReturnsMatchingBook() {
        BookSearchParametersDto parameters = new BookSearchParametersDto(
                " THE ", " TOLKIEN ", "9780000000002");
        BookSpecificationBuilder builder = new BookSpecificationBuilder();

        Page<Book> result = bookRepository.findAll(
                builder.build(parameters), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Book::getId).containsExactly(2L);
    }

    @Test
    @DisplayName("Deleting a book preserves its row and hides it from queries")
    void deleteById_ExistingBook_SoftDeletesBook() {
        bookRepository.deleteById(1L);
        bookRepository.flush();
        entityManager.clear();

        assertThat(bookRepository.findById(1L)).isEmpty();
        assertThat(bookRepository.findAll()).extracting(Book::getId)
                .containsExactlyInAnyOrder(2L, 3L);
        assertThat(bookRepository.findAllByCategoriesId(1L, PageRequest.of(0, 10)))
                .extracting(Book::getId).containsExactly(2L);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM books WHERE id = 1", Boolean.class)).isTrue();
    }

    @Test
    @DisplayName("Book relationships load active categories only")
    void findById_BookWithDeletedCategory_ReturnsOnlyActiveCategories() {
        Book result = bookRepository.findById(1L).orElseThrow();

        assertThat(result.getCategories()).extracting(Category::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }
}
