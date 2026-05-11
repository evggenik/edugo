package com.evggenn.edugo.lesson;

public enum LessonConflictType {
    CLASS_OCCUPIED("This class already has a lesson at the specified time"),
    TEACHER_BUSY("This teacher already has a lesson at the specified time");

    private final String message;

    LessonConflictType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
