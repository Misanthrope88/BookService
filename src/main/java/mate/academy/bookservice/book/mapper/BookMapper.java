package mate.academy.bookservice.book.mapper;

import mate.academy.bookservice.book.dto.BookDto;
import mate.academy.bookservice.book.dto.CreateBookRequestDto;
import mate.academy.bookservice.book.model.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDto toDto(Book book);

    Book toModel(CreateBookRequestDto bookDto);
}

