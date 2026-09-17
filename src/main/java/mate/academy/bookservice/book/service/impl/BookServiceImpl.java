package mate.academy.bookservice.book.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.book.model.Book;
import mate.academy.bookservice.book.repository.BookRepository;
import mate.academy.bookservice.book.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }
}
