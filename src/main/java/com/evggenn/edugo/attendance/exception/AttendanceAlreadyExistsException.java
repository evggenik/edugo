package com.evggenn.edugo.attendance.exception;

public class AttendanceAlreadyExistsException extends RuntimeException {
    public AttendanceAlreadyExistsException(Long studentId, Long lessonId) {
        super(String.format(
                "Student %d already has attendance record for lesson %d",
                studentId, lessonId));
    }
}
