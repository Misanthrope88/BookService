package mate.academy.bookservice.service;

import lombok.RequiredArgsConstructor;
import mate.academy.bookservice.dto.UserLoginRequestDto;
import mate.academy.bookservice.dto.UserLoginResponseDto;
import mate.academy.bookservice.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UserLoginResponseDto login(UserLoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        return new UserLoginResponseDto(jwtUtil.generateToken(authentication.getName()));
    }
}
