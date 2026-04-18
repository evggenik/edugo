package com.evggenn.edugo.period.exception;

import java.time.LocalDate;

public class InvalidPeriodDatesException extends RuntimeException {
    public InvalidPeriodDatesException(LocalDate newStart, LocalDate newEnd) {
        super(String.format("Start date: %s can not be after end date: %s", newStart, newEnd));
    }
}
