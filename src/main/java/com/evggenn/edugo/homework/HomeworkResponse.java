package com.evggenn.edugo.homework;

import java.time.LocalDate;

public record HomeworkResponse(
        Long id,
        String description,
        LocalDate dueDate,
        Long lessonId
) {
    public static HomeworkResponse from(Homework homework) {
        return new HomeworkResponse(
                homework.getId(),
                homework.getDescription(),
                homework.getDueDate(),
                homework.getLesson().getId()
        );
    }
}
