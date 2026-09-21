package mate.academy.bookservice.service;

import java.util.List;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;

public interface BookService {
    BookDto create(CreateBookRequestDto bookDto);

    List<BookDto> getAll();

    BookDto getById(Long id);
}
