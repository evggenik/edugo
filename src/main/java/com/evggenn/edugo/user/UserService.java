package com.evggenn.edugo.user;

import com.evggenn.edugo.exception.EmailAlreadyExistsException;
import com.evggenn.edugo.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, RoleRepository roleRepo,
                       PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional(readOnly = true)
    public User findByEmailOrThrow(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
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
