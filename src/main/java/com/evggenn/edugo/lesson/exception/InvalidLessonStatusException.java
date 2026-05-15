package com.evggenn.edugo.lesson.exception;

import com.evggenn.edugo.lesson.LessonStatus;

public class InvalidLessonStatusException extends RuntimeException {
    public InvalidLessonStatusException(LessonStatus status) {
        super(String.format("Cannot change lesson status: current status is %s", status));
    }
}
