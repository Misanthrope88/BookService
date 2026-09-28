package mate.academy.bookservice.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.bookservice.dto.BookDto;
import mate.academy.bookservice.dto.BookDtoWithoutCategoryIds;
import mate.academy.bookservice.dto.CreateBookRequestDto;
import mate.academy.bookservice.model.Book;
import mate.academy.bookservice.model.Category;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookMapper {
    @Mapping(target = "categoryIds", ignore = true)
    BookDto toDto(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Book toEntity(CreateBookRequestDto bookDto);

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateBookFromDto(CreateBookRequestDto bookDto, @MappingTarget Book book);

    @AfterMapping
    default BookDto fillCategoryIds(Book book, @MappingTarget BookDto bookDto) {
        Set<Long> categoryIds = book.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        return new BookDto(bookDto.id(), bookDto.title(), bookDto.author(), bookDto.isbn(),
                bookDto.price(), bookDto.description(), bookDto.coverImage(), categoryIds);
    }
}
