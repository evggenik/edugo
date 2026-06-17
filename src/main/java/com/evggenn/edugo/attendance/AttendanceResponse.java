package com.evggenn.edugo.attendance;

public record AttendanceResponse(
        Long id,
        AttendanceStatus status,
        Long studentId,
        String studentFullName,
        Long lessonId
) {
    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getStatus(),
                attendance.getStudent().getId(),
                attendance.getStudent().getFirstName() +
                        " " + attendance.getStudent().getLastName(),
                attendance.getLesson().getId()
        );
    }
}
