package com.evggenn.edugo.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubjectRequest {
    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject name must be at most 100 characters")
    private String name;
}
