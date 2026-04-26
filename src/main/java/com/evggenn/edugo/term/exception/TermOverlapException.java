package com.evggenn.edugo.term.exception;

public class TermOverlapException extends RuntimeException{

    public TermOverlapException(String currentYear) {
        super(String.format("The new period overlaps the existing one for year: %s", currentYear));
    }
}
