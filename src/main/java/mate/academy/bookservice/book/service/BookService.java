package mate.academy.bookservice.book.service;

import java.util.List;
import mate.academy.bookservice.book.model.Book;

public interface BookService {
    Book save(Book book);

    List<Book> findAll();
}
