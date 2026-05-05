package com.evggenn.edugo.user;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String middleName,
        Instant createdAt,
        Set<RoleName> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getCreatedAt(),
                user.getRoles().stream()
                        .map(Role::getName).collect(Collectors.toSet())
        );
    }
}
