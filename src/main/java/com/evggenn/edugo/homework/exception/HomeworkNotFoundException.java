package com.evggenn.edugo.homework.exception;

public class HomeworkNotFoundException extends RuntimeException {
    public HomeworkNotFoundException(Long id) {
        super("Homework with id " + id + " not found");
    }
}
