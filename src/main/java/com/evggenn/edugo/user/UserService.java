package com.evggenn.edugo.user;

import com.evggenn.edugo.exception.EmailAlreadyExistsException;
import com.evggenn.edugo.exception.UserNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findByEmailWithRolesOrThrow(@NotNull String email) {
        return userRepo.findByEmailWithRoles(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Transactional
    public User createUser(String email,
                           String rawPassword,
                           String firstName,
                           String lastName,
                           String middleName) {

        if (userRepo.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        Role role = roleRepo.findByName(Role.STUDENT)
                .orElseThrow(() -> new IllegalStateException("Role STUDENT not found"));

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .middleName(middleName)
                .build();

        user.getRoles().add(role);

        return userRepo.save(user);
    }
}
