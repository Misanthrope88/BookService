package mate.academy.bookservice.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.bookservice.model.Book;

public interface BookRepository {
    Book save(Book book);

    List<Book> findAll();

    Optional<Book> findById(Long id);
}
