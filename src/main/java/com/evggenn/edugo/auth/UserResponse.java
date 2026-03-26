package com.evggenn.edugo.auth;

import lombok.Data;
import java.time.Instant;

@Data
public class UserResponse {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String middleName;

    private Instant createdAt;
}
