package com.evggenn.edugo.user;

import com.evggenn.edugo.user.exception.EmailAlreadyExistsException;
import com.evggenn.edugo.user.exception.SchoolRoleNotFoundException;
import com.evggenn.edugo.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private Role studentRole;

    private User expectedUser;

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder()
                .id(1L)
                .name(RoleName.STUDENT)
                .build();

        expectedUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("hash_password")
                .firstName("Vasia")
                .lastName("Vasilkov")
                .middleName("Zagagulivich")
                .build();

        expectedUser.getRoles().add(studentRole);
    }

    @Test
    public void createUser_shouldReturnUser_whenEmailNotExists() {

        when(userRepo.existsByEmail("test@test.com")).thenReturn(false);

        when(passwordEncoder.encode("password")).thenReturn("hash_password");

        when(roleRepo.findByName(RoleName.STUDENT)).thenReturn(Optional.of(studentRole));

        when(userRepo.save(any(User.class))).thenReturn(expectedUser);

        User createdUser = userService.createUser(
                "test@test.com",
                "password",
                "Vasia",
                "Vasilkov",
                "Zagagulivich",
                RoleName.STUDENT
        );

        assertThat(createdUser.getId()).isEqualTo(expectedUser.getId());
        assertThat(createdUser.getEmail()).isEqualTo("test@test.com");
        assertThat(createdUser.getFirstName()).isEqualTo("Vasia");
        assertThat(createdUser.getPassword()).isEqualTo("hash_password");

        verify(userRepo).existsByEmail("test@test.com");
        verify(passwordEncoder).encode("password");
        verify(roleRepo).findByName(RoleName.STUDENT);
        verify(userRepo).save(any(User.class));

    }
    @Test
    public void createUser_whenEmailExists_throwsEmailAlreadyExistsException() {

        when(userRepo.existsByEmail("test@test.com")).thenReturn(true);

        Throwable thrown = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(
                    "test@test.com",
                    "password",
                    "Vasia",
                    "Vasilkov",
                    "Zagagulivich",
                    RoleName.STUDENT
            );
        });

        assertEquals("Email already exists: test@test.com", thrown.getMessage());
    }

    @Test
    public void createUser_whenRoleNotExists_throwsSchoolRoleNotFoundException() {

        when(userRepo.existsByEmail("test@test.com")).thenReturn(false);
        when(roleRepo.findByName(RoleName.STUDENT)).thenReturn(Optional.empty());

        Throwable thrown = assertThrows(SchoolRoleNotFoundException.class, () -> {
            userService.createUser(
                    "test@test.com",
                    "password",
                    "Vasia",
                    "Vasilkov",
                    "Zagagulivich",
                    RoleName.STUDENT
            );
        });

        assertEquals("SchoolRole not found: " + RoleName.STUDENT, thrown.getMessage());
    }

    @Test
    public void findByEmailWithRolesOrThrow_whenUserFound_returnsUser() {

        when(userRepo.findByEmailWithRoles(expectedUser.getEmail())).thenReturn(Optional.of(expectedUser));

        User createdUser = userService.findByEmailWithRolesOrThrow("test@test.com");

        assertThat(createdUser.getRoles()).hasSize(1);
        assertThat(createdUser.getRoles()).extracting(Role::getName).containsExactlyInAnyOrder(RoleName.STUDENT);

    }

    @Test
    public void findByEmailWithRolesOrThrow_whenUserNotFound_throwsException() {

        when(userRepo.findByEmailWithRoles(expectedUser.getEmail())).thenReturn(Optional.empty());

        Throwable thrown = assertThrows(UserNotFoundException.class, () -> {
            userService.findByEmailWithRolesOrThrow(expectedUser.getEmail());
        });

        assertEquals("User not found with email: " + expectedUser.getEmail(), thrown.getMessage());
    }
}