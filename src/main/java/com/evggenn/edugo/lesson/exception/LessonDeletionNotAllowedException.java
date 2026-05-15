package com.evggenn.edugo.lesson.exception;

import com.evggenn.edugo.lesson.LessonStatus;

public class LessonDeletionNotAllowedException extends RuntimeException {
    public LessonDeletionNotAllowedException(LessonStatus status) {
        super(String.format("You cannot delete lesson with status: %s", status));
    }
}