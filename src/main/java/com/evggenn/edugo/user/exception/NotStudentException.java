package com.evggenn.edugo.user.exception;

public class NotStudentException extends RuntimeException {
    public NotStudentException(Long id) {
        super(String.format("User with id: %d not a student", id));
    }
}
