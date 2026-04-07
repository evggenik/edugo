package com.evggenn.edugo.schoolclass;

import java.util.Set;

public record SchoolClassResponse(
        Long id,
        String name,
        String academicYear,
        TeacherResponse teacher,
        Set<StudentResponse> students
) {}