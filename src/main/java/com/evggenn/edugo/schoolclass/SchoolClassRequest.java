package com.evggenn.edugo.schoolclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SchoolClassRequest(
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = "\\d{4}-\\d{4}", message = "Academic year must be in format YYYY-YYYY")
        String academicYear,
        Long teacherId
) {}
