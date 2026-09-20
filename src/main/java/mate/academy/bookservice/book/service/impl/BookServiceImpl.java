package mate.academy.bookservice.book.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.book.dto.BookDto;
import mate.academy.bookservice.book.dto.CreateBookRequestDto;
import mate.academy.bookservice.book.mapper.BookMapper;
import mate.academy.bookservice.book.model.Book;
import mate.academy.bookservice.book.repository.BookRepository;
import mate.academy.bookservice.book.service.BookService;
import mate.academy.bookservice.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookDto create(CreateBookRequestDto bookDto) {
        Book book = bookMapper.toModel(bookDto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> getAll() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto getById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find book by id: " + id));
        return bookMapper.toDto(book);
    }
}
