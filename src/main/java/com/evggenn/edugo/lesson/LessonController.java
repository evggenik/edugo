package com.evggenn.edugo.lesson;

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
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    public ResponseEntity<LessonResponse> createLesson(
            @Valid @RequestBody LessonCreateRequest request) {
        Lesson lesson = lessonService.createLesson(
                request.topic(),
                request.startTime(),
                request.endTime(),
                request.room(),
                request.schoolClassId(),
                request.subjectId(),
                request.teacherId(),
                request.termId()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(lesson.getId())
                .toUri();

        return ResponseEntity.created(location).body(LessonResponse.from(lesson));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody LessonUpdateRequest request) {
        lessonService.updateLesson(
                id,
                request.topic(),
                request.startTime(),
                request.endTime(),
                request.room(),
                request.schoolClassId(),
                request.subjectId(),
                request.teacherId(),
                request.termId()
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/content")
    public ResponseEntity<Void> updateLessonContent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody LessonTopicUpdateRequest request) {
        lessonService.updateLessonContent(
                id,
                principal.getId(),
                request.topic(),
                request.room()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<LessonResponse>> getLessonsByTeacherClassAndTerm(
            @RequestParam Long teacherId,
            @RequestParam Long schoolClassId,
            @RequestParam Long termId) {
        List<Lesson> lessonList = lessonService.findLessonsByTeacherClassAndTerm(
                teacherId, schoolClassId, termId);

        List<LessonResponse> responseList = lessonList.stream().map(LessonResponse::from).toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(
            @PathVariable Long id) {
        Lesson lesson = lessonService.getLessonById(id);

        return ResponseEntity.ok(LessonResponse.from(lesson));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        lessonService.completeLesson(id, principal.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        lessonService.cancelLesson(id, principal.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long id) {

        lessonService.deleteLesson(id);

        return ResponseEntity.noContent().build();
    }

}
