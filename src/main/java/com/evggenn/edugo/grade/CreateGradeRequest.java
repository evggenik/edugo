package com.evggenn.edugo.grade;

import jakarta.validation.constraints.NotNull;

public record CreateGradeRequest(
        @NotNull Short value,
        @NotNull GradeType type,
        String comment,
        @NotNull Long studentId,
        Long lessonId,
        Long termId,
        Long subjectId
        ) {}
