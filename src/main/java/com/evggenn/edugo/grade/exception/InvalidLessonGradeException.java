package com.evggenn.edugo.grade.exception;

import com.evggenn.edugo.grade.GradeType;

public class InvalidLessonGradeException extends RuntimeException {
    public InvalidLessonGradeException(GradeType type) {
        super(String.format("LessonId is required for: %s", type));
    }
}