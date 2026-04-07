package com.evggenn.edugo.schoolclass;

public record SchoolClassRequest(
        String name,
        String academicYear,
        Long teacherId
) {}
