package com.evggenn.edugo.grade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateGradeRequest(
        @Min(2) @Max(5) Short value,
        String comment
) {}
