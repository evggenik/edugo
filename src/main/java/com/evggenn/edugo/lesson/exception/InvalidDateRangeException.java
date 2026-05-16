package com.evggenn.edugo.lesson.exception;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String msg) {
        super(String.format("Cannot change lesson status: current status is %s", msg));
    }
}
