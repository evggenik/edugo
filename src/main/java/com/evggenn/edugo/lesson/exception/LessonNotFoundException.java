package com.evggenn.edugo.lesson.exception;

public class LessonNotFoundException extends RuntimeException {
    public LessonNotFoundException(Long id) {
        super(String.format("Lesson not found with id: %d", id));
    }
}