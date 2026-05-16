package com.evggenn.edugo.lesson;

import java.time.LocalDateTime;

public record LessonShortResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String room,
        String subjectName,
        String teacherFirstName,
        String teacherLastName) {

    public static LessonShortResponse from(Lesson lesson) {
        return new LessonShortResponse(
                lesson.getId(),
                lesson.getStartTime(),
                lesson.getEndTime(),
                lesson.getRoom(),
                lesson.getSubject().getName(),
                lesson.getTeacher().getFirstName(),
                lesson.getTeacher().getLastName()
        );
    }
}
