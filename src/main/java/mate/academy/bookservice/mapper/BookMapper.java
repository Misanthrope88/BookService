package mate.academy.bookservice.mapper;

import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.CreateBookRequestDto;
import mate.academy.bookservice.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookMapper {
    BookDto toDto(Book book);

    Book toModel(CreateBookRequestDto bookDto);
}

