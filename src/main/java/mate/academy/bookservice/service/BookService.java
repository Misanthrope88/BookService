package mate.academy.bookservice.service;

import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.BookSearchParametersDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto create(CreateBookRequestDto bookDto);

    Page<BookDto> getAll(Pageable pageable);

    BookDto getById(Long id);

    Page<BookDto> search(BookSearchParametersDto searchParameters, Pageable pageable);

    BookDto update(Long id, CreateBookRequestDto bookDto);

    void deleteById(Long id);
}
