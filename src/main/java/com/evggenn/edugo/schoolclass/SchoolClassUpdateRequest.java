package com.evggenn.edugo.schoolclass;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SchoolClassUpdateRequest(
        @Size(min = 1, max = 10) String name,
        @Positive Long teacherId
) {}
