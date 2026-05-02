package com.evggenn.edugo.auth;

import com.evggenn.edugo.user.*;
import com.evggenn.edugo.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserService userService;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserDetailsService userDetailsService;

    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthService authService;

    private final LoginRequest loginRequest = new LoginRequest();
    private final RegisterRequest registerRequest = new RegisterRequest();
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("hash_password")
                .firstName("Vasia")
                .lastName("Vasilkov")
                .middleName("Zagagulivich")
                .roles(Set.of(new Role(1L, RoleName.STUDENT)))
                .build();

        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password");

        registerRequest.setEmail("test@test.com");
        registerRequest.setPassword("raw_password");
        registerRequest.setFirstName("Vasia");
        registerRequest.setLastName("Vasilkov");
        registerRequest.setMiddleName("Zagagulivich");
    }

    @Test
    void login_shouldReturnLoginResponse_whenEmailAndPasswordCorrect() {
        UserDetails userDetails = new CustomUserDetails(user);

        String generatedToken = "generatedToken";

        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(generatedToken);

        LoginResponse loginResponse = authService.login(loginRequest);

        verify(authenticationManager).authenticate(any());

        assertThat(loginResponse.getEmail()).isEqualTo(loginRequest.getEmail());
        assertThat(loginResponse.getToken()).isEqualTo(generatedToken);
    }

    @Test
    void register_shouldReturnUserResponse_whenRegisterRequestCorrect() {
        when(userService.createUser(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getMiddleName(),
                RoleName.STUDENT
        )).thenReturn(user);

        UserResponse userResponse = authService.register(registerRequest);

        assertThat(userResponse.getEmail()).isEqualTo(user.getEmail());
        assertThat(userResponse.getFirstName()).isEqualTo(user.getFirstName());
    }

    @Test
    void register_shouldThrowEmailAlreadyExistsException_whenEmailExists() {
        when(userService.createUser(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getMiddleName(),
                RoleName.STUDENT
        )).thenThrow(new EmailAlreadyExistsException("test@test.com"));

        Throwable throwable = assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        assertThat(throwable.getMessage()).isEqualTo("Email already exists: " + user.getEmail());
    }
}