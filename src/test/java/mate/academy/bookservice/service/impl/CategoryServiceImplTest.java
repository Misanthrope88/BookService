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
import mate.academy.bookservice.dto.BookDtoWithoutCategoryIds;
import mate.academy.bookservice.dto.CategoryDto;
import mate.academy.bookservice.dto.CreateCategoryDto;
import mate.academy.bookservice.exception.EntityNotFoundException;
import mate.academy.bookservice.mapper.BookMapper;
import mate.academy.bookservice.mapper.CategoryMapper;
import mate.academy.bookservice.model.Book;
import mate.academy.bookservice.model.Category;
import mate.academy.bookservice.repository.BookRepository;
import mate.academy.bookservice.repository.CategoryRepository;
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

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private BookMapper bookMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Fiction");
        category.setDescription("Fiction books");
        categoryDto = new CategoryDto(1L, "Fiction", "Fiction books");
    }

    @Test
    @DisplayName("Category listing maps results and preserves pagination")
    void findAll_ExistingCategories_ReturnsMappedPage() {
        PageRequest pageable = PageRequest.of(1, 2);
        when(categoryRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(category), pageable, 3));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        Page<CategoryDto> result = categoryService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(categoryDto);
        assertThat(result.getPageable()).isEqualTo(pageable);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("An existing category is returned as a DTO")
    void getById_ExistingCategory_ReturnsCategoryDto() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryDto result = categoryService.getById(1L);

        assertThat(result).isEqualTo(categoryDto);
    }

    @Test
    @DisplayName("Looking up a missing category throws a not-found exception")
    void getById_MissingCategory_ThrowsEntityNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find category by id: 99");
    }

    @Test
    @DisplayName("Category creation saves the mapped entity and returns its DTO")
    void save_ValidRequest_ReturnsSavedCategory() {
        CreateCategoryDto request = new CreateCategoryDto("Fiction", "Fiction books");
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryDto result = categoryService.save(request);

        assertThat(result).isEqualTo(categoryDto);
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Category update maps the request onto the existing entity")
    void update_ExistingCategory_ReturnsUpdatedCategory() {
        CreateCategoryDto request = new CreateCategoryDto("Fantasy", "Fantasy books");
        CategoryDto expected = new CategoryDto(1L, "Fantasy", "Fantasy books");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expected);

        CategoryDto result = categoryService.update(1L, request);

        assertThat(result).isEqualTo(expected);
        verify(categoryMapper).updateCategoryFromDto(request, category);
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Deleting an existing category delegates to the repository")
    void deleteById_ExistingCategory_DeletesCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteById(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deleting a missing category throws without deleting anything")
    void deleteById_MissingCategory_ThrowsEntityNotFoundException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find category by id: 99");
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Category books are mapped without category IDs")
    void getBooksByCategoryId_ExistingCategory_ReturnsMappedBooks() {
        Book book = new Book();
        book.setId(2L);
        BookDtoWithoutCategoryIds expected = new BookDtoWithoutCategoryIds(2L, "The Hobbit",
                "Tolkien", "9780000000001", new BigDecimal("20.00"), null, null);
        PageRequest pageable = PageRequest.of(0, 5);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.findAllByCategoriesId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(book), pageable, 1));
        when(bookMapper.toDtoWithoutCategories(book)).thenReturn(expected);

        Page<BookDtoWithoutCategoryIds> result = categoryService.getBooksByCategoryId(1L, pageable);

        assertThat(result.getContent()).containsExactly(expected);
        assertThat(result.getPageable()).isEqualTo(pageable);
    }

    @Test
    @DisplayName("Books cannot be listed for a missing category")
    void getBooksByCategoryId_MissingCategory_ThrowsEntityNotFoundException() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getBooksByCategoryId(99L, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find category by id: 99");
        verify(bookRepository, never()).findAllByCategoriesId(any(), any());
    }
}
