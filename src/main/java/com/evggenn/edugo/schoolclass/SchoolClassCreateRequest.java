package com.evggenn.edugo.schoolclass;

import jakarta.validation.constraints.NotBlank;

public record SchoolClassCreateRequest(
        @NotBlank String name,
        Long teacherId
) {}
