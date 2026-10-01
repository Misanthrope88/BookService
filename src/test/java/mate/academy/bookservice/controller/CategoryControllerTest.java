package mate.academy.bookservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import mate.academy.bookservice.config.SecurityConfig;
import mate.academy.bookservice.dto.BookDtoWithoutCategoryIds;
import mate.academy.bookservice.dto.CategoryDto;
import mate.academy.bookservice.dto.CreateCategoryDto;
import mate.academy.bookservice.exception.EntityNotFoundException;
import mate.academy.bookservice.security.JwtAuthenticationFilter;
import mate.academy.bookservice.security.JwtUtil;
import mate.academy.bookservice.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private final CategoryDto categoryDto = new CategoryDto(1L, "Fiction", "Fiction books");
    private final CreateCategoryDto createRequest = new CreateCategoryDto(
            "Fiction", "Fiction books");

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Users can list categories with pagination and sorting")
    void getAll_UserRole_ReturnsCategoryPage() throws Exception {
        PageRequest pageable = PageRequest.of(1, 2, Sort.by("name"));
        when(categoryService.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(categoryDto), pageable, 3));

        mockMvc.perform(get("/categories").param("page", "1").param("size", "2")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Fiction"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Users can retrieve a category by ID")
    void getById_ExistingCategory_ReturnsCategoryJson() throws Exception {
        when(categoryService.getById(1L)).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(categoryDto)));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("A missing category produces the application's 404 response")
    void getById_MissingCategory_ReturnsNotFound() throws Exception {
        when(categoryService.getById(99L))
                .thenThrow(new EntityNotFoundException("Can't find category by id: 99"));

        mockMvc.perform(get("/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find category by id: 99"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Category books are returned without category IDs")
    void getBooksByCategoryId_ExistingCategory_ReturnsBookPage() throws Exception {
        PageRequest pageable = PageRequest.of(0, 5);
        BookDtoWithoutCategoryIds bookDto = new BookDtoWithoutCategoryIds(2L, "The Hobbit",
                "Tolkien", "9780000000001", new BigDecimal("20.00"), null, null);
        when(categoryService.getBooksByCategoryId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(bookDto), pageable, 1));

        mockMvc.perform(get("/categories/1/books").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].title").value("The Hobbit"))
                .andExpect(jsonPath("$.content[0].categoryIds").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admins can create a category from JSON")
    void create_ValidRequest_ReturnsCreatedCategory() throws Exception {
        when(categoryService.save(createRequest)).thenReturn(categoryDto);

        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(categoryDto)));
        verify(categoryService).save(createRequest);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("A blank category name is rejected before reaching the service")
    void create_BlankName_ReturnsBadRequest() throws Exception {
        CreateCategoryDto invalidRequest = new CreateCategoryDto(" ", "Fiction books");

        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must not be blank"));
        verify(categoryService, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admins can update a category by ID")
    void update_ValidRequest_ReturnsUpdatedCategory() throws Exception {
        CreateCategoryDto updateRequest = new CreateCategoryDto("Fantasy", "Fantasy books");
        CategoryDto updated = new CategoryDto(1L, "Fantasy", "Fantasy books");
        when(categoryService.update(1L, updateRequest)).thenReturn(updated);

        mockMvc.perform(put("/categories/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        verify(categoryService).update(1L, updateRequest);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admins can delete a category and receive an empty 204 response")
    void deleteById_ExistingCategory_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(categoryService).deleteById(1L);
    }

    @ParameterizedTest
    @CsvSource({"POST, /categories", "PUT, /categories/1", "DELETE, /categories/1"})
    @WithMockUser(roles = "USER")
    @DisplayName("Users cannot create, update or delete categories")
    void writeEndpoints_UserRole_ReturnForbidden(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
        verify(categoryService, never()).save(any());
        verify(categoryService, never()).update(any(), any());
        verify(categoryService, never()).deleteById(any());
    }

    @Test
    @DisplayName("Anonymous requests cannot access categories")
    void getAll_AnonymousRequest_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isUnauthorized());
        verify(categoryService, never()).findAll(any());
    }
}
