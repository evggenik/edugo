package com.evggenn.edugo.homework;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HomeworkRequest(
        @NotBlank String description,
        @NotNull LocalDate dueDate,
        @NotNull Long lessonId
) {}
