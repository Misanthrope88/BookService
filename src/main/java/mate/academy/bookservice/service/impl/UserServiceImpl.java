package mate.academy.bookservice.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.UserRegistrationRequestDto;
import mate.academy.bookservice.dto.UserResponseDto;
import mate.academy.bookservice.exception.RegistrationException;
import mate.academy.bookservice.mapper.UserMapper;
import mate.academy.bookservice.model.Role;
import mate.academy.bookservice.model.RoleName;
import mate.academy.bookservice.model.User;
import mate.academy.bookservice.repository.RoleRepository;
import mate.academy.bookservice.repository.UserRepository;
import mate.academy.bookservice.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
        user.setPassword(passwordEncoder.encode(request.password()));
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("USER role is missing"));
        user.getRoles().add(userRole);
        return userMapper.toDto(userRepository.save(user));
    }
}
