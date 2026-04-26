package com.evggenn.edugo.term.exception;

public class TermAlreadyExistsException extends RuntimeException {
    public TermAlreadyExistsException(String name, String currentYear) {
        super(String.format("The period: %s already exists for year: %s", name, currentYear));
    }
}
