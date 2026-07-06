package com.evggenn.edugo.homework.exception;

import lombok.Getter;

import java.time.LocalDate;

public class InvalidDueDateException extends RuntimeException {

    public enum Reason {
        BEFORE_LESSON,
        AFTER_TERM_END
    }
    @Getter
    private final Reason reason;

    private InvalidDueDateException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public static InvalidDueDateException beforeLesson(LocalDate dueDate, LocalDate lessonDate) {
        return new InvalidDueDateException(String.format(
                "Due date %s cannot be before lesson date %s", dueDate, lessonDate),
                Reason.BEFORE_LESSON);
    }

    public static InvalidDueDateException afterTermEnd(LocalDate dueDate, LocalDate termEnd) {
        return new InvalidDueDateException(String.format(
                "Due date %s cannot be after term end date %s", dueDate, termEnd),
                Reason.AFTER_TERM_END);
    }
}