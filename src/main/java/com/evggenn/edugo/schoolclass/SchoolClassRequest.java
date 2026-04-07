package com.evggenn.edugo.schoolclass;

import jakarta.validation.constraints.NotBlank;

public record SchoolClassRequest(
        @NotBlank String name,
        @NotBlank String academicYear,
        Long teacherId
) {}
