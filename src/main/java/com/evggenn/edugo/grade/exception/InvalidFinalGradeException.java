package com.evggenn.edugo.grade.exception;

import com.evggenn.edugo.grade.GradeType;

public class InvalidFinalGradeException extends RuntimeException {
    public InvalidFinalGradeException(GradeType type) {
        super(String.format("TermId is required for: %s", type));
    }
}