package com.evggenn.edugo.user.exception;

public class TeacherAlreadyHasSubjectException extends RuntimeException {
    public TeacherAlreadyHasSubjectException(String subject, String teacher) {
        super(String.format("Subject: %s already taught by teacher: %s", subject, teacher));
    }
}
