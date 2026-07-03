package com.evggenn.edugo.homework;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record HomeworkUpdateRequest(
        @Size(min=1) String description,
        LocalDate dueDate
) { }
