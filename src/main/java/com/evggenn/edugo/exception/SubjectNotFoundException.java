package com.evggenn.edugo.exception;

public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException(Long subjectId) {
        super("Subject not found with id: " + subjectId);
    }
}
