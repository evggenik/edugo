package com.evggenn.edugo.exception;

public class SchoolClassAlreadyExistsException extends RuntimeException {
    public SchoolClassAlreadyExistsException(String schoolClass) {
        super(String.format("SchoolClass: %s already exists for current year", schoolClass));
    }
}
