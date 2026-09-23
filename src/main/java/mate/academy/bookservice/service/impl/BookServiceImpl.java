package mate.academy.bookservice.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.BookSearchParametersDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;
import mate.academy.bookservice.exception.EntityNotFoundException;
import mate.academy.bookservice.mapper.BookMapper;
import mate.academy.bookservice.model.Book;
import mate.academy.bookservice.repository.BookRepository;
import mate.academy.bookservice.repository.specification.BookSpecificationBuilder;
import mate.academy.bookservice.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;

    @Override
    @Transactional
    public BookDto create(CreateBookRequestDto bookDto) {
        Book book = bookMapper.toModel(bookDto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> getAll(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(bookMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto getById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find book by id: " + id));
        return bookMapper.toDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> search(BookSearchParametersDto searchParameters, Pageable pageable) {
        return bookRepository.findAll(bookSpecificationBuilder.build(searchParameters), pageable)
                .map(bookMapper::toDto);
    }

    @Override
    @Transactional
    public BookDto update(Long id, CreateBookRequestDto bookDto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find book by id: " + id));
        bookMapper.updateBookFromDto(bookDto, book);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't find book by id: " + id);
        }
        bookRepository.deleteById(id);
    }
}
