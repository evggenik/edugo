package com.evggenn.edugo.attendance.exception;

import com.evggenn.edugo.lesson.LessonStatus;

public class LessonNotCompletedException extends RuntimeException {
    public LessonNotCompletedException(LessonStatus status) {
        super(String.format(
                "Lesson must be COMPLETED to mark attendance, current status: %s", status));
    }
}
