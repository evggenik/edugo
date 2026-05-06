package com.evggenn.edugo.user.exception;

public class NotTeacherException extends RuntimeException {
    public NotTeacherException(Long id) {
        super(String.format("User with id: %d not a teacher", id));
    }
}