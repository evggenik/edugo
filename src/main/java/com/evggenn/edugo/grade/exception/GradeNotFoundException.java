package com.evggenn.edugo.grade.exception;

public class GradeNotFoundException extends RuntimeException {
    public GradeNotFoundException(Long id) {
        super(String.format("Grade not found with id: %d", id));
    }
}