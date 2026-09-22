package mate.academy.bookservice.service;

import java.util.List;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.BookSearchParametersDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;

public interface BookService {
    BookDto create(CreateBookRequestDto bookDto);

    List<BookDto> getAll();

    BookDto getById(Long id);

    List<BookDto> search(BookSearchParametersDto searchParameters);

    BookDto update(Long id, CreateBookRequestDto bookDto);

    void deleteById(Long id);
}
