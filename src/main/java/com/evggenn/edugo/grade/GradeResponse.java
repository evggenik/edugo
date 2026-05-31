package com.evggenn.edugo.grade;


import java.time.Instant;

public record GradeResponse(
        Long id,
        short value,
        GradeType type,
        String comment,
        Instant gradedAt,
        Long studentId,
        String studentName,
        Long lessonId,
        String lessonTopic,
        Long termId,
        String termName,
        Long subjectId,
        String subjectName
        ) {
    public static GradeResponse from(Grade grade) {
        return new GradeResponse(
                grade.getId(),
                grade.getValue(),
                grade.getType(),
                grade.getComment(),
                grade.getGradedAt(),
                grade.getStudent().getId(),
                grade.getStudent().getFirstName() + " " + grade.getStudent().getLastName(),
                grade.getLesson() != null ? grade.getLesson().getId() : null,
                grade.getLesson() != null ? grade.getLesson().getTopic() : null,
                grade.getTerm() != null ? grade.getTerm().getId() : null,
                grade.getTerm() != null ? grade.getTerm().getName() : null,
                grade.getSubject().getId(),
                grade.getSubject().getName()
        );
    }
}
