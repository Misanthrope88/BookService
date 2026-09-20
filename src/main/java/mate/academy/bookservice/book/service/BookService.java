package mate.academy.bookservice.book.service;

import java.util.List;
import mate.academy.bookservice.book.dto.BookDto;
import mate.academy.bookservice.book.dto.CreateBookRequestDto;

public interface BookService {
    BookDto create(CreateBookRequestDto bookDto);

    List<BookDto> getAll();

    BookDto getById(Long id);
}
