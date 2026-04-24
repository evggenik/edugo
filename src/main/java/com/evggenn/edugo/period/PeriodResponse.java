package com.evggenn.edugo.period;

import java.time.LocalDate;

public record PeriodResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate
        ) { }
