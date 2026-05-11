package com.evggenn.edugo.lesson;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record LessonCreateRequest(
        @Size(max = 200) String topic,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Size(max = 20) String room,
        @NotNull Long schoolClassId,
        @NotNull Long subjectId,
        @NotNull Long teacherId,
        @NotNull Long  termId
) {}
