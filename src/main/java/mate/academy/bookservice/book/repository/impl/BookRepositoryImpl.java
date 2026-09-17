package mate.academy.bookservice.book.repository.impl;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.book.model.Book;
import mate.academy.bookservice.book.repository.BookRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {
    private final EntityManager entityManager;

    @Override
    public Book save(Book book) {
        entityManager.persist(book);
        return book;
    }

    @Override
    public List<Book> findAll() {
        return entityManager.createQuery("FROM Book", Book.class)
                .getResultList();
    }
}
