package com.evggenn.edugo.term.exception;

public class TermNotFoundException extends RuntimeException {
    public TermNotFoundException(Long updatedId, String currentYear) {
        super("Period not found with id " + updatedId + " and current year " + currentYear);
    }
}
