package com.evggenn.edugo.grade;

import com.evggenn.edugo.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/grades")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    public ResponseEntity<GradeResponse> createGrade(
            @Valid @RequestBody CreateGradeRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Grade grade = gradeService.createGrade(
                request.value(),
                request.type(),
                request.comment(),
                request.studentId(),
                request.lessonId(),
                request.termId(),
                request.subjectId(),
                currentUser.getId()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(grade.getId())
                .toUri();

        return ResponseEntity.created(location).body(GradeResponse.from(grade));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGradeRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        gradeService.updateGrade(
                id,
                request.value(),
                request.comment(),
                currentUser.getId()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<GradeResponse>> getGrades(
            @RequestParam Long subjectId,
            @RequestParam Long termId,
            @RequestParam Long studentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<Grade> grades = gradeService.getGradesBySubjectTermAndStudent(
                subjectId, termId, studentId,  currentUser.getId());

        return ResponseEntity.ok(grades.stream()
                .map(GradeResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeResponse> getGrade(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Grade grade = gradeService.getGrade(id, currentUser.getId());

        return ResponseEntity.ok(GradeResponse.from(grade));
    }
}
