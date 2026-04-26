package com.evggenn.edugo.term;

import java.time.LocalDate;

public record TermResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate
        ) { }
