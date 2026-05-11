package com.evggenn.edugo.lesson;

import java.time.LocalDateTime;

public record LessonResponse(
        Long id,
        String topic,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String room,
        LessonStatus status,
        SchoolClassInfo schoolClass,
        SubjectInfo subject,
        TeacherInfo teacher,
        TermInfo term
) {
    public record SchoolClassInfo(Long id, String name) {}
    public record SubjectInfo(Long id, String name) {}
    public record TeacherInfo(Long id, String firstName, String lastName) {}
    public record TermInfo(Long id, String name) {}

    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTopic(),
                lesson.getStartTime(),
                lesson.getEndTime(),
                lesson.getRoom(),
                lesson.getStatus(),
                new SchoolClassInfo(lesson.getSchoolClass().getId(), lesson.getSchoolClass().getName()),
                new SubjectInfo(lesson.getSubject().getId(), lesson.getSubject().getName()),
                new TeacherInfo(lesson.getTeacher().getId(), lesson.getTeacher().getFirstName(), lesson.getTeacher().getLastName()),
                new TermInfo(lesson.getTerm().getId(), lesson.getTerm().getName())
        );
    }
}
