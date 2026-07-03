package com.evggenn.edugo.homework;

import com.evggenn.edugo.user.CustomUserDetails;
import com.evggenn.edugo.util.AcademicYearUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/homeworks")
public class HomeworkController {

    private final HomeworkService homeworkService;

    @PostMapping
    public ResponseEntity<HomeworkResponse> createHomework(
            @RequestBody @Valid HomeworkRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Homework homework = homeworkService.createHomework(
                request.description(),
                request.dueDate(),
                request.lessonId(),
                AcademicYearUtil.getCurrentAcademicYear(),
                currentUser.getId()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(homework.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(HomeworkResponse.from(homework));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateHomework(
            @PathVariable Long id,
            @RequestBody @Valid HomeworkUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
            ) {

        homeworkService.updateHomework(
                id,
                request.description(),
                request.dueDate(),
                currentUser.getId()
        );

        return ResponseEntity.noContent().build();
    }

}
