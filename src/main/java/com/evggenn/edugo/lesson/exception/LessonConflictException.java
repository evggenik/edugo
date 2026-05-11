package com.evggenn.edugo.lesson.exception;

import com.evggenn.edugo.lesson.LessonConflictType;

public class LessonConflictException extends RuntimeException {
    private final LessonConflictType type;

    public LessonConflictException(LessonConflictType type) {
        super(type.getMessage());
        this.type = type;
    }

    public LessonConflictType getType() {
        return type;
    }
}