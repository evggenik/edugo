package com.evggenn.edugo.period.exception;

public class PeriodOverlapException extends RuntimeException{

    public PeriodOverlapException(String currentYear) {
        super(String.format("The new period overlaps the existing one for year: %s", currentYear));
    }
}
