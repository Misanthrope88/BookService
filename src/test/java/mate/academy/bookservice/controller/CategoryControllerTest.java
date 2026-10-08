package mate.academy.bookservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import mate.academy.bookservice.AbstractIntegrationTest;
import mate.academy.bookservice.dto.CategoryDto;
import mate.academy.bookservice.dto.CreateCategoryDto;
import mate.academy.bookservice.util.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest extends AbstractIntegrationTest {
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final CategoryDto categoryDto = TestUtil.fictionCategoryDto();
    private final CreateCategoryDto createRequest = TestUtil.createCategoryRequest();

    @BeforeAll
    static void setUp(@Autowired WebApplicationContext webContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Users can list categories with pagination and sorting")
    void getAll_UserRole_ReturnsCategoryPage() throws Exception {
        mockMvc.perform(get("/categories").param("page", "1").param("size", "1")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Fiction"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Users can retrieve a category by ID")
    void getById_ExistingCategory_ReturnsCategoryJson() throws Exception {
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(categoryDto)));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("A missing category produces the application's 404 response")
    void getById_MissingCategory_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find category by id: 99"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Category books are returned without category IDs")
    void getBooksByCategoryId_ExistingCategory_ReturnsBookPage() throws Exception {
        mockMvc.perform(get("/categories/1/books").param("size", "1")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("The Hobbit"))
                .andExpect(jsonPath("$.content[0].categoryIds").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(2));
        mockMvc.perform(get("/categories/1/books").param("page", "1").param("size", "1")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].categoryIds").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("Admins can create a category from JSON")
    void create_ValidRequest_ReturnsCreatedCategory() throws Exception {
        MvcResult result = mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        CategoryDto created = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);

        assertThat(created.id()).isNotNull();
        assertThat(created).isEqualTo(TestUtil.categoryDto(created.id(), createRequest));
        mockMvc.perform(get("/categories/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(created)));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Long.class))
                .isEqualTo(4);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("A blank category name is rejected without inserting a category")
    void create_BlankName_ReturnsBadRequest() throws Exception {
        CreateCategoryDto invalidRequest = new CreateCategoryDto(" ", "Fiction books");

        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must not be blank"));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Long.class))
                .isEqualTo(3);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("Admins can update a category by ID")
    void update_ValidRequest_ReturnsUpdatedCategory() throws Exception {
        CreateCategoryDto updateRequest = TestUtil.createCategoryRequest();
        CategoryDto updated = TestUtil.categoryDto(1L, updateRequest);

        mockMvc.perform(put("/categories/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Long.class))
                .isEqualTo(3);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("Admins can delete a category and receive an empty 204 response")
    void deleteById_ExistingCategory_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isNotFound());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM categories WHERE id = 1", Boolean.class)).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM books_categories WHERE category_id = 1", Long.class))
                .isEqualTo(3);
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
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(categoryDto)));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Long.class))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Anonymous requests cannot access categories")
    void getAll_AnonymousRequest_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isUnauthorized());
    }
}
