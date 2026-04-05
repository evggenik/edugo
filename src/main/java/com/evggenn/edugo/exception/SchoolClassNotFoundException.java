package com.evggenn.edugo.exception;

public class SchoolClassNotFoundException extends RuntimeException {
    public SchoolClassNotFoundException(String className, String academicYear) {
        super(String.format("School class: %s not found for year: %s", className, academicYear));
    }

    public SchoolClassNotFoundException(Long id) {
        super("School class not found with id: " + id);
    }
}
