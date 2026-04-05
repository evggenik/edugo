package com.evggenn.edugo.exception;

public class SchoolClassAlreadyExistsException extends RuntimeException {
    public SchoolClassAlreadyExistsException(String schoolClass, String academicYear) {
        super(String.format("SchoolClass: %s already exists for year: %s", schoolClass, academicYear));
    }
}
