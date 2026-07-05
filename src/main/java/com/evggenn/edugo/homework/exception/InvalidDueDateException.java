package com.evggenn.edugo.homework.exception;

import java.time.LocalDate;

public class InvalidDueDateException extends RuntimeException {
    private InvalidDueDateException(String message) {
        super(message);
    }

    public static InvalidDueDateException beforeLesson(LocalDate dueDate, LocalDate lessonDate) {
        return new InvalidDueDateException(String.format(
                "Due date %s cannot be before lesson date %s", dueDate, lessonDate));
    }

    public static InvalidDueDateException afterTermEnd(LocalDate dueDate, LocalDate termEnd) {
        return new InvalidDueDateException(String.format(
                "Due date %s cannot be after term end date %s", dueDate, termEnd));
    }
}