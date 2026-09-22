package mate.academy.bookservice.repository.specification;

import java.util.Locale;
import mate.academy.bookservice.dto.BookSearchParametersDto;
import mate.academy.bookservice.model.Book;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BookSpecificationBuilder {
    private static final String TITLE = "title";
    private static final String AUTHOR = "author";
    private static final String ISBN = "isbn";

    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> specification = (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();
        if (StringUtils.hasText(searchParameters.title())) {
            specification = specification.and(containsIgnoreCase(
                    TITLE, searchParameters.title()));
        }
        if (StringUtils.hasText(searchParameters.author())) {
            specification = specification.and(containsIgnoreCase(
                    AUTHOR, searchParameters.author()));
        }
        if (StringUtils.hasText(searchParameters.isbn())) {
            specification = specification.and(equalsIgnoreCase(
                    ISBN, searchParameters.isbn()));
        }
        return specification;
    }

    private Specification<Book> containsIgnoreCase(String fieldName, String value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + value.trim().toLowerCase(Locale.ROOT) + "%");
    }

    private Specification<Book> equalsIgnoreCase(String fieldName, String value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                criteriaBuilder.lower(root.get(fieldName)),
                value.trim().toLowerCase(Locale.ROOT));
    }
}
