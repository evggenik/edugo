package com.evggenn.edugo.subject;

public record SubjectResponse(
        Long id,
        String name) {

    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName());
    }
}
