package com.evggenn.edugo.exception;

public class StudentAlreadyInClassException extends RuntimeException {
    public StudentAlreadyInClassException(Long studentId, String academicYear) {
        super(String.format("Student with id: %d is already in class for year: %s", studentId, academicYear));
    }
}
