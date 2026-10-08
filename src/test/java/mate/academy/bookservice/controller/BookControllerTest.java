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

import java.math.BigDecimal;
import java.util.Set;
import mate.academy.bookservice.AbstractIntegrationTest;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;
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
class BookControllerTest extends AbstractIntegrationTest {
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final BookDto bookDto = TestUtil.hobbitDto();
    private final CreateBookRequestDto createRequest = TestUtil.createBookRequest();

    @BeforeAll
    static void setUp(@Autowired WebApplicationContext webContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Users can list books with pagination and sorting")
    void getAll_UserRole_ReturnsBookPage() throws Exception {
        mockMvc.perform(get("/books").param("page", "1").param("size", "2")
                        .param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].title").value("Pride and Prejudice"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Search filters persisted books by title, author and ISBN")
    void searchBooks_ValidParameters_ReturnsMatchingBooks() throws Exception {
        mockMvc.perform(get("/books/search").param("title", "Hobbit")
                        .param("author", "Tolkien").param("isbn", "9780000000001")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].isbn").value("9780000000001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Users can retrieve a book with its category IDs")
    void getBookById_ExistingBook_ReturnsBookJson() throws Exception {
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookDto)));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("A missing book produces the application's 404 response")
    void getBookById_MissingBook_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find book by id: 99"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("Admins can create a book and persist its category relationships")
    void createBook_ValidRequest_ReturnsCreatedBook() throws Exception {
        MvcResult result = mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        BookDto created = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookDto.class);

        assertThat(created.id()).isNotNull();
        assertThat(created).isEqualTo(TestUtil.bookDto(created.id(), createRequest));
        mockMvc.perform(get("/books/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(created)));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class))
                .isEqualTo(5);
        assertThat(jdbcTemplate.queryForList(
                "SELECT category_id FROM books_categories WHERE book_id = ?",
                Long.class, created.id())).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Invalid book fields are rejected without inserting a book")
    void createBook_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateBookRequestDto invalidRequest = new CreateBookRequestDto(" ", "Tolkien",
                "9780000000001", BigDecimal.ZERO, null, null, Set.of());

        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title must not be blank"))
                .andExpect(jsonPath("$.price").value("Price must be greater than zero"))
                .andExpect(jsonPath("$.categoryIds").exists());
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class))
                .isEqualTo(4);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("Admins can update a book by ID")
    void updateBook_ValidRequest_ReturnsUpdatedBook() throws Exception {
        CreateBookRequestDto updateRequest = new CreateBookRequestDto("Updated title",
                "Tolkien", "9780000000001", new BigDecimal("25.00"),
                "Updated description", "updated.jpg", Set.of(2L));
        BookDto updated = TestUtil.bookDto(1L, updateRequest);

        mockMvc.perform(put("/books/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class))
                .isEqualTo(4);
        assertThat(jdbcTemplate.queryForList(
                "SELECT category_id FROM books_categories WHERE book_id = 1", Long.class))
                .containsExactly(2L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("Admins can delete a book and receive an empty 204 response")
    void deleteBook_ExistingBook_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isNotFound());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM books WHERE id = 1", Boolean.class)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"POST, /books", "PUT, /books/1", "DELETE, /books/1"})
    @WithMockUser(roles = "USER")
    @DisplayName("Users cannot create, update or delete books")
    void writeEndpoints_UserRole_ReturnForbidden(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookDto)));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Long.class))
                .isEqualTo(4);
    }

    @Test
    @DisplayName("Anonymous requests cannot access books")
    void getAll_AnonymousRequest_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isUnauthorized());
    }
}
