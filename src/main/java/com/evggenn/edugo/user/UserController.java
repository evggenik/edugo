package com.evggenn.edugo.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllByRole(
            @RequestParam RoleName roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

        Page<UserResponse> users = userService.getUsersByRole(roleName, pageable);

        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.role()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(UserResponse.from(user));
    }

    @PostMapping("/{teacherId}/subjects/{subjectId}")
    public ResponseEntity<Void> addSubjectToTeacher(
            @PathVariable Long teacherId,
            @PathVariable Long subjectId) {

        userService.addSubjectToTeacher(teacherId, subjectId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{teacherId}/subjects/{subjectId}")
    public ResponseEntity<Void> removeSubjectFromTeacher(
            @PathVariable Long teacherId,
            @PathVariable Long subjectId) {

        userService.removeSubjectFromTeacher(teacherId, subjectId);

        return ResponseEntity.noContent().build();
    }
}
