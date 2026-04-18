package com.evggenn.edugo.period.exception;

public class PeriodAlreadyExistsException extends RuntimeException {
    public PeriodAlreadyExistsException(String name, String currentYear) {
        super(String.format("The period: %s already exists for year: %s", name, currentYear));
    }
}
