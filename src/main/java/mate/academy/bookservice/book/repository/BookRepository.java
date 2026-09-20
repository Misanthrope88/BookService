package mate.academy.bookservice.book.repository;

import java.util.List;
import mate.academy.bookservice.book.model.Book;

public interface BookRepository {
    Book save(Book book);

    List<Book> findAll();
}
