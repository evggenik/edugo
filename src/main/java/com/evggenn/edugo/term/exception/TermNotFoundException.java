package com.evggenn.edugo.term.exception;

public class TermNotFoundException extends RuntimeException {
    public TermNotFoundException(Long updatedId, String currentYear) {
        super("Term not found with id " + updatedId + " and current year " + currentYear);
    }

    public TermNotFoundException(Long termId) {
        super("Term not found with id " + termId);
    }
}
