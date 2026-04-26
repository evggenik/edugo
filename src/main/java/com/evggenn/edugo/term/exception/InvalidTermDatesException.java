package com.evggenn.edugo.term.exception;

import java.time.LocalDate;

public class InvalidTermDatesException extends RuntimeException {
    public InvalidTermDatesException(LocalDate newStart, LocalDate newEnd) {
        super(String.format("Start date: %s can not be after end date: %s", newStart, newEnd));
    }
}
