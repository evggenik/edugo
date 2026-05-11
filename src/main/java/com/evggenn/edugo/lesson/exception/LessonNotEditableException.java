package com.evggenn.edugo.lesson.exception;

public class LessonNotEditableException extends RuntimeException {
    public LessonNotEditableException(String lessonStatus) {
        super(String.format("Lesson with status: %s can not be modified", lessonStatus));
    }
}