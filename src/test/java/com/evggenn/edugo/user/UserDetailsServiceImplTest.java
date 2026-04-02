package com.evggenn.edugo.user;

import com.evggenn.edugo.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User expectedUser;

    @BeforeEach
    void setUp() {
        expectedUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("hash_password")
                .firstName("Vasia")
                .lastName("Vasilkov")
                .middleName("Zagagulivich")
                .roles(Set.of(new Role(1L, Role.STUDENT)))
                .build();
    }

    @Test
    void loadUserByUsername_shouldReturnCustomUserDetails_whenEmailCorrect() {
        String email = "test@test.com";

        when(userService.findByEmailWithRolesOrThrow(email)).thenReturn(expectedUser);

        UserDetails customUserDetails = userDetailsService.loadUserByUsername(email);

        verify(userService).findByEmailWithRolesOrThrow(email);
        assertThat(customUserDetails.getUsername()).isEqualTo(expectedUser.getEmail());
    }

    @Test
    void loadUserByUsername_shouldThrowUserNotFoundException_whenEmailNotCorrect() {
        String email = "test@test.comm";

        when(userService.findByEmailWithRolesOrThrow(email)).thenThrow(new UserNotFoundException(email));

        Throwable throwable = assertThrows(UserNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });

        verify(userService).findByEmailWithRolesOrThrow(email);
        assertThat(throwable.getMessage()).isEqualTo("User not found with email: " + email);
    }
}