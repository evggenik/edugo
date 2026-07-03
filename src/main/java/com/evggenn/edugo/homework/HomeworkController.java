package com.evggenn.edugo.homework;

import com.evggenn.edugo.user.CustomUserDetails;
import com.evggenn.edugo.util.AcademicYearUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/homework")
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

}
