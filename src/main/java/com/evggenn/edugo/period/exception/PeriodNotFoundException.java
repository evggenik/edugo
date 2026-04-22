package com.evggenn.edugo.period.exception;

public class PeriodNotFoundException extends RuntimeException {
    public PeriodNotFoundException(Long updatedId, String currentYear) {
        super("Period not found with id " + updatedId + " and current year " + currentYear);
    }
}
