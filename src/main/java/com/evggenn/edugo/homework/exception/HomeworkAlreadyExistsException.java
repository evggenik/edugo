package com.evggenn.edugo.homework.exception;

public class HomeworkAlreadyExistsException extends RuntimeException {
    public HomeworkAlreadyExistsException(Long lessonId) {
        super("The homework for lesson with id " + lessonId + " already exists");
    }
}
