package com.evggenn.edugo.lesson.exception;

public class LessonOutOfTermException extends RuntimeException {
    public LessonOutOfTermException(String termName) {
        super(String.format("The time of the lesson is not within the term: %s", termName));
    }
}