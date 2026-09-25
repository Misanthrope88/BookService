package mate.academy.bookservice.mapper;

import mate.academy.bookservice.dto.UserRegistrationRequestDto;
import mate.academy.bookservice.dto.UserResponseDto;
import mate.academy.bookservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserResponseDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toModel(UserRegistrationRequestDto request);
}
