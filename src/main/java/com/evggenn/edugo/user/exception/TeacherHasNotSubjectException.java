package com.evggenn.edugo.user.exception;

public class TeacherHasNotSubjectException extends RuntimeException {
    public TeacherHasNotSubjectException(String teacher, String subject) {
        super(String.format("Teacher: %s does not have the subject: %s", teacher, subject));
    }
}