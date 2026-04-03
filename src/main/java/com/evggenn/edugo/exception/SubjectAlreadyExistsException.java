package com.evggenn.edugo.exception;

public class SubjectAlreadyExistsException extends RuntimeException {
    public SubjectAlreadyExistsException(String subjectName) {
        super("Subject already exists: " + subjectName);
    }
}
