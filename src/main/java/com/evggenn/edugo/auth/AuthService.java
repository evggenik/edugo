package com.evggenn.edugo.auth;

import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public UserResponse register(RegisterRequest request) {
        User user = userService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getMiddleName()
        );

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setMiddleName(user.getMiddleName());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token, request.getEmail());
    }
}
