package com.evggenn.edugo.lesson;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
}
