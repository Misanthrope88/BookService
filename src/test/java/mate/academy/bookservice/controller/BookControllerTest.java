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
import java.util.Set;
import mate.academy.bookservice.config.SecurityConfig;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.BookSearchParametersDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;
import mate.academy.bookservice.exception.EntityNotFoundException;
import mate.academy.bookservice.security.JwtAuthenticationFilter;
import mate.academy.bookservice.security.JwtUtil;
import mate.academy.bookservice.service.BookService;
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

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private final BookDto bookDto = new BookDto(1L, "The Hobbit", "Tolkien", "9780000000001",
            new BigDecimal("20.00"), "A journey", "cover.jpg", Set.of(2L));
    private final CreateBookRequestDto createRequest = new CreateBookRequestDto(
            "The Hobbit", "Tolkien", "9780000000001", new BigDecimal("20.00"),
            "A journey", "cover.jpg", Set.of(2L));

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Users can list books with pagination and sorting")
    void getAll_UserRole_ReturnsBookPage() throws Exception {
        PageRequest pageable = PageRequest.of(1, 2, Sort.by("title").descending());
        when(bookService.getAll(pageable))
                .thenReturn(new PageImpl<>(List.of(bookDto), pageable, 3));

        mockMvc.perform(get("/books").param("page", "1").param("size", "2")
                        .param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("The Hobbit"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Search query parameters are passed to the service")
    void searchBooks_ValidParameters_ReturnsMatchingBooks() throws Exception {
        BookSearchParametersDto parameters = new BookSearchParametersDto(
                "Hobbit", "Tolkien", "9780000000001");
        PageRequest pageable = PageRequest.of(0, 5);
        when(bookService.search(parameters, pageable))
                .thenReturn(new PageImpl<>(List.of(bookDto), pageable, 1));

        mockMvc.perform(get("/books/search").param("title", "Hobbit")
                        .param("author", "Tolkien").param("isbn", "9780000000001")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].isbn").value("9780000000001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Users can retrieve a book with its category IDs")
    void getBookById_ExistingBook_ReturnsBookJson() throws Exception {
        when(bookService.getById(1L)).thenReturn(bookDto);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookDto)));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("A missing book produces the application's 404 response")
    void getBookById_MissingBook_ReturnsNotFound() throws Exception {
        when(bookService.getById(99L))
                .thenThrow(new EntityNotFoundException("Can't find book by id: 99"));

        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Can't find book by id: 99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admins can create a book from JSON")
    void createBook_ValidRequest_ReturnsCreatedBook() throws Exception {
        when(bookService.create(createRequest)).thenReturn(bookDto);

        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(bookDto)));
        verify(bookService).create(createRequest);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Invalid book fields are rejected before reaching the service")
    void createBook_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateBookRequestDto invalidRequest = new CreateBookRequestDto(" ", "Tolkien",
                "9780000000001", BigDecimal.ZERO, null, null, Set.of());

        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title must not be blank"))
                .andExpect(jsonPath("$.price").value("Price must be greater than zero"))
                .andExpect(jsonPath("$.categoryIds").exists());
        verify(bookService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admins can update a book by ID")
    void updateBook_ValidRequest_ReturnsUpdatedBook() throws Exception {
        CreateBookRequestDto updateRequest = new CreateBookRequestDto("Updated title",
                "Tolkien", "9780000000001", new BigDecimal("25.00"), null, null, Set.of(3L));
        BookDto updated = new BookDto(1L, updateRequest.title(), updateRequest.author(),
                updateRequest.isbn(), updateRequest.price(), null, null, Set.of(3L));
        when(bookService.update(1L, updateRequest)).thenReturn(updated);

        mockMvc.perform(put("/books/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        verify(bookService).update(1L, updateRequest);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admins can delete a book and receive an empty 204 response")
    void deleteBook_ExistingBook_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(bookService).deleteById(1L);
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
        verify(bookService, never()).create(any());
        verify(bookService, never()).update(any(), any());
        verify(bookService, never()).deleteById(any());
    }

    @Test
    @DisplayName("Anonymous requests cannot access books")
    void getAll_AnonymousRequest_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isUnauthorized());
        verify(bookService, never()).getAll(any());
    }
}
