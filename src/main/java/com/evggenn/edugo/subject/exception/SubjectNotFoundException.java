package com.evggenn.edugo.subject.exception;

public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException(Long subjectId) {
        super("Subject not found with id: " + subjectId);
    }
}
