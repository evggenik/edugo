package com.evggenn.edugo.homework.exception;

public class HomeworkNotFoundException extends RuntimeException {
    public HomeworkNotFoundException(Long id) {
        super("Homework with id " + id + " not found");
    }

    public static HomeworkNotFoundException byLesson(Long lessonId) {
        return new HomeworkNotFoundException(
                "Homework for lesson with id " + lessonId + " not found");
    }

    private HomeworkNotFoundException(String message) {
        super(message);
    }
}
