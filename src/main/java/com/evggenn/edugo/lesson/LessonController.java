package com.evggenn.edugo.lesson;

import com.evggenn.edugo.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
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

    @GetMapping("/journal")
    public ResponseEntity<List<LessonResponse>> getLessonsByTeacherClassAndTerm(
            @RequestParam Long teacherId,
            @RequestParam Long schoolClassId,
            @RequestParam Long termId) {
        List<Lesson> lessonList = lessonService.getLessonsByTeacherClassAndTerm(
                teacherId, schoolClassId, termId);

        return ResponseEntity.ok(lessonList.stream()
                .map(LessonResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(
            @PathVariable Long id) {
        Lesson lesson = lessonService.getLessonById(id);

        return ResponseEntity.ok(LessonResponse.from(lesson));
    }

    @GetMapping()
    public ResponseEntity<List<LessonShortResponse>> getLessons(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to) {

        List<Lesson> lessons;

        if ((classId == null && teacherId == null) || (classId != null && teacherId != null)) {
            throw new IllegalArgumentException("Exactly one of classId or teacherId is required");
        }

        if (classId != null) {
            lessons = lessonService.getLessonsByClass(classId, from, to);
        } else {
            lessons = lessonService.getLessonsByTeacher(teacherId, from, to);
        }

        return ResponseEntity.ok(lessons.stream()
                .map(LessonShortResponse::from).toList());
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
