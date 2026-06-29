package com.evggenn.edugo.homework.exception;

import com.evggenn.edugo.lesson.LessonStatus;

public class LessonCancelledException extends RuntimeException {
    public LessonCancelledException(LessonStatus status) {
        super(String.format(
                "We do not assign homeworks for lessons with status %s", status)
        );
    }
}
