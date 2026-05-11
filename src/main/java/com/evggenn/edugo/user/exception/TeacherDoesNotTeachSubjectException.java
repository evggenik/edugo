package com.evggenn.edugo.user.exception;

public class TeacherDoesNotTeachSubjectException extends RuntimeException {
    public TeacherDoesNotTeachSubjectException(Long teacherId, Long subjectId) {
        super(String.format(
                "Teacher with id: %d does not teach subject with id: %d",
                teacherId, subjectId)
        );
    }
}
