package com.evggenn.edugo.attendance;

import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(
        @NotNull AttendanceStatus status,
        @NotNull Long lessonId,
        @NotNull Long studentId) { }
