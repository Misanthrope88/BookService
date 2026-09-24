package mate.academy.bookservice.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.UserRegistrationRequestDto;
import mate.academy.bookservice.dto.UserResponseDto;
import mate.academy.bookservice.exception.RegistrationException;
import mate.academy.bookservice.mapper.UserMapper;
import mate.academy.bookservice.model.User;
import mate.academy.bookservice.repository.UserRepository;
import mate.academy.bookservice.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.existsByEmail(request.email())) {
            throw new RegistrationException(
                    "Email is already registered: " + request.email()
            );
        }

        User user = userMapper.toModel(request);
        return userMapper.toDto(userRepository.save(user));
    }
}
