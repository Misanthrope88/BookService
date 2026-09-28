package mate.academy.bookservice.service.impl;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.UserRegistrationRequestDto;
import mate.academy.bookservice.dto.UserResponseDto;
import mate.academy.bookservice.exception.EntityNotFoundException;
import mate.academy.bookservice.exception.RegistrationException;
import mate.academy.bookservice.mapper.UserMapper;
import mate.academy.bookservice.model.Role;
import mate.academy.bookservice.model.RoleName;
import mate.academy.bookservice.model.ShoppingCart;
import mate.academy.bookservice.model.User;
import mate.academy.bookservice.repository.RoleRepository;
import mate.academy.bookservice.repository.ShoppingCartRepository;
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
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.existsByEmailIncludingDeleted(request.email())) {
            throw new RegistrationException(
                    "Email is already registered: " + request.email()
            );
        }

        User user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new EntityNotFoundException(RoleName.USER + " role is missing"));
        user.setRoles(Set.of(userRole));
        User savedUser = userRepository.save(user);
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(savedUser);
        shoppingCartRepository.save(shoppingCart);
        return userMapper.toDto(savedUser);
    }
}
