package com.evggenn.edugo.grade.exception;

import com.evggenn.edugo.lesson.LessonStatus;

public class GradeNotEditableException extends RuntimeException {
    public GradeNotEditableException(LessonStatus lessonStatus) {
        super(String.format(
                "You can not change grade with the lesson status: %s", lessonStatus));
    }
}