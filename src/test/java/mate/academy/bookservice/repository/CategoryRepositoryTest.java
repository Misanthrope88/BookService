package mate.academy.bookservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import mate.academy.bookservice.AbstractIntegrationTest;
import mate.academy.bookservice.model.Category;
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
class CategoryRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Category pagination excludes soft-deleted rows")
    void findAll_DeletedCategoryPresent_ReturnsOnlyActiveCategories() {
        Page<Category> result = categoryRepository.findAll(
                PageRequest.of(0, 1, Sort.by("name")));

        assertThat(result.getContent()).extracting(Category::getId).containsExactly(2L);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(categoryRepository.findById(3L)).isEmpty();
        assertThat(categoryRepository.existsById(3L)).isFalse();
    }

    @Test
    @DisplayName("Deleting a referenced category preserves its row and book links")
    void deleteById_ReferencedCategory_SoftDeletesCategory() {
        categoryRepository.deleteById(1L);
        categoryRepository.flush();
        entityManager.clear();

        assertThat(categoryRepository.findById(1L)).isEmpty();
        assertThat(categoryRepository.findAll()).extracting(Category::getId).containsExactly(2L);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM categories WHERE id = 1", Boolean.class)).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM books_categories WHERE category_id = 1", Long.class))
                .isEqualTo(3);
    }
}
